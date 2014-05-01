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

      /**
       * PhenotypeService.populateDescendantPhenotypes( ontologyPrefix + uri, function(pvos){
       * 
       * var phenotypeGeneMap= pvos; var map = new Ext.util.HashMap(); for(var key in pvos) { var pheneValueArray =
       * pvos[key];
       * 
       * if (pheneValueArray.length > 0){ for ( var k = 0; k < pheneValueArray.length; k++) { map.add(key,
       * pheneValueArray[k]); console.log('nerucarta returned decendandt phenotype'+key+ " , "+ pheneValueArray[k]); } } }
       * ref.populateNeurocartaGrid(map,uri); grid.setLoading(false);
       * 
       * 
       * });
       */

   },

   populateGrid : function(gvos, uri) {

      var grid = ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' );

      var data = [];
      for ( var key in gvos) {
         console.log( 'gene value objects returned from neurocarta' + gvos[key] );
         var vo = gvos[key];
         var pname = key.split( '":"' )

         var linkToGemma = "";

         if ( vo.geneBioType == "protein_coding" ) {
            linkToGemma = ASPIREdb.GemmaURLUtils.makeGeneUrl( vo.symbol );
         }

         var row = [ vo.symbol, vo.geneBioType, vo.name, pname[1], linkToGemma ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );

      grid.enableToolbar( gvos, uri );

   },

   populateNeurocartaGrid : function(map, uri) {

      var grid = ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' );

      var data = [];
      var vos = [];

      map.each( function(key, value, length) {
         console.log( key, value, length );
         var linkToGemma = "";

         if ( value.geneBioType == "protein_coding" ) {
            linkToGemma = ASPIREdb.GemmaURLUtils.makeGeneUrl( value.symbol );
         }
         vos.push( value );

         var row = [ value.symbol, value.geneBioType, value.name, key, linkToGemma ];
         data.push( row );
      } );

      grid.store.loadData( data );
      grid.setLoading( false );

      grid.enableToolbar( vos, uri );

   },

   clearGridAndMask : function() {
      ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' ).store.removeAll();
      ASPIREdb.view.NeurocartaGeneWindow.getComponent( 'neurocartaGeneGrid' ).setLoading( true );
   }

} );