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

Ext.require([ 'Ext.Window', 'ASPIREdb.view.GeneManagerGrid','ASPIREdb.GemmaURLUtils' ]);

Ext.define('ASPIREdb.view.GeneManagerWindow', {
	extend : 'Ext.Window',
	alias : 'widget.geneManagerWindow',
	singleton : true,
	title : 'Gene Manager',
	closable : true,
	closeAction : 'hide',
	width : 1000,
	height : 800,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',
	
	
	items : [ {
		xtype : 'geneManagerGrid',
		itemId : 'geneManagerGrid'
	} ],

	initComponent : function() {
		var ref = this;
		this.callParent();

	},
	
	initGridAndShow : function(ids){
		
		var ref = this;
		
		var grid = ASPIREdb.view.GeneManagerWindow.getComponent('geneManagerGrid');
		
		ref.show();
		grid.setLoading(true);
		
		GeneService.getGenesInsideVariants(ids, {
			callback : function(vos) {
				ASPIREdb.view.GeneManagerWindow.getComponent('geneManagerGrid').setLodedvariantvalueObjects(vos);
				
				ASPIREdb.view.GeneManagerWindow.populateGrid(vos);
				
				
			}
		});
		
	},
	
	

	//VariantValueObject
	populateGrid : function(vos) {		
		
		var grid = ASPIREdb.view.GeneManagerWindow.getComponent('geneManagerGrid');
		
		var data = [];
		for ( var i = 0; i < vos.length; i++) {
			var vo = vos[i];
			
			var linkToGemma = "";
			
			if (vo.geneBioType == "protein_coding"){
				linkToGemma = ASPIREdb.GemmaURLUtils.makeGeneUrl(vo.symbol);
				var row = [ vo.symbol, vo.geneBioType, vo.name, linkToGemma ];
				data.push(row);
			}
			
			
			
		}

		grid.store.loadData(data);
		grid.setLoading(false);
		
		grid.enableToolbar(vos);

	},	
	
	
	clearGridAndMask : function(){
		ASPIREdb.view.GeneManagerWindow.getComponent('geneManagerGrid').store.removeAll();
		ASPIREdb.view.GeneManagerWindow.getComponent('geneManagerGrid').setLoading(true);				
	}

});