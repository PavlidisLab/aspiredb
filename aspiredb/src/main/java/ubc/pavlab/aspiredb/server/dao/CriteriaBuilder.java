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

import org.hibernate.criterion.*;
import org.hibernate.criterion.Junction;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.*;
import ubc.pavlab.aspiredb.shared.query.*;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.Disjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.*;

import java.io.Serializable;
import java.util.Set;

/**
 * Constructs Hibernate Criterion based on various subclasses of RestrictionExpression.
 *
 * RestrictionExpression tree is traversed pre-order.
 * Conjunction, Disjunction are non-leaf nodes.
 * SimpleRestriction, SetRestriction are leaf nodes.
 * 
 * @author anton
 * @version $Id: CriteriaBuilder.java,v 1.23 2013/07/02 18:20:22 anton Exp $
 */
public class CriteriaBuilder {

    public enum EntityType {
        SUBJECT(Subject.class), VARIANT(Variant.class);

        protected Class clazz;

        private EntityType(Class clazz) {
            this.clazz = clazz;
        }
    }

    /**
     *
     * @param restrictionExpression
     * @param target specifies what query should return (subjects or variants)
     * @return
     */
    public static Criterion buildCriteriaRestriction( RestrictionExpression restrictionExpression, EntityType target ) {
        if ( restrictionExpression instanceof Disjunction ) {
            return processRestrictionExpression( ( Disjunction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof Conjunction ) {
            return processRestrictionExpression( ( Conjunction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof SetRestriction ) {
            return processRestrictionExpression( ( SetRestriction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof PhenotypeRestriction ) {
            return processRestrictionExpression( ( PhenotypeRestriction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof SimpleRestriction ) {
            return processRestrictionExpression( ( SimpleRestriction ) restrictionExpression, target );
        } else if ( restrictionExpression instanceof VariantTypeRestriction ) {
            return processRestrictionExpression( ( VariantTypeRestriction ) restrictionExpression, target );
        }
        throw new IllegalArgumentException( "Restriction type " + restrictionExpression.getClass().getSimpleName()
                + " is not supported yet." );
    }

    private static Criterion processRestrictionExpression( Disjunction disjunction, EntityType target ) {
        Junction criteriaDisjunction = Restrictions.disjunction();
        for ( RestrictionExpression restriction : disjunction.getRestrictions() ) {
            criteriaDisjunction.add( buildCriteriaRestriction( restriction, target ) );
        }
        return criteriaDisjunction;
    }

    private static Criterion processRestrictionExpression( Conjunction conjunction, EntityType target ) {
        Junction criteriaConjunction = Restrictions.conjunction();
        for ( RestrictionExpression restriction : conjunction.getRestrictions() ) {
            criteriaConjunction.add( buildCriteriaRestriction( restriction, target ) );
        }
        return criteriaConjunction;
    }

    private static Criterion processRestrictionExpression(SetRestriction setRestriction, EntityType target) {
        Property property = setRestriction.getProperty();
        SetOperator operator = (SetOperator) setRestriction.getOperator();
        Set<? extends Serializable> values = setRestriction.getValues();

        DetachedCriteria subquery = DetachedCriteria.forClass(target.clazz);

        Junction criteriaDisjunction = Restrictions.disjunction();

        if ( property instanceof CharacteristicProperty ) {
            for (Serializable value: values) {
                criteriaDisjunction.add( createCharacteristicCriterion (
                                ( CharacteristicProperty ) property,
                                TextOperator.EQUAL,
                                ( TextValue ) value,
                                target ));
            }
        } else if ( property instanceof LabelProperty ) {
            for (Serializable value: values) {
                criteriaDisjunction.add( createLabelCriterion(
                        ( LabelProperty ) property,
                        TextOperator.EQUAL,
                        ( LabelValueObject ) value, target ));
            }
        } else if ( property instanceof CNVTypeProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            for (Serializable value: values) {
                criteriaDisjunction.add( createCNVTypeCriterion(
                        TextOperator.EQUAL,
                        fullEntityPropertyName(target, propertyOf, property),
                        ( TextValue ) value ));
            }
        } else if ( property instanceof ExternalSubjectIdProperty ) {
            EntityType propertyOf = EntityType.SUBJECT;
            for (Serializable value: values) {
                criteriaDisjunction.add( createTextCriterion(
                        TextOperator.EQUAL,
                        fullEntityPropertyName(target, propertyOf, property),
                        ( ( TextValue ) value ).getValue() ));
            }
        } else if ( property instanceof TextProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            for (Serializable value: values) {
                criteriaDisjunction.add( createTextCriterion(
                        TextOperator.EQUAL,
                        fullEntityPropertyName(target, propertyOf, property),
                        ( ( TextValue ) value ).getValue() ));
            }
        } else if ( property instanceof GenomicLocationProperty ) {
            for (Serializable value: values) {
                criteriaDisjunction.add( overlapsGenomicRegionCriterion( (GenomicRange) value ) );
            }
        } else if ( property instanceof GeneProperty ) {
            for (Serializable value: values) {
                GeneValueObject gene = ( GeneValueObject ) value;
                criteriaDisjunction.add( overlapsGenomicRegionCriterion( gene.getGenomicRange() ) );
            }
        } else if ( property instanceof NeurocartaPhenotypeProperty ) {
            for (Serializable value : values) {
                NeurocartaPhenotypeValueObject neurocartaPhenotype = ( NeurocartaPhenotypeValueObject ) value;
                for ( GeneValueObject gene : neurocartaPhenotype.getGenes() ) {
                    criteriaDisjunction.add( overlapsGenomicRegionCriterion(gene.getGenomicRange()) );
                }
            }
        } else {
            throw new IllegalArgumentException("Not supported!");
        }

        subquery.add(criteriaDisjunction);
        subquery.setProjection(Projections.distinct(Projections.id()));

        switch (operator) {
            case IS_IN:
                return Subqueries.propertyIn( "id", subquery );
            case IS_NOT_IN:
                return Subqueries.propertyNotIn("id", subquery);
            default:
                throw new IllegalArgumentException("Operator not supported.");
        }
    }

    private static Criterion processRestrictionExpression( PhenotypeRestriction restriction, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addPhenotypeAlias(subquery, target);

        subquery.add( Restrictions.conjunction()
                .add( Restrictions.eq( "phenotype.name", restriction.getPhenotypeName() ) )
                .add( Restrictions.eq( "phenotype.value", restriction.getValue() ) ) );

        subquery.setProjection( Projections.distinct( Projections.id() ) );

        return Subqueries.propertyIn( "id", subquery );
    }

    private static void addPhenotypeAlias(DetachedCriteria subquery, EntityType target) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias( "phenotypes", "phenotype" );
        } else {
            subquery.createAlias( "subject", "subject" ).createAlias( "subject.phenotypes", "phenotype" );
        }
    }

    private static Criterion processRestrictionExpression( SimpleRestriction restriction, EntityType target ) {
        Property property = restriction.getProperty();
        Operator operator = restriction.getOperator();
        Serializable value = restriction.getValue();

        if ( property instanceof CharacteristicProperty ) {
            return createCharacteristicCriterion( ( CharacteristicProperty ) property, operator, ( TextValue ) value,
                    target );
        } else if ( property instanceof LabelProperty ) {
            return createLabelCriterion( ( LabelProperty ) property, ( TextOperator ) operator,
                    ( LabelValueObject ) value, target );
        } else if ( property instanceof CNVTypeProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            return createCNVTypeCriterion( ( TextOperator ) operator,
                    fullEntityPropertyName(target, propertyOf, property), ( TextValue ) value );
        } else if ( property instanceof ExternalSubjectIdProperty ) {
            EntityType propertyOf = EntityType.SUBJECT;
            return createTextCriterion( ( TextOperator ) operator,
                    fullEntityPropertyName(target, propertyOf, property), ( ( TextValue ) value ).getValue() );
        } else if ( property instanceof NumericProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            return createNumericalCriterion( ( NumericOperator ) operator, propertyPrefix( target, propertyOf )
                    + property.getName(), ( NumericValue ) value );
        } else if ( property instanceof TextProperty ) {
            EntityType propertyOf = EntityType.VARIANT;
            return createTextCriterion( ( TextOperator ) operator,
                    fullEntityPropertyName(target, propertyOf, property),
                    ( ( TextValue ) value ).getValue() );
        } else if ( property instanceof GenomicLocationProperty ) {
            return createGenomicRangeCriterion( (SetOperator) operator, ( GenomicRange ) value, target );
        } else if ( property instanceof GeneProperty ) {
            GeneValueObject gene = ( GeneValueObject ) value;
            return createGenomicRangeCriterion( (SetOperator) operator, gene.getGenomicRange(), target );
        } else if ( property instanceof NeurocartaPhenotypeProperty ) {
            NeurocartaPhenotypeValueObject neurocartaPhenotype = ( NeurocartaPhenotypeValueObject ) value;
            Junction criteriaDisjunction = Restrictions.disjunction();
            for ( GeneValueObject gene : neurocartaPhenotype.getGenes() ) {
                criteriaDisjunction.add( createGenomicRangeCriterion( (SetOperator) operator,
                        gene.getGenomicRange(), target ) );
            }
            return criteriaDisjunction;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Criterion processRestrictionExpression( VariantTypeRestriction restriction, EntityType target ) {
        if ( target == EntityType.SUBJECT ) {
            return Restrictions.eq( "variant.class", restriction.getType().name() );
        } else {
            return Restrictions.eq( "class", restriction.getType().name() );
        }
    }


    private static Criterion overlapsGenomicRegionCriterion(GenomicRange range) {
        Junction variantInsideRegion = Restrictions.conjunction()
                .add( Restrictions.eq( "location.chromosome", range.getChromosome() ) )
                .add( Restrictions.ge( "location.start", range.getBaseStart() ) )
                .add( Restrictions.le( "location.end", range.getBaseEnd() ) );

        Junction variantHitsStartOfRegion = Restrictions.conjunction()
                .add( Restrictions.eq( "location.chromosome", range.getChromosome() ) )
                .add( Restrictions.le( "location.start", range.getBaseStart() ) )
                .add( Restrictions.ge( "location.end", range.getBaseStart() ) );

        Junction variantHitsEndOfRegion = Restrictions.conjunction()
                .add( Restrictions.eq( "location.chromosome", range.getChromosome() ) )
                .add( Restrictions.le( "location.start", range.getBaseEnd() ) )
                .add( Restrictions.ge( "location.end", range.getBaseEnd() ) );

        Criterion rangeCriterion = Restrictions.disjunction().add( variantInsideRegion ).add( variantHitsStartOfRegion )
                .add( variantHitsEndOfRegion );

        return rangeCriterion;
    }

    private static Criterion createGenomicRangeCriterion( SetOperator operator, GenomicRange range, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addLocationAlias( subquery, target );

        subquery.add( overlapsGenomicRegionCriterion( range ) );

        subquery.setProjection( Projections.distinct( Projections.id() ) );
        switch (operator) {
            case IS_IN:
                return Subqueries.propertyIn( "id", subquery );
            case IS_NOT_IN:
                return Subqueries.propertyNotIn("id", subquery);
            default:
                throw new IllegalArgumentException("Operator not supported.");
        }
    }

    private static void addLocationAlias(DetachedCriteria subquery, EntityType target) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias( "variants", "variant" );
            subquery.createAlias( "variant.location", "location" );
        }
    }

    private static Criterion createCNVTypeCriterion( TextOperator operator, String property, TextValue value ) {
        CnvType enumValue = CnvType.valueOf( value.toString() );
        return createTextCriterion( operator, property, enumValue );
    }

    private static Criterion createLabelCriterion( LabelProperty property, TextOperator operator,
            LabelValueObject value, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );
        
        addLabelAlias(subquery, target);

        if ( property instanceof VariantLabelProperty ) {
            subquery.add( Restrictions.eq( "variant_label.id", value.getId() ) );
        } else if ( property instanceof SubjectLabelProperty ) {
            subquery.add( Restrictions.eq( "subject_label.id", value.getId() ) );
        }

        subquery.setProjection( Projections.distinct( Projections.id() ) );

        if ( operator == TextOperator.EQUAL ) {
            return Subqueries.propertyIn( "id", subquery );
        } else if ( operator == TextOperator.NOT_EQUAL ) {
            return Subqueries.propertyNotIn( "id", subquery );
        }

        throw new IllegalArgumentException();
    }

    private static void addLabelAlias(DetachedCriteria subquery, EntityType target) {
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

    /**
     * 
     * @param subquery (side effects)
     * @param target
     */
    private static void addCharacteristicAlias(DetachedCriteria subquery, EntityType target) {
        if ( target == EntityType.SUBJECT ) {
            subquery.createAlias("variants", "variant")
                    .createAlias("variant.characteristics", "characteristic", CriteriaSpecification.LEFT_JOIN);
        } else {
            subquery.createAlias("characteristics", "characteristic", CriteriaSpecification.LEFT_JOIN);
        }
    }
    
    private static Criterion createCharacteristicCriterion( CharacteristicProperty property, Operator operator,
            TextValue value, EntityType target ) {
        DetachedCriteria subquery = DetachedCriteria.forClass( target.clazz );

        addCharacteristicAlias( subquery, target );

        Junction conjunction = Restrictions.conjunction().add(
                Restrictions.eq( "characteristic.key", property.getName() ) );

        if ( operator instanceof NumericOperator ) {
            NumericValue numValue = new NumericValue( Integer.valueOf( value.getValue() ) );
            conjunction
                    .add( createNumericalCriterion( ( NumericOperator ) operator, "characteristic.value", numValue ) );
        } else if ( operator instanceof TextOperator ) {
            conjunction
                    .add( createTextCriterion( ( TextOperator ) operator, "characteristic.value", value.toString() ) );
        } else {
            throw new IllegalArgumentException( "Operator type not supported." );
        }

        subquery.add( conjunction );

        subquery.setProjection( Projections.distinct( Projections.id() ) );
        return Subqueries.propertyIn( "id", subquery );
    }

    /**
     *
     * @param operator
     * @param property
     * @param value
     * @return
     */
    private static Criterion createNumericalCriterion( NumericOperator operator, String property, NumericValue value ) {
        Criterion criterion;
        switch ( operator ) {
            case GREATER:
                criterion = Restrictions.gt( property, value.getValue() );
                break;
            case LESS:
                criterion = Restrictions.lt( property, value.getValue() );
                break;
            case EQUAL:
                criterion = Restrictions.eq( property, value.getValue() );
                break;
            case NOT_EQUAL:
                criterion = Restrictions.ne( property, value.getValue() );
                break;
            default:
                throw new IllegalArgumentException();
        }
        return criterion;
    }

    /**
     *
     *
     * @param operator EQUALS or NOT_EQUALS
     * @param property property
     * @param value text value
     * @return Hibernate criterion
     */
    private static Criterion createTextCriterion( TextOperator operator, String property, Object value ) {
        Criterion criterion;
        switch ( operator ) {
            case EQUAL:
                criterion = Restrictions.eq( property, value );
                break;
            case NOT_EQUAL:
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
    private static String fullEntityPropertyName(EntityType filterTarget, EntityType propertyOf, Property property) {
        return propertyPrefix( filterTarget, propertyOf ) + property.getName();
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
