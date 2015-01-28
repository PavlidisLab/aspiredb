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

Ext.require([ 'Ext.Window', 'ASPIREdb.view.PhenotypeEnrichmentGrid' ]);

Ext.define('ASPIREdb.view.PhenotypeEnrichmentWindow', {
	extend : 'Ext.Window',
	alias : 'widget.phenotypeEnrichmentWindow',
	singleton : true,
	title : 'Phenotype Enrichment',
	closable : true,
	closeAction : 'hide',
	width : 800,
	height : 500,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',

	items : [ {
		xtype : 'phenotypeEnrichmentGrid',
		itemId : 'phenotypeEnrichmentGrid'
	} ],

	dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'phenotypeEnrichmentGridToolbar',
      dock : 'top',
      items : [ {
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
                  
      }
      );

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

	},
	
	clearGrid : function(){
		
		var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.down('#phenotypeEnrichmentGrid');
		
		grid.getStore().removeAll();		
		
	}

});