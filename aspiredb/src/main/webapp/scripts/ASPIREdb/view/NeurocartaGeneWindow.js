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

Ext.require( [ 'Ext.Window', 'ASPIREdb.view.NeurocartaGeneGrid', 'ASPIREdb.GemmaURLUtils' ] );

/**
 * Window that displays the list of genes associated with a phenotype from Phenocarta.
 */
Ext.define( 'ASPIREdb.view.NeurocartaGeneWindow', {
   extend : 'Ext.Window',
   alias : 'widget.neurocartaGeneWindow',
   singleton : true,
   title : 'Genes associated with phenotype in Phenocarta',
   closable : true,
   closeAction : 'hide',
   constrain : true,
   width : 800,
   height : 500,
   layout : 'fit',
   modal : true,
   bodyStyle : 'padding: 5px;',

   items : [ {
      xtype : 'neurocartaGeneGrid',
      itemId : 'neurocartaGeneGrid'
   } ],

   initComponent : function() {

      this.callParent();

   },

   initGridAndShow : function(uri, name) {

      var ref = this;

      var grid = ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' );

      ref.show();
      ref.setTitle( "Genes associated with " + name + " in Phenocarta" );

      grid.setLoading( true );

      var ontologyPrefix = "http://purl.obolibrary.org/obo/";

      GeneService.findGenesAndURIsWithNeurocartaPhenotype( ontologyPrefix + uri, function(gvos) {

         ref.populateGrid( gvos, uri );
         grid.setLoading( false );

      } );

   },

   populateGrid : function(gvos, uri) {

      var grid = ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' );

      var data = [];
      for ( var key in gvos) {
         var vo = gvos[key];
         var pname = key.split( '":"' )

         var linkToGemma = "";

         if ( vo.geneBioType == "protein_coding" ) {
            linkToGemma = ASPIREdb.GemmaURLUtils.makeGeneUrl( vo.symbol );
         }

         // see ASPIREdb.store.GeneStore
         var row = [ vo.symbol, vo.geneBioType, vo.name, null, pname[1], linkToGemma ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );

      grid.enableToolbar( gvos, uri );

   },

   clearGridAndMask : function() {
      ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' ).store.removeAll();
      ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' ).setLoading( true );
   }

} );