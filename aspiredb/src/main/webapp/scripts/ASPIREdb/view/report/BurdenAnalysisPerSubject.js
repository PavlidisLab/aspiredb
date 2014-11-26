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
Ext.require( [  ] );

/**
 * Create Burden Analysis Per Subject
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisPerSubject', {
   extend : 'Ext.panel.Panel',
   layout : 'fit',

   /**
    * Display table contents as plain text for downloading to a TSV
    */
   saveAsTXT : function() {
      ASPIREdb.TextDataDownloadWindow.showBurdenAnalysisDownload( Ext.getStore( 'burdenAnalysisPerSubjectStore' ) );
   },

   extractFields : function(data) {
      var ret = [];
      if ( data == null || data.length == 0 ) {
         console.log( 'data is empty' );
         return;
      }
      for ( var field in data) {
         if ( field === 'PATIENT_ID' ) {
            ret.push( field );
         } else {
            ret.push( {
               name : field,
               type : 'number'
            } );
         }
      }
      return ret;
   },

   createStore : function(data) {
      if ( data == null || data.length == 0 ) {
         console.log( 'data is empty' );
         return;
      }

      // fields
      var fields = this.extractFields( data[0] );

      // convert to Extjs Store
      var store = new Ext.data.JsonStore( {
         storeId : 'burdenAnalysisPerSubjectStore',
         fields : fields,
         data : data,
      } );

      return store;
   },

   createGrid : function(data) {
      var me = this;
      var columns = [ {
         dataIndex : 'PATIENT_ID',
         text : 'Patient ID',
      }, {
         dataIndex : 'NUM_DELETION',
         text : '# deletion',
      }, {
         dataIndex : 'NUM_DUPLICATION',
         text : '# duplication',
      }, {
         dataIndex : 'NUM_UNKNOWN',
         text : '# uknown',
      }, {
         dataIndex : 'TOTAL',
         text : 'Total # of CNVs',
         flex : 2,
      }, {
         dataIndex : 'TOTAL_SIZE',
         text : 'Total size',
         flex : 2,
      }, {
         dataIndex : 'AVG_SIZE',
         text : 'Avg size',
         flex : 2,
      }, {
         dataIndex : 'NUM_GENES',
         text : '# genes',
      }, {
         dataIndex : 'NUM_CNVS_WITH_GENE',
         text : '# gene-disrupting CNVs',
         // width : 130,
         flex : 3,
      }, {
         dataIndex : 'AVG_GENES_PER_CNV',
         text : 'Avg # genes per CNV',
         // width : 120,
         flex : 3,
      } ];

      var grid = Ext.create( 'Ext.grid.Panel', {
         store : me.createStore( data ),
         itemId : 'burdenAnalysisPerSubjectGrid',
         columns : columns
      } );

      grid.doLayout();
      grid.show();

      return grid;
   },

   calculateAndShow : function(variantIds) {

      var me = this;

      GeneService.getBurdenAnalysisPerSubject( variantIds, {
         callback : function(results) {
            var grid = me.createGrid( results );
            me.add( grid );
            var window = me.up( '#variantReportWindow' );
            if ( window != null ) {
               window.setLoading( false );
            }

         },
         errorHandler : function(errorString, exception) {
            var msg = 'Error calculating Burden Analysis Per Subject: ' + errorString;
            console.log( msg, exception );
            Ext.Msg.alert( 'Error', msg );
         }
      } );

   },

   createReport : function(store) {

      var me = this;
      
      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore(store, 'id');
      
      var window = me.up( '#variantReportWindow' );
      if ( window != null ) {
         window.setLoading( true );
      }

      this.calculateAndShow( variantIds );

      return me;
   },

} );
