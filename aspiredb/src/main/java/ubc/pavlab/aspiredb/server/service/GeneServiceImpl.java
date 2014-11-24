/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.server.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.UserGeneSetDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.ExternalDependencyException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.GenomicLocation;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * author: anton date: 01/05/13
 */
@Service("geneService")
@RemoteProxy(name = "GeneService")
public class GeneServiceImpl implements GeneService {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private VariantDao variantDao;
    @Autowired
    private LabelDao labelDao;
    @Autowired
    private UserGeneSetDao userGeneSetDao;
    @Autowired
    private BioMartQueryService bioMartQueryService;
    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    enum CnvBurdenAnalysisPerSubject {
        PATIENT_ID, LABEL_NAME, NUM_DELETION, NUM_DUPLICATION, NUM_UNKNOWN, TOTAL, TOTAL_SIZE, AVG_SIZE, NUM_GENES, NUM_CNVS_WITH_GENE, AVG_GENES_PER_CNV
    }

    protected static Log log = LogFactory.getLog( GeneServiceImpl.class );

    @Override
    @RemoteMethod
    public Map<String, GeneValueObject> findGenesAndURIsWithNeurocartaPhenotype( String phenotypeValueUri )
            throws NotLoggedInException, ExternalDependencyException {
        Map<String, GeneValueObject> genes = new HashMap<String, GeneValueObject>();
        genes = this.neurocartaQueryService.findPhenotypeGenes( phenotypeValueUri );

        return genes;
    }

    @Override
    @RemoteMethod
    public Collection<GeneValueObject> findGenesWithNeurocartaPhenotype( String phenotypeValueUri )
            throws NotLoggedInException, ExternalDependencyException {

        return this.neurocartaQueryService.fetchGenesAssociatedWithPhenotype( phenotypeValueUri );
    }

    private Map<String, String> statsToString( Map<String, ?> stats ) {
        HashMap<String, String> ret = new HashMap<>();
        for ( String key : stats.keySet() ) {
            if ( key.equals( CnvBurdenAnalysisPerSubject.AVG_SIZE.toString() )
                    || key.equals( CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV.toString() ) ) {
                ret.put( key, String.format( "%.1f", stats.get( key ) ) );
            } else if ( key.equals( CnvBurdenAnalysisPerSubject.PATIENT_ID.toString() ) ) {
                ret.put( key, String.format( "%s", stats.get( key ).toString() ) );
            } else if ( key.equals( CnvBurdenAnalysisPerSubject.LABEL_NAME.toString() ) ) {
                ret.put( key, String.format( "%s", stats.get( key ).toString() ) );
            } else {
                ret.put( key, String.format( "%.0f", stats.get( key ) ) );
            }
        }
        return ret;
    }

    /**
     * Returns the BurdenAnalysis per Subject label (See Bug 4129). Takes the average of all the per-sample metrics in a
     * subject label.
     * 
     * @param subjectIds
     * @return a map with the patientID as index, e.g. { LABEL_NAME : 'Control', NUM_DELETION : 2, NUM_DUPLICATION : 4,
     *         }'
     * @throws NotLoggedInException
     * @throws BioMartServiceException
     */
    @Override
    @RemoteMethod
    public Collection<Map<String, String>> getBurdenAnalysisPerSubjectLabel( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {
        Collection<Map<String, String>> results = new ArrayList<>();

        Map<Label, Collection<String>> labelPatientId = new HashMap<>();
        Map<String, Collection<Long>> subjectVariants = new HashMap<>();
        for ( Variant v : variantDao.load( variantIds ) ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );

            // group variants by patient id
            Collection<Long> variantsAdded = subjectVariants.get( subject.getPatientId() );
            if ( variantsAdded == null ) {
                variantsAdded = new ArrayList<>();
                subjectVariants.put( subject.getPatientId(), variantsAdded );
            }
            variantsAdded.add( v.getId() );

            // organize labels
            for ( Label label : labelDao.getSubjectLabelsBySubjectId( subject.getId() ) ) {
                if ( !labelPatientId.containsKey( label ) ) {
                    labelPatientId.put( label, new HashSet<String>() );
                }
                labelPatientId.get( label ).add( subject.getPatientId() );
            }
        }

        // store stats by patientId
        Map<String, Map<String, Double>> patientIdStats = new HashMap<>();
        for ( String patientId : subjectVariants.keySet() ) {
            Map<String, Double> stats = getCnvBurdenAnalysisPerSubject( subjectVariants.get( patientId ) );
            patientIdStats.put( patientId, stats );
        }

        // now aggregate patient stats by label by taking the average
        for ( Label label : labelPatientId.keySet() ) {
            Map<String, Double> perLabelStats = new HashMap<>();

            // add all the values up for each patient
            for ( String patientId : labelPatientId.get( label ) ) {
                for ( String statName : patientIdStats.get( patientId ).keySet() ) {
                    Double statVal = patientIdStats.get( patientId ).get( statName );
                    if ( !perLabelStats.containsKey( statName ) ) {
                        perLabelStats.put( statName, 0.0 );
                    }
                    perLabelStats.put( statName, perLabelStats.get( statName ) + statVal );
                }
            }

            // nothing to do when there's no patients associated with this label
            if ( labelPatientId.get( label ).size() == 0 ) {
                continue;
            }

            // divide by the number of patients
            for ( String statName : perLabelStats.keySet() ) {
                perLabelStats.put( statName, perLabelStats.get( statName ) / labelPatientId.get( label ).size() );
            }

            // finally save the results
            Map<String, String> statsStr = statsToString( perLabelStats );

            // assume unique label name?
            statsStr.put( CnvBurdenAnalysisPerSubject.LABEL_NAME.toString(), label.getName() );

            results.add( statsStr );
        }

        return results;
    }

