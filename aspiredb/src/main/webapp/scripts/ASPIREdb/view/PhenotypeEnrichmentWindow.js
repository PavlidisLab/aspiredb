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

Ext.require([ 'Ext.Window', 'ASPIREdb.view.PhenotypeEnrichmentGrid', 'ASPIREdb.view.PhenotypeEnrichmentChart' ]);

Ext.define('ASPIREdb.view.PhenotypeEnrichmentWindow', {
	extend : 'Ext.Window',
	alias : 'widget.phenotypeEnrichmentWindow',
	singleton : true,
	title : 'Phenotype Enrichment',
	closable : true,
	closeAction : 'hide',
	width : 900,
	height : 600,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',

	items : [   {
      xtype : 'phenotypeEnrichmentChart',
      itemId : 'phenotypeEnrichmentChart',
   }, {
		xtype : 'phenotypeEnrichmentGrid',
		itemId : 'phenotypeEnrichmentGrid',
	}, ],

	dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'phenotypeEnrichmentGridToolbar',
      dock : 'top',
      items : [ {
        xtype : 'button',
        text : 'Show table',
        itemId : 'showChartButton',
      }, {
         xtype : 'tbfill',
      }, {
         xtype : 'button',
         itemId : 'saveButton',
         text : '',
         tooltip : 'Download table contents as text',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'
      } ]

   } ],
   
	initComponent : function() {
		var ref = this;

		this.callParent();
		
      var saveButton = this.down('#phenotypeEnrichmentGridToolbar').down('#saveButton');
      saveButton.on('click', function(){
         ASPIREdb.TextDataDownloadWindow.showPhenotypeEnrichmentDownload(ref.valueObjects);           
      } );

      var showChartButton = this.down('#phenotypeEnrichmentGridToolbar').down('#showChartButton');
      showChartButton.on('click', function(){
         var showChart = showChartButton.text === "Show chart";
         if ( showChart ) {
            showChartButton.setText( "Show table" )
         } else {
            showChartButton.setText( "Show chart" )
         }
         ref.showChart(showChart);
      } );

	},

	populateGrid : function(vos) {
	   
	   var ref = this;

		var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentGrid');
		
		ref.valueObjects= vos;

		var data = [];
		for ( var i = 0; i < vos.length; i++) {
			var vo = vos[i];

			var row = [ vo.name, vo.inGroupTotalString, vo.outGroupTotalString, vo.PValueString, vo.PValueCorrectedString ];
			data.push(row);
		}		

		grid.store.loadData(data);
		grid.store.sort('pValue','ASC');

	},

	asFraction : function(fraction) {
      var tokens = fraction.split( '/' );
      if ( tokens.length != 2 ) {
         return fraction;
      }
      return parseInt( tokens[0] ) / parseInt( tokens[1] ) * 1.0;
   },
	
  populateChart : function(vos) {
      
      var ref = this;

      var chart = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentChart');
      
      ref.valueObjects= vos;

      var data = [];
      for ( var i = 0; i < vos.length; i++) {
         var vo = vos[i];

         //var row = [ vo.name, vo.inGroupTotalString, vo.outGroupTotalString, vo.PValueString, vo.PValueCorrectedString ];
         var row = { name : vo.name, 
            inGroup : this.asFraction( vo.inGroupTotalString ) * 100.0,
            outGroup : this.asFraction( vo.outGroupTotalString ) * 100.0,
            inGroupStr : vo.inGroupTotalString,
            outGroupStr : vo.outGroupTotalString,
            pValue : parseFloat(vo.PValueString), 
            qValue : parseFloat(vo.PValueCorrectedString)
            }
         data.push(row);
      }     

      chart.store.loadData(data);
      chart.store.sort('pValue','ASC');
   },
	   
	clearGrid : function(){
		
		var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentGrid');
		
		grid.getStore().removeAll();		
		
	},
	
	clearChart : function(){
      
      var chart = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentChart');
      
      chart.getStore().removeAll();     
      
   },
   
	showChart : function(isShow) {
	   var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentGrid');
	   var chart = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentChart');
      grid.setVisible(!isShow);
      chart.setVisible(isShow);
	}

});