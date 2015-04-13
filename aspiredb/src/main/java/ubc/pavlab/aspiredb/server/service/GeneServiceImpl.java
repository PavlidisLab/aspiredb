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
    private UserGeneSetDao userGeneSetDao;
    @Autowired
    private BioMartQueryService bioMartQueryService;
    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    DecimalFormat dformat = new DecimalFormat( "#.#####" );

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
        Collection<Variant> variants = ( Collection<Variant> ) variantDao.load( variantIds );

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
