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
Ext.require( [] );

/**
 * e.g. 2**, 9, 10*
 */
Ext.data.Types.FLOATWITHPVAL = {
   convert : function(v, n) {
      return v;
   },
   sortType : function(v) {
      v = Ext.isNumber( v ) ? v : parseFloat( String( v.replace( /\*/g, '' ) ), 10 );
      return isNaN( v ) ? 0 : v;
   },
   type : 'floatWithPval'
};

/**
 * e.g. 2 / 3, 5 / 9
 */
Ext.data.Types.FRACTION = {
   convert : function(v, n) {
      return v;
   },
   sortType : function(v) {
      var numerator = String( v.replace( / *\/.*/, '' ) );
      var denominator = String( v.replace( /.*\/ */, '' ) );
      v = Ext.isNumber( v ) ? v : parseFloat( (numerator / denominator), 10 );
      return isNaN( v ) ? 0 : v;
   },
   type : 'fraction'
};

/**
 * Create Burden Analysis Per Subject Label
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel', {
   extend : 'Ext.panel.Panel',
   layout : 'fit',

   /**
    * Display table contents as plain text for downloading to a TSV
    */
   saveAsTXT : function() {
      ASPIREdb.TextDataDownloadWindow.showBurdenAnalysisDownload( Ext.getStore( 'burdenAnalysisPerSubjectStore' ) );
   },

   /**
    * Extract data elements (fields)
    */
   extractFields : function(data) {
      var ret = [];
      if ( data == null || data.length == 0 ) {
         console.log( 'data is empty' );
         return;
      }

      // set data types
      for ( var field in data) {
         if ( field === 'NUM_SAMPLES' ) {
            ret.push( {
               name : field,
               type : 'fraction'
            } );
         } else if ( field === 'LABEL_NAME' ) {
            ret.push( {
               name : field,
               type : 'text'
            } );
         } else {
            ret.push( {
               name : field,
               type : 'floatWithPval'
            } );
         }
      }
      // console.log('ret='+Ext.JSON.encode(ret))
      return ret;
   },

   oldCreateStore : function(data) {
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

   createStore : function(data) {
      var fields = [ {
         name : 'name',
      }, {
         name : 'group1',
         type : 'float'
      }, {
         name : 'group2',
         type : 'float'
      }, {
         name : 'pValue',
         type : 'float'
      }, {
         name : 'qValue',
         type : 'float'
      } ];
      // TODO
      /*
       * var data = [ { name : 'Group size', group1 : 200, group2 : 400, pValue : 0.004, qValue : 0.403, }, { name :
       * 'Total CNV length', group1 : 200, group2 : 400, pValue : 0.004, qValue : 0.403, }, { name : 'Average CNV
       * length', group1 : 200, group2 : 400, pValue : 0.004, qValue : 0.403, }, { name : 'Number of CNVs', group1 :
       * 200, group2 : 400, pValue : 0.004, qValue : 0.403, }, { name : 'CNVs per subject', group1 : 200, group2 : 400,
       * pValue : 0.004, qValue : 0.403, }, { name : 'Genes per CNV', group1 : 200, group2 : 400, pValue : 0.004, qValue :
       * 0.403, } ];
       */
      var store = new Ext.data.JsonStore( {
         storeId : 'burdenAnalysisPerSubjectStore',
         fields : fields,
         data : data,
      } );

      return store;
   },

   createColumns : function() {

      var columns = [ {
         dataIndex : 'name',
         text : '',
         flex : 2
      }, {
         dataIndex : 'group1',
         text : 'Group 1',
         flex : 1
      }, {
         dataIndex : 'group2',
         text : 'Group 2',
         flex : 1
      }, {
         dataIndex : 'pValue',
         text : 'P-value',
         flex : 1
      }, {
         dataIndex : 'qValue',
         text : 'Corrected p-value',
         flex : 1
      } ];

      return columns;
   },

   createGrid : function(data) {
      var me = this;
      var columns = me.createColumns();
      var store = me.createStore( data );
      var grid = Ext.create( 'Ext.grid.Panel', {
         store : store,
         itemId : 'burdenAnalysisPerSubjectLabelGrid',
         columns : columns
      } );

      grid.doLayout();
      grid.show();

      return grid;
   },

   oldCreateGrid : function(data) {
      var me = this;
      var columns = [ {
         dataIndex : 'LABEL_NAME',
         text : 'Subject label',
      }, {
         dataIndex : 'NUM_SAMPLES',
         text : 'Num samples',
      }, {
         dataIndex : 'NUM_DELETION',
         text : '# deletion',
      }, {
         dataIndex : 'NUM_DUPLICATION',
         text : '# duplication',
      }, {
         dataIndex : 'NUM_UNKNOWN',
         text : '# unknown',
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
         text : '# genes overlapped by CNVs',
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
         itemId : 'burdenAnalysisPerSubjectLabelGrid',
         columns : columns
      } );

      grid.doLayout();
      grid.show();

      return grid;
   },

   calculateAndShow : function(variantIds) {

      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );
      var label1 = window.down( '#subjectLabelCombo1' ).getValue();
      var label2 = window.down( '#subjectLabelCombo2' ).getValue();

      GeneService.getBurdenAnalysisPerSubjectLabel( label1, label2, variantIds, {
         callback : function(results) {
            var grid = me.createGrid( results );
            me.add( grid );

            if ( window != null ) {
               window.setLoading( false );
            }
         },
         errorHandler : function(errorString, exception) {
            var msg = 'Error calculating Burden Analysis Per Subject Label: ' + errorString;
            console.log( msg, exception );
            Ext.Msg.alert( 'Error', msg );

            if ( window != null ) {
               window.setLoading( false );
            }
         }
      } );

      // TODO
      /*
       * var results = []; var grid = me.createGrid( results ); me.add( grid ); var window = me.up(
       * '#burdenAnalysisWindow' ); if ( window != null ) { window.setLoading( false ); }
       */

   },

   createReport : function(store) {

      var me = this;

      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore( store, 'id' );

      var window = me.up( '#burdenAnalysisWindow' );
      if ( window != null ) {
         window.setLoading( true );
      }

      this.calculateAndShow( variantIds );

      return me;
   },

} );
