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
Ext.require([ 'ASPIREdb.store.SubjectStore', 'ASPIREdb.view.CreateLabelWindow',
		'ASPIREdb.ActiveProjectSettings' ]);

/**
 * Queries Subject values and loads them into a {@link Ext.grid.Panel}
 * 
 */
Ext.define('ASPIREdb.view.subject.SubjectGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.subjectGrid',
	title : 'Subject',
	multiSelect : true,
	store : Ext.create('ASPIREdb.store.SubjectStore'),

	columns : [
			{
				text : "Subject Id",
				dataIndex : 'patientId',
				flex : 1
			},
			{
				text : "Labels",
				dataIndex : 'label',
				renderer : function(value) {
					var ret = "";
					for ( var i = 0; i < value.length; i++) {
						ret += "<span style='background-color: "
								+ value[i].colour + "'>" + value[i].name
								+ "</span>&nbsp;";
					}
					return ret;
				},
				flex : 1
			}, ],

	tbar : [ {
		xtype : 'button',
		id : 'addLabelButton',
		text : '',
		tooltip : 'Add a new Label',
		icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
	}, {
		xtype : 'button',
		id : 'removeLabelButton',
		text : '',
		tooltip : 'Remove labels from a Subject',
		icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
		listeners : {
			click : function() {
				alert("Clicked Remove Labels");
			}
		}
	}, {
		xtype : 'button',
		id : 'configLabelButton',
		text : '',
		tooltip : 'Configure Labels',
		icon : 'scripts/ASPIREdb/resources/images/icons/wrench.png',
		listeners : {
			click : function() {
				alert('Clicked Config');
			}
		}
	}, {
		xtype : 'tbfill'
	}, {
		xtype : 'button',
		text : '',
		tooltip : 'Download table contents as text',
		icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
		listeners : {
			click : function() {
				alert("Clicked Save");
			}
		}
	} ],

	/**
	 * 
	 */
	initComponent : function() {

		this.callParent();

		var me = this;

		ASPIREdb.EVENT_BUS.on('filter_submit', function(filterConfigs) {
			QueryService.querySubjects(filterConfigs, {
				callback : function(pageLoad) {
					var subjectValueObjects = pageLoad.items;

					// TODO: fix me (define grid/store in initComponent)
					// me.items.removeAll();

					var data = [];
					for ( var i = 0; i < subjectValueObjects.length; i++) {
						var val = subjectValueObjects[i];
						var row = [ val.id, val.patientId, val.labels ];
						data.push(row);
					}
					me.store.loadData(data);

					var ids = [];
					for ( var i = 0; i < subjectValueObjects.length; i++) {
						var o = subjectValueObjects[i];
						ids.push(o.id);
					}
					ASPIREdb.EVENT_BUS.fireEvent('subjects_loaded', ids);

				}
			});
		});

		// add event handlers to buttons
		Ext.getCmp('addLabelButton').on('click', this.onMakeLabelClick);
	},

	/**
	 * Assigns a Label to a Subject
	 * 
	 * @param event
	 */
	onMakeLabelClick : function(event) {

		var ids = [];
		var grid = this.findParentByType('grid');
		var selSubjects = grid.getSelectionModel().getSelection();

		if (selSubjects.length == 0) {
			alert("At least one subject must be selected");
			return;
		}

		for ( var i = 0; i < selSubjects.length; i++) {
			ids.push(selSubjects[i].get('id'));
		}

		Ext.define('ASPIREdb.view.CreateLabelWindowSubject', {
			extend : 'ASPIREdb.view.CreateLabelWindow',

			// override
			onOkButtonClick : function() {
				this.callParent();

				var theLabel = this.getLabel();

				// store in database
				SubjectService.addLabel(ids, theLabel);

				// update local store
				for ( var i = 0; i < selSubjects.length; i++) {
					selSubjects[i].get('label').push(theLabel);
				}

				// refresh grid
				grid.store.sync();
				grid.getView().refresh();
			},
		});

		var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
		labelWindow.show();
	},

});
