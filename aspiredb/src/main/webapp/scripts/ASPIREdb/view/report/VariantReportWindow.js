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
Ext.require( [ 'ASPIREdb.view.report.VariantReportPanel', 'ASPIREdb.view.report.BurdenAnalysisPerSubject','ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel' ] );

/**
 * Create Variant Report Window
 */
Ext.define( 'ASPIREdb.view.report.VariantReportWindow', {
   extend : 'Ext.Window',
   width : 950,
   height : 500,
   id : 'variantReportWindow',
   title : 'Variant Report',
   layout : 'fit',
   resizable : true,
   tbar : [ {
      itemId : 'saveTxtButton',
      text : 'Save as TXT',
      tooltip : 'Save as TXT',
//      icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
   }, {
      xtype : 'button',
      itemId : 'savePngButton',
      text : 'Save as PNG',
      tooltip : 'Save as PNG',
//      icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
   } ],

   statics : {
      getColumnDataFromStore : function(store, columnName) {
         var result = [];
         for (var i = 0; i < store.data.length; i++) {
            
            // extract values
            var row = store.data.getAt( i ).data;
            var val = row[columnName];
            
            if ( val == undefined ) {
               console.log("Error " + val);
               continue;
            }
            
         // For CNV specific attributes like type and length, ignore SNVs
            if ( !this.isVariantTypeAndReportFieldCompatible( columnName, row["variantType"] ) ) {
               continue;
            }
            
            // show NA for empty values
            if ( val === "" ) {
                val = "NA";
            }
            
            result.push( val );
         }
         return result;
      },
      
      isVariantTypeAndReportFieldCompatible : function( columnName, variantType ) {
         if ( variantType !== "CNV" ) {
            if ( columnName === "type" || columnName === "cnvLength" ) {
               return false;
            }
         }
         
         return true;
      },
   },

   initComponent : function() {

      this.callParent();

      this.reportTypeStore = Ext.create( 'Ext.data.ArrayStore', {
         fields : [ {
            name : 'id',
            type : 'string'
         }, {
            name : 'name',
            type : 'string'
         } ],
         // [ VariantValueObjectPropertyName : DisplayValue ]
         data : [
                 // gene summary tables
                 [ 'genesPerSubject', 'Genes per subject' ], 
                 [ 'genesPerSubjectLabel', 'Genes per subject label' ], 
                 
                 // commonly used reports
                 [ 'chromosome', 'Chromosome' ],
                 [ 'patientId', 'Patient ID' ], 
                 [ 'variantType', 'Variant type' ], 
                 
                 // cnv specific
                 [ 'type', 'CNV type' ], 
                 [ 'cnvLength', 'CNV length' ], 
                 
                 // characteristics
                 [ 'Array Platform', 'Array platform' ],
                 [ 'Array Report', 'Array report' ], 
                 [ 'Characteristics', 'Characteristics' ],
                 [ 'Inheritance', 'Inheritance' ], 
                 
                  ],
         autoLoad : true,
         autoSync : true,
      } );

      this.down( 'toolbar' ).insert( 0, {
         xtype : 'tbfill'
      } );
      this.down( 'toolbar' ).insert( 0, {
         xtype : 'combo',
         itemId : 'reportCombo',
         fieldLabel : 'Data',
         store : this.reportTypeStore,
         displayField : 'name',
         valueField : 'id',
         queryMode : 'local',
         editable : false,
         forceSelection : true,
         width : 300,
         listeners : {
            'change' : this.reportComboSelectHandler,
            afterrender : function(combo) {
               var recordSelected = combo.getStore().getAt( 0 );
               combo.setValue( recordSelected.get( 'id' ) );
            }
         }
      } );

   },

   createAndShow : function(variantStore) {
      var me = this;

      me.variantStore = variantStore;
      
      me.doLayout();
      me.show();
   },

   reportComboSelectHandler : function(sel) {
      var me = this;
      var selReportType = sel.value;
      var window = this.up( '#variantReportWindow' );
      
      var reportPanel = window.down( '#variantReport' );
      if ( reportPanel != null ) {
         window.remove( reportPanel );
         reportPanel.destroy();
      }
      
      var saveTextHandler = null;
      if ( selReportType === "genesPerSubject") {
         window.down( '#savePngButton' ).hide();
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerSubject', { id : 'variantReport' } );
      } else if ( selReportType === "genesPerSubjectLabel" ) {
         window.down( '#savePngButton' ).hide();
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel', { id : 'variantReport' } );
      } else {
         window.down( '#savePngButton' ).show();
         reportPanel = Ext.create( 'ASPIREdb.view.report.VariantReportPanel' );
      }
      
      window.down( '#saveTxtButton' ).on( 'click', reportPanel.saveAsTXT );

      window.down( '#savePngButton' ).on( 'click', function(e) {
         var me = this;
         Ext.MessageBox.confirm( 'Confirm Download', 'Would you like to download the chart as an image?',
            function(choice) {
               if ( choice == 'yes' ) {
                  reportPanel.saveAsPNG();
               }
            } );
      } );

      window.add( reportPanel );
      
      reportPanel.createReport( window.variantStore, selReportType );
      
      window.doLayout();
   },

} );