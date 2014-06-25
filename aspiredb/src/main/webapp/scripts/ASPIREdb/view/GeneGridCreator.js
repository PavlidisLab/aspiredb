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

// This grid is created dynamically based on 'suggested properties'( see VariantService.suggestProperties) this dynamic
// nature is why this grid is constructed this way
// TODO js documentation
Ext.define( 'ASPIREdb.view.GeneGridCreator',
   {
      /**
       * @memberOf ASPIREdb.view.GeneGridCreator
       */
      extend : 'Ext.util.Observable',
      singleton : true,

      storeFields : [ 'id', 'patientId', 'geneName', 'genomeCoordinates', 'noInsertion', 'noDeletion', 'noSynonymous',
                     'noNonSynonym', 'NoLoss', 'NoGain', 'largestCNV', 'smallestCNV', 'NoVariants' ],

      initComponent : function() {
         this.callParent();

      },

      /**
       * @public
       * @param {GeneValueObject[]}
       *           vvos
       * @param {string[]}
       *           properties
       */
      createGeneGrid : function(vvos, properties) {

         var fieldData = [];

         var dataIndexes = this.storeFields;

         for (var i = 0; i < dataIndexes.length; i++) {

            if ( dataIndexes[i] == 'noInsertion' || dataIndexes[i] == 'noDeletion' || dataIndexes[i] == 'noSynonymous'|| dataIndexes[i]=='noNonSynonym'|| dataIndexes[i]=='noLoss'||dataIndexes[i]=='noGain' ||dataIndexes[i]=='noVariants') {
               fieldData.push( {
                  name : dataIndexes[i],
                  type : 'int'
               } );
            } else {
               fieldData.push( {
                  name : dataIndexes[i],
                  type : 'auto'
               } );
            }
         }

         characteristicNames = [];

         for (var i = 0; i < properties.length; i++) {

            if ( properties[i].characteristic ) {
               fieldData.push( properties[i].displayName );
               characteristicNames.push( properties[i].name );
            }

         }

         var visibleLabels = this.createVisibleLabels( vvos );
         var storeData = this.constructGeneStoreData( vvos, characteristicNames, visibleLabels );

         var store = Ext.create( 'Ext.data.ArrayStore', {
            fields : fieldData,
            data : storeData,
         // groupField : 'patientId'

         } );

         var columnHeaders = [ 'Patient Id', 'Gene Name', 'Genome Coordinates', '# Insertion', '# Deletion', '# Synonymous',
                               '# NonSynonym', '# Loss', '# Gain', 'largest CNV', 'smallest CNV', '# Variants' ];
         var columnConfig = [];

         columnConfig.push( {
            text : 'Patient Id',
            flex : 1,
            dataIndex : 'patientId'
         } );

         columnConfig.push( {
            text : 'Gene Name',
            flex : 1,
            dataIndex : 'geneName'
         } );

         columnConfig.push( {
            text : 'Genome Coordinates',
            flex : 1,
            dataIndex : 'genomeCoordinates'
         } );

         columnConfig.push( {
            text : '# Insertion',
            flex : 1,
            dataIndex : 'noInsertion',
            hidden : true
         } );

         columnConfig.push( {
            text : '# Deletion',
            flex : 1,
            dataIndex : 'noDeletion',
            hidden : true
         } );

         columnConfig.push( {
            text : '# Synonymous',
            flex : 1,
            dataIndex : 'noSynonymous',
            hidden : true
         } );

       /**  columnConfig.push( {
            text : "Labels",
            dataIndex : 'labelIds',
            renderer : function(value) {
               var ret = "";
               for (var i = 0; i < value.length; i++) {

                  var label = this.visibleLabels[value[i]];
                  if ( label === undefined ) {
                     continue;
                  }
                  if ( label.isShown ) {
                     ret += label.htmlLabel;
                  }
               }
               return ret;
            },
            flex : 1
         } );*/

         columnConfig.push( {
            text : '# NonSynonym',
            flex : 1,
            dataIndex : 'noNonSynonym',
            hidden : true

         } );

         columnConfig.push( {
            text : '# Loss',
            flex : 1,
            dataIndex : 'noLoss',
            hidden : true
         } );

         columnConfig.push( {
            text : '# Gain',
            flex : 1,
            dataIndex : 'noGain',
            hidden : true
         } );

         columnConfig.push( {
            text : 'largest CNV',
            flex : 1,
            dataIndex : 'largestCNV',
            hidden : true
         } );

         columnConfig.push( {
            text : 'smallest CNV',
            flex : 1,
            dataIndex : 'smallestCNV',
            hidden : true
         } );

         columnConfig.push( {
            text : '# Variants',
            flex : 1,
            dataIndex : 'noVariants',
            hidden : true
         } );


         for (var i = 0; i < characteristicNames.length; i++) {

            var config = {};

            config.text = characteristicNames[i];
            config.flex = 1;
            config.dataIndex = characteristicNames[i];
            config.hidden = true;

            columnConfig.push( config );

            columnHeaders.push( characteristicNames[i] );

         }
         Ext.state.Manager.setProvider( new Ext.state.CookieProvider( {
            expires : new Date( new Date().getTime() + (1000 * 60 * 60 * 24 * 7) )
         } ) );

         // TODO styling
         grid = Ext.create( 'Ext.grid.Panel', {
            store : store,
            itemId : 'geneGrid',
            columns : columnConfig,
            columnHeaders : columnHeaders,
            // multiSelect : true,

            // listeners : {
            // cellclick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
            //
            // this.selModel.select( record.index, false, false );
            // }
            // },

            // selModel : Ext.create( 'Ext.selection.CellModel', {
            selModel : Ext.create( 'Ext.selection.RowModel', {
               preventFocus : true,
               mode : 'MULTI'

            } ),

            stripeRows : true,
            height : 180,
            width : 500,
            title : 'Gene View',

            // known Extjs bug. Disabling for now until fixed. See Bug 4063
            // requires : [ 'Ext.grid.feature.Grouping' ],
            // features : [ Ext.create( 'Ext.grid.feature.Grouping', {
            // groupHeaderTpl : '{name} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
            // startCollapsed : true,
            //
            // } ) ],

            visibleLabels : visibleLabels

         } );

         return grid;
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
       * constructVariantCountStoreData : function(vvos, characteristicNames, subjectId){ var storeData = []; var
       * countCNV=0; var countSNV=0; var countINDEL=0; var variantype =[];
       * 
       * for ( var i = 0; i < vvos.length; i++) {
       * 
       * var vvo = vvos[i];
       * 
       * if (vvo.patientId == subjectId){
       * 
       * switch (vvo.variantType){ case "CNV": countCNV++; break; case "SNV": countSNV++; break; case "INDEL":
       * countINDEL++; break; } } } },
       */
      /**
       * @public
       * @param {VariantValueObject[]}
       *           vvos
       * @param {string[]}
       *           characteristicNames
       * @param {string[]}
       *           visibleLabels
       * 
       */
      constructGeneStoreData : function(vvos, characteristicNames, visibleLabels) {

         var storeData = [];

         for (var i = 0; i < vvos.length; i++) {

        /**   var vvo = vvos[i];

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

            if ( vvo.variantType == "INDEL" ) {
               dataRow.push( vvo.length );
            } else {
               dataRow.push( "" );
            }

            for (var j = 0; j < characteristicNames.length; j++) {

               var dataRowValue = "";

               for ( var char in vvo.characteristics) {
                  if ( char == characteristicNames[j] ) {
                     dataRowValue = vvo.characteristics[char].value;
                     break;
                  }
               }

               dataRow.push( dataRowValue );
            }

            storeData.push( dataRow );*/
         }

         return storeData;

      }

   } );