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

Ext.require([ 'Ext.grid.Panel', 'ASPIREdb.store.GeneStore', 'ASPIREdb.TextDataDownloadWindow','ASPIREdb.GemmaURLUtils', 'Ext.selection.CheckboxModel', 'ASPIREdb.view.SaveUserGeneSetWindow' ]);

// TODO js documentation
Ext.define('ASPIREdb.view.NeurocartaGeneGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.neurocartaGeneGrid',
	id : 'neurocartaGeneGrid',
	emptyText : 'No genes found',
	border: false,
	multiSelect : true,

	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'neurocartaGeneGridToolbar',
		dock : 'top'
	} ],

	columns : [ {
		header : 'Gene Symbol',
		dataIndex : 'symbol',
		flex : 1
	}, {
		header : 'Type',
		dataIndex : 'geneBioType',
		hidden: true,
		flex : 1
	}, {
		header : 'Gene Name',
		dataIndex : 'name',
		flex : 1
	}, {
		header : 'View in Gemma',
		dataIndex : 'linkToGemma',
		flex : 1,
		renderer : function(value) {

			if (value == '') {
				return;
			}

			return '<a href="' + value + '" target="_blank" > <img src="scripts/ASPIREdb/resources/images/gemmaTiny.gif" /> </a>';
		}
	} ],
	config :{
		selectedgenes :[],
	},
	
	selModel : Ext.create('Ext.selection.CheckboxModel', {
		mode: 'MULTI',		
    }),


	store : Ext.create('ASPIREdb.store.GeneStore'),

	initComponent : function() {
		this.callParent();
				
		var me = this;
		
		this.on('select', me.geneSelectHandler, me);
	},
	
	geneSelectHandler : function(ref, record, index, eOpts) {
		var selGenes = this.getSelectionModel().getSelection();
		this.selectedgenes=[];
		for (var i=0; i<selGenes.length; i++){
			this.selectedgenes.push(selGenes[i].data);
		}
		
		ASPIREdb.EVENT_BUS.fireEvent('new_geneSet_selected', this.selectedgenes);
		ASPIREdb.view.NeurocartaGeneWindow.down('#saveButtonGeneSet').enable();
	},
	
			
	enableToolbar : function(vos,uri) {

		if (vos.length < 1) {
			return;
		}

		var geneSymbols = [];

		for ( var i = 0; i < vos.length; i++) {
			var vo = vos[i];
			geneSymbols.push(vo.symbol);
		}

		var url = ASPIREdb.GemmaURLUtils.makeViewGeneNetworkInGemmaURL(geneSymbols);
		
		//This kind of weird technique is being used because the baked in extjs button href config way was not working
		var viewCoexpressionNetworkInGemmaLink = {
			
			xtype : 'box',
			itemId : 'viewCoexpressionNetworkLink',
			autoEl : {
				tag : 'a',
				href : url,
				target : '_blank',
				cn: 'View Coexpression Network in Gemma',
				/**children:[{
					tag:'img',
					src:'scripts/ASPIREdb/resources/images/gemmaTiny.gif',
					'ext:qtip': 'View Coexpression Network in Gemma',
				}]*/
				
			}
		};
		
		var neurocartaUrl = ASPIREdb.GemmaURLUtils.makeNeurocartaPhenotypeUrl(uri);
		
		var viewNeurocartaGenesLink = {
				
				xtype : 'box',
				itemId : 'viewNeurocartaGeneLink',
				autoEl : {
					tag : 'a',
					href : neurocartaUrl,
					target : '_blank',
					cn: 'View Phenocarta Phenotypes',
					/**
					children:[{
						tag:'img',
						src:'scripts/ASPIREdb/resources/images/icons/neurocarta.png',
						'ext:qtip': 'View Phenocarta Phenotypes'
					}]*/
				}
			};
		
		var toolbar = this.getDockedComponent('neurocartaGeneGridToolbar');
		
		toolbar.remove('viewCoexpressionNetworkLink');
		toolbar.remove('viewNeurocartaGeneLink');
		toolbar.remove('saveButtonGeneHits');
		toolbar.removeAll();
		
		toolbar.add(viewCoexpressionNetworkInGemmaLink);
		
		toolbar.add('-');
		toolbar.add(' ');
		
		toolbar.add(viewNeurocartaGenesLink);
		
		toolbar.add('-');
		toolbar.add(' ');
		
		var ref = this;
		
		this.getDockedComponent('neurocartaGeneGridToolbar').add({
			xtype : 'button',
			id : 'saveButtonGeneHits',
			text : '',
			tooltip : 'Download table contents as text',
			icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
			handler: function(){
				ASPIREdb.TextDataDownloadWindow.showGenesDownload(ref.getStore().getRange(), ['Gene Symbol', 'Type','Gene Name']);
			}
		});
		
		this.getDockedComponent('neurocartaGeneGridToolbar').add({
			xtype : 'button',
			id : 'saveButtonGeneSet',
			text : 'Save to Gene Lists',
			tooltip : 'Save Genes to User gene Set',
			disabled: true,
			handler: function(){
				ASPIREdb.view.SaveUserGeneSetWindow.initAndShow(ref.selectedgenes);
				
				
			}
		});
		
		
		

	}
});
