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

   initComponent : function() {
      this.callParent();
   },

   isVariantTypeAndReportFieldCompatible : function( columnName, variantType ) {
      if ( variantType !== "CNV" ) {
         if ( columnName === "type" || columnName === "cnvLength" ) {
            return false;
         }
      }
      
      return true;
   },
   
   /**
    * Return the value where the bin is found
    */
   findBin : function(val, BINS, BINS_TEXT) {
      for (var i = 0; i < BINS.length; i++) {
         if ( (BINS[i] - val) >= 0 ) {
//            console.log( val + " is in bin " + BINS_TEXT[i] );
            return BINS_TEXT[i];
         }
      }

      // it must be the last bin
//      console.log( val + " is in bin " + BINS_TEXT[BINS_TEXT.length - 1] );
      return BINS_TEXT[BINS_TEXT.length - 1];
   },

   /**
    * Returns the textual representation of the bins for the chart axis, e.g. <100000, 20000-30000
    */
   bins2text : function(BINS) {
      var BINS_TEXT = [];
      for (var i = 0; i < BINS.length; i++) {
         var bin = "";
         if ( i == 0 ) {
            bin = "<=" + BINS[i];
         } else if ( i == BINS.length - 1 ) {
            bin = (BINS[i - 1] + 1) + "-" + BINS[i];
            BINS_TEXT.push( bin );
            bin = ">=" + ( BINS[i] + 1 );
         } else {
            bin = (BINS[i - 1] + 1) + "-" + BINS[i];
         }
         BINS_TEXT.push( bin );
      }
      return BINS_TEXT;
   },

   /**
    * Use for numerical data types, bin the data first and plot the frequencies
    * 
    */
   calculateBinFrequencies : function(data, columnName, countColumnName) {

      var map = new Ext.util.HashMap();

      // setup bins
      var BINS = Array.apply( 0, Array( 20 ) ).map( function(val, i) {
         return 10000 * (i + 1);
      } );
      var BINS_TEXT = this.bins2text( BINS );

      // initialize map with all the bins
      for (var i = 0; i < BINS_TEXT.length; i++) {
         map.add( BINS_TEXT[i], 0 );
      }

      for (var i = 0; i < data.length; i++) {

         var val = data[i];
         
         // bin the value
         var bin = this.findBin( val, BINS, BINS_TEXT );

         // now lets calculate and store it!
         var count = map.get( bin );
         count++;
         map.add( bin, count );
      }

      // a collection of freq objects
      var data = [];
      map.each( function(key, value, length) {
         // console.log( key, value, length );
         var freq = {}
         freq[columnName] = key;
         freq[countColumnName] = value;
         data.push( freq );
      } );

      return data;
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
         if ( map.get( val ) == undefined ) {
            map.add( val, 1 );
         } else {
            var count = map.get( val );
            count++;
            map.add( val, count );
         }
      }

      // a collection of freq objects
      var data = [];
      map.each( function(key, value, length) {
         // console.log(key, value, length);
         var freq = {}
         freq[columnName] = key;
         freq[countColumnName] = value;
         data.push( freq );
      } );

      return data;
   },

   getColumnDataFromArray : function(data, columnName) {
      var result = [];
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
               console.log("Error: Attribute '" + columnName + "' not found in " + row );
               continue;
            } else {
               val = characteristic['value'];
            }
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
   
   /**
    * Display the data in a column chart series
    * 
    * mergedFreqData = '[{"type":"LOSS","asd1":136, "asd2":200},{"type":"GAIN","asd1":97, "asd2":100}]'
    * columnName = "type"
    * labelNames = ["asd1","asd2"]
    */
   createLabelReport : function(mergedFreqData, columnName, labelNames) {
      var me = this;
      
      var countColumnName = 'count';
      
      var seriesTitle = labelNames;
      
      var title = ASPIREdb.ActiveProjectSettings.getActiveProjectName();
      var xField = columnName;
      var yField = seriesTitle;
      
      var fields = [columnName].concat(seriesTitle); // insert "type" into the first position
     
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
         padding : '10 0 0 0',
         animate : true,
         shadow : false,
         layout : 'fit',
         style : 'background: #fff;',
         legend : {
             position : 'bottom',
             boxStrokeWidth : 0,
             labelFont : '12px Helvetica'
         },
         store : myDataStore,
         insetPadding : 40,
         items : [ {
            type : 'text',
            text : title,
            font : '20px Helvetica',
            width : 100,
            height : 30,
            x : 40, // the sprite x position
            y : 12
         // the sprite y position
         }, {
            type : 'text',
            text : 'ASPIREdb',
            font : '10px Helvetica',
            x : 12,
            y : 380
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
                  var label = item.series.title[Ext.Array.indexOf(item.series.yField, item.yField)];
                  var msg = label + " " +  storeItem.get( xField ) + ': ' + storeItem.get( item.yField );
                  this.update( msg );
               }
            }
         } ]
      } ] );

      me.doLayout();
      me.show();

   },
   
   /**
    * Adds the contents of freqData to mergedFreqData
    * 
    * freqData = "[{"type":"LOSS","withVar":68},{"type":"GAIN","withVar":48}]"
    * freqData = "[{"type":"LOSS","label2":20},{"type":"GAIN","label2":10}]"
    * 
    * mergedFreqData = "[{"type":"LOSS","withVar":68,"label2":20},{"type":"GAIN","withVar":48,"label2":10}]"
    */
   addFreqData : function( freqData, mergedFreqData ) {
      for ( var i = 0; i < freqData.length; i++ ) {
         var ele = freqData[i];
         var mEle = mergedFreqData[i];
         
         if ( mEle == undefined ) {
            mergedFreqData.push(ele);
            continue;
         }
         
         for ( var attr in ele ) {
            var val = ele[attr];
            
            var mVal = mEle[attr];
            
            if ( mVal == undefined) {
               mEle[attr] = val;
            }
         }
      }
      
      return mergedFreqData;
   },
   
   createReport : function(store, columnName) {

      var me = this;
      var reportWindow = me.up('#variantReportWindow');
      
      reportWindow.setLoading(true);
      
      // get a list of variants grouped by Subject labels
      var variantIds = this.getColumnDataFromStore( store, 'id' );
      VariantService.groupVariantsBySubjectLabels(variantIds,{
         callback: function(variantsByLabel) {
            
            var mergedFreqData = [];
            var labelNames = [];
            for ( var labelName in variantsByLabel ) {
                labelNames.push(labelName);
                var data = variantsByLabel[labelName];
                var rawData = me.getColumnDataFromArray( data, columnName );
                
                if ( rawData.length == 0 ) {
                   console.log("Could not extract '" + columnName + "' from data")
                   continue;
                }
                
                // columns that needs binning
                var COLUMN_TYPE_BIN = [ "cnvLength" ];
                
                var countColumnName = labelName;
                
                var freqData = null;
                if ( COLUMN_TYPE_BIN.indexOf( columnName ) != -1 ) {
                   freqData = me.calculateBinFrequencies( rawData, columnName, countColumnName );
                } else {
                   freqData = me.calculateFrequencies( rawData, columnName, countColumnName );
                }
                
                me.addFreqData(freqData, mergedFreqData);
                
            }
            
            me.createLabelReport(mergedFreqData, columnName, labelNames);
            
            reportWindow.setLoading(false);
         },
         errorHandler : function(message, exception) {
            Ext.Msg.alert( 'Error', message )
            console.log( message )
            console.log( dwr.util.toDescriptiveString(exception.stackTrace,3) )
            
            reportWindow.setLoading(false);
         }
      });
      
   },

} );
