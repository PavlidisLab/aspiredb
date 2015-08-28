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
 * Create Burden Analysis Per Subject Label
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel', {
   extend : 'Ext.panel.Panel',
   layout : 'fit',

   config : {
      label1 : null,
      label2 : null,
   },

   /**
    * Display table contents as plain text for downloading to a TSV
    */
   saveAsTXT : function() {
      ASPIREdb.TextDataDownloadWindow.showBurdenAnalysisDownload( Ext.getStore( 'burdenAnalysisPerSubjectStore' ) );
   },

   createStore : function(data) {
      var fields = [ {
         name : 'name',
      }, {
         name : 'group1',
         type : 'float',
         useNull : true
      }, {
         name : 'group2',
         type : 'float',
         useNull : true
      }, {
         name : 'pValue',
         type : 'float',
         useNull : true
      } ];

      var store = new Ext.data.JsonStore( {
         storeId : 'burdenAnalysisPerSubjectStore',
         fields : fields,
         data : data,
      } );

      return store;
   },

   createColumns : function() {

      var precisionRenderer = function(value, metaData, record, row, col, store, gridView) {
         return value.toPrecision( 4 );
      };

      var columns = [ {
         dataIndex : 'name',
         text : '',
         flex : 2,
         renderer : function(value, metaData) {
            if ( value === "Mean number of variants overlapping a gene" ) {
               metaData.tdAttr = 'data-qtip="' + 'The average number of variants per subject group' + '"qwidth="auto"'
            } else if ( value === "Mean number of genes overlapping a variant" ) {
               metaData.tdAttr = 'data-qtip="' + 'The average number of genes per subject group' + '"qwidth="auto"'
            }
            return value;
         }
      }, {
         dataIndex : 'group1',
         text : 'Group 1',
         flex : 1,
         renderer : precisionRenderer
      }, {
         dataIndex : 'group2',
         text : 'Group 2',
         flex : 1,
         renderer : precisionRenderer
      }, {
         dataIndex : 'pValue',
         text : 'P-value',
         flex : 1,
         renderer : precisionRenderer,
         tooltip : 'Mann-Whitney U test',
         tooltipType : 'title'
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
      grid.store.sort( 'pValue', 'ASC' );

      grid.doLayout();
      grid.show();

      return grid;
   },

   calculateAndShow : function(variantIds) {

      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );
      // var label1 = window.down( '#subjectLabelCombo1' ).getValue();
      // var label2 = window.down( '#subjectLabelCombo2' ).getValue();
      var label1 = me.label1;
      var label2 = me.label2;

      if ( label1 !== null && label2 !== null && label1.id === label2.id ) {
         Ext.Msg.alert( 'Error', "Subject labels must be different!" );
         window.setLoading( false );
         return;
      }

      BurdenAnalysisService.getBurdenAnalysisPerSubjectLabel( label1, label2, variantIds, {
         callback : function(results) {
            var grid = me.createGrid( results );
            me.add( grid );

            if ( window !== null ) {
               window.setLoading( false );
            }
         },
         errorHandler : function(errorString, exception) {
            var msg = 'Error calculating Burden Analysis Per Subject Label: ' + errorString;
            console.log( msg, exception );
            Ext.Msg.alert( 'Error', msg );

            if ( window !== null ) {
               window.setLoading( false );
            }
         }
      } );

   },

   createReport : function(store) {

      var me = this;

      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore( store, 'id' );

      var window = me.up( '#burdenAnalysisWindow' );
      if ( window !== null ) {
         window.setLoading( true );
      }

      this.calculateAndShow( variantIds );

      return me;
   },

} );
