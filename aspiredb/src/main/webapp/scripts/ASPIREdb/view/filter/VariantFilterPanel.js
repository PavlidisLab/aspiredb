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
Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.AndFilterContainer',
              'ASPIREdb.view.filter.OrVariantFilterContainer', 'ASPIREdb.view.filter.FilterPanel' ] );

/**
 * Deals with conjunctions and disjunctions of restriction expressions.
 */
Ext.define( 'ASPIREdb.view.filter.VariantFilterPanel', {
   extend : 'ASPIREdb.view.filter.FilterPanel',
   alias : 'widget.filter_variant',
   title : 'Variant Filter',
   bodyStyle : 'background: #FFFFC2;',
   items : [ {
      xtype : 'filter_and',
      title : 'Variant Location:',
      itemId : 'variantFilterContainer',
      filterItemType : 'ASPIREdb.view.filter.OrVariantFilterContainer',
      suggestValuesRemoteFunction : VariantService.suggestValues,
      propertyStore : {
         // autoLoad: true,
         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestVariantLocationProperties,
            // dwrParam : ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0],
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }

         },
      }
   },

   ],

   getFilterConfig : function() {
      var config = new VariantFilterConfig();
      var conjunction = new Conjunction();
      conjunction.restrictions = [];

      var locationConjunction = new Conjunction();
      locationConjunction.restrictions = [];

      var variantFilterContainer = this.getComponent( 'variantFilterContainer' );

      conjunction.restrictions.push( variantFilterContainer.getRestrictionExpression() );

      var disjunction = new Disjunction();
      disjunction.restrictions = [];
      disjunction.restrictions.push( variantFilterContainer.getRestrictionExpression() );

      conjunction.restrictions.push( disjunction );

      config.restriction = conjunction;
      return config;
   },

   setFilterConfig : function(config) {

      var variantFilterContainer = this.getComponent( 'variantFilterContainer' );

      if ( config.restriction.restrictions ) {

         restrictions = config.restriction.restrictions;

         for (var i = 0; i < restrictions.length; i++) {

            variantFilterContainer.setRestrictionExpression( restrictions[i] );

         }

      }

   },

   separateVariantDisjunctions : function(disjunctions, variantType) {

      var separatedDisjunctions = [];

      var addVariantRestrictionToDisjunctions = function(innerRestriction, outerRestriction, somethingElseToDo) {

         if ( innerRestriction.type && innerRestriction.type == variantType ) {

            separatedDisjunctions.push( outerRestriction );

         }

      };

      var somethingElseToDoFunction = function() {
      };

      FilterUtil.traverseRidiculousObjectQueryGraphAndDoSomething( disjunctions, null,
         addVariantRestrictionToDisjunctions, somethingElseToDoFunction );

      return separatedDisjunctions;

   },

   shouldExpandVariantTypeBox : function(restrictions) {
      if ( restrictions.restrictions.length > 0 && restrictions.restrictions[0].restrictions.length > 1 ) {
         return true;
      }

      return false;
   },

   initComponent : function() {
      this.callParent();
   }

} );
