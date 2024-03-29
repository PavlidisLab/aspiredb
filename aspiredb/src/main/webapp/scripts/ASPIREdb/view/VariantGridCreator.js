/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 */

Ext.require( [ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel', 'Ext.selection.RowModel',
              'ASPIREdb.view.GeneHitsByVariantWindow', 'ASPIREdb.ActiveProjectSettings' ] );
 
/**
 * Converts invalid numerical values to NaNs so that they can be sorted properly.
 */
Ext.data.Types.ROBUSTFLOAT = {
   convert : function(v, data) {
      return isNaN( parseFloat( v ) ) ? '' : '' + parseFloat( v );
   },
   sortType : function(v) {
      return isNaN( parseFloat( v ) ) ? NaN : parseFloat( v );
   },
   type : 'RobustFloat'
};

/**
 * This grid is created dynamically based on 'suggested properties'( see VariantService.suggestProperties) this dynamic
 * nature is why this grid is constructed this way
 */
Ext.define( 'ASPIREdb.view.VariantGridCreator',
   { 
      /**
       * @memberOf ASPIREdb.view.VariantGridCreator
       */
      extend : 'Ext.util.Observable',
      singleton : true,

      storeFields : [ 'id', 'patientId', 'variantType', 'genomeCoordinates', 'chromosome', 'baseStart', 'baseEnd',
                     'labelIds', 'type', 'copyNumber', 'cnvLength', 'dbSNPID', 'observedBase', 'referenceBase',
                     'indelLength', 'gene' ],

      initComponent : function() {
         this.callParent();

      },

      /**
       * The main function that returns a ExtJS Grid '#variantGrid' from a collection of VariantValueObjects and variant
       * GeneValueObjects.
       * 
       * @public
       * @param {VariantValueObject[]}
       *           vvos
       * @param {string[]}
       *           properties
       */
      createVariantGrid : function(vvos, properties, variantGenes) {

         var fieldData = [];

         var dataIndexes = this.storeFields;

         for (var i = 0; i < dataIndexes.length; i++) {

            if ( dataIndexes[i] == 'baseStart' || dataIndexes[i] == 'baseEnd' || dataIndexes[i] == 'cnvLength'
               || dataIndexes[i] == 'indelLength' ) {
               fieldData.push( {
                  name : dataIndexes[i],
                  type : 'RobustFloat'
               } );
            } else {
               fieldData.push( {
                  name : dataIndexes[i],
                  type : 'auto'
               } );
            }
         }

         var characteristicNames = this.extractCharacteristicNames( vvos ).sort();
         // determine the type by getting a single value
         var firstCharacteristics = null;
         if ( vvos != null && vvos.length > 0 ) {
            firstCharacteristics = vvos[0].characteristics;
         }
         for (var i = 0; i < characteristicNames.length; i++) {
            var characteristic = characteristicNames[i];
            if ( fieldData.indexOf( characteristic ) == -1 ) {

               if ( firstCharacteristics != null && firstCharacteristics[characteristic] != null ) {

                  // type check
                  var val = firstCharacteristics[characteristic].value;
                  var type = null;

                  if ( val == null ) {
                     type = 'auto';
                  } else if ( !isNaN( parseFloat( val ) ) ) {
                     type = 'RobustFloat';
                  } else {
                     'auto'
                  }

                  fieldData.push( {
                     'name' : characteristic,
                     'type' : type
                  } );

               } else {
                  fieldData.push( characteristic );
               }

            }
         }

         var visibleLabels = this.createVisibleLabels( vvos );
         var storeData = this.constructVariantStoreData( vvos, variantGenes, characteristicNames, visibleLabels );

         var store = Ext.create( 'Ext.data.ArrayStore', {
            storeId : 'variantGrid',
            fields : fieldData,
            data : storeData,
            pageSize : 100,

         } );

         // these are required variant properties
         // var columnHeaders = [ 'Patient Id', 'Type', 'Genome Coordinates', 'Copy Number', 'CNV Type', 'CNV Length',
         // 'DB SNP ID', 'Observed Base', 'Reference Base', 'Indel Length', 'Gene' ];
         var columnHeaders = [ 'Patient Id', 'Type', 'Genome Coordinates', 'Chromosome', 'Base Start', 'Base End',
                              'Labels', 'Copy Number', 'CNV Type', 'CNV Length', 'DB SNP ID', 'Observed Base',
                              'Reference Base', 'Indel Length', 'Gene' ];
         var columnConfig = [];

         columnConfig.push( {
            text : 'Patient Id',
            dataIndex : 'patientId'
         } );

         columnConfig.push( {
            text : 'Type',
            width : 50,
            dataIndex : 'variantType'
         } );

         /**
          * Render clickable UCSC Genome Browser links
          */
         columnConfig.push( {
            text : 'Genome Coordinates',
            flex : 1,
            dataIndex : 'genomeCoordinates',
            renderer : function(value) {

               return value + "&nbsp;<a href='http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=chr" + value
                  + "' target='_blank'><img src='scripts/ASPIREdb/resources/images/ucsc.png'></a>";
            },
         } );

         columnConfig.push( {
            text : 'Chromosome',
            dataIndex : 'chromosome',
            hidden : true
         } );

         columnConfig.push( {
            text : 'Base Start',
            flex : 1,
            dataIndex : 'baseStart',
            hidden : true
         } );

         columnConfig.push( {
            text : 'Base End',
            flex : 1,
            dataIndex : 'baseEnd',
            hidden : true
         } );

         columnConfig.push( {
            text : "Labels",
            dataIndex : 'labelIds',
            renderer : function(value, metaData) {
               return ASPIREdb.Utils.renderLabel( this.visibleLabels, value, metaData );
            },
            flex : 1
         } );

         columnConfig.push( {
            text : 'Copy Number',
            flex : 1,
            dataIndex : 'copyNumber',
            hidden : true

         } );

         columnConfig.push( {
            text : 'CNV Type',
            flex : 1,
            dataIndex : 'type',
            hidden : true
         } );

         columnConfig.push( {
            text : 'CNV Length',
            flex : 1,
            dataIndex : 'cnvLength',
            hidden : true
         } );

         columnConfig.push( {
            text : 'DB SNP ID',
            flex : 1,
            dataIndex : 'dbSNPID',
            hidden : true
         } );

         columnConfig.push( {
            text : 'Observed Base',
            flex : 1,
            dataIndex : 'observedBase',
            hidden : true
         } );

         columnConfig.push( {
            text : 'Reference Base',
            flex : 1,
            dataIndex : 'referenceBase',
            hidden : true
         } );

         columnConfig.push( {
            text : 'Indel Length',
            flex : 1,
            dataIndex : 'indelLength',
            hidden : true
         } );

         columnConfig.push( {
            text : 'Gene',
            flex : 2,
            dataIndex : 'gene',
            hidden : false,

            /**
             * Render a GeneValueObject in the cell grid
             */
            renderer : function(value, metaData, record, rowIdx, colIdx, store) {
               var ret = "";
               var symbols = [];
               var tooltip = "";

               if ( value === null ) {
                  return ret;
               }
               for (var i = 0; i < value.length; i++) {
                  var g = value[i];
                  var url = ASPIREdb.GemmaURLUtils.makeGeneUrl( g.symbol );

                  symbols.push( g.symbol + "&nbsp;<a target='_blank' href='" + url
                     + "'><img src='scripts/ASPIREdb/resources/images/gemmaTiny.gif' /></a>" );
                  tooltip += "<p><b>" + g.symbol + "</b>&nbsp;" + g.name + "</p>"
               }
               ret = symbols.join( ', ' );

               metaData.tdAttr = 'data-qtip="' + tooltip + '"';

               return ret;
            }
         } );

         for (var i = 0; i < characteristicNames.length; i++) {

            var config = {};

            config.text = characteristicNames[i];
            config.flex = 1;
            config.dataIndex = characteristicNames[i];
            config.hidden = true;

            // construct links for those characteristics that contains http://
            config.renderer = function(value) {
               if ( value.indexOf( 'http://' ) == 0 ) {
                  return '<a href="' + value + '" target="_blank">' + value + '</a>';
               } else {
                  return value;
               }
            };

            columnConfig.push( config );

            columnHeaders.push( characteristicNames[i] );

         }
         Ext.state.Manager.setProvider( new Ext.state.CookieProvider( {
            expires : new Date( new Date().getTime() + (1000 * 60 * 60 * 24 * 7) )
         } ) );

         grid = Ext.create( 'Ext.grid.Panel', {
            store : store,
            itemId : 'variantGrid',
            columns : columnConfig,
            columnHeaders : columnHeaders,
            plugins : {
               ptype : 'bufferedrenderer',
               trailingBufferZone : 20, // Keep 20 rows rendered in the table behind scroll
               leadingBufferZone : 50
            // Keep 50 rows rendered in the table ahead of scroll
            },

            listeners : {
               cellclick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {

                  var subjectStore = Ext.getStore( 'subjectStore' )
                  var selectedVariantRecords = grid.getSelectionModel().getSelection();
                  
                  var temp = {};
                  for (var i = 0; i < selectedVariantRecords.length; i++) {
                	  var rec = selectedVariantRecords[i];
                	  var patientId = rec.get( 'patientId' );
                	  var subjectId = subjectStore.getAt( subjectStore.findExact( 'patientId', patientId ) ).get( 'id' )
                	  temp[subjectId] = true;
                  }

                  // Unique subjects only
                  var r = [];
                  for (var k in temp) {
                	  r.push(k);
                  }
                  
                  
                  ASPIREdb.EVENT_BUS.fireEvent( 'select_subject_from_variant_grid', r );
               }
            },

            selModel : Ext.create( 'Ext.selection.RowModel', {
               preventFocus : true,
               mode : 'MULTI'

            } ),

            stripeRows : true,
            height : 180,
            width : 500,
            title : 'Variant Table',
            tooltip : 'A tabular view of variant information. To reveal additional variant details, click on the drop-down arrow on the right hand side of a column header.',

            visibleLabels : visibleLabels

         } );

         return grid;
      },

      extractCharacteristicNames : function(vvos) {
         var characteristicNames = [];
         for (var i = 0; i < vvos.length; i++) {
            for ( var char in vvos[i].characteristics) {
               if ( characteristicNames.indexOf( char ) == -1 ) {
                  characteristicNames.push( char );
               }
            }
         }
         return characteristicNames;
      },

      /**
       * Extract labels from value object
       * 
       * @param visibleLabels
       */
      createVisibleLabels : function(vvos) {
         var visibleLabels = {};

         for (var i = 0; i < vvos.length; i++) {
            var labels = vvos[i].labels;
            for (var j = 0; j < labels.length; j++) {
               var label = labels[j];
               visibleLabels[label.id] = label;
            }
         }

         return visibleLabels;
      },

      /**
       * @public
       * @param {VariantValueObject[]}
       *           vvos
       * @param {GeneValueObject[]}
       *           variantGenes
       * @param {string[]}
       *           characteristicNames
       * @param {string[]}
       *           visibleLabels
       * 
       */
      constructVariantStoreData : function(vvos, variantGenes, characteristicNames, visibleLabels) {

         var storeData = [];

         for (var i = 0; i < vvos.length; i++) {

            var vvo = vvos[i];

            var dataRow = [];

            dataRow.push( vvo.id );

            dataRow.push( vvo.patientId );

            dataRow.push( vvo.variantType );
            dataRow.push( vvo.genomicRange.chromosome + ":" + vvo.genomicRange.baseStart + "-"
               + vvo.genomicRange.baseEnd );
            dataRow.push( vvo.genomicRange.chromosome );
            dataRow.push( vvo.genomicRange.baseStart );
            dataRow.push( vvo.genomicRange.baseEnd );

            // create only one unique label instance
            var labels = [];
            for (var j = 0; j < vvo.labels.length; j++) {
               var aLabel = visibleLabels[vvo.labels[j].id];

               // this happens when a label has been assigned
               // by the admin and the user has no permissions
               // to modify the label
               if ( aLabel == undefined ) {
                  aLabel = vvo.labels[j];
               }

               labels.push( aLabel.id );
            }

            dataRow.push( labels );

            if ( vvo.variantType == "CNV" ) {
               dataRow.push( vvo.type );
               dataRow.push( vvo.copyNumber );
               dataRow.push( vvo.cnvLength );
            } else {
               dataRow.push( "" );
               dataRow.push( "" );
               dataRow.push( "" );
            }

            if ( vvo.variantType == "SNV" ) {
               dataRow.push( vvo.dbSNPID );
               dataRow.push( vvo.observedBase );
               dataRow.push( vvo.referenceBase );
            } else {
               dataRow.push( "" );
               dataRow.push( "" );
               dataRow.push( "" );
            }

            if ( vvo.variantType == "Indel" ) {
               dataRow.push( vvo.indelLength );
            } else {
               dataRow.push( "" );
            }

            // aggregate GeneValueObjects that are 'protein_coding'
            var geneSymbols = [];
            for (var geneIdx = 0; geneIdx < variantGenes[vvo.id].length; geneIdx++) {
               var g = variantGenes[vvo.id][geneIdx];
               if ( g.geneBioType === "protein_coding" ) {
                  geneSymbols.push( g );
               }
            }
            dataRow.push( geneSymbols );
            vvo.overlappingGenes = geneSymbols;

            // aggregate characteristics
            for (var j = 0; j < characteristicNames.length; j++) {
               var dataRowValue = "";
               for ( var char in vvo.characteristics) {
                  // make this comparison case insensitive since
                  // it doesn't matter in the database
                  // See Bug 4169
                  if ( char.toLowerCase() == characteristicNames[j].toLowerCase() ) {
                     dataRowValue = vvo.characteristics[char].value;
                     break;
                  }
               }
               dataRow.push( dataRowValue );
            }

            storeData.push( dataRow );
         }

         return storeData;

      },

   } );
