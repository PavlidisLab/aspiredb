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
Ext.require([ 'Ext.grid.Panel', 'Ext.Panel', 'Ext.data.ArrayStore' ]);

/**
 * Queries Subject values and loads them into a {@link PagingGridPanel}
 * 
 */
Ext.define('ASPIREdb.view.subject.SubjectGrid', {
	extend : 'Ext.Panel',
	alias : 'widget.subjectGrid',
	title : 'Subject',
	layout : 'fit',

	/**
	 * 
	 */
	initComponent : function() {

		this.callParent();

		// DWR
		var me = this;
		SubjectService.getSubjects({
			callback : function(subjectValueObjects) {
				me.subjectValueObjects = subjectValueObjects;
				me.add(me.createGrid(subjectValueObjects));
			}
		});
	},

	/**
	 * 
	 * @param subjectValueObjects
	 *            {@link SubjectValueObject}
	 * @returns
	 */
	createGrid : function(subjectValueObjects) {
		var store = this.createStore(subjectValueObjects);

		var grid = Ext.create('Ext.grid.Panel', {
			store : store,
			border : false,
			columns : [ {
				text : "Subject Id",
				dataIndex : 'subjectId',
				flex : 1,
				width : 50
			}, {
				text : "Labels",
				dataIndex : 'label',
				flex : 1,
				width : 50
			}, ],
		});

		return grid;
	},

	/**
	 * 
	 * @param subjectValueObjects
	 *            {@link SubjectValueObject}
	 * @returns
	 */
	createStore : function(subjectValueObjects) {

		var data = [];

		for ( var key in subjectValueObjects) {
			var val = subjectValueObjects[key];
			var visibleLabels = val.labels.join();
			var row = [ val.patientId, visibleLabels ];
			data.push(row);
		}

		var store = Ext.create('Ext.data.ArrayStore', {

			fields : [ {
				name : 'subjectId'
			}, {
				name : 'label'
			} ],
			data : data,
			storeId : 'subjectStore'
		});

		return store;
	},

});