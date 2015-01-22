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
 * Create Variant Report Chart Panel
 */
Ext.define( 'ASPIREdb.view.report.PhenotypePerSubjectLabel', {
   extend : 'Ext.Panel',
   alias : 'widget.phenotypePerSubjectLabel',
   itemId : 'phenotypePerSubjectLabel',
   xtype : 'clustered-column',
   layout : 'fit',

   config : {},

   initComponent : function() {
      this.callParent();
   },

   /**
    * Display the data in a column chart series
    * 
    * mergedFreqData = '[{"type":"LOSS","asd1":136, "asd2":200},{"type":"GAIN","asd1":97, "asd2":100}]' columnName =
    * "type" labelNames = ["asd1","asd2"]
    */
   createLabelReport : function(mergedFreqData) {
      var me = this;

      var countColumnName = '# of variants';

      var seriesTitle = [ 'label_A', 'non_label_A' ];

      // var mergedFreqData = [ {'phenotype' : 'pheno_1', 'label_A' : 20, 'non_label_A' : 10 },
      // {'phenotype' : 'pheno_2', 'label_A' : 40, 'non_label_A' : 60 },
      // ];
      var fields = [ 'phenotype', 'label_A', 'non_label_A' ];
      var title = 'Project: ' + ASPIREdb.ActiveProjectSettings.getActiveProjectName();
      // var varCountsText = "# of variants: " + Ext.util.JSON.encode( totals ).replace( '{', '' ).replace( '}', ''
      // ).replace( /,/g, ', ' )
      // .replace( /"/g, '' ).replace(/:/g,' ');
      var varCountsText = "# of variants: ";
      var columnName = 'phenotypes';
      var xField = columnName;
      var yField = seriesTitle;

      // convert to Extjs Store
      var myDataStore = Ext.create( 'Ext.data.JsonStore', {
         storeId : 'reportStore',
         fields : fields,
         data : mergedFreqData,
      } );

      me.add( [ {
         xtype : 'chart',
         id : 'variantChart',
         width : '100%',
         // height : 410,
         padding : '10 0 0 0', // (top, right, bottom, left).
         animate : true,
         shadow : false,
         layout : 'fit',
         style : 'background: #fff;',
         legend : {
            padding : 0,
            position : 'bottom',
            boxStrokeWidth : 0,
            labelFont : '11px Helvetica'
         },
         store : myDataStore,
         insetPadding : 50,
         items : [ {
            type : 'text',
            text : title,
            font : '18px Helvetica',
            width : 100,
            height : 100,
            x : 40, // the sprite x position
            y : 10
         // the sprite y position
         }, {
            type : 'text',
            text : 'ASPIREdb',
            font : '12px Helvetica',
            x : 12,
            y : 380
         }, {
            type : 'text',
            text : varCountsText,
            font : '12px Helvetica',
            x : 40,
            y : 340
         } ],
         axes : [ {
            type : 'numeric',
            position : 'left',
            fields : yField,
            grid : true,
            minimum : 0,
            title : countColumnName,
            label : {
               renderer : function(v) {
                  // return v + '%';
                  return v;
               }
            }
         }, {
            type : 'category',
            position : 'bottom',
            fields : xField,
            title : columnName,
            grid : true,
            label : {
               rotate : {
                  degrees : -90
               }
            }
         } ],
         series : [ {
            type : 'column',
            axis : 'left',
            xField : xField,
            yField : yField,
            title : seriesTitle,
            style : {
               opacity : 0.80
            },
            highlight : {
               fill : '#000',
               'stroke-width' : 1,
               stroke : '#000'
            },
            tips : {
               trackMouse : true,
               width : 150,
               style : 'background: #FFF',
               renderer : function(storeItem, item) {
                  var label = item.series.title[Ext.Array.indexOf( item.series.yField, item.yField )];
                  var msg = label + " " + storeItem.get( xField ) + ': ' + storeItem.get( item.yField );
                  this.update( msg );
               }
            }
         } ]
      } ] );

      me.doLayout();
      me.show();

   },

   saveAsTXT : function() {
      ASPIREdb.TextDataDownloadWindow.showChartDownload( Ext.getStore( 'reportStore' ) );
   },

   saveAsPNG : function() {
      this.down( '#variantChart' ).save( {
         type : 'image/png'
      } );
   },

   createReport : function(store, columnName) {

      var me = this;
      var reportWindow = me.up( '#variantReportWindow' );

      reportWindow.setLoading( true );

      // get a list of variants grouped by Subject labels
      // var variantIds = this.getColumnDataFromStore( store, 'id' );
      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore( store, 'id' );

      var mergedFreqData = [ {
         'phenotype' : 'pheno_1',
         'label_A' : 20,
         'non_label_A' : 10
      }, {
         'phenotype' : 'pheno_2',
         'label_A' : 40,
         'non_label_A' : 60
      }, ];

      reportWindow.setLoading( false );
      me.createLabelReport( mergedFreqData );

      /*
       * VariantService.groupVariantsBySubjectLabels( variantIds, { callback : function(variantsByLabel) {
       * 
       * var mergedFreqData = []; var labelNames = [];
       *  // special case for histogram, gather all data and generate bins var allData = []; var bins = null; if (
       * me.isHistogramType( columnName ) && !me.logTransform ) { Object.keys( variantsByLabel ).forEach(
       * function(labelName) { for (var j = 0; j < variantsByLabel[labelName].length; j++) { var val =
       * variantsByLabel[labelName][j][columnName]; if ( val != undefined ) { allData.push( val ) } } } ) bins =
       * me.generateBins( allData ); }
       * 
       * for ( var labelName in variantsByLabel) { labelNames.push( labelName );
       * 
       * var freqData = me.createFreqData( variantsByLabel, labelName, columnName, bins ); if ( freqData == null ) {
       * continue; }
       * 
       * me.addFreqData( freqData, mergedFreqData ); }
       * 
       * me.createLabelReport( mergedFreqData, columnName, labelNames );
       * 
       * reportWindow.setLoading( false ); }, errorHandler : function(message, exception) { Ext.Msg.alert( 'Error',
       * message ) console.log( message ) console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
       * 
       * reportWindow.setLoading( false ); } } );
       */
   }

} );