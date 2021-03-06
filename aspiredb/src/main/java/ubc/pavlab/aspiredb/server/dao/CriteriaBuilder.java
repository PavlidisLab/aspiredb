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
package ubc.pavlab.aspiredb.server.dao;

import java.util.List;
import java.util.Set;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.server.util.GenomeBin;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;
import ubc.pavlab.aspiredb.shared.NumericValue;
import ubc.pavlab.aspiredb.shared.TextValue;
import ubc.pavlab.aspiredb.shared.query.CNVTypeProperty;
import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;
import ubc.pavlab.aspiredb.shared.query.ExternalSubjectIdProperty;
import ubc.pavlab.aspiredb.shared.query.GeneProperty;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.LabelProperty;
import ubc.pavlab.aspiredb.shared.query.NeurocartaPhenotypeProperty;
import ubc.pavlab.aspiredb.shared.query.NumericProperty;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.SubjectLabelProperty;
import ubc.pavlab.aspiredb.shared.query.TextProperty;
import ubc.pavlab.aspiredb.shared.query.VariantLabelProperty;
import ubc.pavlab.aspiredb.shared.query.VariantTypeProperty;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.Disjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.PhenotypeRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.SetRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;
import ubc.pavlab.aspiredb.shared.query.restriction.VariantTypeRestriction;

/**
 * Constructs Hibernate Criterion based on various subclasses of RestrictionExpression. RestrictionExpression tree is
 * traversed pre-order. Conjunction, Disjunction are non-leaf nodes. SimpleRestriction, SetRestriction are leaf nodes.
 * 
 * @author anton
 * @version $Id: CriteriaBuilder.java,v 1.23 2013/07/02 18:20:22 anton Exp $
 */
public class CriteriaBuilder {

    private static Logger log = LoggerFactory.getLogger( CriteriaBuilder.class );

    public enum EntityType {
        SUBJECT(Subject.class), VARIANT(Variant.class);

        protected Class<?> clazz;

        private EntityType( Class<?> clazz ) {
            this.clazz = clazz;
        }
    }

