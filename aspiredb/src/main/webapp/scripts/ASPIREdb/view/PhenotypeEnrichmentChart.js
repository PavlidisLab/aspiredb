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
 * Part of Burden Analysis that shows the number of subjects across phenotypes.
 */
Ext.define( 'ASPIREdb.view.PhenotypeEnrichmentChart', {
   extend : 'Ext.chart.Chart',
   alias : 'widget.phenotypeEnrichmentChart',

   id : 'variantChart',
   width : '100%',
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

   yField : [ 'inGroup', 'outGroup' ],
   xField : 'name',

   store : Ext.create( 'Ext.data.JsonStore', {
      storeId : 'phenotypeEnrichmentChartStore',
      sortInfo : {
         field : 'pValue',
         direction : 'ASC'// or 'DESC' (case sensitive for local sorting)
      },
      fields : [ {
         name : 'name',
         type : 'string',
      }, {
         name : 'inGroup',
         type : 'numeric',
         minimum : 0.0,
         maximum : 100.0,
      }, {
         name : 'outGroup',
         type : 'numeric',
         minimum : 0.0,
         maximum : 100.0,
      }, {
         name : 'inGroupStr',
         type : 'string'
      }, {
         name : 'outGroupStr',
         type : 'string'
      }, {
         name : 'pValue',
         type : 'numeric'
      }, {
         name : 'qValue',
         type : 'numeric'
      } ],
   } ),

   insetPadding : 50,
   items : [ {
      type : 'text',
      text : 'ASPIREdb',
      font : '12px Helvetica',
      x : 12,
      y : 380
   }, ],
   axes : [ {
      type : 'numeric',
      position : 'left',
      fields : [ 'inGroup', 'outGroup' ], // yField
      grid : true,
      minimum : 0,
      title : "Percentage of phenotypes",
      label : {
         renderer : function(v) {
            return v + '%';
         }
      }
   }, {
      type : 'category',
      position : 'bottom',
      fields : [ 'name' ], // xField
      title : "Phenotypes",
      grid : true,
      label : {
         rotate : {
            degrees : -90
         },
         renderer : function(v) {
            return v.substring( 0, 25 ) + "..."
         }
      }
   } ],
   series : [ {
      type : 'column',
      axis : 'left',
      xField : [ 'name' ],
      yField : [ 'inGroup', 'outGroup' ],
      title : [ "Group 1", "Group 2" ],
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
            var fractionStr = storeItem.get( 'outGroupStr' );
            if ( label === "Group 1" ) {
               fractionStr = storeItem.get( 'inGroupStr' );
            }
            var msg = storeItem.get( item.series.xField ) + '<br>' + label + ': '
               + storeItem.get( item.yField ).toPrecision( 4 ) + '% (' + fractionStr + ')' + '<br>p-value: '
               + storeItem.get( 'pValue' ).toPrecision( 4 ) + '<br>q-value: '
               + storeItem.get( 'qValue' ).toPrecision( 4 );
            this.update( msg );
         }
      }
   } ],

   initComponent : function() {
      this.callParent();
   },

} );
