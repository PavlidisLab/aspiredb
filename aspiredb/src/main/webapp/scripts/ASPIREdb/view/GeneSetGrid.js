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

Ext.require([ 'Ext.grid.Panel', 'ASPIREdb.store.GeneSetStore','ASPIREdb.TextDataDownloadWindow' ]);

// TODO js documentation
Ext.define('ASPIREdb.view.GeneSetGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.geneSetGrid',
	emptyText : 'No genes found',
	id : 'geneSetGrid',
	border: true,
	store : Ext.create('ASPIREdb.store.GeneSetStore'),
		
	config:{
		// collection of all the PhenotypeSummaryValueObject loaded
		LoadedGeneSetNames : [],
		//collection of selected gene value objects
		selGeneSet : {},	
	},

	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'geneSetGridToolbar',
		dock : 'top'
		}],
		
	columns : [{
	            
			header : 'Name',
			dataIndex : 'geneSetName',
			flex : 1     		
	    },
	    {
	        header : 'Description',
	        dataIndex : 'geneDescription',
	        flex : 1
	    },
	    {
	       	header : 'size',
	        dataIndex : 'geneSetSize',
	        flex : 1	       	
	}],	
	
	selModel : Ext.create('Ext.selection.CheckboxModel', {
		mode: 'SINGLE',
	}),
	

	

	initComponent : function() {
		
		this.callParent();
		this.on('select', this.geneSetSelectHandler, this);	
	
	},
	
	
	geneSetSelectHandler : function(ref, record, index, eOpts) {
		
		this.selGeneSet = this.getSelectionModel().getSelection();
		var geneSetName= this.selGeneSet[0].data.geneSetName;		
		this.loadGenesForGeneSet(geneSetName);

	},
	
	loadGenesForGeneSet: function(geneSetName){
		var me=this;
		//TODO: This DWR is returning the null objects even though java is returning the correct objects
		UserGeneSetService.loadUserGeneSet(geneSetName, {
				callback : function(gvos) {
					
					me.populateGeneGrid(gvos);	
				}
			});
	},
	
	//GeneValueObject
	populateGeneGrid : function(gvos) {
		
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var grid =panel.down ('#geneGrid');
		
			
		var data = [];
		for ( var i = 0; i < gvos.length; i++) {
			var gvo = gvos[i];
			var row = [ gvo.symbol,gvo.name];		
			data.push(row);					
		}
			
		grid.store.loadData(data);
		grid.setLoading(false);		
		grid.getView().refresh();

	},	
	
	
	enableToolbar : function(names) {
		
		this.getDockedComponent('geneSetGridToolbar').remove('addGeneset');
		this.getDockedComponent('geneSetGridToolbar').remove('editGeneset');
		this.getDockedComponent('geneSetGridToolbar').remove('removeGeneset');
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'addGeneset',
			text : 'Add',
			tooltip : 'Add new gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
				//for testing [purpose
				var genevalueObject={symbol: "MBP", geneBioType: "protein_coding", name: "myelin basic protein", linkToGemma: "http://chibi.ubc.ca/Gemma/gene/showGene.html?name=MBP&taxon=human"};
				ASPIREdb.view.SaveUserGeneSetWindow.initAndShow(genevalueObject);
								
			}
		});
		
		this.getDockedComponent('geneSetGridToolbar').add('-');
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'editGeneset',
			text : 'Edit',
			tooltip : 'Edit new gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
				//
								
			}
		});
		
		this.getDockedComponent('geneSetGridToolbar').add('-');
		var ref=this;
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'removeGeneset',
			text : 'Delete',
			tooltip : 'Remove the selected gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/delete.png',
			handler: function(){
				//Delete gene set
				UserGeneSetService.deleteUserGeneSet(ref.selGeneSet[0].data.geneSetName, {
					callback : function() {
						var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
						var grid =panel.down ('#geneSetGrid');
						grid.getView().refresh();
						console.log('selected geneset :'+ref.selGeneSet[0].data.geneSetName+' deleted')
					}
				});
			}
		});
	
	}
});
