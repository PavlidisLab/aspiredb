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
Ext.require( [ 'ASPIREdb.view.report.VariantReportPanel' ] );

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
         // TODO Add labels
         data : [ [ 'type', 'CNV type' ], [ 'variantType', 'Variant type' ], [ 'chromosome', 'Chromosome' ],
                 [ 'inheritance', 'Inheritance' ], [ 'Array Platform', 'Array Platform' ],
                 [ 'Array Report', 'Array Report' ], [ 'Characteristics', 'Characteristics' ],
                 [ 'cnvLength', 'CNV Length' ], [ 'patientId', 'Patient ID' ] ],
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
         listeners : {
            'change' : this.reportComboSelectHandler,
            afterrender : function(combo) {
               var recordSelected = combo.getStore().getAt( 0 );
               combo.setValue( recordSelected.get( 'id' ) );
            }
         }
      } );

      this.down( '#savePngButton' ).on( 'click', this.saveChartHandler );

   },

   createAndShow : function(variantStore) {
      var me = this;

      me.variantStore = variantStore;
      
      me.doLayout();
      me.show();
   },

   saveChartHandler : function(e) {
      var me = this;
      Ext.MessageBox.confirm( 'Confirm Download', 'Would you like to download the chart as an image?',
         function(choice) {
            if ( choice == 'yes' ) {
               me.up( '#variantReportWindow' ).chart.save( {
                  type : 'image/png'
               } );
            }
         } );
   },

   reportComboSelectHandler : function(sel) {
      var reportType = sel.value;
      var window = this.up( '#variantReportWindow' );

      var reportPanel = Ext.create( 'ASPIREdb.view.report.VariantReportPanel' );
      reportPanel.createReport( window.variantStore, reportType );
      var chart = reportPanel.down( "#variantChart" );

      window.down( '#saveTxtButton' ).on( 'click', function() {
         // When Save button is clicked open text data download
         ASPIREdb.TextDataDownloadWindow.showChartDownload( Ext.getStore('reportStore') );
      } );

      window.remove( window.down( '#variantReport' ) );
      window.add( reportPanel );
      window.chart = chart;
      window.doLayout();
   },

} );