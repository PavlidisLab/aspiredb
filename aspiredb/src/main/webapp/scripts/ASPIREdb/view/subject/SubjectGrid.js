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
Ext.require([ 'Ext.grid.Panel', 'Ext.toolbar.Toolbar', 'Ext.grid.*', 'Ext.ComponentQuery', 'Ext.Panel', 'Ext.panel.Panel','Ext.data.ArrayStore' ]);

/**
 * Queries Subject values and loads them into a {@link PagingGridPanel}
 * 
 */
Ext.define('ASPIREdb.view.subject.SubjectGrid', {
	extend : 'Ext.panel.Panel',
	alias : 'widget.subjectGrid',
	title : 'Subject',
	layout : 'fit',
	dockedItems: [{
		xtype: 'toolbar',
		items: [{
			id: 'addLabelButton',
			text: '',
			tooltip: 'Add a new Label',
			icon: 'scripts/ASPIREdb/resources/images/icons/add.png',
		}, {
			id: 'configLabelButton',
			text: '',
			tooltip: 'Configure Labels',
			icon: 'scripts/ASPIREdb/resources/images/icons/wrench.png',
			listeners: {
				click: function() {
					alert("Clicked Configure Labels");
				}
			}
		}, {
			xtype: 'tbfill'
		}, {
			text: '',
			tooltip: 'Download table contents as text',
			icon: 'scripts/ASPIREdb/resources/images/icons/disk.png',
			listeners: {
				click: function() {
					alert("Clicked Save");
				}
			}
		}]
	}],
	
	/**
	 * 
	 */
	initComponent : function() {

		this.callParent();

		var me = this;
		
		// grid panel
		SubjectService.getSubjects({
			callback : function(subjectValueObjects) {
				me.subjectValueObjects = subjectValueObjects;
				me.add(me.createGrid(subjectValueObjects));
			}
		});
		
		// add handlers to buttons
		Ext.getCmp('addLabelButton').on('click', this.onMakeLabelClick);
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
			id: 'subjectGrid',
			multiSelect: true,
			columns : [ {
				text : "Subject Id",
				dataIndex : 'patientId',
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
	 */
	onMakeLabelClick : function() {
		// http://docs.sencha.com/extjs/4.2.1/extjs-build/examples/grid/grid-plugins.js
		
		var ids = [];
		var grid = Ext.getCmp('subjectGrid');
		var selSubjects = grid.getSelectionModel().getSelection();
		alert("Clicked Add Label, selected " + grid.getSelectionModel().getCount());
		for (var i = 0; i < selSubjects.length; i++) {
			ids.push(selSubjects[i].get('id'));
		}

		var theLabel = "FooBarLabel"; 
		//SubjectService.addLabel(ids, theLabel);
		
		
		//Collection<SubjectValueObject> subjects = subjectGrid.grid.getSelectionModel().getSelection();
		//console.log("ids[0]="+ids[0]+"; label="+selSubjects[0].get('label'));
	},
	
	/**
	 * 
	 * @param subjectValueObjects
	 *            {@link SubjectValueObject}
	 * @returns
	 */
	createStore : function(subjectValueObjects) {

		var data = [];

		for ( var i = 0; i < subjectValueObjects.length; i++) {
			var val = subjectValueObjects[i];
			var visibleLabels = val.labels.join();
			var row = [ val.id, val.patientId, visibleLabels ];
			data.push(row);
		}

		var store = Ext.create('Ext.data.ArrayStore', {

			fields : [ { name: 'id' },
			           {
				name : 'patientId'
			}, {
				name : 'label'
			} ],
			data : data,
			storeId : 'subjectStore'
		});

		return store;
	},

});