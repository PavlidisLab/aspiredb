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

Ext.require( [ 'Ext.Window', 'ASPIREdb.view.GeneHitsByVariantGrid', 'ASPIREdb.GemmaURLUtils' ] );

/**
 * Gene Variant Window contains a Gene Variant Grid panel.
 */
Ext.define( 'ASPIREdb.view.GeneHitsByVariantWindow', {
   extend : 'Ext.Window',
   alias : 'widget.geneHitsByVariantWindow',
   singleton : true,
   title : 'Gene Hits By Variant',
   header: {
      items: [{
          xtype: 'image',       
          style:'right: auto; left: 0px; top: 6px;',
          src: 'scripts/ASPIREdb/resources/images/qmark.png',          
          listeners: {
             afterrender: function(c) {
                 Ext.create('Ext.tip.ToolTip', {
                     target: c.getEl(),
                     html: 'The genes that overlap with the selected variants. Protein coding genes are shown by default (uncheck the checkbox to show all). Use "Save as gene set" to create a set for later use.'
                 });
             }
         }
      }],
      layout: 'fit'
  },      
   closable : true,
   closeAction : 'hide',
   width : 800,
   height : 500,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   items : [ {
      xtype : 'geneHitsByVariantGrid',
      itemId : 'geneHitsByVariantGrid'
   } ],

   initComponent : function() {
      var ref = this;
      this.callParent();

   },

   initGridAndShow : function(ids) {

      var ref = this;

      var grid = ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid' );

      ref.show();
      grid.setLoading( true );

      GeneService.getGenesPerVariant( ids, {
         callback : function(variantGenes) {
            ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid' ).setLodedvariantvalueObjects(
               variantGenes );

            ASPIREdb.view.GeneHitsByVariantWindow.populateGrid( variantGenes );

            // by default, show just protein-coding genes
            var grid = ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid' );
            grid.store.filter( 'geneBioType', 'protein_coding' );
         }
      } );

   },

   countVariantsPerGene : function(variantGenes) {
      var variantCounts = {};
      var geneVos = [];

      for ( var variantId in variantGenes) {

         var genes = variantGenes[variantId];
         for (var i = 0; i < genes.length; i++) {
            var vo = genes[i];

            if ( variantCounts[vo.symbol] != undefined ) {
               variantCounts[vo.symbol]++;
               continue;
            }

            variantCounts[vo.symbol] = 1;
            geneVos.push( vo );

         }

      }
      return {
         'geneVos' : geneVos,
         'variantCounts' : variantCounts
      }
   },

   // VariantValueObject
   populateGrid : function(variantGenes) {

      // in case there's too many rows to display
      var ROW_LIMIT = 50000;

      var grid = ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid' );

      var geneInfo = this.countVariantsPerGene( variantGenes );
      var geneVos = geneInfo.geneVos;
      var variantCounts = geneInfo.variantCounts;

      var data = [];
      var geneSymbols = [];

      for (var i = 0; i < geneVos.length; i++) {
         var vo = geneVos[i];
         var phenName = "";
         var linkToGemma = ASPIREdb.GemmaURLUtils.makeGeneUrl( vo.symbol );
         var row = [ vo.symbol, vo.geneBioType, vo.name, variantCounts[vo.symbol], phenName, linkToGemma ];
         geneSymbols.push( vo.symbol );
         data.push( row );
      }

      // there's a limit to how much the browser can handle
      if ( data.length >= ROW_LIMIT ) {
         var msg = 'Only ' + ROW_LIMIT + 'out of ' + data.length + ' rows are displayed';
         Ext.Msg.alert( 'Too many rows to display', msg );
         console.log( msg );
         grid.store.loadData( data.slice( 0, ROW_LIMIT ) );
      } else {
         console.log( 'Found ' + data.length + ' genes that overlapped' );
         grid.store.loadData( data );
      }

      grid.setLoading( false );
      grid.enableToolbar( geneSymbols );

   },

   clearGridAndMask : function() {
      ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid' ).store.removeAll();
      ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid' ).setLoading( true );
   }

} );