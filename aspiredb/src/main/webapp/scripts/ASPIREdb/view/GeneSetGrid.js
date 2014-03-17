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
Ext.define('ASPIREdb.view.GeneSetGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.geneSetGrid',
	emptyText : 'No genes found',
	id : 'geneSetGrid',
	border: true,
	multiSelect : true,
	store : Ext.create('ASPIREdb.store.GeneSetStore'),

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
		itemId : 'geneSetGridToolbar',
		//dock : 'top'
		/**items : [{
			minWidth: 80,
			text: 'Save'
		},{
			minWidth: 80,
			text: 'Cancel'
		}]*/
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
	            	header : 'Name',
	        		dataIndex : 'geneSetName',
	        		flex : 1
	            },
	            {
	            	header : 'Description',
	            	dataIndex : 'geneDescription',
	            	flex : 1
	            }, {
	            	header : 'size',
	            	dataIndex : 'geneSetSize',
	            	flex : 1
	            }
	],		
	selModel : Ext.create('Ext.selection.CheckboxModel', {
		mode: 'MULTI',
	}),

	

	initComponent : function() {
		
		this.callParent();
		var me = this;
		
		UserGeneSetService.getSavedUserGeneSetNames( {
			callback : function(geneSetNames) {							
				me.populateGrid(geneSetNames);
		
			}
		});
		

		//this.on('select', me.geneSelectHandler, me);
		

	},
	
	//GeneValueObject
	populateGrid : function(names) {	
				
		var data = [];
		for ( var i = 0; i < names.length; i++) {
			var row = [ names[i],'test','0'];		
			data.push(row);								
		}
		console.log('load gene set into store'+this.store.data);
		this.store.loadData(data);
		this.setLoading(false);	
		//this.getView().refresh();
		
		//this.enableToolbar(names);

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
					
		this.getDockedComponent('geneSetGridToolbar').remove('saveButtonGeneSet');
		
		this.getDockedComponent('geneSetGridToolbar').add('-');
		
		var ref = this;
					
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'saveButtonGeneSet',
			text : 'Save Gene Set',
			tooltip : 'Save Gene Set',
			disabled: true,
			handler: function(){
				ASPIREdb.view.SaveUserGeneSetWindow.initAndShow(ref.selectedgenes);
								
			}
		});
		

	}
});
