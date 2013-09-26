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
              'ASPIREdb.TextDataDownloadWindow' ]);

/**
 * Queries Subject values and loads them into a {@link Ext.grid.Panel}
 * 
 */
Ext.define('ASPIREdb.view.SubjectGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.subjectGrid',
	title : 'Subject',
	id : 'subjectGrid',
	multiSelect : true,
	store : Ext.create('ASPIREdb.store.SubjectStore'),
	config : {
		visibleLabelIds : [],
		filterConfigs : [],
		valueObjects : [],
	},
	constructor : function(cfg) {
		this.initConfig(cfg);
		this.callParent(arguments);
	},
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
						if (value[i].isShown) {
							ret += "<span style='background-color: "
									+ value[i].colour + "'>" + value[i].name
									+ "</span>&nbsp;";
						}
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
		id : 'labelSettingsButton',
		text : '',
		tooltip : 'Configure label settings',
		icon : 'scripts/ASPIREdb/resources/images/icons/wrench.png',
	}, {
		xtype : 'tbfill'
	}, {
		xtype : 'button',
		id : 'saveButton',
		text : '',
		tooltip : 'Download table contents as text',
		icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'
		
	} ],

	/**
	 * 
	 */
	initComponent : function() {

		this.callParent();

		var me = this;

		ASPIREdb.EVENT_BUS.on('filter_submit', function(filterConfigs) {
			me.filterConfigs = filterConfigs;
			me.initSubjectLabelStore(me);
		});

		// add event handlers to buttons
		this.down('#addLabelButton').on('click', this.onMakeLabelClick);
		this.down('#labelSettingsButton').on('click', this.onLabelSettingsClick);
		this.down('#saveButton').on('click', function(){
			ASPIREdb.TextDataDownloadWindow.showSubjectDownload(me.valueObjects);
		});
	},

	/**
	 * Populate grid with Subjects and Labels
	 * 
	 * @param grid
	 */
	initSubjectLabelStore : function(grid) {
		QueryService.querySubjects(grid.filterConfigs, {
			callback : function(pageLoad) {
				grid.valueObjects = pageLoad.items;
				
				// TODO: fix me (define grid/store in initComponent)
				// me.items.removeAll();

				var labelMap = {};
				var data = [];
				grid.visibleLabelIds = [];
				for ( var i = 0; i < grid.valueObjects.length; i++) {
					var val = grid.valueObjects[i];
					var row = [ val.id, val.patientId, val.labels ];
					for ( var j = 0; j < val.labels.length; j++) {
						labelMap[val.labels[j]] = 1;
						if (grid.visibleLabelIds.indexOf(val.labels[j]) == -1) {
							grid.visibleLabelIds.push(val.labels[j]);
						}
					}
					data.push(row);
				}
				
				grid.store.loadData(data);

				// refresh grid
				grid.store.sync();
				grid.getView().refresh();

				var ids = [];
				for ( var i = 0; i < grid.valueObjects.length; i++) {
					var o = grid.valueObjects[i];
					ids.push(o.id);
				}
				ASPIREdb.EVENT_BUS.fireEvent('subjects_loaded', ids);
			}
		});
	},

	/**
	 * Assigns a Label to a Subject
	 * 
	 * @param event
	 */
	onMakeLabelClick : function(event) {
		var ids = [];
		var grid = this.up('#subjectGrid');
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

				var labelWithoutId = this.getLabel();

				// store in database
				SubjectService.addLabel(ids, labelWithoutId, {
					callback : function(theLabelWithId) {
						theLabel = theLabelWithId;

						grid.visibleLabelIds.push(theLabel);

						// update local store
						for ( var i = 0; i < selSubjects.length; i++) {
							selSubjects[i].get('label').push(theLabel);
						}

						// refresh grid
						grid.store.sync();
						grid.getView().refresh();
					}
				});
			},
		});

		var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
		labelWindow.show();
	},

	/**
	 * Display LabelSettingsWindow
	 */
	onLabelSettingsClick : function(event) {
		var grid = this.up('#subjectGrid');

		var labelControlWindow = Ext.create('ASPIREdb.view.LabelControlWindow',
				{
					visibleLabelIds : grid.getVisibleLabelIds(),
					isSubjectLabel : true,
				});

		labelControlWindow.on('destroy', function(btn, e, eOpts) {
			grid.initSubjectLabelStore(grid);
		});

		labelControlWindow.show();
	},

});