    /**
     * @param restrictionExpression
     * @param target specifies what query should return (subjects or variants)
     * @return
     */
    public static Criterion buildCriteriaRestriction( RestrictionExpression restrictionExpression, EntityType target ) {
        Criterion result = null;
        if ( restrictionExpression instanceof Disjunction ) {
            result = processRestrictionExpression( ( Disjunction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof Conjunction ) {
            result = processRestrictionExpression( ( Conjunction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof SetRestriction ) {
            result = processRestrictionExpression( ( SetRestriction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof PhenotypeRestriction ) {
            result = processRestrictionExpression( ( PhenotypeRestriction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof SimpleRestriction ) {
            result = processRestrictionExpression( ( SimpleRestriction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof VariantTypeRestriction ) {
            result = processRestrictionExpression( ( VariantTypeRestriction ) restrictionExpression, target );
        } else {
            throw new IllegalArgumentException( "Restriction type " + restrictionExpression.getClass().getSimpleName()
                    + " is not supported yet." );
        }
        return result;
    }

    /**
     * @param subquery (side effects)
     * @param target
     */
    private static void addCharacteristicAlias( DetachedCriteria subquery, EntityType target ) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias( "variants", "variant" ).createAlias( "variant.characteristics", "characteristic",
                    CriteriaSpecification.LEFT_JOIN );
        } else {
            subquery.createAlias( "characteristics", "characteristic", CriteriaSpecification.LEFT_JOIN );
        }
    }

    private static void addLabelAlias( DetachedCriteria subquery, EntityType target ) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias( "variants", "variant", CriteriaSpecification.LEFT_JOIN )
                    .createAlias( "variant.labels", "variant_label", CriteriaSpecification.LEFT_JOIN )
                    .createAlias( "labels", "subject_label", CriteriaSpecification.LEFT_JOIN );
        } else {
            subquery.createAlias( "subject", "subject" )
                    .createAlias( "subject.labels", "subject_label", CriteriaSpecification.LEFT_JOIN )
                    .createAlias( "labels", "variant_label", CriteriaSpecification.LEFT_JOIN );
        }
    }

    private static void addLocationAlias( DetachedCriteria subquery, EntityType target ) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias( "variants", "variant" );
            subquery.createAlias( "variant.location", "location" );
        }
    }

    private static void addPhenotypeAlias( DetachedCriteria subquery, EntityType target ) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias( "phenotypes", "phenotype" );
        } else {
            subquery.createAlias( "subject", "subject" ).createAlias( "subject.phenotypes", "phenotype" );
        }
    }

    private static Criterion createCharacteristicCriterion( CharacteristicProperty property, Operator operator,
            TextValue value, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addCharacteristicAlias( subquery, target );

        Junction conjunction = Restrictions.conjunction().add(
                Restrictions.eq( "characteristic.key", property.getName() ) );

        switch ( operator ) {
            case TEXT_EQUAL:
            case TEXT_NOT_EQUAL:
                conjunction.add( createTextCriterion( operator, "characteristic.value", value.toString() ) );
                break;
            case NUMERIC_EQUAL:
            case NUMERIC_GREATER:
            case NUMERIC_LESS:
            case NUMERIC_NOT_EQUAL:
                NumericValue numValue = new NumericValue( Integer.valueOf( value.getValue() ) );
                conjunction.add( createNumericalCriterion( operator, "characteristic.value", numValue ) );
                break;
            default:
                throw new IllegalArgumentException( "Operator type not supported." );
        }

        subquery.add( conjunction );

        subquery.setProjection( Projections.distinct( Projections.id() ) );
        return Subqueries.propertyIn( "id", subquery );
    }

    private static Criterion createCNVTypeCriterion( Operator operator, String property, TextValue value ) {
        CnvType enumValue = CnvType.valueOf( value.toString() );
        return createTextCriterion( operator, property, enumValue );
    }

    private static Criterion createGenomicRangeCriterion( Operator operator, GenomicRange range, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addLocationAlias( subquery, target );

        subquery.add( overlapsGenomicRegionCriterion( range ) );

        subquery.setProjection( Projections.distinct( Projections.id() ) );
        switch ( operator ) {
            case IS_IN_SET:
                return Subqueries.propertyIn( "id", subquery );
            case IS_NOT_IN_SET:
                return Subqueries.propertyNotIn( "id", subquery );
            default:
                throw new IllegalArgumentException( "Operator not supported." );
        }
    }

    private static Criterion createLabelCriterion( LabelProperty property, Operator operator, LabelValueObject value,
            EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addLabelAlias( subquery, target );

        if ( property instanceof VariantLabelProperty ) {
            subquery.add( Restrictions.eq( "variant_label.id", value.getId() ) );
        } else if ( property instanceof SubjectLabelProperty ) {
            subquery.add( Restrictions.eq( "subject_label.id", value.getId() ) );
        }

        subquery.setProjection( Projections.distinct( Projections.id() ) );

        if ( operator == Operator.TEXT_EQUAL ) {
            return Subqueries.propertyIn( "id", subquery );
        } else if ( operator == Operator.TEXT_NOT_EQUAL ) {
            return Subqueries.propertyNotIn( "id", subquery );
        }

        throw new IllegalArgumentException();
    }

    /**
     * @param operator
     * @param property
     * @param value
     * @return
     */
    private static Criterion createNumericalCriterion( Operator operator, String property, NumericValue value ) {
        Criterion criterion;
        switch ( operator ) {
            case NUMERIC_GREATER:
                criterion = Restrictions.gt( property, value.getValue() );
                break;
            case NUMERIC_LESS:
                criterion = Restrictions.lt( property, value.getValue() );
                break;
            case NUMERIC_EQUAL:
                criterion = Restrictions.eq( property, value.getValue() );
                break;
            case NUMERIC_NOT_EQUAL:
                criterion = Restrictions.ne( property, value.getValue() );
                break;
            default:
                throw new IllegalArgumentException();
        }
        return criterion;
    }

    /**
     * @param operator EQUALS or NOT_EQUALS
     * @param property property
     * @param value text value
     * @return Hibernate criterion
     */
    private static Criterion createTextCriterion( Operator operator, String property, Object value ) {
        Criterion criterion;
        switch ( operator ) {
            case TEXT_EQUAL:
                criterion = Restrictions.eq( property, value );
                break;
            case TEXT_NOT_EQUAL:
                criterion = Restrictions.ne( property, value );
                break;
            default:
                throw new IllegalArgumentException();
        }
        return criterion;
    }

    /**
     * Construct full entity property name to be used in criteria query.
     * 
     * @param filterTarget entity type criteria query will return
     * @param propertyOf entity type that the property is member of
     * @param property Property
     * @return full entity property name
     */
    private static String fullEntityPropertyName( EntityType filterTarget, EntityType propertyOf, Property property ) {
        return propertyPrefix( filterTarget, propertyOf ) + property.getName();
    }

    /**
     * @param range
     * @return
     */
    private static Criterion overlapsGenomicRegionCriterion( GenomicRange range ) {

        List<Integer> bins = GenomeBin.relevantBins( range.getChromosome(), range.getBaseStart(), range.getBaseEnd() );

        // debug code - generates native SQL to check things relating to a test
        // System.err.println( range + " " + " length=" + ( range.getBaseEnd() - range.getBaseStart() ) + " bins="
        // + StringUtils.join( bins, "," ) );
        // if ( range.getChromosome().equals( "17" ) ) {
        // System.err.println( range
        // + " >> "
        // + String.format( "select distinct location1_.* from GENOMIC_LOC location1_ where "
        // + "location1_.BIN in (%s)  and " + "( (location1_.START>=%d and location1_.END<=%d) "
        // + "or (location1_.START<=%d and location1_.END>=%d) "
        // + "or (location1_.START<=%d and location1_.END>=%d) ); ", StringUtils.join( bins, "," ),
        // range.getBaseStart(), range.getBaseEnd(), range.getBaseStart(), range.getBaseStart(),
        // range.getBaseEnd(), range.getBaseEnd() ) );
        // }

        Junction variantInsideRegion = Restrictions.conjunction()
                .add( Restrictions.ge( "location.start", range.getBaseStart() ) )
                .add( Restrictions.le( "location.end", range.getBaseEnd() ) );

        Junction variantHitsStartOfRegion = Restrictions.conjunction()
                .add( Restrictions.le( "location.start", range.getBaseStart() ) )
                .add( Restrictions.ge( "location.end", range.getBaseStart() ) );

        Junction variantHitsEndOfRegion = Restrictions.conjunction()
                .add( Restrictions.le( "location.start", range.getBaseEnd() ) )
                .add( Restrictions.ge( "location.end", range.getBaseEnd() ) );

        // Note addition of bin restriction. We only care about variants that fall into one of the bins touched by the
        // given range
        Criterion rangeCriterion = Restrictions
                .conjunction()
                .add( Restrictions.in( "location.bin", bins ) )
                // the same bin may exist in different chromosomes
                .add( Restrictions.eq( "location.chromosome", range.getChromosome() ) )
                .add( Restrictions.disjunction().add( variantInsideRegion ).add( variantHitsStartOfRegion )
                        .add( variantHitsEndOfRegion ) );

        log.debug( "RangeCriterion=" + rangeCriterion );

        return rangeCriterion;
    }

    private static Criterion processRestrictionExpression( Conjunction conjunction, EntityType target ) {
        Junction criteriaConjunction = Restrictions.conjunction();
        for ( RestrictionExpression restriction : conjunction.getRestrictions() ) {
            criteriaConjunction.add( buildCriteriaRestriction( restriction, target ) );
        }
        return criteriaConjunction;
    }

    private static Criterion processRestrictionExpression( Disjunction disjunction, EntityType target ) {
        Junction criteriaDisjunction = Restrictions.disjunction();
        for ( RestrictionExpression restriction : disjunction.getRestrictions() ) {
            criteriaDisjunction.add( buildCriteriaRestriction( restriction, target ) );
        }
        return criteriaDisjunction;
    }

    private static Criterion processRestrictionExpression( PhenotypeRestriction restriction, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addPhenotypeAlias( subquery, target );

        subquery.add( Restrictions.conjunction().add( Restrictions.eq( "phenotype.name", restriction.getName() ) )
                .add( Restrictions.eq( "phenotype.value", restriction.getValue() ) ) );

        subquery.setProjection( Projections.distinct( Projections.id() ) );

        return Subqueries.propertyIn( "id", subquery );
    }

    private static Criterion processRestrictionExpression( SetRestriction setRestriction, EntityType target ) {
        Property property = setRestriction.getProperty();
        Operator operator = setRestriction.getOperator();
        Set<Object> values = setRestriction.getValues();

        log.debug( "Property=" + property + "; operator=" + operator + "; values="
                + StringUtils.collectionToCommaDelimitedString( values ) );

        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        Junction criteriaDisjunction = Restrictions.disjunction();

        if ( property instanceof CharacteristicProperty ) {
            for ( Object value : values ) {
                criteriaDisjunction.add( createCharacteristicCriterion( ( CharacteristicProperty ) property,
                        Operator.TEXT_EQUAL, ( TextValue ) value, target ) );
            }
        } else if ( property instanceof LabelProperty ) {
            for ( Object value : values ) {
                criteriaDisjunction.add( createLabelCriterion( ( LabelProperty ) property, Operator.TEXT_EQUAL,
                        ( LabelValueObject ) value, target ) );
            }
        } else if ( property instanceof CNVTypeProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            for ( Object value : values ) {
                criteriaDisjunction.add( createCNVTypeCriterion( Operator.TEXT_EQUAL,
                        fullEntityPropertyName( target, propertyOf, property ), ( TextValue ) value ) );
            }
        } else if ( property instanceof VariantTypeProperty ) {
            for ( Object value : values ) {
                criteriaDisjunction.add( createVariantTypeCriterion( target, ( TextValue ) value ) );
            }
        } else if ( property instanceof ExternalSubjectIdProperty ) {
            EntityType propertyOf = EntityType.SUBJECT;
            for ( Object value : values ) {
                criteriaDisjunction.add( createTextCriterion( Operator.TEXT_EQUAL,
                        fullEntityPropertyName( target, propertyOf, property ), ( ( TextValue ) value ).getValue() ) );
            }
        } else if ( property instanceof TextProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            for ( Object value : values ) {
                criteriaDisjunction.add( createTextCriterion( Operator.TEXT_EQUAL,
                        fullEntityPropertyName( target, propertyOf, property ), ( ( TextValue ) value ).getValue() ) );
            }
        } else if ( property instanceof GenomicLocationProperty ) {
            for ( Object value : values ) {
                criteriaDisjunction.add( overlapsGenomicRegionCriterion( ( GenomicRange ) value ) );
            }
        } else if ( property instanceof GeneProperty ) {
            for ( Object value : values ) {
                GeneValueObject gene = ( GeneValueObject ) value;
                criteriaDisjunction.add( overlapsGenomicRegionCriterion( gene.getGenomicRange() ) );
            }
        } else if ( property instanceof NeurocartaPhenotypeProperty ) {
            for ( Object value : values ) {
                NeurocartaPhenotypeValueObject neurocartaPhenotype = ( NeurocartaPhenotypeValueObject ) value;
                for ( GeneValueObject gene : neurocartaPhenotype.getGenes() ) {
                    criteriaDisjunction.add( overlapsGenomicRegionCriterion( gene.getGenomicRange() ) );
                }
            }
        } else {
            throw new IllegalArgumentException( "Not supported!" );
        }

        subquery.add( criteriaDisjunction );
        subquery.setProjection( Projections.distinct( Projections.id() ) );
        log.debug( "subquery = " + subquery );

        switch ( operator ) {
            case IS_IN_SET:
                return Subqueries.propertyIn( "id", subquery );
            case IS_NOT_IN_SET:
                return Subqueries.propertyNotIn( "id", subquery );
            default:
                throw new IllegalArgumentException( "Operator not supported." );
        }
    }

    /**
     * Criteria for VariantTypes, e.g. CNV, SNV, ...
     * 
     * @param target
     * @param value
     * @return
     */
    private static Criterion createVariantTypeCriterion( EntityType target, TextValue value ) {
        String type = value.toString();
        if ( target == EntityType.SUBJECT ) {
            return Restrictions.eq( "variant.class", type );
        }
        return Restrictions.eq( "class", type );
    }

    private static Criterion processRestrictionExpression( SimpleRestriction restriction, EntityType target ) {
        Property property = restriction.getProperty();
        Operator operator = restriction.getOperator();
        Object value = restriction.getValue();

        log.debug( "Property=" + property + "; operator=" + operator + "; value=" + value );

        if ( property instanceof CharacteristicProperty ) {
            return createCharacteristicCriterion( ( CharacteristicProperty ) property, operator, ( TextValue ) value,
                    target );
        } else if ( property instanceof LabelProperty ) {
            return createLabelCriterion( ( LabelProperty ) property, operator, ( LabelValueObject ) value, target );
        } else if ( property instanceof CNVTypeProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            return createCNVTypeCriterion( operator, fullEntityPropertyName( target, propertyOf, property ),
                    ( TextValue ) value );
        } else if ( property instanceof VariantTypeProperty ) {
            return createVariantTypeCriterion( target, ( TextValue ) value );
        } else if ( property instanceof ExternalSubjectIdProperty ) {
            EntityType propertyOf = EntityType.SUBJECT;
            return createTextCriterion( operator, fullEntityPropertyName( target, propertyOf, property ),
                    ( ( TextValue ) value ).getValue() );
        } else if ( property instanceof NumericProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            return createNumericalCriterion( operator, propertyPrefix( target, propertyOf ) + property.getName(),
                    ( NumericValue ) value );
        } else if ( property instanceof TextProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            return createTextCriterion( operator, fullEntityPropertyName( target, propertyOf, property ),
                    ( ( TextValue ) value ).getValue() );
        } else if ( property instanceof GenomicLocationProperty ) {
            return createGenomicRangeCriterion( operator, ( GenomicRange ) value, target );
        } else if ( property instanceof GeneProperty ) {
            GeneValueObject gene = ( GeneValueObject ) value;
            return createGenomicRangeCriterion( operator, gene.getGenomicRange(), target );
        } else if ( property instanceof NeurocartaPhenotypeProperty ) {
            NeurocartaPhenotypeValueObject neurocartaPhenotype = ( NeurocartaPhenotypeValueObject ) value;
            Junction criteriaDisjunction = Restrictions.disjunction();
            for ( GeneValueObject gene : neurocartaPhenotype.getGenes() ) {
                criteriaDisjunction.add( createGenomicRangeCriterion( operator, gene.getGenomicRange(), target ) );
            }
            return criteriaDisjunction;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Criterion processRestrictionExpression( VariantTypeRestriction restriction, EntityType target ) {
        if ( target == EntityType.SUBJECT ) {
            return Restrictions.eq( "variant.class", restriction.getType() );
        }
        return Restrictions.eq( "class", restriction.getType() );

    }

    /**
     * Get prefix to be used to fully name entity property based on.
     * 
     * @param filterTarget entity type criteria query will return
     * @param propertyOf entity type that the property is member of
     * @return either 'variant.' or 'subject.'
     */
    private static String propertyPrefix( EntityType filterTarget, EntityType propertyOf ) {
        String prefix = "";
        if ( filterTarget == EntityType.SUBJECT && propertyOf == EntityType.VARIANT ) {
            prefix = "variant.";
        } else if ( filterTarget == EntityType.VARIANT && propertyOf == EntityType.SUBJECT ) {
            prefix = "subject.";
        }
        return prefix;
    }
}
