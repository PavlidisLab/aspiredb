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

Ext.require([ 'Ext.grid.*', 'ASPIREdb.store.GeneStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.data.*','Ext.util.*', 'Ext.state.*', 'Ext.form.*' ,'ASPIREdb.GeneSuggestionStore','ASPIREdb.model.GeneProperty',
      		'ASPIREdb.model.PropertyValue']);

var rowEditing = Ext.create('Ext.grid.plugin.RowEditing', {
    //clicksToMoveEditor: 1,
	clicksToEdit: 2,
     autoCancel: false
});

/**
 * Create Gene Grid
 */
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
		selectedGene :[],	
		gvos : [],
		selectedGeneSet : [],
		suggestionContext: null,
		
	},

	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'geneGridToolbar',
		dock : 'top'
		}],

	columns : [ 
	            {
	            	header : 'Gene Symbol',
	            	dataIndex : 'symbol',
	            	flex : 1,
	            	editor: {
		                // defaults to textfield if no xtype is supplied
		                allowBlank: false
		            }
	            }, 
	            {
	            	header : 'Gene Name',
	            	dataIndex : 'name',
	            	flex : 1,
	            	editor: {
		                // defaults to textfield if no xtype is supplied
		                allowBlank: false
		            }
	            }
	],		
	
	plugins: [rowEditing],
	listeners: {
        'selectionchange': function(view, records) {
            this.down('#removeGene').setDisabled(!records.length);
            this.selectedGene=this.getSelectionModel().getSelection();
           // console.log('selected gene sumbol : '+this.selectedGene[0].data.symbol);
        }
    },
	

	initComponent : function() {
		this.callParent();
		var me = this;
		me.enableToolbar();
		
		ASPIREdb.EVENT_BUS.on('geneSet_selected', this.geneSetSelectHandler, this);
		
		
	},
	
	/**
	 * Store the gene value object when selected
	 * @param GeneSetValueObject selGeneSet
	 */
	geneSetSelectHandler : function(selGeneSet){
			this.selectedGeneSet= selGeneSet;		
	},
	
	/**
	 * Enable the tool bar in Gene grid
	 *  
	 */
	enableToolbar : function() {
		
		this.getDockedComponent('geneGridToolbar').removeAll();
				
		this.getDockedComponent('geneGridToolbar').add({
			xtype : 'combo',
			id : 'geneName',
			emptyText : 'Genes',			
			width :200,
			displayField : 'displayName',
			triggerAction : 'query',
			minChars : 0,
			matchFieldWidth : false,
			//hideTrigger : true,
			triggerAction : 'query',
			autoSelect : false,
			enableKeyEvents : true,
			store : Ext.create('ASPIREdb.GeneSuggestionStore', {
				remoteFunction : VariantService.suggestGeneValues
			}),			
			listConfig : {
				loadingText : 'Searching...',
				emptyText : 'No results found.',
				
			},
			listeners : {
				select : {
					fn : function(obj, records) {
						// ASPIREdb.EVENT_BUS.fireEvent('query_update');
	
					},
					scope : this
					,
				}
			},
	
		});
		
		
		this.getDockedComponent('geneGridToolbar').add('-');
		
		var ref=this;
		
		this.getDockedComponent('geneGridToolbar').add({
			xtype : 'button',
			id : 'addGene',
			text : '',
			tooltip : 'Add genes to selected gene set',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
				//TODO: have to populate human taxon gene list auto complete features
				var genesymbol =ref.down('#geneName').getValue();
				console.log('added genes name  : '+genesymbol);
				var geneSetName =ref.selectedGeneSet[0].data.geneSetName;
				var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
				var grid =panel.down ('#geneGrid');
				
				UserGeneSetService.isGeneInGeneSet(geneSetName,genesymbol,{
					callback : function(gvoSta) {
						if (gvoSta){
							Ext.Msg.alert('Gen Set','Gene already exist in gene set');
							grid.down('#geneName').setValue('');
						}else if (ref.selectedGeneSet[0]!=null){
							  UserGeneSetService.addGenes(geneSetName, genesymbol,{
								callback : function(geneName) {
									
									
									//ref.gvos.push(gvo);
									var data = [];
									var row = [ genesymbol,'',geneName,''];		
									data.push(row);
																			
									//TODO : refresh grid when loaded
									grid.store.add(data);
									grid.getView().refresh(true);
									grid.setLoading(false);
									grid.down('#geneName').setValue('');
									ASPIREdb.EVENT_BUS.fireEvent('gene_added', data);
															
									
								}
						    });
						}
						else  Ext.Msg.alert('Error','select the Gene Set Name to add Genes ');
						
					}
				});
				
				
				
				
			}
		});
				
		this.getDockedComponent('geneGridToolbar').add({
			xtype : 'button',
			id : 'removeGene',
			text : '',
			tooltip : 'Remove the selected gene',
			icon:'scripts/ASPIREdb/resources/images/icons/delete.png',
			handler: function(){
				var geneSymbol =ref.selectedGene[0].data.symbol;
				UserGeneSetService.deleteGene(ref.selectedGeneSet[0].data.geneSetName, geneSymbol, {				
					callback : function() {
						
						var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
						var geneGrid = panel.down ('#geneGrid');
				
						var selection = geneGrid.getView().getSelectionModel().getSelection()[0];
						if (selection) {
							geneGrid.store.remove(selection);
						}
						//console.log('selected gene :'+ref.selectedGene[0].data.symbol+' deleted');
					}
				
				});
			}
		});
		
				
		
	
	}
});
