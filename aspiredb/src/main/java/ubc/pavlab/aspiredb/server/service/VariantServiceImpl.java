/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubc.pavlab.aspiredb.server.service;

import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ubc.pavlab.aspiredb.server.biomartquery.BioMartQueryService;
import ubc.pavlab.aspiredb.server.dao.CharacteristicDao;
import ubc.pavlab.aspiredb.server.dao.LabelDao;
import ubc.pavlab.aspiredb.server.dao.VariantDao;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.gemma.NeurocartaQueryService;
import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.*;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.suggestions.SuggestionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO Document Me
 *
 * @author ??
 * @version $Id: VariantServiceImpl.java,v 1.39 2013/07/02 18:20:21 anton Exp $
 */
@RemoteProxy(name = "VariantService")
@Service("variantService")
public class VariantServiceImpl extends GwtService implements VariantService {

    private static Logger log = LoggerFactory.getLogger(VariantServiceImpl.class);

    @Autowired
    private VariantDao variantDao;
    @Autowired
    private LabelDao labelDao;
    @Autowired
    private CharacteristicDao characteristicDao;
    @Autowired
    private BioMartQueryService bioMartQueryService;
    @Autowired
    private NeurocartaQueryService neurocartaQueryService;
    @Autowired
    private ChromosomeService chromosomeService;

    private String getSortColumn(PagingLoadConfig config) {
        // default value
        String property = "id";

        if (config.getSortInfo() != null && !config.getSortInfo().isEmpty()) {
            SortInfo sortInfo = config.getSortInfo().iterator().next();
            property = sortInfo.getSortField();
        }

//      String columnName = propertyToColumnName.get( property );
        return "id";
    }

