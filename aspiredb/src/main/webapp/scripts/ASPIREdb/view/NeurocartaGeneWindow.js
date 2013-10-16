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

Ext.require([ 'Ext.Window', 'ASPIREdb.view.NeurocartaGeneGrid','ASPIREdb.GemmaURLUtils' ]);

Ext.define('ASPIREdb.view.NeurocartaGeneWindow', {
	extend : 'Ext.Window',
	alias : 'widget.neurocartaGeneWindow',
	singleton : true,
	title : 'Genes associated with phenotype in Neurocarta',
	closable : true,
	closeAction : 'hide',
	width : 800,
	height : 500,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',

	items : [ {
		xtype : 'neurocartaGeneGrid',
		itemId : 'neurocartaGeneGrid'
	} ],

	initComponent : function() {
		
		this.callParent();

	},
	
	initGridAndShow : function(uri){
			
		var ref = this;
		
		var grid = ASPIREdb.view.NeurocartaGeneWindow.getComponent('neurocartaGeneGrid');
		
		ref.show();
		grid.setLoading(true);
		
		var ontologyPrefix = "http://purl.obolibrary.org/obo/";
	
		GeneService.findGenesWithNeurocartaPhenotype( ontologyPrefix + uri, function(vos){
			
			ref.populateGrid(vos, uri);
			grid.setLoading(false);
			
		});		
		
	},

	//GeneValueObject
	populateGrid : function(vos,uri) {		
		
		var grid = ASPIREdb.view.NeurocartaGeneWindow.getComponent('neurocartaGeneGrid');
		
		var data = [];
		for ( var i = 0; i < vos.length; i++) {
			var vo = vos[i];
			
			var linkToGemma = "";
			
			if (vo.geneBioType == "protein_coding"){
				linkToGemma = ASPIREdb.GemmaURLUtils.makeGeneUrl(vo.symbol);
			}
			

			var row = [ vo.symbol, vo.geneBioType, vo.name, linkToGemma ];
			data.push(row);
		}

		grid.store.loadData(data);
		grid.setLoading(false);
		
		grid.enableToolbar(vos,uri);

	},
	
	clearGridAndMask : function(){
		ASPIREdb.view.NeurocartaGeneWindow.getComponent('neurocartaGeneGrid').store.removeAll();
		ASPIREdb.view.NeurocartaGeneWindow.getComponent('neurocartaGeneGrid').setLoading(true);				
	}

});