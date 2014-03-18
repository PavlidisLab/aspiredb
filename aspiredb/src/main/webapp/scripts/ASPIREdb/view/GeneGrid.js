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
	
	config:{
		// collection of all the PhenotypeSummaryValueObject loaded
		LoadedGeneSetNames : [],
		//collection of selected gene value objects
		selectedgenes :[],	
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
	selModel : Ext.create('Ext.selection.CheckboxModel', {
		mode: 'MULTI',
	}),

	store : Ext.create('ASPIREdb.store.GeneStore'),

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
			xtype : 'button',
			id : 'addGene',
			text : 'Add genes to selected gene set',
			tooltip : 'Add new gene',
			icon:'scripts/ASPIREdb/resources/images/icons/add.png',
			handler: function(){
				//TODO: have to populate human taxon gene list auto complete features
								
			}
		});
		
		this.getDockedComponent('geneSetGridToolbar').add('-');
		
		this.getDockedComponent('geneSetGridToolbar').add({
			xtype : 'button',
			id : 'removeGene',
			text : 'Delete gene',
			tooltip : 'Remove the selected gene',
			icon:'scripts/ASPIREdb/resources/images/icons/delete.png',
			handler: function(){
				//remove the selected gene from the gene set
								
			}
		});
	
	}
});
