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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.inference.TTest;
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
import ubc.pavlab.aspiredb.shared.VariantValueObject;

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
    private LabelService labelService;
    @Autowired
    private UserGeneSetDao userGeneSetDao;
    @Autowired
    private BioMartQueryService bioMartQueryService;
    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    DecimalFormat dformat = new DecimalFormat( "#.#####" );

    enum CnvBurdenAnalysisPerSubject {
        PATIENT_ID, LABEL_NAME, NUM_SAMPLES, NUM_DELETION, NUM_DUPLICATION, NUM_UNKNOWN, TOTAL, TOTAL_SIZE, AVG_SIZE, NUM_GENES, NUM_CNVS_WITH_GENE, AVG_GENES_PER_CNV
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

    private Map<CnvBurdenAnalysisPerSubject, String> statsToString( Map<CnvBurdenAnalysisPerSubject, ?> stats ) {
        HashMap<CnvBurdenAnalysisPerSubject, String> ret = new HashMap<>();
        for ( CnvBurdenAnalysisPerSubject key : stats.keySet() ) {
            if ( key.equals( CnvBurdenAnalysisPerSubject.AVG_SIZE )
                    || key.equals( CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV ) ) {
                ret.put( key, String.format( "%.1f", stats.get( key ) ) );
            } else if ( key.equals( CnvBurdenAnalysisPerSubject.PATIENT_ID ) ) {
                ret.put( key, String.format( "%s", stats.get( key ).toString() ) );
            } else if ( key.equals( CnvBurdenAnalysisPerSubject.LABEL_NAME ) ) {
                ret.put( key, String.format( "%s", stats.get( key ).toString() ) );
            } else {
                ret.put( key, dformat.format( stats.get( key ) ) );
            }
        }
        return ret;
    }

    /**
     * @param variants
     * @return Map<Label.name, Collection<Subject.patientID>>
     */
    private Map<String, Collection<String>> groupSubjectsBySubjectLabel( Collection<Subject> subjects ) {
        Map<String, Collection<String>> labelPatientId = new HashMap<>();
        for ( Subject subject : subjects ) {

            // organize labels
            for ( Label label : subject.getLabels() ) {
                if ( !labelPatientId.containsKey( label.getName() ) ) {
                    labelPatientId.put( label.getName(), new HashSet<String>() );
                }
                labelPatientId.get( label.getName() ).add( subject.getPatientId() );
            }

            // create a fake label to capture those Subjects with no labels
            if ( subject.getLabels().size() == 0 ) {
                String labelName = "NO_LABEL";
                if ( !labelPatientId.containsKey( labelName ) ) {
                    labelPatientId.put( labelName, new HashSet<String>() );
                }
                labelPatientId.get( labelName ).add( subject.getPatientId() );
            }
        }
        return labelPatientId;
    }

    /**
     * @param variants
     * @return Map<Subject.patientId, Map<statsName, statsDoubleValue>>
     * @throws NotLoggedInException
     * @throws BioMartServiceException
     */
    private Map<String, Map<CnvBurdenAnalysisPerSubject, Double>> getVariantStatsBySubject( Collection<Variant> variants )
            throws BioMartServiceException, NotLoggedInException {
        Map<String, Collection<Long>> subjectVariants = new HashMap<>();
        for ( Variant v : variants ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );

            // group variants by patient id
            Collection<Long> variantsAdded = subjectVariants.get( subject.getPatientId() );
            if ( variantsAdded == null ) {
                variantsAdded = new ArrayList<>();
                subjectVariants.put( subject.getPatientId(), variantsAdded );
            }
            variantsAdded.add( v.getId() );

        }

        // store stats by patientId
        Map<String, Map<CnvBurdenAnalysisPerSubject, Double>> patientIdStats = new HashMap<>();
        for ( String patientId : subjectVariants.keySet() ) {
            Map<CnvBurdenAnalysisPerSubject, Double> stats = getCnvBurdenAnalysisPerSubject( subjectVariants
                    .get( patientId ) );
            patientIdStats.put( patientId, stats );
        }

        return patientIdStats;
    }

    /**
     * @param statName
     * @param patientIds
     * @return double[] array of the statName values for all of the patientIds
     */
    private double[] getPatientStats( CnvBurdenAnalysisPerSubject statName, Collection<String> patientIds,
            Map<String, Map<CnvBurdenAnalysisPerSubject, Double>> patientIdStats ) {
        Collection<Double> result = new ArrayList<>();
        for ( String patientId : patientIds ) {
            result.add( patientIdStats.get( patientId ).get( statName ) );
        }
        return ArrayUtils.toPrimitive( result.toArray( new Double[0] ) );
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
    @SuppressWarnings("boxing")
    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<Map<CnvBurdenAnalysisPerSubject, String>> getBurdenAnalysisPerSubjectLabel(
            Collection<Long> variantIds ) throws NotLoggedInException, BioMartServiceException {
        Collection<Map<CnvBurdenAnalysisPerSubject, String>> results = new ArrayList<>();

        Collection<Subject> subjects = new HashSet<>();
        Collection<String> allPatientIds = new HashSet<>();
        Collection<Variant> variants = variantDao.load( variantIds );
        for ( Variant v : variants ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );
            subjects.add( subject );
            allPatientIds.add( subject.getPatientId() );
        }

        Map<String, Collection<String>> labelPatientId = groupSubjectsBySubjectLabel( subjects );
        Map<String, Map<CnvBurdenAnalysisPerSubject, Double>> patientIdStats = getVariantStatsBySubject( variants );

        for ( String label : labelPatientId.keySet() ) {

            // nothing to do when there's no patients associated with this label
            if ( labelPatientId.get( label ).size() == 0 ) {
                continue;
            }

            Collection<String> withLabel = labelPatientId.get( label );

            Collection<String> withoutLabel = new ArrayList<>( allPatientIds );
            withoutLabel.removeAll( withLabel );

            Map<CnvBurdenAnalysisPerSubject, Double> stats = new HashMap<>(); // store values
            Map<CnvBurdenAnalysisPerSubject, String> statsStr = new HashMap<>(); // store as string with p-vals
            results.add( statsStr );

            statsStr.put( CnvBurdenAnalysisPerSubject.LABEL_NAME, label );

            // note that those samples with no variants are not counted towards the total
            statsStr.put( CnvBurdenAnalysisPerSubject.NUM_SAMPLES,
                    String.format( "%d / %d", withLabel.size(), allPatientIds.size() ) );

            for ( CnvBurdenAnalysisPerSubject statName : patientIdStats.get( withLabel.iterator().next() ).keySet() ) {
                double[] withLabelStats = getPatientStats( statName, withLabel, patientIdStats );
                double[] withoutLabelStats = getPatientStats( statName, withoutLabel, patientIdStats );

                Double pval = 1.0;
                String pvalSuffix = "";
                if ( withLabelStats.length > 1 && withoutLabelStats.length > 1 ) {
                    pval = new TTest().tTest( withLabelStats, withoutLabelStats );
                    if ( pval < 0.001 ) {
                        pvalSuffix = "**";
                    } else if ( pval < 0.05 ) {
                        pvalSuffix = "*";
                    }
                }

                double statValue = new Sum().evaluate( withLabelStats );
                String statValueStr = dformat.format( statValue );
                stats.put( statName, statValue );
                statsStr.put( statName, statValueStr + pvalSuffix );
            }

            double statValue = 1.0 * stats.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE )
                    / stats.get( CnvBurdenAnalysisPerSubject.TOTAL );
            stats.put( CnvBurdenAnalysisPerSubject.AVG_SIZE, statValue );
            statsStr.put( CnvBurdenAnalysisPerSubject.AVG_SIZE, dformat.format( statValue ) );

            statValue = 1.0 * stats.get( CnvBurdenAnalysisPerSubject.NUM_GENES )
                    / stats.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE );
            stats.put( CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV, statValue );
            statsStr.put( CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV, dformat.format( statValue ) );

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
    public Collection<Map<CnvBurdenAnalysisPerSubject, String>> getBurdenAnalysisPerSubject( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {
        Collection<Map<CnvBurdenAnalysisPerSubject, String>> results = new ArrayList<>();

        // group variants by patient id
        Map<String, Collection<Long>> subjectVariants = new HashMap<>();
        for ( Variant v : variantDao.load( variantIds ) ) {

            // skip non-CNV
            if ( !( v instanceof CNV ) ) {
                continue;
            }

            Collection<Long> variantsAdded = subjectVariants.get( v.getSubject().getPatientId() );
            if ( variantsAdded == null ) {
                variantsAdded = new ArrayList<>();
                subjectVariants.put( v.getSubject().getPatientId(), variantsAdded );
            }
            variantsAdded.add( v.getId() );
        }

        for ( String patientId : subjectVariants.keySet() ) {
            Map<CnvBurdenAnalysisPerSubject, Double> stats = getCnvBurdenAnalysisPerSubject( subjectVariants
                    .get( patientId ) );

            Map<CnvBurdenAnalysisPerSubject, String> statsStr = statsToString( stats );

            statsStr.put( CnvBurdenAnalysisPerSubject.PATIENT_ID, patientId );

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
    @SuppressWarnings("boxing")
    private Map<CnvBurdenAnalysisPerSubject, Double> getCnvBurdenAnalysisPerSubject( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {

        // Initialize
        Map<CnvBurdenAnalysisPerSubject, Double> results = new HashMap<>();
        for ( CnvBurdenAnalysisPerSubject ba : CnvBurdenAnalysisPerSubject.values() ) {
            // don't add PATIENT_ID and LABEL_NAME
            if ( ba.equals( CnvBurdenAnalysisPerSubject.PATIENT_ID )
                    || ba.equals( CnvBurdenAnalysisPerSubject.LABEL_NAME )
                    || ba.equals( CnvBurdenAnalysisPerSubject.NUM_SAMPLES ) ) {
                continue;
            }
            results.put( ba, 0.0 );
        }

        // Gene overlap
        Map<Long, Collection<GeneValueObject>> genesPerVariant = getGenesPerVariant( variantIds );

        // Calculate some statistics
        for ( Long variantId : genesPerVariant.keySet() ) {

            Variant v = variantDao.load( variantId );

            // skip non-CNV
            if ( !( v instanceof CNV ) ) {
                continue;
            }

            Collection<GeneValueObject> genes = genesPerVariant.get( variantId );

            CNV cnv = ( CNV ) v;

            if ( cnv.getType().equals( CnvType.GAIN ) ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_DUPLICATION,
                        results.get( CnvBurdenAnalysisPerSubject.NUM_DUPLICATION ) + 1 );
            } else if ( cnv.getType().equals( CnvType.LOSS ) ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_DELETION,
                        results.get( CnvBurdenAnalysisPerSubject.NUM_DELETION ) + 1 );
            } else {
                results.put( CnvBurdenAnalysisPerSubject.NUM_UNKNOWN,
                        results.get( CnvBurdenAnalysisPerSubject.NUM_UNKNOWN ) + 1 );
            }

            results.put( CnvBurdenAnalysisPerSubject.TOTAL, results.get( CnvBurdenAnalysisPerSubject.TOTAL ) + 1 );

            results.put( CnvBurdenAnalysisPerSubject.TOTAL_SIZE, results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE )
                    + cnv.getCnvLength() );

            if ( genes.size() > 0 ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_GENES, results.get( CnvBurdenAnalysisPerSubject.NUM_GENES )
                        + genes.size() );

                results.put( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE,
                        results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE ) + 1 );
            }

        }

        results.put( CnvBurdenAnalysisPerSubject.AVG_SIZE, results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE )
                / results.get( CnvBurdenAnalysisPerSubject.TOTAL ) * 1.0 );

        // results.put(
        // CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString(),
        // results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString() )
        // / results.get( CnvBurdenAnalysisPerSubject.TOTAL.toString() ) * 1.0 );

        results.put( CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV, results.get( CnvBurdenAnalysisPerSubject.NUM_GENES )
                / results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE ) * 1.0 );

        return results;
    }

    private boolean isOverlapping( GenomicLocation loc1, GenomicLocation loc2 ) {
        boolean sameChromosome = loc2.getChromosome().equals( loc1.getChromosome() );
        if ( !sameChromosome ) {
            return false;
        }
        boolean geneInsideRegion = ( loc1.getStart() <= loc2.getStart() ) && ( loc1.getEnd() >= loc2.getEnd() );
        if ( geneInsideRegion ) {
            return true;
        }
        boolean geneSurroundsRegion = ( loc1.getStart() >= loc2.getStart() ) && ( loc1.getEnd() <= loc2.getEnd() );
        if ( geneSurroundsRegion ) {
            return true;
        }
        boolean geneHitsEndOfRegion = ( loc1.getStart() <= loc2.getStart() ) && ( loc1.getEnd() >= loc2.getStart() );
        if ( geneHitsEndOfRegion ) {
            return true;
        }
        boolean geneHitsStartOfRegion = ( loc1.getStart() <= loc2.getEnd() ) && ( loc1.getEnd() >= loc2.getEnd() );
        if ( geneHitsStartOfRegion ) {
            return true;
        }
        return false;
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

            GenomicLocation geneLoc = new GenomicLocation( geneRange.getChromosome(), geneRange.getBaseStart(),
                    geneRange.getBaseEnd() );

            if ( isOverlapping( variantLoc, geneLoc ) ) {
                results.add( gene );
                continue;
            }
        }
        return results;
    }

    /**
     * Returns all the genes that overlap with the variantIds, including non-protein coding genes.
     */
    @SuppressWarnings("boxing")
    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<Long, Collection<GeneValueObject>> getGenesPerVariant( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {

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
            }
        }

        // log.info( "Found " + genesFound + " genes that overlap " + results.size() + " variants in "
        // + variantBin.keySet().size() + " bins (" + timer.getTime() + " ms)" );

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

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<String, Map<GeneValueObject, Collection<VariantValueObject>>> getCompoundHeterozygotes(
            Collection<Long> variantIds ) throws NotLoggedInException, BioMartServiceException {

        Map<String, Map<GeneValueObject, Collection<VariantValueObject>>> result = new HashMap<>();

        Map<Long, Collection<GeneValueObject>> map = getGenesPerVariant( variantIds );

        // assume that each variant is a different allele, ie. unique position
        Collection<Variant> variants = variantDao.load( variantIds );

        // transform data to patientId-geneSymbol pairs
        Map<String, Map<GeneValueObject, Collection<VariantValueObject>>> seen = new HashMap<>();
        for ( Variant variant : variants ) {

            // not interested in CNV gains
            // TODO more filtering needed for other special cases?
            if ( variant instanceof CNV ) {
                CNV cnv = ( ( CNV ) variant );
                if ( cnv.getType() == CnvType.GAIN ) {
                    continue;
                }
            }

            Long id = variant.getId();

            String patientId = variant.getSubject().getPatientId();

            Collection<GeneValueObject> geneList = map.get( id );
            if ( geneList == null || geneList.size() == 0 ) {
                continue;
            }

            for ( GeneValueObject gene : geneList ) {
                Map<GeneValueObject, Collection<VariantValueObject>> geneMap = seen.get( patientId );
                if ( geneMap == null ) {
                    geneMap = new HashMap<>();
                    seen.put( patientId, geneMap );
                }

                if ( geneMap.get( gene ) == null ) {
                    Collection<VariantValueObject> varIdList = new HashSet<>();
                    geneMap.put( gene, varIdList );
                }

                geneMap.get( gene ).add( variant.toValueObject() );

            }
        }

        // filter
        for ( String patientId : seen.keySet() ) {
            for ( GeneValueObject gene : seen.get( patientId ).keySet() ) {
                if ( seen.get( patientId ).get( gene ).size() <= 1 ) {
                    continue;
                }
                for ( VariantValueObject variant : seen.get( patientId ).get( gene ) ) {
                    // log.info( String.format( "Patient %s Gene %s Variant %s", patientId, gene, variant ) );

                    if ( result.get( patientId ) == null ) {
                        HashMap<GeneValueObject, Collection<VariantValueObject>> geneMap = new HashMap<>();
                        result.put( patientId, geneMap );
                    }

                    if ( result.get( patientId ).get( gene ) == null ) {
                        Collection<VariantValueObject> variantList = new HashSet<>();
                        result.get( patientId ).put( gene, variantList );
                    }

                    result.get( patientId ).get( gene ).add( variant );
                }
            }
        }
        return result;
    }

}
