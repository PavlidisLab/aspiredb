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

Ext.require([ 'Ext.Window', 'ASPIREdb.view.GeneManagerPanel','ASPIREdb.GemmaURLUtils' ]);

Ext.define('ASPIREdb.view.GeneManagerWindow', {
	extend : 'Ext.Window',
	alias : 'widget.geneManagerWindow',
	singleton : true,
	title : 'Gene Manager',
	closable : true,
	closeAction : 'hide',
	width : 1000,
	height : 500,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',
	
	
	items : [{
		region : 'center',
		itemId : 'ASPIREdb_genemanagerpanel',
		xtype : 'ASPIREdb_genemanagerpanel',
	}],

	initComponent : function() {
	
		var ref = this;
		this.callParent();
		

	},
	
	
	
	initGridAndShow : function(){
		
		var ref = this;
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var grid =panel.down ('#geneSetGrid');
		
		ref.show();
		grid.setLoading(true);
		
		UserGeneSetService.getSavedUserGeneSetNames( {
			callback : function(geneSetNames) {	
				ASPIREdb.view.GeneManagerWindow.populateGeneSetGrid(geneSetNames);			
			}
		});
		
	
	},
	
	
	
	//GeneSet Names
	populateGeneSetGrid : function(names) {
		
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var grid =panel.down ('#geneSetGrid');
		
			
		var data = [];
		for ( var i = 0; i < names.length; i++) {
			var row = [ names[i],'',''];		
			data.push(row);					
		}
			
		grid.store.loadData(data);
		grid.setLoading(false);		
		grid.getView().refresh();
		grid.enableToolbar(names);
	},	
	
	
		
	clearGridAndMask : function(){
		ASPIREdb.view.GeneManagerWindow.getComponent('ASPIREdb_genemanagerpanel').store.removeAll();
		ASPIREdb.view.GeneManagerWindow.getComponent('ASPIREdb_genemanagerpanel').setLoading(true);				
	}

});