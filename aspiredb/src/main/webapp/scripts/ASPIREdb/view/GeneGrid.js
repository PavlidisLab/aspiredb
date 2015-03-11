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

Ext.require( [ 'Ext.grid.*', 'ASPIREdb.store.GeneStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.data.*', 'Ext.util.*',
              'Ext.state.*', 'Ext.form.*', 'ASPIREdb.GeneSuggestionStore', 'ASPIREdb.model.GeneProperty',
              'ASPIREdb.model.PropertyValue' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

/**
 * Create Gene Grid
 */
Ext.define( 'ASPIREdb.view.GeneGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.geneGrid',
   emptyText : 'No genes found',
   id : 'geneGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.GeneStore' ),

   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedGeneSetNames : [],
      // collection of selected gene value objects
      selectedGene : [],
      gvos : [],
      selectedGeneSet : [],
      suggestionContext : null,
      selectedProperty : new GeneProperty(),
      

   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'geneGridToolbar',
      dock : 'top'
   } ],

   columns : [ {
      header : 'Gene Symbol',
      dataIndex : 'symbol',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Gene Name',
      dataIndex : 'name',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : true
      }
   } ],

   // plugins : [ rowEditing ], // users should not be able to edit gene names!
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeGene' ).setDisabled( !records.length );
         this.selectedGene = this.getSelectionModel().getSelection();

      }
   },

   initComponent : function() {
      this.callParent();
      var me = this;
      me.enableToolbar();

      ASPIREdb.EVENT_BUS.on( 'geneSet_selected', this.geneSetSelectHandler, this );

   },

   /**
    * Store the gene value object when selected
    * 
    * @param GeneSetValueObject
    *           selGeneSet
    */
   geneSetSelectHandler : function(selGeneSet) {
      this.selectedGeneSet = selGeneSet;
   },


   populateMultiComboItemFromImportList : function( vos ) {
      this.populateGridFromImportList(vos);
   },
   
   addGenesToGrid : function( gvos, grid ) {
      var data = [];
      
      for(var i = 0; i < gvos.length; i++) {
         var gvo = gvos[i];
         var row = [ gvo.symbol, gvo.geneBioType, gvo.name, '' ];
         data.push( row );
      }

      grid.store.removeAll();
      grid.store.add( data );
      grid.getView().refresh( true );
      grid.setLoading( false );
      grid.down( '#geneName' ).setValue( '' );

      ASPIREdb.EVENT_BUS.fireEvent( 'gene_added', data );
      // update the gene set grid size
      var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
      var geneSetGrid = panel.down( '#geneSetGrid' );

      var selection = geneSetGrid.getView().getSelectionModel().getSelection()[0];
      if ( selection ) {
         selection.set( 'geneSetSize', data.length );
      }   
   },
   
   populateGridFromImportList : function( vos ) {
      
      var ref = this;
      
      if ( ref.selectedGeneSet.length === 0 ) {
         Ext.Msg.alert( 'Error', 'select the Gene Set Name to add Genes ' );
         return;
      }
      
      var geneSymbols = [];
      for( var i = 0; i < vos.length; i++ ) {
         geneSymbols.push(vos[i].symbol);
      }
      
      var geneSetName = ref.selectedGeneSet[0].data.geneSetName;
      UserGeneSetService.addGenesToGeneSet( geneSetName, geneSymbols, {
         callback : function(gvos) {
            
            if ( gvos.length === 0 ) {
               console.log('No genes were added to ' + geneSetName);
               return;
            }
            
            ref.addGenesToGrid(gvos, ref);
            
         }, 
         errorHandler : function(er, exception) {
            Ext.Msg.alert( "Gene Grid Error", er + "\n" + exception.stack );
            console.log( exception.stack );
         }
      });
   },
   
   /**
    * Enable the tool bar in Gene grid
    * 
    */
   enableToolbar : function() {

      this.getDockedComponent( 'geneGridToolbar' ).removeAll();

      this.getDockedComponent( 'geneGridToolbar' ).add(
         {
            xtype : 'combo',
            id : 'geneName',
            emptyText : 'Type gene symbol',
            width : 200,
            displayField : 'displayName',
            triggerAction : 'query',
            minChars : 0,
            matchFieldWidth : false,
            hideTrigger : true,
            autoSelect : true,
            forceSelection : true,
            enableKeyEvents : false,
            store : Ext.create( 'ASPIREdb.GeneSuggestionStore', {
               remoteFunction : VariantService.suggestGeneValues,
               remoteSort : true
            } ),
            tpl : Ext.create( 'Ext.XTemplate', '<tpl for=".">',
               '<div class="x-boundlist-item"><b>{displayName}:</b> {name}</div>', '</tpl>' ),
            // template for the content inside text field
            displayTpl : Ext.create( 'Ext.XTemplate', '<tpl for=".">', '{displayName}: {name}', '</tpl>' ),
            listConfig : {
               loadingText : 'Searching...',
               emptyText : 'No results found.',

            },
            listeners : {
               select : {
                  fn : function(obj, records) {
                     // ASPIREdb.EVENT_BUS.fireEvent('query_update');

                  },
                  scope : this,
               }
            },

         } );

      this.getDockedComponent( 'geneGridToolbar' ).add( '-' );

      var ref = this;

      var toolbar = this.getDockedComponent( 'geneGridToolbar' );
      toolbar.add( {
         xtype : 'button',
         itemId : 'addGene',
         text : '',
         tooltip : 'Add genes to selected gene set',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
         handler : function() {

            var genesymbol = ref.down( '#geneName' ).getValue();
            // console.log( 'added genes name : ' + genesymbol );
            var geneSetName = ref.selectedGeneSet[0].data.geneSetName;
            var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
            var grid = panel.down( '#geneGrid' );

            UserGeneSetService.isGeneInGeneSet( geneSetName, genesymbol, {
               callback : function(gvoSta) {
                  if ( gvoSta ) {
                     Ext.Msg.alert( 'Gene Set', 'Gene already exist in gene set' );
                     grid.down( '#geneName' ).setValue( '' );
                  } else if ( ref.selectedGeneSet[0] !== null ) {
                     UserGeneSetService.addGenesToGeneSet( geneSetName, [ genesymbol ], {
                        callback : function(gvos) {

                           ref.addGenesToGrid(gvos, grid);

                        },
                        errorHandler : function(er, exception) {
                           Ext.Msg.alert( "Gene Grid Error", er + "\n" + exception.stack );
                           console.log( exception.stack );
                        }
                     } );
                  } else
                     Ext.Msg.alert( 'Error', 'select the Gene Set Name to add Genes ' );

               }
            } );

         }
      } );

      toolbar.add( {
         xtype : 'button',
         itemId : 'removeGene',
         text : '',
         tooltip : 'Remove the selected gene',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
         handler : function() {
            var geneSymbol = ref.selectedGene[0].data.symbol;
            UserGeneSetService.deleteGene( ref.selectedGeneSet[0].data.geneSetName, geneSymbol, {
               callback : function() {

                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
                  var geneGrid = panel.down( '#geneGrid' );

                  var selection = geneGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     geneGrid.store.remove( selection );
                  }

                  // resize the gene size in gene set grid
                  // update the gene set grid size
                  var geneSetGrid = panel.down( '#geneSetGrid' );

                  selection = geneSetGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     var oldSize = selection.data.geneSetSize;
                     selection.set( 'geneSetSize', parseInt( oldSize ) - 1 );
                  }
               }

            } );
         }
      } );
      
      toolbar.add( {
         xtype : 'button',
         itemId : 'enterListButton',
         text : '',
         tooltip : 'Enter list ...',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/page_upload.png',
         handler : function() {
            var prop = new GeneProperty();
            ASPIREdb.view.filter.TextImportWindow.setPropertyFilterAndShow( ref );
         }
      } );
      
   }
} );
