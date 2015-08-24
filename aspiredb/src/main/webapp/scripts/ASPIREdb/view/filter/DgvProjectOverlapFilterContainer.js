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
Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.OrFilterContainer' ] );

/**
 * DGV special project overlap.
 */
Ext.define( 'ASPIREdb.view.filter.DgvProjectOverlapFilterContainer', {
   extend : 'Ext.Container',
   alias : 'widget.filter_dgvprojectoverlap',
   layout : {
      type : 'vbox'
   },
   config : {
      propertyStore : {

         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForProjectOverlap,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      },
      propertyStore2 : {

         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForNumberOfVariantsInProjectOverlap,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      },
      propertyStore3 : {

         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForSupportOfVariantsInProjectOverlap,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      },
      suggestValuesRemoteFunction : null,
      projectOverlapFilterItemType : 'ASPIREdb.view.filter.ProjectOverlapPropertyFilter'
   },
   items : [ {
      xtype : 'container',
      itemId : 'filterContainer',
      layout : {
         type : 'vbox',
         defaultMargins : {
            top : 5,
            right : 5,
            left : 5,
            bottom : 5
         }
      }
   } ],

   getRestrictionExpression : function() {
      var filterContainer = this.getComponent( 'filterContainer' );

      var projectOverlapConfig = new ProjectOverlapFilterConfig();

      projectOverlapConfig.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
      var overlapProjectIds = [];
      overlapProjectIds.push( this.specialProjectValueObject.id );

      projectOverlapConfig.overlapProjectIds = overlapProjectIds;

      var overlapRestriction = filterContainer.getComponent( 'overlapItem' ).getRestrictionExpression();

      projectOverlapConfig.restriction1 = this.validateOverlapRestriction( overlapRestriction );

      var numVariantsOverlapRestriction = filterContainer.getComponent( 'numVariantsOverlapItem' )
         .getRestrictionExpression();

      projectOverlapConfig.restriction2 = this.validateOverlapRestriction( numVariantsOverlapRestriction );

      var supportOfVariantsOverlapRestriction = filterContainer.getComponent( 'supportOfVariantsOverlapItem' )
         .getRestrictionExpression();

      projectOverlapConfig.restriction3 = this.validateOverlapRestriction( supportOfVariantsOverlapRestriction );

      // this should be hidden so it will return an empty phenotype filter
      projectOverlapConfig.phenotypeRestriction = filterContainer.getComponent( 'phenRestriction' )
         .getRestrictionExpression();

      projectOverlapConfig.invert = filterContainer.getComponent( 'invertCheckbox' ).getValue();

      return projectOverlapConfig;
   },

   setRestrictionExpression : function(config) {
      var filterContainer = this.getComponent( 'filterContainer' );

      filterContainer.getComponent( 'overlapItem' ).setRestrictionExpression( config.restriction1 );

      filterContainer.getComponent( 'numVariantsOverlapItem' ).setRestrictionExpression( config.restriction2 );

      filterContainer.getComponent( 'supportOfVariantsOverlapItem' ).setRestrictionExpression( config.restriction3 );

      filterContainer.getComponent( 'phenRestriction' ).setRestrictionExpression( config.phenotypeRestriction );

      filterContainer.getComponent( 'invertCheckbox' ).setValue( config.invert );
   },

   validateOverlapRestriction : function(restriction) {

      // the widget defaults to a SetRestriction if nothing is set, so return SimpleRestriction(what dwr expects) if it
      // is not set
      if ( restriction.operator == null || restriction.property == null ) {

         var simpleRestriction = new SimpleRestriction();
         simpleRestriction.property = null;
         simpleRestriction.operator = null;
         var value = new NumericValue();
         value.value = null;
         simpleRestriction.value = value;
         return simpleRestriction;

      }

      return restriction;
   },

   getNewOverlapItemFunction : function(remoteFunction, storeRef, id, hidden) {

      if ( hidden == undefined || hidden == null ) {

         hidden = false;
      }

      var filterTypeItem = this.getProjectOverlapFilterItemType();

      var getNewOverlapItem = function() {

         return Ext.create( filterTypeItem, {
            propertyStore : storeRef,
            suggestValuesRemoteFunction : remoteFunction,
            itemId : id,
            hidden : hidden
         } );

      };

      return getNewOverlapItem;
   },

   initComponent : function() {
      this.callParent();

      var filterContainer = this.getComponent( "filterContainer" );

      var getNewOverlapItem = this.getNewOverlapItemFunction( VariantService.suggestPropertiesForProjectOverlap, this
         .getPropertyStore(), 'overlapItem' );

      var overlapItem = getNewOverlapItem();

      var getNewNumVariantsOverlapItem = this.getNewOverlapItemFunction(
         VariantService.suggestPropertiesForNumberOfVariantsInProjectOverlap, this.getPropertyStore2(),
         'numVariantsOverlapItem' );

      var numVariantsOverlapItem = getNewNumVariantsOverlapItem();

      var getNewVariantSupportOverlapItem = this.getNewOverlapItemFunction(
         VariantService.suggestPropertiesForSupportOfVariantsInProjectOverlap, this.getPropertyStore3(),
         'supportOfVariantsOverlapItem' );

      var supportOfVariantsOverlapItem = getNewVariantSupportOverlapItem();

      filterContainer.insert( 0, {
         xtype : "checkbox",
         itemId : "invertCheckbox"
      } );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Invert Filter: '
      } );

      // keeping this here but hidden so that the dwr object gets constructed properly(with an empty
      // phenotypeRestriction
      filterContainer.insert( 0, {
         xtype : 'filter_phenotype_property',
         itemId : 'phenRestriction',
         hidden : true
      } );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Phenotype Association of target project variants restriction: ',
         hidden : true
      } );

      supportOfVariantsOverlapItem
      filterContainer.insert( 0, supportOfVariantsOverlapItem );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap study support: '
      } );

      filterContainer.insert( 0, numVariantsOverlapItem );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap variant support: '
      } );

      filterContainer.insert( 0, overlapItem );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap size: '
      } );

      this.updateSpecialProjectValue();

   },

   updateSpecialProjectValue : function() {

      var ref = this;

      ProjectService.getDgvProject( {

         callback : function(pvo) {

            ref.specialProjectValueObject = pvo;

            var filterContainer = ref.getComponent( "filterContainer" );

            filterContainer.getComponent( 'phenRestriction' ).updateProjectIds( [ pvo.id ] );
         }
      } );

   }

} );
