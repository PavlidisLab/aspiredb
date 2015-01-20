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
import ubc.pavlab.aspiredb.server.model.Phenotype;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeEnrichmentValueObject;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubic.basecode.math.MultipleTestCorrection;
import ubic.basecode.math.SpecFunc;
import cern.colt.list.DoubleArrayList;

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
                ret.put( key, dformat.format( stats.get( key ) ) );
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
    @SuppressWarnings("boxing")
    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<Map<String, String>> getBurdenAnalysisPerSubjectLabel( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {
        Collection<Map<String, String>> results = new ArrayList<>();

        Map<String, Collection<String>> labelPatientId = new HashMap<>();
        Map<String, Collection<Long>> subjectVariants = new HashMap<>();
        for ( Variant v : variantDao.load( variantIds ) ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );
            SubjectValueObject svo = subject.convertToValueObject();

            // group variants by patient id
            Collection<Long> variantsAdded = subjectVariants.get( subject.getPatientId() );
            if ( variantsAdded == null ) {
                variantsAdded = new ArrayList<>();
                subjectVariants.put( subject.getPatientId(), variantsAdded );
            }
            variantsAdded.add( v.getId() );

            // organize labels
            for ( LabelValueObject label : svo.getLabels() ) {
                if ( !labelPatientId.containsKey( label.getName() ) ) {
                    labelPatientId.put( label.getName(), new HashSet<String>() );
                }
                labelPatientId.get( label.getName() ).add( subject.getPatientId() );
            }
            // create a fake label to capture those Subjects with no labels
            if ( svo.getLabels().size() == 0 ) {
                String labelName = "NO_LABEL";
                if ( !labelPatientId.containsKey( labelName ) ) {
                    labelPatientId.put( labelName, new HashSet<String>() );
                }
                labelPatientId.get( labelName ).add( subject.getPatientId() );
            }
        }

        // store stats by patientId
        Map<String, Map<String, Double>> patientIdStats = new HashMap<>();
        for ( String patientId : subjectVariants.keySet() ) {
            Map<String, Double> stats = getCnvBurdenAnalysisPerSubject( subjectVariants.get( patientId ) );
            patientIdStats.put( patientId, stats );
        }

        // now aggregate patient stats by label by taking the average
        for ( String label : labelPatientId.keySet() ) {
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

            // compute aggregate stats
            perLabelStats.put(
                    CnvBurdenAnalysisPerSubject.AVG_SIZE.toString(),
                    perLabelStats.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE.toString() )
                            / perLabelStats.get( CnvBurdenAnalysisPerSubject.TOTAL.toString() ) * 1.0 );

            perLabelStats.put(
                    CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV.toString(),
                    perLabelStats.get( CnvBurdenAnalysisPerSubject.NUM_GENES.toString() )
                            / perLabelStats.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString() ) * 1.0 );

            // finally save the results
            Map<String, String> statsStr = statsToString( perLabelStats );

            // TODO
            // note that those samples with no variants are not counted towards the total
            statsStr.put( CnvBurdenAnalysisPerSubject.NUM_SAMPLES.toString(),
                    String.format( "%d / %d", labelPatientId.get( label ).size(), patientIdStats.keySet().size() ) );

            // assume unique label name?
            statsStr.put( CnvBurdenAnalysisPerSubject.LABEL_NAME.toString(), label );

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
    @SuppressWarnings("boxing")
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

            // skip non-CNV
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

    /**
     * TODO repurpose code for generic multiple test correction
     * 
     * @param list
     */
    @SuppressWarnings("boxing")
    public void multipleTestCorrectionForPhenotypeEnrichmentList( List<PhenotypeEnrichmentValueObject> list ) {

        DoubleArrayList doubleArrayList = new DoubleArrayList();

        for ( PhenotypeEnrichmentValueObject pvo : list ) {
            doubleArrayList.add( pvo.getPValue() );
        }

        doubleArrayList = MultipleTestCorrection.benjaminiHochberg( doubleArrayList );

        for ( int i = 0; i < doubleArrayList.size(); i++ ) {
            list.get( i ).setPValueCorrected( doubleArrayList.get( i ) );
            list.get( i ).setPValueCorrectedString( dformat.format( doubleArrayList.get( i ) ) );
        }

    }

    /**
     * TODO repurpose code for generic t-test
     * 
     * @param uriPhenotypes -all the phenotypes for a specific uri in the db for subjectIds and complementSubjectIds
     * @param subjectIds
     * @param complementSubjectIds
     */
    public PhenotypeEnrichmentValueObject getPhenotypeEnrichment( Collection<Phenotype> uriPhenotypes,
            Collection<Long> subjectIds, Collection<Long> complementSubjectIds ) {

        Integer successes = 0;

        Integer compSuccesses = 0;

        Integer positives = 0;

        Integer n = subjectIds.size();

        Integer complementGroupSize = complementSubjectIds.size();

        Integer totalSize = subjectIds.size() + complementSubjectIds.size();

        for ( Phenotype p : uriPhenotypes ) {
            // this should always be true the way we are currently using this method.
            if ( p.getValue().equals( "1" ) ) {

                positives++;

                if ( subjectIds.contains( p.getSubject().getId() ) ) {
                    successes++;
                }

                if ( complementSubjectIds.contains( p.getSubject().getId() ) ) {
                    compSuccesses++;
                }

            }
        }

        if ( successes == 0 || successes == n ) {
            return null;
        }

        // do it this way because of possible unobserved phenotypes(no recorded value) for certain subjects, this could
        // be wrong
        Integer negatives = totalSize - positives;

        // note lower.tail: logical; if TRUE (default), probabilities are P[X <= x],
        // otherwise, P[X > x].
        // Since we want P[X >= x], we want to set x = x - 1 and lower.tail false
        double pValue;

        pValue = SpecFunc.phyper( successes - 1, positives, negatives, n, false );

        PhenotypeEnrichmentValueObject vo = new PhenotypeEnrichmentValueObject();

        vo.setPValue( pValue );

        Phenotype valueGrabber = uriPhenotypes.iterator().next();
        vo.setUri( valueGrabber.getUri() );
        vo.setName( valueGrabber.getName() );
        vo.setInGroupTotal( successes );
        vo.setOutGroupTotal( compSuccesses );
        vo.setTotal( totalSize );

        vo.setInGroupTotalString( vo.getInGroupTotal().toString() + "/" + n.toString() );
        vo.setOutGroupTotalString( vo.getOutGroupTotal().toString() + "/" + complementGroupSize.toString() );

        vo.setPValueString( dformat.format( pValue ) );

        return vo;

    }

}
