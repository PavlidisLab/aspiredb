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
	//columnLines : true,
	multiSelect : true,
	config:{
		// collection of all the PhenotypeSummaryValueObject loaded
		LoadedGeneSetNames : [],
		//collection of selected gene value objects
		selectedgenes :[],	
	},

	dockedItems : [ {
		xtype : 'toolbar',
		dock: 'bottom',
		ui : 'footer',
		layout :{
			pack :'center'
		},
		itemId : 'geneGridToolbar',
		//dock : 'top'
		items : [{
			minWidth: 80,
			text: 'Save'
		},{
			minWidth: 80,
			text: 'Cancel'
		}]
}, {
    xtype: 'toolbar',
    items: [{
        text:'Add',
        tooltip:'Add new gene set',
        icon:'scripts/ASPIREdb/resources/images/icons/add.png'
    }, '-', {
        text:'Edit',
        tooltip:'Set options',
        icon:'scripts/ASPIREdb/resources/images/icons/add.png'
    },'-',{
        itemId: 'removeGeneSet',
        text:'Remove',
        tooltip:'Remove the selected gene set',
        icon:'scripts/ASPIREdb/resources/images/icons/delete.png',
        disabled: true
    }],
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
		this.down('#saveButtonGeneSet').enable();
	},
	
	setLodedGeneSetNames :function(names){
		
		this.LoadedGeneSetNames =names;
	},	
	
	enableToolbar : function(names) {
					
		this.getDockedComponent('geneGridToolbar').remove('saveButtonGene');
		
		this.getDockedComponent('geneGridToolbar').add('-');
		
		var ref = this;
					
		this.getDockedComponent('geneGridToolbar').add({
			xtype : 'button',
			id : 'saveButtonGene',
			text : 'Save to Gene Lists',
			tooltip : 'Save Genes to User gene Set',
			disabled: true,
			handler: function(){
				ASPIREdb.view.SaveUserGeneSetWindow.initAndShow(ref.selectedgenes);
								
			}
		});
		

	}
});
