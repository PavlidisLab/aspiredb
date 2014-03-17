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
	height : 800,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',
	
	
	items : [{
		region : 'center',
		xtype : 'ASPIREdb_genemanagerpanel'
	}],

	initComponent : function() {
		var ref = this;
		this.callParent();

	},
	
	initGridAndShow : function(){
		
		var ref = this;
		//ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);
		ref.show();

		
	},
	
	
	
	clearGridAndMask : function(){
		ASPIREdb.view.GeneManagerWindow.getComponent('ASPIREdb_genemanagerpanel').store.removeAll();
		ASPIREdb.view.GeneManagerWindow.getComponent('ASPIREdb_genemanagerpanel').setLoading(true);				
	}

});