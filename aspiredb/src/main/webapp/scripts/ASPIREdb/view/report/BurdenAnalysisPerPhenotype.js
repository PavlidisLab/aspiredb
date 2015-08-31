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
Ext.require( [ 'ASPIREdb.view.PhenotypeEnrichmentChart' ] );

/**
 * Create Burden Analysis Per Phenotype
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisPerPhenotype', {
   extend : 'Ext.panel.Panel',
   layout : 'fit',

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
         type : 'string',
      // sortType : 'asFraction'
      }, {
         name : 'group2',
         type : 'string',
      // sortType : 'asFraction'
      }, {
         name : 'pValue',
         type : 'float',
         useNull : true
      }, {
         name : 'qValue',
         type : 'float',
         useNull : true
      } ];

      var store = new Ext.data.JsonStore( {
         storeId : 'burdenAnalysisPerSubjectStore',
         fields : fields,
         data : data,
         sorters : {
            property : 'pValue',
            direction : 'ASC'// or 'DESC' (case sensitive for local sorting)
         }
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
      }, {
         dataIndex : 'group1',
         text : 'Group 1',
         flex : 1,
      }, {
         dataIndex : 'group2',
         text : 'Group 2',
         flex : 1,
      }, {
         dataIndex : 'pValue',
         text : 'P-value',
         flex : 1,
         renderer : precisionRenderer,
         tooltip : 'Chi-square test',
         tooltipType : 'title'
      }, {
         dataIndex : 'qValue',
         text : 'Corrected p-value',
         flex : 1,
         renderer : precisionRenderer,
         tooltip : 'Chi-square test',
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
         itemId : 'burdenAnalysisPerPhenotypeGrid',
         columns : columns
      } );
      grid.store.sort( 'pValue', 'ASC' );

      grid.doLayout();
      grid.show();

      return grid;
   },

   asFraction : function(fraction) {
      var tokens = fraction.split( '/' );
      if ( tokens.length != 2 ) {
         return fraction;
      }
      return parseInt( tokens[0] ) / parseInt( tokens[1] ) * 1.0;
   },

   createChart : function(vos) {

      var ref = this;

      var chart = Ext.create( 'ASPIREdb.view.PhenotypeEnrichmentChart', {
         itemId : 'phenotypeEnrichmentChart'
      } );

      ref.valueObjects = vos;

      var data = [];
      for (var i = 0; i < vos.length; i++) {
         var vo = vos[i];

         // var row = [ vo.name, vo.inGroupTotalString, vo.outGroupTotalString, vo.PValueString,
         // vo.PValueCorrectedString ];
         var row = {
            name : vo.name,
            inGroup : ref.asFraction( vo.group1 ) * 100.0,
            outGroup : ref.asFraction( vo.group2 ) * 100.0,
            inGroupStr : vo.group1,
            outGroupStr : vo.group2,
            pValue : parseFloat( vo.pValue ),
            qValue : parseFloat( vo.qValue )
         }
         data.push( row );
      }

      chart.store.loadData( data );
      chart.store.sort( 'pValue', 'ASC' );

      return chart;
   },

   calculateAndShow : function(variantIds) {

      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );
      var label1 = me.label1;
      var label2 = me.label2;

      if ( label1 === null || label2 === null ) {
         Ext.Msg.alert( 'Error', "Subject labels are required!" );
         me.setLoading( false );
         return;
      }

      if ( label1.id === label2.id ) {
         Ext.Msg.alert( 'Error', "Subject labels must be different!" );
         me.setLoading( false );
         return;
      }

      BurdenAnalysisService.getBurdenAnalysisPerPhenotype( ASPIREdb.ActiveProjectSettings.getActiveProjectIds(),
         label1, label2, {
            callback : function(results) {

               if ( results !== null && results.length > 0 ) {
                  var grid = me.createGrid( results );
                  me.add( grid );
                  grid.setVisible( false );

                  var chart = me.createChart( results );
                  me.add( chart );
               } else {
                  console.log( 'No results found' );
                  Ext.Msg.alert( 'Results', 'No results found' );
               }

               me.setLoading( false );
            },
            errorHandler : function(errorString, exception) {
               var msg = 'Error calculating Burden Analysis Per Phenotype:<br/>' + errorString;
               console.log( msg, exception );
               // Ext.Msg.alert( 'Error', msg );
               Ext.Msg.show( {
                  title : 'Phenotype Enrichment Error',
                  msg : msg,
                  buttons : Ext.Msg.OK,
                  minWidth : 350,
               } );

               me.setLoading( false );
            }
         } );

   },

   createReport : function(store) {

      var me = this;

      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore( store, 'id' );

      me.setLoading( true );

      this.calculateAndShow( variantIds );

      return me;
   },

} );
