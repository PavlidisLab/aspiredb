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
   calculateBinFrequencies : function(store, columnName, countColumnName) {

      var map = new Ext.util.HashMap();
      var fields = [ columnName, countColumnName ];

      // setup bins
      var BINS = Array.apply( 0, Array( 20 ) ).map( function(val, i) {
         return 10000 * (i + 1);
      } );
      var BINS_TEXT = this.bins2text( BINS );

      // initialize map with all the bins
      for (var i = 0; i < BINS_TEXT.length; i++) {
         map.add( BINS_TEXT[i], 0 );
      }

      for (var i = 0; i < store.data.length; i++) {

         // extract values
         var row = store.data.getAt( i ).data;
         var val = row[columnName];
         if ( val == undefined ) {
            console.log( "Column '" + columnName + "' not found" );
            return;
         }

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

      // convert to Extjs Store
      var newStore = Ext.create( 'Ext.data.JsonStore', {
         storeId : 'reportStore',
         fields : fields,
         data : data,
      } );

      return newStore;
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
   calculateFrequencies : function(store, columnName, countColumnName) {
      var map = new Ext.util.HashMap();
      var fields = [ columnName, countColumnName ];

      for (var i = 0; i < store.data.length; i++) {

         // extract values
         var row = store.data.getAt( i ).data;
         var val = row[columnName];
         if ( val == undefined ) {
            console.log( "Column '" + columnName + "' not found" );
            return;
         }
         
         // show NA for empty values
         if ( val === "" ) {
             val = "NA";
         }

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

      // convert to Extjs Store
      var newStore = Ext.create( 'Ext.data.JsonStore', {
         storeId : 'reportStore',
         fields : fields,
         data : data,
      } );

      return newStore;
   },

   createReport : function(store, columnName) {

      var me = this;

      // var columnName = 'type';
      var countColumnName = 'count';
      var title = ASPIREdb.ActiveProjectSettings.getActiveProjectName();
      var xField = columnName;
      var yField = countColumnName;

      // columns that needs binning
      var COLUMN_TYPE_BIN = [ "cnvLength" ];
      var myDataStore = null;
      if ( COLUMN_TYPE_BIN.indexOf( columnName ) != -1 ) {
         myDataStore = this.calculateBinFrequencies( store, columnName, countColumnName );
      } else {
         myDataStore = this.calculateFrequencies( store, columnName, countColumnName );
      }

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
         // legend : {
         // position : 'bottom',
         // boxStrokeWidth : 0,
         // labelFont : '12px Helvetica'
         // },
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
                  var msg = storeItem.get( xField ) + ': ' + storeItem.get( item.yField );
                  this.update( msg );
               }
            }
         } ]
      } ] );

      me.doLayout();
      me.show();

   },

} );
