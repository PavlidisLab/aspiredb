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

Ext.require([ 'Ext.grid.Panel', 'ASPIREdb.store.GeneSetStore','ASPIREdb.TextDataDownloadWindow', 'Ext.grid.*', 'Ext.data.*' ]);

var rowEditing = Ext.create('Ext.grid.plugin.RowEditing',{
	clicksToEdit: 2,
	clicksToMoveEditor : 1,
	
	});

// TODO js documentation
Ext.define('ASPIREdb.view.GeneSetGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.geneSetGrid',
	emptyText : 'No gene sets found',
	id : 'geneSetGrid',
	plugins: [rowEditing],
	border: true,
	editing:true,
	
	store : Ext.create('ASPIREdb.store.GeneSetStore'),
		
	config:{
		// collection of all the PhenotypeSummaryValueObject loaded
		LoadedGeneSetNames : [],
		//collection of selected gene set value objects
		selGeneSet : [],	
		geneValueObjects : [],
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
	

	initComponent : function() {
		
		this.callParent();
		this.on('select', this.geneSetSelectHandler, this);	
		ASPIREdb.EVENT_BUS.on('gene_added', this.geneAddedHandler, this);
		//ASPIREdb.EVENT_BUS.on('new_geneSet_saved', this.updateGeneSetGridHandler, this);		
	
	},
	
	updateGeneSetGridHandler : function(geneSetName){
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var geneGrid = panel.down ('#geneSetGrid');
		//TODO : refresh grid when loaded
		
		this.store.add(geneSetName);
		this.getView().refresh(true);
		this.setLoading(false);
	},
	
	
	geneAddedHandler : function (gvo){
		this.geneValueObjects.push(gvo);
		
	},
	
	geneSetSelectHandler : function(ref, record, index, eOpts) {
		var me=this;
		this.selGeneSet = this.getSelectionModel().getSelection();
		var geneSetName= this.selGeneSet[0].data.geneSetName;		
		//TODO: This DWR is returning the null objects even though java is returning the correct objects
		UserGeneSetService.loadUserGeneSet(geneSetName, {
				callback : function(gvos) {
					
					me.populateGeneGrid(gvos);	
				}
			});
		ASPIREdb.EVENT_BUS.fireEvent('geneSet_selected', this.selGeneSet);
		
	},
	
		
	//GeneValueObject
	populateGeneGrid : function(gvos) {
		
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var grid =panel.down ('#geneGrid');
		
			
		var data = [];
		for ( var i = 0; i < gvos.length; i++) {
			var gvo = gvos[i];
			var row = [ gvo.symbol,'',gvo.name,''];		
			data.push(row);					
		}
			
		grid.store.loadData(data);
		grid.setLoading(false);		
		grid.getView().refresh();

	},	
	
	
	enableToolbar : function() {
		
	
		this.getDockedComponent('geneSetGridToolbar').removeAll();
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'textfield',
			id : 'geneSetName',
			text : '',
			scope: this,
			allowBlank : false,
			
		});
		
		
		//this.getDockedComponent('geneSetGridToolbar').add('-');
			
		var ref=this;
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'addGeneset',
			text : '',
			tooltip : 'Add new gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
			
				var newGeneSetName =ref.down('#geneSetName').getValue();
				
				geneValueObjects =[];
				geneValueObjects.push(new GeneValueObject());
				UserGeneSetService.saveUserGeneSet(newGeneSetName, geneValueObjects, {				
						callback : function(gvoId) {
							var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
							var geneSetGrid = panel.down ('#geneSetGrid');
							//add gene set name to geneset grid
							var data = [];
							var row = [newGeneSetName,'',''];		
							data.push(row);
							geneSetGrid.store.add(data);
							geneSetGrid.getView().refresh(true);
							geneSetGrid.setLoading(false);
							
							var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
							var grid =panel.down ('#geneGrid');
							grid.store.removeAll(true);
							ref.down('#geneSetName').setValue('');
							console.log('returned gene value object : '+gvoId);
							ASPIREdb.view.SaveUserGeneSetWindow.fireEvent('new_geneSet_saved');
						}
				});
		
			}
		});
				
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'removeGeneset',
			text : '',
			tooltip : 'Remove the selected gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/delete.png',
			handler: function(){
				//Delete gene set
				UserGeneSetService.deleteUserGeneSet(ref.selGeneSet[0].data.geneSetName, {
					callback : function() {
						var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
						var geneSetGrid = panel.down ('#geneSetGrid');
						var selection = geneSetGrid.getView().getSelectionModel().getSelection()[0];
	                    if (selection) {
	                    	geneSetGrid.store.remove(selection);
	                    }
						
						console.log('selected geneset :'+ref.selGeneSet[0].data.geneSetName+' deleted')
					}
				});
				
			}
		});
		
		this.getSelectionModel().on('selectionchange', function(selModel, selections){
	        this.down('#removeGeneset').setDisabled(selections.length === 0);
	    });
		
	
	}
});
