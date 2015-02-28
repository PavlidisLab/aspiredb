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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.SubjectDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Characteristic;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.Indel;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.SNV;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.BurdenAnalysisValueObject;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;
import ubc.pavlab.aspiredb.shared.query.PropertyValue;

/**
 * 
 */
@Service("burdenAnalysisService")
@RemoteProxy(name = "BurdenAnalysisService")
public class BurdenAnalysisServiceImpl implements BurdenAnalysisService {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private VariantService variantService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private VariantDao variantDao;
    @Autowired
    private LabelDao labelDao;
    @Autowired
    private LabelService labelService;
    @Autowired
    private GeneService geneService;

    DecimalFormat dformat = new DecimalFormat( "#.#####" );

    enum CnvBurdenAnalysisPerSubject {
        PATIENT_ID, LABEL_NAME, NUM_SAMPLES, NUM_CNV_LOSS, NUM_CNV_GAIN, NUM_CNV_UNKNOWN, TOTAL_CNV, TOTAL_SIZE, AVG_SIZE, NUM_GENES, NUM_VARIANTS_WITH_GENE, AVG_GENES_PER_CNV, TOTAL_SNV, TOTAL_INDEL
    }

    final Map<CnvBurdenAnalysisPerSubject, String> CnvBurdenAnalysisPerSubjectDesc = new HashMap<>();

