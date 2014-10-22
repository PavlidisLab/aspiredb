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
      xtype : 'button',
      itemId : 'saveChartButton',
      text : '',
      tooltip : 'Save chart as PNG',
      icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
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
         // TODO Add labels, Length
         data : [ [ 'type', 'CNV type' ], [ 'variantType', 'Variant type' ], [ 'chromosome', 'Chromosome' ],
                 [ 'inheritance', 'Inheritance' ], [ 'Array Platform', 'Array Platform' ],
                 [ 'Array Report', 'Array Report' ], [ 'Characteristics', 'Characteristics' ],
                 ['patientId', 'Patient ID'] ],
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
            'select' : this.reportComboSelectHandler,
         }
      } );

      this.down( '#saveChartButton' ).on( 'click', this.saveChartHandler );
   },

   createAndShow : function(variantStore) {
      var me = this;

      me.variantStore = variantStore;

      // var grid = this.down( '#variantGrid' );
      var reportPanel = Ext.create( 'ASPIREdb.view.report.VariantReportPanel' );

      var reportType = me.reportTypeStore.getAt( 0 ).data['id'];
      // var reportType = 'type';
      // var reportType = 'inheritance';
      // var reportType = 'chromosome';

      reportPanel.createReport( variantStore, reportType );

      me.chart = reportPanel.down( "#variantChart" );
      me.add( reportPanel );
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

      window.remove( window.down( '#variantReport' ) );
      window.add( reportPanel );
      window.chart = chart;
      window.doLayout();
   },

} );