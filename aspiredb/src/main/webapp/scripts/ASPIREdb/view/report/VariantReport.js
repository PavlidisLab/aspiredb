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
 * Create Gene Grid
 */
Ext.define( 'ASPIREdb.view.report.VariantReport', {
   extend : 'Ext.Panel',
   alias : 'widget.variantReport',
   id : 'variantReport',
   xtype : 'clustered-column',
   resizable : true,
   layout : 'fit',
   
   initComponent : function() {
      this.callParent();
   },

   
   createReport : function( grid ) {
      
      var me = this;

      // fix, convert grid to a summary store, something like this
      // data : [ { 'LOSS', frequency : 20 }, { 'GAIN', frequency : 30 }, { 'OTHER', frequency : 40 } ]
      
      this.myDataStore = Ext.create('Ext.data.JsonStore', {
         fields: ['month', 'data1', 'data2', 'data3', 'data4' ],
         data: [
             { month: 'Jan', data1: 20, data2: 37, data3: 35, data4: 4 },
             { month: 'Feb', data1: 20, data2: 37, data3: 36, data4: 5 },
             { month: 'Mar', data1: 19, data2: 36, data3: 37, data4: 4 },
             { month: 'Apr', data1: 18, data2: 36, data3: 38, data4: 5 },
             { month: 'May', data1: 18, data2: 35, data3: 39, data4: 4 },
             { month: 'Jun', data1: 17, data2: 34, data3: 42, data4: 4 },
             { month: 'Jul', data1: 16, data2: 34, data3: 43, data4: 4 },
             { month: 'Aug', data1: 16, data2: 33, data3: 44, data4: 4 },
             { month: 'Sep', data1: 16, data2: 32, data3: 44, data4: 4 },
             { month: 'Oct', data1: 16, data2: 32, data3: 45, data4: 4 },
             { month: 'Nov', data1: 15, data2: 31, data3: 46, data4: 4 },
             { month: 'Dec', data1: 15, data2: 31, data3: 47, data4: 4 }
         ]
     });
      
      me.add( [ {
         xtype : 'chart',
         id : 'variantChart',
         width : '100%',
         height : 410,
         padding : '10 0 0 0',
         animate : true,
         resizable : true,
         shadow : false,
         layout : 'fit',
         style : 'background: #fff;',
         legend : {
            position : 'bottom',
            boxStrokeWidth : 0,
            labelFont : '12px Helvetica'
         },
         store : this.myDataStore,
         insetPadding : 40,
         items : [ {
            type : 'text',
            text : 'Column Charts - Clustered Columns',
            font : '22px Helvetica',
            width : 100,
            height : 30,
            x : 40, // the sprite x position
            y : 12
         // the sprite y position
         }, {
            type : 'text',
            text : 'Data: Browser Stats 2012',
            font : '10px Helvetica',
            x : 12,
            y : 380
         }, {
            type : 'text',
            text : 'Source: http://www.w3schools.com/',
            font : '10px Helvetica',
            x : 12,
            y : 390
         } ],
         axes : [ {
            type : 'numeric',
            position : 'left',
            fields : 'data1',
            grid : true,
            minimum : 0,
            label : {
               renderer : function(v) {
                  return v + '%';
               }
            }
         }, {
            type : 'category',
            position : 'bottom',
            fields : 'month',
            grid : true,
            label : {
               rotate : {
                  degrees : -45
               }
            }
         } ],
         series : [ {
            type : 'column',
            axis : 'left',
            title : [ 'IE', 'Firefox', 'Chrome', 'Safari' ], // fix
            xField : 'month',                                // fix
            yField : [ 'data1', 'data2', 'data3', 'data4' ], // fix
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
                  var browser = item.series.title[Ext.Array.indexOf( item.series.yField, item.yField )];
                  var msg = browser + ' for ' + storeItem.get( 'month' ) + ': ' + storeItem.get( item.yField )
                  + '%';
                  this.update( msg );
               }
            }
         } ]
      } ] );

      me.doLayout();
      me.show();

   },

} );
