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

/**
 * Filtering phenotypes.
 */
Ext.define( 'ASPIREdb.view.filter.PhenotypeFilter', {
   extend : 'Ext.Container',
   alias : 'widget.filter_phenotype_property',

   layout : {
      type : 'hbox',
      defaultMargins : {
         right : 2,
      }
   },

   config : {
      projectIds : ASPIREdb.ActiveProjectSettings.getActiveProjectIds(),
   },

   // First method that gets called upon class instantiation
   constructor : function(config) {
      // Initializes config properties
      this.callParent( arguments );
   },

   updateProjectIds : function(newIds) {
      this.projectIds = newIds;
      this.getComponent( 'nameCombo' ).store.projectIds = newIds;
   },

   /**
    * @private
    * @override
    */
   initComponent : function() {

      this.items = [ {
         xtype : 'combo',
         itemId : 'nameCombo',
         emptyText : 'phenotype',
         width : 400,
         matchFieldWidth : false,
         triggerAction : 'query',
         // autoSelect: true,
         hideTrigger : true,
         displayField : 'displayName',
         store : Ext.create( 'ASPIREdb.PhenotypeSuggestionStore', {
            remoteFunction : PhenotypeService.suggestPhenotypes,
            projectIds : this.projectIds,
         } ),
         listConfig : {
            loadingText : 'Searching...',
            emptyText : 'No results.'
         },
         listeners : {
            select : {
               fn : function(obj, records) {
                  var record = records[0];
                  var valueCombo = this.getComponent( 'valueCombo' );
                  valueCombo.clearValue();
                  valueCombo.lastQuery = null;
                  valueCombo.getStore().setProperty( record.raw );
                  ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
               },
               scope : this
            },
         }
      }, {
         xtype : 'combo',
         itemId : 'valueCombo',
         emptyText : 'value',
         displayField : 'displayValue',
         triggerAction : 'query',
         minChars : 0,
         matchFieldWidth : false,
         hideTrigger : true,
         autoSelect : true,
         enableKeyEvents : true,
         store : Ext.create( 'ASPIREdb.ValueSuggestionStore', {
            remoteFunction : PhenotypeService.suggestPhenotypeValues
         } ),
         listeners : {
            select : {
               fn : function(obj, records) {
                  ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
               },
               scope : this,
            }
         },
         listConfig : {
            loadingText : 'Searching...',
            emptyText : 'No results.'
         }
      }, {
         xtype : 'button',
         itemId : 'removeButton',
         text : 'X'
      }, {
         xtype : 'label',
         itemId : 'operationLabel',
         text : ''
      } ];

      this.callParent();

      this.getComponent( "removeButton" ).on( 'click', function(button, event) {
         var item = button.ownerCt;
         var filterContainer = item.ownerCt;
         filterContainer.remove( item );
         filterContainer.doLayout();

         if ( filterContainer.ownerCt.closeEmptyFilter )
            filterContainer.ownerCt.closeEmptyFilter();

         ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
      } );

   },

   getRestrictionExpression : function() {
      var nameCombo = this.getComponent( "nameCombo" );
      var valueCombo = this.getComponent( "valueCombo" );

      var phenotypeRestriction = new PhenotypeRestriction();
      phenotypeRestriction.name = nameCombo.getValue();
      phenotypeRestriction.value = valueCombo.getValue();
      return phenotypeRestriction;
   },

   setRestrictionExpression : function(phenotypeRestriction) {
      var nameCombo = this.getComponent( "nameCombo" );
      var valueCombo = this.getComponent( "valueCombo" );
      nameCombo.setValue( phenotypeRestriction.name );
      valueCombo.setValue( phenotypeRestriction.value );
   },

   setOperationLabel : function(operation) {
      this.down( '#operationLabel' ).setText( operation );
   }

} );