    {
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.NUM_CNV_LOSS, "Mean number of CNV loss" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.NUM_CNV_GAIN, "Mean number of CNV gains" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.NUM_CNV_UNKNOWN,
                "Mean number of unknown types" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.TOTAL_CNV, "Mean number of CNVs" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.TOTAL_SIZE, "Mean total CNV size (bp)" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.AVG_SIZE, "Mean average CNV size (bp)" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.NUM_GENES,
                "Mean number of genes overlapping a variant" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.NUM_VARIANTS_WITH_GENE,
                "Mean number of variants overlapping a gene" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV,
                "Mean number of genes per CNV" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.TOTAL_SNV, "Mean number of SNVs" );
        CnvBurdenAnalysisPerSubjectDesc.put( CnvBurdenAnalysisPerSubject.TOTAL_INDEL, "Mean number of Indels" );
    }

    protected static Log log = LogFactory.getLog( BurdenAnalysisServiceImpl.class );

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
            Double val = patientIdStats.get( patientId ).get( statName );
            if ( val == null ) {
                continue;
            }
            result.add( val );
        }
        return ArrayUtils.toPrimitive( result.toArray( new Double[0] ) );
    }

    @SuppressWarnings("boxing")
    private double[] removeNaNs( double[] d ) {
        Collection<Double> ret = new ArrayList<>();
        for ( int i = 0; i < d.length; i++ ) {
            if ( Double.isInfinite( d[i] ) || Double.isNaN( d[i] ) ) {
                continue;
            }
            ret.add( d[i] );
        }
        return ArrayUtils.toPrimitive( ret.toArray( new Double[ret.size()] ) );
    }

    @SuppressWarnings("boxing")
    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<BurdenAnalysisValueObject> getBurdenAnalysisPerSubjectLabel( LabelValueObject group1,
            LabelValueObject group2, Collection<Long> variantIds ) throws NotLoggedInException, BioMartServiceException {

        Collection<BurdenAnalysisValueObject> ret = new ArrayList<>();

        if ( group1 == null || group2 == null ) {
            log.warn( "Labels can't be null! Group1 is " + group1 + " and group2 is " + group2 );
            return ret;
        }

        if ( variantIds == null ) {
            log.warn( "No variants found." );
            return ret;
        }

        Collection<Subject> subjects = new HashSet<>();
        Collection<String> allPatientIds = new HashSet<>();
        Collection<Variant> variants = variantDao.load( variantIds );
        for ( Variant v : variants ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );
            subjects.add( subject );
            allPatientIds.add( subject.getPatientId() );
        }

        Map<String, Collection<String>> labelPatientId = subjectService.groupSubjectsBySubjectLabel( subjects );

        labelPatientId = getMutuallyExclusivePatientIds( labelPatientId, group1.getName(), group2.getName() );

        if ( !labelPatientId.containsKey( group1.getName() ) ) {
            log.warn( "Subject label " + group1.getName() + " not found" );
            return ret;
        }

        if ( !labelPatientId.containsKey( group2.getName() ) ) {
            log.warn( "Subject label " + group2.getName() + " not found" );
            return ret;
        }

        double group1Size = 1.0 * labelPatientId.get( group1.getName() ).size();
        double group2Size = 1.0 * labelPatientId.get( group2.getName() ).size();
        ret.add( new BurdenAnalysisValueObject( "Total number of subjects", group1Size, group2Size, null ) );

        Map<String, Map<CnvBurdenAnalysisPerSubject, Double>> patientIdStats = getVariantStatsBySubject( variants );

        for ( CnvBurdenAnalysisPerSubject statName : CnvBurdenAnalysisPerSubject.values() ) {

            double[] label1Stats = getPatientStats( statName, labelPatientId.get( group1.getName() ), patientIdStats );
            double[] label2Stats = getPatientStats( statName, labelPatientId.get( group2.getName() ), patientIdStats );

            if ( label1Stats.length == 0 ) {
                log.warn( "No values found for " + statName );
                continue;
            }

            // remove NaNs
            label1Stats = removeNaNs( label1Stats );
            label2Stats = removeNaNs( label2Stats );

            double mean1 = StatUtils.mean( label1Stats );
            double mean2 = StatUtils.mean( label2Stats );
            if ( mean1 + mean2 == 0 ) {
                log.warn( "No " + statName + " stats was found" );
                continue;
            }
            double pval = new MannWhitneyUTest().mannWhitneyUTest( label1Stats, label2Stats );

            ret.add( new BurdenAnalysisValueObject( CnvBurdenAnalysisPerSubjectDesc.get( statName ), mean1, mean2, pval ) );
        }

        return ret;
    }

    /**
     * Excludes those IDs that have both label1 and label2.
     * 
     * @param labelPatientId
     * @param name
     * @param name2
     * @return
     */
    private Map<String, Collection<String>> getMutuallyExclusivePatientIds(
            Map<String, Collection<String>> labelPatientId, String label1, String label2 ) {

        if ( !labelPatientId.containsKey( label1 ) || !labelPatientId.containsKey( label2 ) ) {
            log.warn( "Label not found" );
            return labelPatientId;
        }
        Collection<String> label1IdsOld = new ArrayList<>( labelPatientId.get( label1 ) );
        Collection<String> label2IdsOld = new ArrayList<>( labelPatientId.get( label2 ) );
        Collection<String> label1Ids = labelPatientId.get( label1 );
        Collection<String> label2Ids = labelPatientId.get( label2 );
        boolean removed = label1Ids.removeAll( label2IdsOld );
        label2Ids.removeAll( label1IdsOld );

        // for logging
        if ( removed ) {
            label1IdsOld.removeAll( label1Ids );
            label2IdsOld.removeAll( label2Ids );
            label1IdsOld.addAll( label2IdsOld );
            log.info( "Ignoring " + label1IdsOld.size() + " (" + StringUtils.join( label1IdsOld, "," )
                    + ") subjects with labels " + label1 + " and " + label2 + ". After filtering, " + label1 + " has "
                    + label1Ids.size() + " and " + label2 + " has " + label2Ids.size() );
        }

        return labelPatientId;
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
        Map<Long, Collection<GeneValueObject>> genesPerVariant = geneService.getGenesPerVariant( variantIds );

        // Calculate some statistics
        for ( Long variantId : genesPerVariant.keySet() ) {

            Variant v = variantDao.load( variantId );

            // skip non-CNV
            // if ( !( v instanceof CNV ) ) {
            // continue;
            // }

            Collection<GeneValueObject> genes = genesPerVariant.get( variantId );

            if ( v instanceof CNV ) {
                CNV cnv = ( CNV ) v;

                if ( cnv.getType().equals( CnvType.GAIN ) ) {
                    results.put( CnvBurdenAnalysisPerSubject.NUM_CNV_GAIN,
                            results.get( CnvBurdenAnalysisPerSubject.NUM_CNV_GAIN ) + 1 );
                } else if ( cnv.getType().equals( CnvType.LOSS ) ) {
                    results.put( CnvBurdenAnalysisPerSubject.NUM_CNV_LOSS,
                            results.get( CnvBurdenAnalysisPerSubject.NUM_CNV_LOSS ) + 1 );
                } else {
                    results.put( CnvBurdenAnalysisPerSubject.NUM_CNV_UNKNOWN,
                            results.get( CnvBurdenAnalysisPerSubject.NUM_CNV_UNKNOWN ) + 1 );
                }

                results.put( CnvBurdenAnalysisPerSubject.TOTAL_CNV,
                        results.get( CnvBurdenAnalysisPerSubject.TOTAL_CNV ) + 1 );

                results.put( CnvBurdenAnalysisPerSubject.TOTAL_SIZE,
                        results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE ) + cnv.getCnvLength() );
            } else if ( v instanceof SNV ) {
                results.put( CnvBurdenAnalysisPerSubject.TOTAL_SNV,
                        results.get( CnvBurdenAnalysisPerSubject.TOTAL_SNV ) + 1 );
            } else if ( v instanceof Indel ) {
                results.put( CnvBurdenAnalysisPerSubject.TOTAL_INDEL,
                        results.get( CnvBurdenAnalysisPerSubject.TOTAL_INDEL ) + 1 );
            }

            if ( genes.size() > 0 ) {
                results.put( CnvBurdenAnalysisPerSubject.NUM_GENES, results.get( CnvBurdenAnalysisPerSubject.NUM_GENES )
                        + genes.size() );

                results.put( CnvBurdenAnalysisPerSubject.NUM_VARIANTS_WITH_GENE,
                        results.get( CnvBurdenAnalysisPerSubject.NUM_VARIANTS_WITH_GENE ) + 1 );
            }

        }

        if ( results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE ) > 0
                && results.get( CnvBurdenAnalysisPerSubject.NUM_VARIANTS_WITH_GENE ) > 0 ) {
            results.put( CnvBurdenAnalysisPerSubject.AVG_SIZE, results.get( CnvBurdenAnalysisPerSubject.TOTAL_SIZE )
                    / results.get( CnvBurdenAnalysisPerSubject.TOTAL_CNV ) * 1.0 );

            // results.put(
            // CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString(),
            // results.get( CnvBurdenAnalysisPerSubject.NUM_CNVS_WITH_GENE.toString() )
            // / results.get( CnvBurdenAnalysisPerSubject.TOTAL.toString() ) * 1.0 );

            results.put(
                    CnvBurdenAnalysisPerSubject.AVG_GENES_PER_CNV,
                    results.get( CnvBurdenAnalysisPerSubject.NUM_GENES )
                            / results.get( CnvBurdenAnalysisPerSubject.NUM_VARIANTS_WITH_GENE ) * 1.0 );
        }

        return results;
    }

    @SuppressWarnings({ "boxing", "rawtypes" })
    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Collection<BurdenAnalysisValueObject> getBurdenAnalysisCharacteristic(
            CharacteristicProperty characteristic, LabelValueObject group1, LabelValueObject group2,
            Collection<Long> variantIds ) throws NotLoggedInException, BioMartServiceException,
            NeurocartaServiceException {

        class YesNoCount {
            long yesCount = 0;
            long noCount = 0;

            public long incrementYes() {
                return this.yesCount++;
            }

            public long incrementNo() {
                return this.noCount++;
            }

            public long getYesCount() {
                return this.yesCount;
            }

            public long getNoCount() {
                return this.noCount;
            }
        }

        final int MAX_CHAR_VALUES = 50; // maximum number of unique characteristic values we allow

        Collection<BurdenAnalysisValueObject> ret = new ArrayList<>();

        if ( characteristic == null ) {
            log.warn( "Characteristic can't be null!" );
            return ret;
        }

        if ( group1 == null || group2 == null ) {
            log.warn( "Labels can't be null! Group1 is " + group1 + " and group2 is " + group2 );
            return ret;
        }

        if ( variantIds == null ) {
            log.warn( "No variants found." );
            return ret;
        }

        Map<String, Subject> patientIdSubjects = new HashMap<>();
        Collection<Variant> variants = variantDao.load( variantIds );

        for ( Variant v : variants ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );
            patientIdSubjects.put( subject.getPatientId(), subject );
        }

        // key: labelName, values: patientIds
        Map<String, Collection<String>> labelPatientId = subjectService.groupSubjectsBySubjectLabel( patientIdSubjects
                .values() );

        labelPatientId = getMutuallyExclusivePatientIds( labelPatientId, group1.getName(), group2.getName() );

        if ( !labelPatientId.containsKey( group1.getName() ) ) {
            log.warn( "Subject label " + group1.getName() + " not found" );
            return ret;
        }

        if ( !labelPatientId.containsKey( group2.getName() ) ) {
            log.warn( "Subject label " + group2.getName() + " not found" );
            return ret;
        }

        Collection<PropertyValue> charValues = variantService.suggestValues( characteristic, null );
        if ( charValues.size() > MAX_CHAR_VALUES ) {
            log.warn( "There are too many (>" + MAX_CHAR_VALUES + ") " + characteristic.getDisplayName() + " values!" );
            return ret;
        }

        // keys: charValue, subjectLabel {group1, group2},
        // value: YesNoCount {subjectCountYes, subjectCountNo}
        Map<String, Map<String, YesNoCount>> counts = new HashMap<>();

        // init map
        for ( PropertyValue v : charValues ) {
            Map<String, YesNoCount> m = new HashMap<>();
            counts.put( v.getDisplayValue(), m );
            for ( String label : labelPatientId.keySet() ) {
                m.put( label, new YesNoCount() );
            }
        }

        // count which subjects has a variant that matches the character name and value
        // TODO could potentially use some optimization?
        for ( PropertyValue v : charValues ) {
            for ( String label : labelPatientId.keySet() ) {
                for ( String patientId : labelPatientId.get( label ) ) {

                    boolean found = subjectHasCharacteristicValue( patientIdSubjects.get( patientId ).getVariants(),
                            characteristic.getDisplayName(), v );

                    YesNoCount yesNo = counts.get( v.getDisplayValue() ).get( label );

                    if ( found ) {
                        yesNo.incrementYes();
                    } else {
                        yesNo.incrementNo();
                    }

                }
            }
        }

        // calculate chi-squred tests for each charValue using counts
        for ( String charValue : counts.keySet() ) {

            YesNoCount grp1 = counts.get( charValue ).get( group1.getName() );
            YesNoCount grp2 = counts.get( charValue ).get( group2.getName() );

            String grp1frac = grp1.getYesCount() + "/" + ( grp1.getYesCount() + grp1.getNoCount() );
            String grp2frac = grp2.getYesCount() + "/" + ( grp2.getYesCount() + grp2.getNoCount() );

            long[][] d = { { grp1.getYesCount(), grp1.getNoCount() }, { grp2.getYesCount(), grp2.getNoCount() } };

            double pval = new ChiSquareTest().chiSquareTest( d );

            if ( Double.isNaN( pval ) ) {
                log.warn( charValue + "'s p-value is NaN, grp1 " + grp1frac + ", grp2 " + grp2frac );
                continue;
            }

            ret.add( new BurdenAnalysisValueObject( charValue, grp1frac, grp2frac, pval ) );
        }

        return ret;
    }

    @SuppressWarnings("rawtypes")
    private boolean subjectHasCharacteristicValue( Collection<Variant> variants, String displayName,
            PropertyValue charValue ) {

        for ( Variant v : variants ) {
            for ( Characteristic c : v.getCharacteristics() ) {
                if ( !c.getKey().equalsIgnoreCase( displayName ) ) {
                    continue;
                }
                if ( c.getValue().equalsIgnoreCase( charValue.getDisplayValue() ) ) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean subjectHasVariantLabel( Collection<Variant> variants, LabelValueObject label ) {

        for ( Variant v : variants ) {
            for ( Label l : v.getLabels() ) {
                if ( l.getId() == label.getId() ) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings({ "boxing" })
    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Collection<BurdenAnalysisValueObject> getBurdenAnalysisVariantLabel( LabelValueObject group1,
            LabelValueObject group2, Collection<Long> variantIds ) throws NotLoggedInException,
            BioMartServiceException, NeurocartaServiceException {

        class YesNoCount {
            long yesCount = 0;
            long noCount = 0;

            public long incrementYes() {
                return this.yesCount++;
            }

            public long incrementNo() {
                return this.noCount++;
            }

            public long getYesCount() {
                return this.yesCount;
            }

            public long getNoCount() {
                return this.noCount;
            }
        }

        final int MAX_VARIANT_LABELS = 50; // maximum number of unique characteristic values we allow

        Collection<BurdenAnalysisValueObject> ret = new ArrayList<>();

        if ( group1 == null || group2 == null ) {
            log.warn( "Labels can't be null! Group1 is " + group1 + " and group2 is " + group2 );
            return ret;
        }

        if ( variantIds == null ) {
            log.warn( "No variants found." );
            return ret;
        }

        Map<String, Subject> patientIdSubjects = new HashMap<>();
        Collection<Variant> variants = variantDao.load( variantIds );

        for ( Variant v : variants ) {
            Subject subject = subjectDao.load( v.getSubject().getId() );
            patientIdSubjects.put( subject.getPatientId(), subject );
        }

        // key: labelName, values: patientIds
        Map<String, Collection<String>> labelPatientId = subjectService.groupSubjectsBySubjectLabel( patientIdSubjects
                .values() );

        labelPatientId = getMutuallyExclusivePatientIds( labelPatientId, group1.getName(), group2.getName() );

        if ( !labelPatientId.containsKey( group1.getName() ) ) {
            log.warn( "Subject label " + group1.getName() + " not found" );
            return ret;
        }

        if ( !labelPatientId.containsKey( group2.getName() ) ) {
            log.warn( "Subject label " + group2.getName() + " not found" );
            return ret;
        }

        Collection<LabelValueObject> variantLabels = labelService.getVariantLabelsByProjectId( variants.iterator()
                .next().getSubject().getProjects().iterator().next().getId() );
        if ( variantLabels.size() > MAX_VARIANT_LABELS ) {
            log.warn( "There are too many (>" + MAX_VARIANT_LABELS + ") variant labels!" );
            return ret;
        }

        // keys: charValue, subjectLabel {group1, group2},
        // value: YesNoCount {subjectCountYes, subjectCountNo}
        Map<String, Map<String, YesNoCount>> counts = new HashMap<>();

        // init map
        for ( LabelValueObject l : variantLabels ) {
            Map<String, YesNoCount> m = new HashMap<>();
            counts.put( l.getName(), m );
            for ( String subjectLabel : labelPatientId.keySet() ) {
                m.put( subjectLabel, new YesNoCount() );
            }
        }

        // count which subjects has a variant that matches the character name and value
        // TODO could potentially use some optimization?
        for ( LabelValueObject l : variantLabels ) {
            for ( String subjectLabel : labelPatientId.keySet() ) {
                for ( String patientId : labelPatientId.get( subjectLabel ) ) {

                    boolean found = subjectHasVariantLabel( patientIdSubjects.get( patientId ).getVariants(), l );

                    YesNoCount yesNo = counts.get( l.getName() ).get( subjectLabel );

                    if ( found ) {
                        yesNo.incrementYes();
                    } else {
                        yesNo.incrementNo();
                    }

                }
            }
        }

        // calculate chi-squred tests for each charValue using counts
        for ( String charValue : counts.keySet() ) {

            YesNoCount grp1 = counts.get( charValue ).get( group1.getName() );
            YesNoCount grp2 = counts.get( charValue ).get( group2.getName() );

            String grp1frac = grp1.getYesCount() + "/" + ( grp1.getYesCount() + grp1.getNoCount() );
            String grp2frac = grp2.getYesCount() + "/" + ( grp2.getYesCount() + grp2.getNoCount() );

            long[][] d = { { grp1.getYesCount(), grp1.getNoCount() }, { grp2.getYesCount(), grp2.getNoCount() } };

            double pval = new ChiSquareTest().chiSquareTest( d );

            if ( Double.isNaN( pval ) ) {
                log.warn( charValue + "'s p-value is NaN, grp1 " + grp1frac + ", grp2 " + grp2frac );
                continue;
            }

            ret.add( new BurdenAnalysisValueObject( charValue, grp1frac, grp2frac, pval ) );
        }

        return ret;
    }
}