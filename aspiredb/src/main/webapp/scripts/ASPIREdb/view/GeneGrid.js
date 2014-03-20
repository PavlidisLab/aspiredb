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

Ext.require([ 'Ext.grid.Panel', 'ASPIREdb.store.GeneStore', 'ASPIREdb.TextDataDownloadWindow' ]);

// TODO js documentation
Ext.define('ASPIREdb.view.GeneGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.geneGrid',
	emptyText : 'No genes found',
	id : 'geneGrid',
	border: true,
	store : Ext.create('ASPIREdb.store.GeneStore'),
	
	config:{
		// collection of all the PhenotypeSummaryValueObject loaded
		LoadedGeneSetNames : [],
		//collection of selected gene value objects
		selectedgenes :[],	
		gvos : [],
	},

	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'geneSetGridToolbar',
		dock : 'top'
		}],

	columns : [ 
	            {
	            	header : 'Gene Symbol',
	            	dataIndex : 'symbol',
	            	flex : 1
	            }, {
	            	header : 'Gene Name',
	            	dataIndex : 'name',
	            	flex : 1
	            }
	],		
	

	

	initComponent : function() {
		this.callParent();
		var me = this;
		me.enableToolbar();

	},
	
	
	enableToolbar : function(names) {
		
		this.getDockedComponent('geneSetGridToolbar').remove('addGeneset');
		this.getDockedComponent('geneSetGridToolbar').remove('editGeneset');
		this.getDockedComponent('geneSetGridToolbar').remove('removeGeneset');
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'textfield',
			id : 'geneName',
			text : '',			
			//tooltip : 'Gene Names',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
				//TODO: have to populate human taxon gene list auto complete features
				//BioMartQueryService.getGenes(genesymbol),			
			}
		});
		
		
		this.getDockedComponent('geneSetGridToolbar').add('-');
		
		var ref=this;
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'addGene',
			text : '',
			tooltip : 'Add genes to selected gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
				//TODO: have to populate human taxon gene list auto complete features
				var genesymbol =ref.down('#geneName').getValue();
				UserGeneSetService.getGenes(genesymbol,{
						callback : function(gvo) {
							ref.gvos.push(gvo);
							var data = [];
							var row = [ gvo.symbol,'',gvo.name,''];		
							data.push(row);
							var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
							var grid =panel.down ('#geneGrid');
							//TODO : refresh grid when loaded
							grid.store.loadData(data);
							grid.setLoading(false);		
							grid.getView().refresh();
						}
				});
				
				ASPIREdb.EVENT_BUS.fireEvent('gene_added', ref.gvos);
			}
		});
				
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'removeGene',
			text : '',
			tooltip : 'Remove the selected gene',
			icon:'scripts/ASPIREdb/resources/images/icons/delete.png',
			handler: function(){
				//remove the selected gene from the gene set
				
				//TODO : refresh grid when loaded
				ref.store.getView.refresh();				
			}
		});
	
	}
});
