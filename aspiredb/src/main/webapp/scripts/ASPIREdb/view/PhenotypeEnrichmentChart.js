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

Ext.require([  ]);

// TODO js documentation
Ext.define('ASPIREdb.view.PhenotypeEnrichmentChart', {
	extend : 'Ext.chart.Chart',
	alias : 'widget.phenotypeEnrichmentChart',
	
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
   
   yField : ['inGroup', 'outGroup'],
   xField : 'name',
   
   store : Ext.create( 'Ext.data.JsonStore', {
      storeId : 'phenotypeEnrichmentChartStore',
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
      } ],
   } ),
      
   insetPadding : 50,
   items : [ 
    /*{
      type : 'text',
      text : 'Project: '
         + ASPIREdb.ActiveProjectSettings.getActiveProjectName(),
      font : '18px Helvetica',
      width : 100,
      height : 100,
      x : 40, // the sprite x position
      y : 10
   // the sprite y position
   },*/ 
   {
      type : 'text',
      text : 'ASPIREdb',
      font : '12px Helvetica',
      x : 12,
      y : 380
   }, /*{
      type : 'text',
      text : varCountsText,
      font : '12px Helvetica',
      x : 40,
      y : 340
   }*/
   ],
   axes : [ {
      type : 'numeric',
      position : 'left',
      fields : ['inGroup', 'outGroup'], // yField
      grid : true,
      minimum : 0,
      title : "Percentage of phenotypes",
      label : {
         renderer : function(v) {
            // return v + '%';
            return v;
         }
      }
   }, {
      type : 'category',
      position : 'bottom',
      fields : ['name'],   // xField
      title : "Phenotypes",
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
      xField : ['name'],
      yField : ['inGroup', 'outGroup'],
      title : ["In-group", "Out-group"],
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
            var msg = label + " " + storeItem.get( item.series.xField ) + ': ' + storeItem.get( item.yField );
            this.update( msg );
         }
      }
   } ],

	initComponent : function() {
		this.callParent();
	},
	
   /**
    * Display the data in a column chart series
    * 
    * mergedFreqData = '[{"type":"LOSS","asd1":136, "asd2":200},{"type":"GAIN","asd1":97, "asd2":100}]' columnName =
    * "type" labelNames = ["asd1","asd2"]
    */
  /* createChart : function(mergedFreqData, columnName, labelNames) {
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
            if ( totals[label] == undefined ) {
               totals[label] = o[label]
            } else {
               totals[label] += o[label]
            }
         }
      } )

      var title = 'Project: '
         + ASPIREdb.ActiveProjectSettings.getActiveProjectName();
      var varCountsText = "# of variants: " + Ext.util.JSON.encode( totals ).replace( '{', '' ).replace( '}', '' ).replace( /,/g, ', ' )
            .replace( /"/g, '' ).replace(/:/g,' ');
      var xField = columnName;
      var yField = seriesTitle;

      var fields = [ columnName ].concat( seriesTitle ); // insert "type" into the first position

      // convert to Extjs Store
      var myDataStore = Ext.create( 'Ext.data.JsonStore', {
         storeId : 'reportStore',
         fields : fields,
         data : mergedFreqData,
      } );

      me.add( [ {
         xtype : 'chart',
          } ] );

      me.doLayout();
      me.show();

   },
*/
	
});
