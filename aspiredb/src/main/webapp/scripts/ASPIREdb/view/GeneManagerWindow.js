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
/**
 * Gene manager has Gene Panel
 */
Ext.define('ASPIREdb.view.GeneManagerWindow', {
	extend : 'Ext.Window',
	alias : 'widget.geneManagerWindow',
	singleton : true,
	title : 'Gene Set Manager',
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

	 config :{
	    genesetSize :[],
	 },
	
	initComponent : function() {
	
		var ref = this;
		this.callParent();		

	},
	
	/**
	 * Show the gene manager window
	 */	
	initGridAndShow : function(){
		
		var ref = this;
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var grid =panel.down ('#geneSetGrid');
		
		ref.show();
		grid.setLoading(true);
		
		ref.genesetSize=[]
		
		/**UserGeneSetService.getSavedUserGeneSetNames( {
			callback : function(geneSetNames) {	
			   
			   for (var i=0;i<geneSetNames.length;i++){
			      UserGeneSetService.loadUserGeneSet( geneSetNames[i], {
	               callback : function(gvos) {
	                  
	                 ref.genesetSize.push(gvos.length);
	               }
	            } );
			   }
			         }
      });
			 */
		// ASPIREdb.view.GeneManagerWindow.populateGeneSetGrid(geneSetNames,ref.genesetSize);
		UserGeneSetService.getSavedUserGeneSets( {
         callback : function(gvos) { 
            ASPIREdb.view.GeneManagerWindow.populateGeneSetGrid(gvos);
         }
		});
	
	
	},
	
	
	
	/**
	 * Populate and gene set names in the gene set grid
	 */
	populateGeneSetGrid : function(gvos) {
		
		var panel = ASPIREdb.view.GeneManagerWindow.down('#ASPIREdb_genemanagerpanel');
		var grid =panel.down ('#geneSetGrid');
		
			
		var data = [];
		for ( var i = 0; i < gvos.length; i++) {
			var row = [ gvos[i].name,'',gvos[i].object.length];		
			data.push(row);					
		}
			
		grid.store.loadData(data);
		grid.setLoading(false);		
		grid.getView().refresh();
		grid.enableToolbar();
	},	
	
	
		
	clearGridAndMask : function(){
		ASPIREdb.view.GeneManagerWindow.getComponent('ASPIREdb_genemanagerpanel').store.removeAll();
		ASPIREdb.view.GeneManagerWindow.getComponent('ASPIREdb_genemanagerpanel').setLoading(true);				
	}

});