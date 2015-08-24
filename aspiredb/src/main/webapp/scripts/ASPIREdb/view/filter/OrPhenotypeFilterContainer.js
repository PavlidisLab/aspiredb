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
Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.PropertyFilter' ] );

/**
 * This could be combined with OrFilterContainer with a little re-jiggering making a new file for now because it is a
 * little simpler and because of time constraints
 */
Ext.define( 'ASPIREdb.view.filter.OrPhenotypeFilterContainer', {
   extend : 'ASPIREdb.view.filter.FilterPanel',
   alias : 'widget.filter_or_pheno',
   closable : true,
   title : 'AND',
   width : 910,

   /*
    * border: 1, style: { border: "1px solid lightgray" },
    */
   getRestrictionExpression : function() {
      var filterContainer = this.getComponent( 'filterContainer' );
      return filterContainer.getRestrictionExpression();
   },

   setRestrictionExpression : function(restriction) {
      var filterContainer = this.getComponent( 'filterContainer' );

      filterContainer.removeAll();

      filterContainer.setRestrictionExpression( restriction );

   },

   createFilterContainer : function() {
      var me = this;
      return {
         xtype : 'container',
         itemId : 'filterContainer',
         layout : {
            type : 'vbox',
            defaultMargins : {
               // top : 5,
               right : 5,
               left : 5,
               bottom : 5
            }
         },
         getRestrictionExpression : function() {
            var disjunction = new Disjunction();
            disjunction.restrictions = [];
            this.items.each( function(item, index, length) {
               disjunction.restrictions.push( item.getRestrictionExpression() );
            } );
            return disjunction;
         },

         setRestrictionExpression : function(restriction) {

            var filterContainer = me.getComponent( "filterContainer" );

            if ( restriction instanceof Disjunction ) {

               for (var i = 0; i < restriction.restrictions.length; i++) {

                  var filter = Ext.create( 'ASPIREdb.view.filter.PhenotypeFilter' );

                  filter.setRestrictionExpression( restriction.restrictions[i] );

                  filterContainer.add( filter );

               }

            }

         },

         items : [ Ext.create( 'ASPIREdb.view.filter.PhenotypeFilter' ) ]
      }
   },

   initComponent : function() {
      var me = this;
      this.items = [ {
         xtype : 'container',
         layout : {
            type : 'hbox',
            defaultMargins : {
               // top : 5,
               right : 10,
            // left : 5,
            // bottom : 5
            }
         },
         items : [ {
            xtype : 'tbspacer',
            flex : 1,
         }, {
            xtype : 'button',
            itemId : 'addButton',
            text : 'OR'
         } ]
      }, me.createFilterContainer() ];

      this.callParent();

      // Adds the 'OR' text after each variant filter property
      me.down( "#filterContainer" ).on( 'add', function(ref, component, index, opts) {
         if ( index == 0 ) {
            return;
         }
         var filterProperty = ref.items.items[index - 1];
         filterProperty.setOperationLabel( 'OR' );
      } );

      me.down( "#addButton" ).on( 'click', function(button, event) {
         var filterContainer = me.getComponent( "filterContainer" );
         filterContainer.add( Ext.create( 'ASPIREdb.view.filter.PhenotypeFilter' ) );
         filterContainer.doLayout();
      } );

   }
} );
