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

Ext.require( [ 'Ext.grid.Panel', 'ASPIREdb.store.GeneStore', 'ASPIREdb.TextDataDownloadWindow',
              'Ext.grid.plugin.BufferedRenderer' ] );

/**
 * Grid that shows which gene overlaps the selected variants
 */
Ext.define( 'ASPIREdb.view.GeneHitsByVariantGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.geneHitsByVariantGrid',
   emptyText : 'No genes found',
   id : 'geneHitsByVariantGrid',
   border : false,
   multiSelect : true,
   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedVariantValueObjects : [],
   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'geneHitsByVariantGridToolbar',
      dock : 'top'
   } ],

   columns : [
              {
                 header : 'Gene Symbol',
                 dataIndex : 'symbol',
                 flex : 1
              },
              {
                 header : 'Type',
                 dataIndex : 'geneBioType',
                 flex : 1
              },
              {
                 header : 'Gene Name',
                 dataIndex : 'name',
                 flex : 1
              },
              {
                 header : 'Variant Count',
                 dataIndex : 'variantCount',
                 flex : 1
              },
              {
                 header : 'Phenotype Name',
                 dataIndex : 'pheneName',
                 hidden : true,
                 disabled : true,
                 flex : 1
              },
              {
                 header : 'View in Gemma',
                 dataIndex : 'linkToGemma',
                 flex : 1,
                 renderer : function(value) {

                    if ( value == '' ) {
                       return;
                    }

                    return '<a href="' + value
                       + '" target="_blank" > <img src="scripts/ASPIREdb/resources/images/gemmaTiny.gif" /> </a>';
                 }
              } ],

   selModel : Ext.create( 'Ext.selection.CheckboxModel', {
      mode : 'MULTI',
   } ),

   store : Ext.create( 'ASPIREdb.store.GeneStore' ),

   // BufferedRenderer configs
   loadMask : true,
   plugins : 'bufferedrenderer',

   initComponent : function() {
      this.callParent();
      var me = this;
   },

   /**
    * Returns GeneValueObjects from the store records
    * 
    * @param records
    *           a collection of records in the store, e.g. this.getSelectionModel().getSelection();
    */
   extractGenes : function(records) {

      var genes = [];

      var geneSymbols = [];
      for (var i = 0; i < records.length; i++) {

         // make a copy
         var gene = JSON.parse( JSON.stringify( records[i].data ) );

         // delete non-gene value object fields, see GeneStore
         delete gene.patientId;
         delete gene.variantCoordinate;
         delete gene.pheneName;

         // avoid adding duplicated genes
         if ( geneSymbols.indexOf( gene.symbol ) == -1 ) {
            genes.push( gene );
            geneSymbols.push( gene.symbol );
         }
      }

      return genes;

   },

   setLodedvariantvalueObjects : function(vvo) {

      this.LoadedVariantValueObjects = vvo;
   },

   enableToolbar : function(vos) {

      if ( vos.length < 1 ) {
         return;
      }

      var geneSymbols = [];

      for (var i = 0; i < vos.length; i++) {
         var vo = vos[i];
         geneSymbols.push( vo.symbol );
      }

      var url = ASPIREdb.GemmaURLUtils.makeViewGeneNetworkInGemmaURL( geneSymbols );

      // This kind of weird technique is being used because the baked in extjs button href config way was not working
      var viewCoexpressionNetworkInGemmaLink = {
         xtype : 'box',
         itemId : 'viewCoexpressionNetworkButton',
         autoEl : {
            tag : 'a',
            href : url,
            target : '_blank',
            cn : 'View Coexpression Network in Gemma'
         }
      };

      // this.getDockedComponent('geneHitsByVariantGridToolbar').remove('viewCoexpressionNetworkButton');
      // this.getDockedComponent('geneHitsByVariantGridToolbar').remove('saveButtonGeneHits');

      // make sure we don't add to any existing items
      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).removeAll();

      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).add( viewCoexpressionNetworkInGemmaLink );

      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).add( '-' );

      
       
      // FIXME, do this more elegantly, just apply a row filter ...
      /*
      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).add( {
         xtype : 'checkbox',
         itemId : 'viewProteinCodingGeneOnlyCheckbox',
         defaultType : 'checkboxfield',
         fieldLabel : 'Protein-coding',
         checked : true,
         uncheckedValue : '',
         scope : this,
         handler : function() {

            if ( this.uncheckedValue == 'unchecked' ) {
               this.store.removeAll( true );
               for (var i = 0; i < this.LoadedVariantValueObjects.length; i++) {

                  var vvo = this.LoadedVariantValueObjects[i];

                  if ( vvo.geneBioType == "protein_coding" ) {
                     this.store.add( vvo );
                  }
                  // else this.store.remove(vvo);

               }

               this.getView().refresh( true );
               this.setLoading( false );
               this.uncheckedValue = '';
            } else {
               for (var i = 0; i < this.LoadedVariantValueObjects.length; i++) {

                  var vvo = this.LoadedVariantValueObjects[i];

                  if ( vvo.geneBioType != "protein_coding" ) {
                     this.store.add( vvo );
                  }

               }

               this.getView().refresh( true );
               this.setLoading( false );
               this.uncheckedValue = 'unchecked';
            }
         }

      } );
*/
      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).add( '-' );


      var ref = this;

      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).add( {
         xtype : 'button',
         id : 'saveButtonGeneHits',
         text : '',
         tooltip : 'Download table contents as text',
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
         handler : function() {
            var colNames = [];
            for (var i = 0; i < ref.columns.length; i++) {
               // omitted columns
               if ( ref.columns[i].text === 'Phenotype Name' ) {
                  continue;
               }
               if ( ref.columns[i].text === 'View in Gemma' ) {
                  continue;
               }
               colNames.push( ref.columns[i].text );
            }
            ASPIREdb.TextDataDownloadWindow.showGenesDownload( ref.getStore().getRange(), colNames );
         }
      } );

      this.getDockedComponent( 'geneHitsByVariantGridToolbar' ).add( {
         xtype : 'button',
         id : 'saveButtonGeneSet',
         text : 'Save to Gene Lists',
         tooltip : 'Save Genes to User gene Set',
         disabled : false,
         handler : function() {
            var grid = this.up( '#geneHitsByVariantGrid' );
            var genes = grid.extractGenes( grid.getSelectionModel().getSelection() );

            if ( genes.length == 0 ) {
               Ext.Msg.alert('Error','No genes were selected');
               return;
            }
            
            ASPIREdb.view.SaveUserGeneSetWindow.initAndShow( genes );
         }
      } );

   }
} );
