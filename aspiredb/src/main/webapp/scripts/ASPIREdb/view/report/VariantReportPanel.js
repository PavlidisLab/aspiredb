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
Ext.define( 'ASPIREdb.view.report.VariantReportPanel', {
   extend : 'Ext.Panel',
   alias : 'widget.variantReportPanel',
   id : 'variantReport',
   xtype : 'clustered-column',
   layout : 'fit',

   config : {
      logTransform : false,
      N_BINS : 20,
   },

   initComponent : function() {
      this.callParent();
   },

   /**
    * Return the value where the bin is found
    */
   findBin : function(val, bins, binsText) {
      for (var i = 0; i < bins.length; i++) {
         if ( (bins[i] - Number(val)) >= 0 ) {
            // console.log( val + " is in bin " + BINS_TEXT[i] );
            return binsText[i];
         }
      }

      // it must be the last bin
      // console.log( val + " is in bin " + BINS_TEXT[BINS_TEXT.length - 1] );
      return binsText[binsText.length - 1];
   },

   /**
    * Returns the textual representation of the bins for the chart axis, e.g. <100000, 20000-30000
    */
   bins2text : function(BINS) {
      var BINS_TEXT = [];

      for (var i = 0; i < BINS.length; i++) {
         var bin = "";
         var val = BINS[i];
         var prevVal = "";

         if ( i > 0 ) {
            prevVal = BINS[i - 1];
         }

         if ( i === 0 ) {
            bin = "<=" + this.formatNumberComma( val );
         } else if ( i == BINS.length - 1 ) {
            bin = this.formatNumberComma( prevVal + 1 ) + "-" + this.formatNumberComma( val );
            BINS_TEXT.push( bin );
            bin = ">=" + this.formatNumberComma( val + 1 );
         } else {
            bin = this.formatNumberComma( prevVal + 1 ) + "-" + this.formatNumberComma( val );
         }
         BINS_TEXT.push( bin );
      }
      return BINS_TEXT;
   },

   formatNumberComma : function(num) {
      return Ext.util.Format.number( num, "0,000" );
   },

   /**
    * Use for numerical data types, bin the data first and plot the frequencies
    * 
    */
   calculateBinFrequencies : function(data, columnName, countColumnName, bins, binsText) {

      var map = new Ext.util.HashMap();

      if ( bins === null ) {
         bins = Array.apply( 0, Array( 20 ) ).map( function(val, i) {
            return 10000 * (i + 1);
         } );
         binsText = this.bins2text( bins );
      }

      // initialize map with all the bins
      for (var i = 0; i < binsText.length; i++) {
         map.add( binsText[i], 0 );
      }

      for (var j = 0; j < data.length; j++) {

         var val = data[j];

         // bin the value
         var bin = this.findBin( val, bins, binsText );

         // now lets calculate and store it!
         var count = map.get( bin );
         count++;
         map.add( bin, count );
      }

      // a collection of freq objects
      var ret = [];
      map.each( function(key, value, length) {
         // console.log( key, value, length );
         var freq = {};
         freq[columnName] = key;
         freq[countColumnName] = value;
         ret.push( freq );
      } );

      return ret;
   },

   /**
    * @param store
    *           Array of Variant objects,
    * 
    * e.g. [ Object{"id":"var1", "type":"LOSS" }, Object{"id":"var2", "type":"LOSS" }, ]
    * 
    * @param columnName
    *           input column to extract, e.g. 'type'
    * @param countColumnName
    *           output column name to store the frequency in, e.g. 'count'
    * @returns e.g. { "type" : "LOSS", "count" : 2 }
    */
   calculateFrequencies : function(data, columnName, countColumnName) {
      var map = new Ext.util.HashMap();

      for (var i = 0; i < data.length; i++) {

         var val = data[i];

         // now lets calculate and store it!
         if ( map.get( val ) === undefined ) {
            map.add( val, 1 );
         } else {
            var count = map.get( val );
            count++;
            map.add( val, count );
         }
      }

      // a collection of freq objects
      var ret = [];
      map.each( function(key, value, length) {
         // console.log(key, value, length);
         var freq = {};
         freq[columnName] = key;
         freq[countColumnName] = value;
         ret.push( freq );
      } );

      return ret;
   },

   getColumnDataFromArray : function(data, columnName) {
      var result = [];
      var errorCount = 0;
      for (var i = 0; i < data.length; i++) {

         // extract values
         var row = data[i];
         var val = row[columnName];

         // special case for VariantValueObjects
         if ( columnName === "chromosome" ) {
            val = row['genomicRange'][columnName];
         }

         if ( val == undefined ) {

            // possibly a characteristic ..., special case for VariantValueObject
            var characteristic = row['characteristics'][columnName];

            if ( characteristic == undefined ) {
               // only print a few error messages
               if ( errorCount++ < 1 ) {
                  console.log( "Error: Attribute '" + columnName + "' not found in variant " + row.genomeCoordinates
                     + " of patient " + row.patientId + " " );
               }
               continue;   
            } else {
               val = characteristic['value'];
            }
         }

         // For CNV specific attributes like type and length, ignore SNVs
         if ( !ASPIREdb.view.report.VariantReportWindow.isVariantTypeAndReportFieldCompatible( columnName,
            row["variantType"] ) ) {
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

   saveAsPNG : function() {
      this.down( '#variantChart' ).save( {
         type : 'image/png'
      } );
   },

   /**
    * Display the data in a column chart series
    * 
    * mergedFreqData = '[{"type":"LOSS","asd1":136, "asd2":200},{"type":"GAIN","asd1":97, "asd2":100}]' columnName =
    * "type" labelNames = ["asd1","asd2"]
    */
   createLabelReport : function(mergedFreqData, columnName, labelNames) {
      var me = this;

      var countColumnName = '# of variants';

      var seriesTitle = labelNames;

      // get total counts for each label
      var totals = {};
      mergedFreqData.forEach( function(o) {
         for ( var label in o) {
            if ( label === columnName ) {
               continue;
            }
            if ( totals[label] === undefined ) {
               totals[label] = o[label];
            } else {
               totals[label] += o[label];
            }
         }
      } );

      var title = 'Project: ' + ASPIREdb.ActiveProjectSettings.getActiveProjectName();
      var varCountsText = "# of variants: " + Ext.util.JSON.encode( totals ).replace( '{', '' ).replace( '}', '' ).replace( /,/g, ', ' )
            .replace( /"/g, '' ).replace( /:/g, ' ' );
      var xField = columnName;
      var yField = seriesTitle;

      var fields = [ columnName ].concat( seriesTitle ); // insert "type" into the first position

      // xField = "type"
      // yField = "withVar"
      // fields = ["type", "withVar"]
      // mergedFreqData = { { type : "LOSS", withVar : 68}, { type : "GAIN", withVar : 40 } }

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

   /**
    * Adds the contents of freqData to mergedFreqData
    * 
    * freqData = "[{"type":"LOSS","withVar":68},{"type":"GAIN","withVar":48}]" freqData =
    * "[{"type":"LOSS","label2":20},{"type":"GAIN","label2":10}]"
    * 
    * mergedFreqData = "[{"type":"LOSS","withVar":68,"label2":20},{"type":"GAIN","withVar":48,"label2":10}]"
    */
   addFreqData : function(freqData, mergedFreqData) {
      for (var i = 0; i < freqData.length; i++) {
         var ele = freqData[i];
         var mEle = mergedFreqData[i];

         if ( mEle === undefined ) {
            mergedFreqData.push( ele );
            continue;
         }

         for ( var attr in ele) {
            var val = ele[attr];

            var mVal = mEle[attr];

            if ( mVal === undefined ) {
               mEle[attr] = val;
            }
         }
      }

      return mergedFreqData;
   },

   createFreqData : function(variantsByLabel, labelName, columnName, bins, binsText) {
      var me = this;

      var data = variantsByLabel[labelName];

      var freqData = null;
      var rawData = me.getColumnDataFromArray( data, columnName );

      if ( rawData.length === 0 ) {
         console.log( "Could not extract '" + columnName + "' from data" );
         return null;
      }

      var countColumnName = labelName;

      if ( bins !== null ) {
         freqData = me.calculateBinFrequencies( rawData, columnName, countColumnName, bins, binsText );
      } else {

         freqData = me.calculateFrequencies( rawData, columnName, countColumnName );
      }

      return freqData;
   },

   isHistogramType : function(data) {
      
      return ( !isNaN(Number(data[0])) && Ext.Array.unique(data).length >= 10 ); 
      
   },

   generateBinsLogTransform : function(data, logbase) {

      var start = 5;
      var bins = Array.apply( start, Array( this.N_BINS ) ).map( function(val, i) {
         return Math.pow(logbase, start + i);
      } );
      
      return bins;
   },
   
   generateBins : function(data) {
      var binSize = this.N_BINS;
      var bins = Array( binSize );
      
      data = data.sort( function(a, b) {
         return Number(a) - Number(b);
      } );
      
      var unique = [];
      
      data.forEach( function(e) {
         if ( unique.indexOf( e ) == -1 && e !== undefined )
            unique.push( Number(e) );
      } );
      
      data = unique;
      var dataMid = data[Math.floor( (data.length - 1) / 2 )];
      var binWidth = Math.ceil((data[Math.floor( (data.length - 1) * 3 / 4 )] - dataMid) / binSize / 2);
      
      
      for (var i = 0; i < binSize; i++) {
         bins[i] = i - Math.floor( binSize / 2 );
      }

      bins.forEach( function(e, i) {
         bins[i] = e * binWidth + dataMid;
      } );
      
      if ( bins[0] < 0 ) {
         bins.forEach( function(e, i) {
            bins[i] = i * binWidth + Ext.Array.min(data);
         } );
      }
      
//      console.log("dataMid="+dataMid+"; binWidth="+binWidth+"; bins[0]=" + bins[0] + "; bins[last]="+bins[bins.length-1]);
      
      return bins;
   },

   createReport : function(store, columnName) {

      var me = this;
      var reportWindow = me.up( '#variantReportWindow' );

      reportWindow.setLoading( true );

      // get a list of variants grouped by Subject labels
      // var variantIds = this.getColumnDataFromStore( store, 'id' );
      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore( store, 'id' );

      VariantService.groupVariantsBySubjectLabels( variantIds, {
         callback : function(variantsByLabel) {

            var mergedFreqData = [];
            var labelNames = [];

            // special case for histogram, gather all data and generate bins
            var allData = [];
            var bins = null;
            var binsText = null;
            var logbase = 2;
            
            Object.keys( variantsByLabel ).forEach( function(labelName) {
               var vals = me.getColumnDataFromArray( variantsByLabel[labelName], columnName );
               allData = allData.concat(vals);
            } );
            
            if ( me.isHistogramType( allData ) ) {
               if ( !me.logTransform ) {
                  bins = me.generateBins( allData );
               } else {
                  bins = me.generateBinsLogTransform( allData, logbase );
               }
               binsText = me.bins2text( bins );
            }

            for ( var labelName in variantsByLabel) {
               labelNames.push( labelName );

               var freqData = me.createFreqData( variantsByLabel, labelName, columnName, bins, binsText );
               if ( freqData === null ) {
                  continue;
               }

               me.addFreqData( freqData, mergedFreqData );
            }

            me.createLabelReport( mergedFreqData, columnName, labelNames );

            reportWindow.setLoading( false );
         },
         errorHandler : function(message, exception) {
            Ext.Msg.alert( 'Error', message );
            console.log( message );
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) );

            reportWindow.setLoading( false );
         }
      } );

   },

} );