    private String getSortDirection(PagingLoadConfig config) {
        // default value
        String direction = "ASC";

        if (config.getSortInfo() != null && !config.getSortInfo().isEmpty()) {
            SortInfo sortInfo = config.getSortInfo().iterator().next();
            direction = sortInfo.getSortDir().toString();
        }
        return direction;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<Property> suggestPropertiesForVariantType(VariantType variantType) {
        Collection<Property> properties = new ArrayList<Property>();

        properties.add(new VariantLabelProperty());

        properties.addAll(suggestEntityProperties(variantType));

        Collection<String> characteristics = characteristicDao.getKeysMatching("");
        for (String characteristic : characteristics) {
            properties.add(new CharacteristicProperty(characteristic));
        }
        return properties;
    }

    private Collection<Property> suggestEntityProperties(VariantType variantType) {
        Collection<Property> properties = new ArrayList<Property>();

        switch (variantType) {
            case CNV:
                properties.add(new CopyNumberProperty());
                properties.add(new CNVTypeProperty());
                properties.add(new CnvLengthProperty());
                break;
            case SNV:
                properties.add(new DbSnpIdProperty());
                properties.add(new ObservedBaseProperty());
                properties.add(new ReferenceBaseProperty());
                break;
            case INDEL:
                properties.add(new IndelLengthProperty());
                break;
            case INVERSION:
                break;
            case TRANSLOCATION:
                properties.add(new TranslocationTypeProperty());
                break;
        }
        return properties;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<Property> suggestProperties() throws NotLoggedInException {
        Collection<Property> properties = new ArrayList<Property>();
        for (VariantType type : VariantType.values()) {
            properties.addAll(suggestEntityProperties(type));
        }

        properties.add(new VariantLabelProperty());

        Collection<String> characteristics = characteristicDao.getKeysMatching("");
        for (String characteristic : characteristics) {
            properties.add(new CharacteristicProperty(characteristic));
        }

        return properties;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<PropertyValue> suggestValues(Property property, SuggestionContext suggestionContext) throws NotLoggedInException, BioMartServiceException, NeurocartaServiceException {
        List<PropertyValue> values = new ArrayList<PropertyValue>();
        if (property instanceof CharacteristicProperty) {
            Collection<String> stringValues = characteristicDao.getValuesForKey(property.getName());
            for (String stringValue : stringValues) {
                values.add(new PropertyValue<TextValue>(new TextValue(stringValue)));
            }
        } else if (property instanceof LabelProperty) {
            List<LabelValueObject> labels = suggestLabels(suggestionContext);
            for (LabelValueObject label : labels) {
                values.add(new PropertyValue<LabelValueObject>(label));
            }
        } else if (property instanceof GeneProperty) {
            String query = suggestionContext.getValuePrefix();
            if (query.length() >= 2) {
                final Collection<GeneValueObject> genes = bioMartQueryService.findGenes(query);
                for (GeneValueObject gene : genes) {
                    values.add(new PropertyValue<GeneValueObject>(gene));
                }
            }
        } else if (property instanceof NeurocartaPhenotypeProperty) {
            String query = suggestionContext.getValuePrefix();
            if (query.length() >= 3) {
                final Collection<NeurocartaPhenotypeValueObject> phenotypes = neurocartaQueryService.findPhenotypes(query);
                for (NeurocartaPhenotypeValueObject phenotype : phenotypes) {
                    values.add(new PropertyValue<NeurocartaPhenotypeValueObject>(phenotype));
                }
            }
        } else if (property instanceof GenomicLocationProperty) {
            values.addAll(suggestVariantLocationValues(property, suggestionContext));
        } else if (property instanceof TextProperty) {
            Collection<String> stringValues = ((TextProperty) property).getDataType().getAllowedValues();
            if (stringValues.isEmpty()) {
                stringValues = variantDao.suggestValuesForEntityProperty(property, suggestionContext);
            }
            for (String stringValue : stringValues) {
                values.add(new PropertyValue<TextValue>(new TextValue(stringValue)));
            }
        }
        return values;
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public Collection<String> suggestCharacteristicPropertyValues(CharacteristicProperty property) {
        return characteristicDao.getValuesForKey(property.getName());
    }

    @Override
    @RemoteMethod
    @Transactional(readOnly = true)
    public VariantValueObject getVariant(Long id) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();

        Variant variant = variantDao.load(id);
        return variant.toValueObject();
    }

    @Override
    @RemoteMethod
    public Collection<Property> suggestVariantLocationProperties() throws NotLoggedInException {
        Collection<Property> properties = new ArrayList<Property>();

        properties.add(new GenomicLocationProperty());
        properties.add(new GeneProperty());
        properties.add(new NeurocartaPhenotypeProperty());

        return properties;
    }

    @Override
    @RemoteMethod
    public Collection<PropertyValue> suggestVariantLocationValues(Property property, SuggestionContext suggestionContext)
            throws NotLoggedInException, BioMartServiceException, NeurocartaServiceException {
        Collection<PropertyValue> values = new ArrayList<PropertyValue>();

        if (property instanceof GeneProperty) {
            Collection<GeneValueObject> genes = this.bioMartQueryService.findGenes(suggestionContext.getValuePrefix());
            for (GeneValueObject gene : genes) {
                values.add(new PropertyValue<GeneValueObject>(gene));
            }
        } else if (property instanceof GenomicLocationProperty) {
            return suggestVariantRange(suggestionContext.getValuePrefix());
        } else if (property instanceof NeurocartaPhenotypeProperty) {
            Collection<NeurocartaPhenotypeValueObject> phenotypes = this.neurocartaQueryService.findPhenotypes(suggestionContext.getValuePrefix());
            for (NeurocartaPhenotypeValueObject phenotype : phenotypes) {
                values.add(new PropertyValue<NeurocartaPhenotypeValueObject>(phenotype));
            }
        }

        return values;
    }

    private List<PropertyValue> suggestVariantRange(String query) {
        final GenomicRangeParser.ParseResult parseResult = GenomicRangeParser.parse(query);
        Map<String, ChromosomeValueObject> chromosomes = chromosomeService.getChromosomes();
        List<PropertyValue> suggestions = new ArrayList<PropertyValue>();
        if (parseResult == null) {
            return suggestions;
        }

        final GenomicRangeParser.State state = parseResult.getState();
        if (state == GenomicRangeParser.State.INITIAL || state == GenomicRangeParser.State.CHROMOSOME) {
            for (String chromosome : chromosomes.keySet()) {
                if (chromosome.startsWith(query.toUpperCase())) {
                    suggestions.add(new PropertyValue<GenomicRange>(new GenomicRange(chromosome)));
                }
            }
        } else if (state == GenomicRangeParser.State.COORDINATE) {
            if (parseResult.isBase()) {
                suggestions.add(new PropertyValue<GenomicRange>(new GenomicRange(
                        parseResult.getChromosome(), parseResult.getStartBase(), parseResult.getEndBase())));
                return suggestions;
            }
        } else if (state == GenomicRangeParser.State.START_RANGE || state == GenomicRangeParser.State.END_RANGE) {
            String chromosomeName = parseResult.getChromosome().toUpperCase();
            String startRange = parseResult.getStartBand();

            ChromosomeValueObject chromosomeValueObject = chromosomes.get(chromosomeName);

            Map<String, ChromosomeBand> bands = chromosomeValueObject.getBands();

            boolean hasStartRange = startRange != null && bands.keySet().contains(startRange.toLowerCase());

            for (ChromosomeBand band : bands.values()) {
                final String suggestString;
                if (hasStartRange) {
                    suggestString = chromosomeName + startRange + "-" + band.getName();
                    if (suggestString.toLowerCase().startsWith(query.toLowerCase())) {
                        GenomicRange suggestion = new GenomicRange(chromosomeName, startRange, band.getName());
                        suggestion.setBaseStart(bands.get(startRange).getStart());
                        suggestion.setBaseEnd(band.getEnd());
                        suggestions.add(new PropertyValue<GenomicRange>(suggestion));
                    }
                } else {
                    suggestString = chromosomeName + band.getName();
                    if (suggestString.toLowerCase().startsWith(query.toLowerCase())) {
                        GenomicRange suggestion = new GenomicRange(chromosomeName, band.getName(), null);
                        suggestion.setBaseStart(band.getStart());
                        suggestion.setBaseEnd(band.getEnd());
                        suggestions.add(new PropertyValue<GenomicRange>(suggestion));
                    }
                }
            }
        }
        return suggestions;
    }

    @Override
    @Transactional
    public LabelValueObject addLabel(Long id, LabelValueObject label) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();
        Variant variant = variantDao.load(id);
        Label labelEntity = labelDao.findOrCreate(label);
        variant.addLabel(labelEntity);
        return labelEntity.toValueObject();
    }

    @Override
    @Transactional
    public LabelValueObject addLabel(Collection<Long> ids, LabelValueObject label) throws NotLoggedInException {
        throwGwtExceptionIfNotLoggedIn();
        Collection<Variant> variants = variantDao.load(ids);
        Label labelEntity = labelDao.findOrCreate(label);
        for (Variant variant : variants) {
            variant.addLabel(labelEntity);
            variantDao.update(variant);
        }
        return labelEntity.toValueObject();
    }

    @Override
    @Transactional
    public void removeLabel(Long id, LabelValueObject label) throws NotLoggedInException {
        Variant variant = variantDao.load(id);
        Label labelEntity = labelDao.load(label.getId());
        variant.removeLabel(labelEntity);
        variantDao.update(variant);
    }

    @Override
    @Transactional
    public void removeLabel(Collection<Long> variantIds, LabelValueObject label) throws NotLoggedInException {
        for (Long variantId : variantIds) {
            removeLabel(variantId, label);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelValueObject> suggestLabels(SuggestionContext suggestionContext) {
        // TODO: filter out labels non-applicable to variants
        // labelDao.getLabelsMatching( partialName );
        Collection<Label> labels = labelDao.getVariantLabels();
        List<LabelValueObject> vos = new ArrayList<LabelValueObject>();
        for (Label label : labels) {
            vos.add(label.toValueObject());
        }
        return vos;
    }
}
