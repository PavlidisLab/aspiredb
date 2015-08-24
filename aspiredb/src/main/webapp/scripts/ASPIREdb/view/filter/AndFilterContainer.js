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
 * Conjunction "AND" of expressions.
 */
Ext.define( 'ASPIREdb.view.filter.AndFilterContainer', {
   extend : 'Ext.Container',
   alias : 'widget.filter_and',
   layout : {
      type : 'vbox',
      defaultMargins : {
         // top : 5,
         right : 5,
         left : 5,
         bottom : 2
      }
   },
   config : {
      propertyStore : null,
      suggestValuesRemoteFunction : null,
      filterItemType : null,
   },
   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },
   items : [ {
      xtype : 'container',
      itemId : 'filterContainer',
      layout : {
         type : 'vbox',
      },
      getRestrictionExpression : function() {
         var conjunction = new Conjunction();
         conjunction.restrictions = [];
         this.items.each( function(item, index, length) {

            var itemRestriction = item.getRestrictionExpression();

            if ( FilterUtil.isSimpleRestriction( itemRestriction ) ) {
               if ( FilterUtil.validateSimpleRestriction( itemRestriction ) ) {
                  conjunction.restrictions.push( itemRestriction );
               }

            } else if ( itemRestriction instanceof Disjunction ) {

               var nonEmptyDisjunction = new Disjunction();

               var nonEmptyRestrictionsArray = [];

               if ( itemRestriction.restrictions ) {

                  for (var i = 0; i < itemRestriction.restrictions.length; i++) {

                     var disjunctedRestriction = itemRestriction.restrictions[i];

                     if ( FilterUtil.isSimpleRestriction( disjunctedRestriction ) ) {
                        if ( FilterUtil.validateSimpleRestriction( disjunctedRestriction ) ) {
                           nonEmptyRestrictionsArray.push( disjunctedRestriction );
                        }

                     }

                  }

               }

               else {
                  // to help flush out any bugs
                  alert( "multi nested disjunction andfilterconatiner" );

               }

               if ( nonEmptyRestrictionsArray.length > 0 ) {

                  nonEmptyDisjunction.restrictions = nonEmptyRestrictionsArray;

                  conjunction.restrictions.push( nonEmptyDisjunction );

               }

            } else {
               // to help flush out any bugs
               alert( "Unsupported Restriction andfiltercontainer" );

            }
         } );
         return conjunction;
      }
   }, {
      xtype : 'button',
      itemId : 'addButton',
      text : 'AND',
   } ],

   getRestrictionExpression : function() {
      var filterContainer = this.getComponent( 'filterContainer' );
      return filterContainer.getRestrictionExpression();
   },

   setRestrictionExpression : function(restriction) {
      var filterContainer = this.getComponent( 'filterContainer' );

      var addMultiItemToContainer = this.getAddMultiItemToContainerFunction( filterContainer );

      var getNewItem = this.getNewItemFunction();

      var filterItemType = this.getFilterItemType();

      if ( filterItemType == 'ASPIREdb.view.filter.PhenotypeFilter' ) {

         filterContainer.removeAll();
         for (var i = 0; i < restriction.restrictions.length; i++) {

            addMultiItemToContainer( restriction.restrictions[i], null, getNewItem );

         }
      } else if ( filterItemType == 'ASPIREdb.view.filter.VariantFilter' ) {

         filterContainer.removeAll();
         for (var i = 0; i < restriction.restrictions.length; i++) {

            addMultiItemToContainer( restriction.restrictions[i], null, getNewItem );

         }
      } else if ( filterItemType == 'ASPIREdb.view.filter.OrFilterContainer'
         || filterItemType == 'ASPIREdb.view.filter.OrPhenotypeFilterContainer'
         || filterItemType == 'ASPIREdb.view.filter.OrVariantFilterContainer'
         || filterItemType == 'ASPIREdb.view.filter.PropertyFilter'
         || filterItemType == 'ASPIREdb.view.filter.ProjectOverlapPropertyFilter' ) {
         filterContainer.removeAll();

         if ( restriction.restrictions ) {

            FilterUtil.traverseRidiculousObjectQueryGraphAndDoSomething( restriction.restrictions, restriction,
               addMultiItemToContainer, getNewItem );

         } else {

            var item = this.getNewItem();

            item.setSimpleRestrictionExpression( restriction );

            filterContainer.add( item );

         }

      }

   },

   getAddMultiItemToContainerFunction : function(filterContainer) {

      var addMultiItemToContainer = function(restriction, outerRestriction, getNewItem) {

         if ( !(restriction instanceof VariantTypeRestriction) ) {

            if ( outerRestriction instanceof Disjunction ) {

               var item = getNewItem();

               item.setRestrictionExpression( outerRestriction );

               filterContainer.add( item );

            } else {

               var item = getNewItem();

               item.setRestrictionExpression( restriction );

               filterContainer.add( item );

            }

         }

      };

      return addMultiItemToContainer;

   },

   getNewItemFunction : function() {

      var filterTypeItem = this.getFilterItemType();
      var propertyStore = this.getPropertyStore();
      var suggestValuesRemoteFunction = this.getSuggestValuesRemoteFunction();

      var getNewItem = function() {

         return Ext.create( filterTypeItem, {
            propertyStore : propertyStore,
            suggestValuesRemoteFunction : suggestValuesRemoteFunction,
         } );

      };

      return getNewItem;
   },

   initComponent : function() {
      this.callParent();

      var me = this;
      var filterContainer = this.getComponent( "filterContainer" );

      var getNewItem = this.getNewItemFunction();

      var item = getNewItem();
      // Add first item.
      filterContainer.insert( 0, item );

      // Adds the 'OR' text after each variant filter property
      me.down( "#filterContainer" ).on( 'add', function(ref, component, index, opts) {
         if ( index == 0 ) {
            return;
         }
         var filterProperty = ref.items.items[index - 1];
         if ( !(Ext.getClassName( filterProperty ) === "ASPIREdb.view.filter.PropertyFilter") ) {
            return;
         }
         filterProperty.setOperationLabel( 'AND' );
      } );

      // Attach button listener
      me.getComponent( "addButton" ).on( 'click', function(button, event) {
         filterContainer.add( getNewItem() );
         filterContainer.doLayout();
      } );
   }

} );
