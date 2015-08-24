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
 * Disjunction "OR" of expressions.
 */
Ext
   .define(
      'ASPIREdb.view.filter.OrFilterContainer',
      {
         extend : 'ASPIREdb.view.filter.FilterPanel',
         alias : 'widget.filter_or',
         closable : true,
         title : 'OR Filter',
         width : 910,
         layout : {
            type : 'vbox'
         },
         config : {
            propertyStore : null,
            suggestValuesRemoteFunction : null
         },
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

         initComponent : function() {
            var me = this;
            this.items = [
                          {
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
                             },
                             getRestrictionExpression : function() {
                                var disjunction = new Disjunction();
                                disjunction.restrictions = [];
                                this.items
                                   .each( function(item, index, length) {
                                      disjunction.restrictions.push( item.getRestrictionExpression() );
                                      if ( disjunction.restrictions[index].property !== null
                                         && disjunction.restrictions[index].property.displayName == 'GeneSet' ) {
                                         disjunction.restrictions[index].property = new GeneProperty();
                                         disjunction.restrictions[index].property.displayName = 'Gene';
                                         disjunction.restrictions[index].values = disjunction.restrictions[index].values[index].object;
                                      }

                                   } );

                                return disjunction;
                             },

                             setRestrictionExpression : function(restriction) {

                                var filterContainer = me.getComponent( "filterContainer" );

                                if ( restriction instanceof Disjunction ) {

                                   for (var i = 0; i < restriction.restrictions.length; i++) {

                                      var filter = Ext.create( 'ASPIREdb.view.filter.PropertyFilter', {
                                         propertyStore : me.getPropertyStore(),
                                         suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
                                      } );

                                      filter.setRestrictionExpression( restriction.restrictions[i] );

                                      filterContainer.add( filter );

                                   }

                                } else {
                                   // this else block probably never gets called
                                   var filter = Ext.create( 'ASPIREdb.view.filter.PropertyFilter', {
                                      propertyStore : me.getPropertyStore(),
                                      suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
                                   } );

                                   filter.setRestrictionExpression( restriction );

                                   filterContainer.add( filter );

                                }

                                filterContainer.doLayout();

                             },

                             items : [ {
                                xtype : 'panel',
                                border : 0,
                                layout : {
                                   type : 'hbox',
                                   defaultMargins : {
                                      // top : 5,
                                      right : 5,
                                   // left : 5,
                                   // bottom : 5
                                   }
                                },
                                items : [ {
                                   xtype : 'filter_property',
                                   itemId : 'filterProperty',
                                   propertyStore : me.getPropertyStore(),
                                   suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
                                }, {
                                   xtype : 'button',
                                   itemId : 'addButton',
                                   // padding : '5 5 5 5',
                                   // icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
                                   text : 'OR'
                                } ]
                             } ]

                          }, ];

            this.callParent();

            me.down( "#addButton" ).on( 'click', function(button, event) {
               var filterContainer = me.getComponent( "filterContainer" );
               filterContainer.add( Ext.create( 'ASPIREdb.view.filter.PropertyFilter', {
                  propertyStore : me.getPropertyStore(),
                  suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
               } ) );
               filterContainer.doLayout();
            } );
         }
      } );
