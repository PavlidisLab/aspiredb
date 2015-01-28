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

Ext.require([ 'ASPIREdb.store.PhenotypeEnrichmentStore', 'ASPIREdb.TextDataDownloadWindow' ]);

// TODO js documentation
Ext.define('ASPIREdb.view.PhenotypeEnrichmentGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.phenotypeEnrichmentGrid',
	

	columns : [ {
		header : 'Name',
		dataIndex : 'name',
		flex : 1
	}, {
		header : 'In Group Present',
		dataIndex : 'inGroupPresent',
		flex : 1
	}, {
		header : 'Out Group Present',
		dataIndex : 'outGroupPresent',
		flex : 1
	}, {
		header : 'P-value',
		dataIndex : 'pValue',
		flex : 1
	}, {
		header : 'Corrected P-value',
		dataIndex : 'corrpValue',
		flex : 1
	} ],

	store : Ext.create('ASPIREdb.store.PhenotypeEnrichmentStore'),

	initComponent : function() {
		this.callParent();

		

	}
	
	
	
});
