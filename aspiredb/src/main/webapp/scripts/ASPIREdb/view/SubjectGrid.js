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
	config : {

		// member variables

		// labels that are displayable
		// { label.id : label.valueObject }
		visibleLabels : {},

		// all the subject value objects in the grid
		valueObjects : [],

		// selected subjects in the grid
		selSubjects : [],
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
				dataIndex : 'labelIds',
				renderer : function(value) {
					var ret = "";
					for ( var i = 0; i < value.length; i++) {
						var label = this.visibleLabels[value[i]];
						if (label == undefined) {
							continue;
						}
						if (label.isShown) {

							ret += "<span style='background-color: "
									+ label.colour + "'>" + label.name
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

		ASPIREdb.EVENT_BUS.on('filter_submit', this.filterSubmitHandler, this);

		this.on('selectionchange', me.selectionChangeHandler, me);
		this.on('select', me.onSelectHandler, me);
		
		ASPIREdb.EVENT_BUS.on('label_change', function() {
			me.getView().refresh();
		});
	},

	/**
	 * Populate grid with Subjects and Labels
	 * 
	 * @param me
	 */
	filterSubmitHandler : function(filterConfigs) {

		var me = this;
		
		me.setLoading(true);
		me.getStore().removeAll();

		QueryService.querySubjects(filterConfigs, {
			callback : function(pageLoad) {
				me.valueObjects = pageLoad.items;

				// TODO: fix me (define grid/store in initComponent)
				// me.items.removeAll();

				var data = [];
				me.visibleLabels = {};
				for ( var i = 0; i < me.valueObjects.length; i++) {
					var val = me.valueObjects[i];

					// create only one unique label instance
					var labelIds = [];
					for ( var j = 0; j < val.labels.length; j++) {
						var aLabel = me.visibleLabels[val.labels[j].id];
						if (aLabel == undefined) {
							aLabel = val.labels[j];
							me.visibleLabels[aLabel.id] = aLabel;
						}
						labelIds.push(aLabel.id);
					}

					var row = [ val.id, val.patientId, labelIds ];
					data.push(row);
				}

				me.store.loadData(data);
				
				me.setLoading(false);

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

	onSelectHandler : function(ref, record, index, eOpts) {
		ASPIREdb.EVENT_BUS.fireEvent('subject_selected', record.get('id'));
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
						LabelService.updateLabel(addedLabel);

						var existingLab = me.visibleLabels[addedLabel.id];
						if (existingLab == undefined) {
							me.visibleLabels[addedLabel.id] = addedLabel;
						} else {
							existingLab.isShown = true;
						}

						// update local store
						for ( var i = 0; i < me.selSubjects.length; i++) {
							me.selSubjects[i].get('labelIds').push(
									addedLabel.id);
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

		labelControlWindow.show();
	},

});