    /**
     * Returns the BurdenAnalysis per Subject (See Bug 4129).
     * 
     * @param subjectIds
     * @return a map with the patientID as index, e.g. { PATIENT_ID : 'Patient_01', NUM_DELETION : 2, NUM_DUPLICATION :
     *         4, }'
     * @throws NotLoggedInException
     * @throws BioMartServiceException
     */
    @Override
    @RemoteMethod
    public Collection<Map<String, String>> getBurdenAnalysisPerSubject( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {
        Collection<Map<String, String>> results = new ArrayList<>();

        // group variants by patient id
        Map<String, Collection<Long>> subjectVariants = new HashMap<>();
        for ( Variant v : variantDao.load( variantIds ) ) {
            Collection<Long> variantsAdded = subjectVariants.get( v.getSubject().getPatientId() );
            if ( variantsAdded == null ) {
                variantsAdded = new ArrayList<>();
                subjectVariants.put( v.getSubject().getPatientId(), variantsAdded );
            }
            variantsAdded.add( v.getId() );
        }

        for ( String patientId : subjectVariants.keySet() ) {
            Map<String, Double> stats = getCnvBurdenAnalysisPerSubject( subjectVariants.get( patientId ) );

            Map<String, String> statsStr = statsToString( stats );

            statsStr.put( CnvBurdenAnalysisPerSubject.PATIENT_ID.toString(), patientId );

            results.add( statsStr );
        }

        return results;
    }

    /**
     * @param variantIds
     * @return
     * @throws NotLoggedInException
     * @throws BioMartServiceException
     */
    private Map<String, Double> getCnvBurdenAnalysisPerSubject( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {

        // Initialize
        Map<String, Double> results = new HashMap<>();
        for ( CnvBurdenAnalysisPerSubject ba : CnvBurdenAnalysisPerSubject.values() ) {
            // don't add PATIENT_ID and LABEL_NAME
            if ( ba.equals( CnvBurdenAnalysisPerSubject.PATIENT_ID )
                    || ba.equals( CnvBurdenAnalysisPerSubject.LABEL_NAME ) ) {
                continue;
            }
            results.put( ba.toString(), 0.0 );
        }

        // Gene overlap
        Map<Long, Collection<GeneValueObject>> genesPerVariant = getGenesPerVariant( variantIds );

        // Calculate some statistics
        for ( Long variantId : genesPerVariant.keySet() ) {

            Variant v = variantDao.load( variantId );
            if ( !( v instanceof CNV ) ) {
                continue;
            }

            Collection<GeneValueObject> genes = genesPerVariant.get( variantId );

            CNV cnv = ( CNV ) v;

            if ( cnv.getType().equals( CnvType.GAIN ) ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_DUPLICATION.toString(),
                        results.get( CnvBurdenAnalysisPerSubject.NUM_DUPLICATION.toString() ) + 1 );
            } else if ( cnv.getType().equals( CnvType.LOSS ) ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_DELETION.toString(),
                        results.get( CnvBurdenAnalysisPerSubject.NUM_DELETION.toString() ) + 1 );
            } else {
                results.put( CnvBurdenAnalysisPerSubject.NUM_UNKNOWN.toString(),
                        results.get( CnvBurdenAnalysisPerSubject.NUM_UNKNOWN.toString() ) + 1 );
            }

            results.put( CnvBurdenAnalysisPerSubject.TOTAL.toString(),
                    results.get( CnvBurdenAnalysisPerSubject.TOTAL.toString() ) + 1 );

            results.put( CnvBurdenAnalysisPerSubject.TOTAL_SIZE.toString(),
                    results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE.toString() ) + cnv.getCnvLength() );

            if ( genes.size() > 0 ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_GENES.toString(),
                        results.get( CnvBurdenAnalysisPerSubject.NUM_GENES.toString() ) + genes.size() );

                results.put( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString(),
                        results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString() ) + 1 );
            }

        }

        results.put(
                CnvBurdenAnalysisPerSubject.AVG_SIZE.toString(),
                results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE.toString() )
                        / results.get( CnvBurdenAnalysisPerSubject.TOTAL.toString() ) * 1.0 );

        // results.put(
        // CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString(),
        // results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString() )
        // / results.get( CnvBurdenAnalysisPerSubject.TOTAL.toString() ) * 1.0 );

        results.put(
                CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV.toString(),
                results.get( CnvBurdenAnalysisPerSubject.NUM_GENES.toString() )
                        / results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString() ) * 1.0 );

        return results;
    }

    /**
     * @param location
     * @param genes
     * @return genes that overlap the location
     */
    private Collection<GeneValueObject> findGeneOverlap( GenomicLocation variantLoc, Collection<GeneValueObject> genes ) {
        Collection<GeneValueObject> results = new ArrayList<>();

        if ( variantLoc == null || genes == null ) {
            log.debug( "Either variant " + variantLoc + " or genes " + genes + " is null!" );
            return results;
        }

        for ( GeneValueObject gene : genes ) {
            GenomicRange geneRange = gene.getGenomicRange();
            boolean sameChromosome = geneRange.getChromosome().equals( variantLoc.getChromosome() );
            if ( !sameChromosome ) {
                continue;
            }
            boolean geneInsideRegion = ( variantLoc.getStart() <= geneRange.getBaseStart() )
                    && ( variantLoc.getEnd() >= geneRange.getBaseEnd() );
            boolean geneSurroundsRegion = ( variantLoc.getStart() >= geneRange.getBaseStart() )
                    && ( variantLoc.getEnd() <= geneRange.getBaseEnd() );
            boolean geneHitsEndOfRegion = ( variantLoc.getStart() <= geneRange.getBaseStart() )
                    && ( variantLoc.getEnd() >= geneRange.getBaseStart() );
            boolean geneHitsStartOfRegion = ( variantLoc.getStart() <= geneRange.getBaseEnd() )
                    && ( variantLoc.getEnd() >= geneRange.getBaseEnd() );
            if ( geneInsideRegion || geneSurroundsRegion || geneHitsEndOfRegion || geneHitsStartOfRegion ) {
                results.add( gene );
            }
        }
        return results;
    }

    /**
     * Returns all the genes that overlap with the variantIds, including non-protein coding genes.
     */
    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<Long, Collection<GeneValueObject>> getGenesPerVariant( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {

        int genesFound = 0;

        StopWatch timer = new StopWatch();
        timer.start();

        Map<Long, Collection<GeneValueObject>> results = new HashMap<>();

        Map<Integer, Collection<Variant>> variantBin = new HashMap<>();

        // group variants by bin
        for ( Variant variant : variantDao.load( variantIds ) ) {
            GenomicLocation location = variant.getLocation();

            for ( int bin : GenomeBin.relevantBins( location.getChromosome(), location.getStart(), location.getEnd() ) ) {

                if ( !variantBin.containsKey( bin ) ) {
                    variantBin.put( bin, new ArrayList<Variant>() );
                }
                variantBin.get( bin ).add( variant );
            }
        }

        // for each gene, overlap with matching variant, fast computation
        for ( int bin : variantBin.keySet() ) {
            Collection<GeneValueObject> genesInsideBin = this.bioMartQueryService.fetchGenesByBin( bin );
            if ( genesInsideBin == null || genesInsideBin.size() == 0 ) {
                continue;
            }
            for ( Variant variant : variantBin.get( bin ) ) {
                Collection<GeneValueObject> genesInsideRange = findGeneOverlap( variant.getLocation(), genesInsideBin );
                if ( !results.containsKey( variant.getId() ) ) {
                    results.put( variant.getId(), new ArrayList<GeneValueObject>() );
                }
                results.get( variant.getId() ).addAll( genesInsideRange );
                genesFound += genesInsideRange.size();
            }
        }

        log.info( "Found " + genesFound + " genes that overlap " + results.size() + " variants (" + timer.getTime()
                + " ms)" );

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Collection<GeneValueObject> getGenesInsideVariants( Collection<Long> ids ) throws NotLoggedInException,
            BioMartServiceException {

        Collection<GeneValueObject> result = new HashSet<>();
        Map<Long, Collection<GeneValueObject>> map = getGenesPerVariant( ids );
        for ( Collection<GeneValueObject> genes : map.values() ) {
            result.addAll( genes );
        }
        return result;
    }

    @Override
    @RemoteMethod
    public boolean isGeneSetName( String name ) {

        List<UserGeneSet> geneSet = userGeneSetDao.findByName( name );

        if ( geneSet.size() > 0 ) {
            return true;
        }

        return false;

    }

    @Override
    @Transactional
    @RemoteMethod
    public Long saveUserGeneSet( String geneSetName, List<GeneValueObject> genes ) {
        final List<UserGeneSet> geneSet = userGeneSetDao.findByName( geneSetName );
        UserGeneSet savedUserGeneSet = null;
        if ( geneSet.isEmpty() ) {
            UserGeneSet userGeneSet = new UserGeneSet( geneSetName, ( Serializable ) genes );
            savedUserGeneSet = userGeneSetDao.create( userGeneSet );
        } else if ( geneSet.size() == 1 ) {
            UserGeneSet userGeneSet = geneSet.iterator().next();
            userGeneSet.setObject( ( Serializable ) genes );
            userGeneSetDao.update( userGeneSet );
            savedUserGeneSet = userGeneSet;
        } else {
            throw new IllegalStateException(
                    "Found more than one saved gene sets with same name belonging to one user." );
        }
        return savedUserGeneSet.getId();
    }

}
