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

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
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
import ubc.pavlab.aspiredb.server.model.UserGeneSet;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

/**
 * author: anton date: 01/05/13
 */
@Service("geneService")
@RemoteProxy(name = "GeneService")
public class GeneServiceImpl implements GeneService {

    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private VariantDao variantDao;
    @Autowired
    private UserGeneSetDao userGeneSetDao;
    @Autowired
    private BioMartQueryService bioMartQueryService;
    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    enum CnvBurdenAnalysisPerSubject {
        PATIENT_ID, NUM_DELETION, NUM_DUPLICATION, NUM_UNKNOWN, TOTAL, TOTAL_SIZE, AVG_SIZE, NUM_GENES, NUM_CNVS_WITH_GENE, AVG_GENES_PER_CNV
    }

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
            } else {
                ret.put( key, String.format( "%.0f", stats.get( key ) ) );
            }
        }
        return ret;
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
     * @param subject
     * @return
     */
    private Map<String, Double> getCnvBurdenAnalysisPerSubject( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {

        // Initialize
        Map<String, Double> results = new HashMap<>();
        for ( CnvBurdenAnalysisPerSubject ba : CnvBurdenAnalysisPerSubject.values() ) {
            results.put( ba.toString(), 0.0 );
        }

        // Gene overlap
        Map<Long, List<GeneValueObject>> genesPerVariant = getGenesPerVariant( variantIds );

        // Calculate some statistics
        for ( Long variantId : genesPerVariant.keySet() ) {

            Variant v = variantDao.load( variantId );
            if ( !( v instanceof CNV ) ) {
                continue;
            }

            List<GeneValueObject> genes = genesPerVariant.get( variantId );

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
     * Returns all the genes that overlap with the variantIds, including non-protein coding genes.
     */
    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Map<Long, List<GeneValueObject>> getGenesPerVariant( Collection<Long> variantIds )
            throws NotLoggedInException, BioMartServiceException {

        Map<Long, List<GeneValueObject>> results = new HashMap<>();

        // Used to remove duplicates
        HashMap<String, GeneValueObject> genes = new HashMap<String, GeneValueObject>();
        for ( Variant variant : variantDao.load( variantIds ) ) {

            GenomicLocation location = variant.getLocation();

            Collection<GeneValueObject> genesInsideRange = this.bioMartQueryService.fetchGenesByLocation(
                    String.valueOf( location.getChromosome() ), ( long ) location.getStart(),
                    ( long ) location.getEnd() );

            for ( GeneValueObject geneValueObject : genesInsideRange ) {
                genes.put( geneValueObject.getEnsemblId(), geneValueObject );
            }
            List<GeneValueObject> gvos = new ArrayList<GeneValueObject>( genes.values() );
            results.put( variant.getId(), gvos );
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    @RemoteMethod
    public Collection<GeneValueObject> getGenesInsideVariants( Collection<Long> ids ) throws NotLoggedInException,
            BioMartServiceException {

        Collection<GeneValueObject> result = new HashSet<>();
        Map<Long, List<GeneValueObject>> map = getGenesPerVariant( ids );
        for ( List<GeneValueObject> genes : map.values() ) {
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
