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
Ext
		.require([ 'ASPIREdb.store.SubjectStore',
				'ASPIREdb.view.CreateLabelWindow',
				'ASPIREdb.view.LabelControlWindow',
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
	constructor : function(cfg) {
		// the filters used to select which subjects to show
		var filterConfigs = [];

		// labels that are displayed
		var visibleLabels = [];

		// all the value objects in the grid
		var valueObjects = [];

		// selected subjects in the grid
		var selSubjects = [];

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
				dataIndex : 'labels',
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

	/**
	 * 
	 */
	initComponent : function() {

		this.callParent();

		var me = this;

		this.labelsMenu = Ext.create('Ext.menu.Menu', {
			items : [ {
				itemId : 'makeLabel',
				text : 'Make label...',
				disabled : true,
				handler : this.makeLabelHandler,
				scope : this
			}, {
				itemId : 'labelSettings',
				text : 'Settings...',
				disabled : false,
				handler : this.labelSettingsHandler,
				scope : this
			} ]
		});

		this.labelsButton = Ext.create('Ext.Button', {
			text : '<b>Labels</b>',
			itemId : 'labelsButton',
			menu : this.labelsMenu
		});

		this.saveButton = Ext.create('Ext.Button', {
			itemId : 'saveButton',
			text : '',
			tooltip : 'Download table contents as text',
			icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
		});

		this.toolbar = Ext.create('Ext.toolbar.Toolbar', {
			itemId : 'subjectGridToolbar',
			dock : 'top'
		});

		this.toolbar.add(this.labelsButton);
		this.toolbar.add(Ext.create('Ext.toolbar.Fill'));
		this.toolbar.add(this.saveButton);
		this.addDocked(this.toolbar);

		// add event handlers to buttons
		this.saveButton.on('click', function() {
			ASPIREdb.TextDataDownloadWindow
					.showSubjectDownload(me.valueObjects);
		}, this);

		ASPIREdb.EVENT_BUS.on('filter_submit', function(filterConfigs) {
			me.filterConfigs = filterConfigs;
			me.initSubjectLabelStore(me);
		}, this);

	},

	/**
	 * Populate grid with Subjects and Labels
	 * 
	 * @param me
	 */
	initSubjectLabelStore : function(me) {

		QueryService.querySubjects(me.filterConfigs, {
			callback : function(pageLoad) {
				me.valueObjects = pageLoad.items;

				// TODO: fix me (define grid/store in initComponent)
				// me.items.removeAll();

				var data = [];
				me.visibleLabels = [];
				for ( var i = 0; i < me.valueObjects.length; i++) {
					var val = me.valueObjects[i];
					var row = [ val.id, val.patientId, val.labels ];
					for ( var j = 0; j < val.labels.length; j++) {
						me.visibleLabels.push(val.labels[j]);
					}
					data.push(row);
				}

				me.store.loadData(data);

				me.on('selectionchange', me.selectionChangeHandler, me);

				// refresh grid
				me.store.sync();
				me.getView().refresh();

				var ids = [];
				for ( var i = 0; i < me.valueObjects.length; i++) {
					var o = me.valueObjects[i];
					ids.push(o.id);
				}
				ASPIREdb.EVENT_BUS.fireEvent('subjects_loaded', ids);

			}
		});
	},

	selectionChangeHandler : function() {

		this.selSubjects = this.getSelectionModel().getSelection();

		if (this.selSubjects.length == 0) {
			this.down('#makeLabel').disable();
			return;
		} else {
			this.down('#makeLabel').enable();
		}

	},

	/**
	 * Assigns a Label
	 * 
	 */
	makeLabelHandler : function(event) {

		var me = this;

		var selSubjectIds = [];
		for ( var i = 0; i < me.selSubjects.length; i++) {
			selSubjectIds.push(me.selSubjects[i].data.id);
		}

		Ext.define('ASPIREdb.view.CreateLabelWindowSubject', {
			isSubjectLabel : true,
			extend : 'ASPIREdb.view.CreateLabelWindow',

			// override
			onOkButtonClick : function() {
				this.callParent();

				var vo = this.getLabel();

				// store in database
				SubjectService.addLabel(selSubjectIds, vo, {
					callback : function(addedLabel) {

						addedLabel.isShown = true;
						LabelService.updateLabel(addedLabel, {
							timeout : 500,
							errorHandler : function(message) {
								alert("Error updating label: " + message);
							}
						});

						var idx = me.visibleLabels.indexOf(addedLabel);
						if ( idx == -1) {
							me.visibleLabels.push(addedLabel);
						} else {
							me.visibleLabels[idx].isShown = true;
						}

						// update local store
						for ( var i = 0; i < me.selSubjects.length; i++) {
							me.selSubjects[i].get('labels').push(addedLabel);
						}

						// refresh grid
						me.store.sync();
						me.getView().refresh();
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
	labelSettingsHandler : function(event) {
		var me = this;

		var labelControlWindow = Ext.create('ASPIREdb.view.LabelControlWindow',
				{
					visibleLabels : me.visibleLabels,
					isSubjectLabel : true,
				});

		labelControlWindow.on('destroy', function(btn, e, eOpts) {
			me.initSubjectLabelStore(me);
		}, this);

		labelControlWindow.show();
	},

});
