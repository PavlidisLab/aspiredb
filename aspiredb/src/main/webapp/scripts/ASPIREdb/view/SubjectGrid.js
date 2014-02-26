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
				// This is very slow we need to rethink this
				renderer : function(value) {
					
					var ret = "";
					for ( var i = 0; i < value.length; i++) {
						var label = this.visibleLabels[value[i]];
						if (label == undefined) {
							continue;
						}
						if (label.isShown) {
							var fontcolor = (parseInt(label.colour, 16) > 0xffffff/2) ? 'black' : 'white';	
							ret += "<font color="+fontcolor+"><span style='background-color: "
									+ label.colour + "'>" + label.name
									+ "</span></font>&nbsp;";
						}
					}
					return ret;
				},
				flex : 1
			},
			{
				text : "# of variants",
				dataIndex : 'varientNos',
				renderer : function(value) {
					return value;
				},
				flex : 1
			},
			{
				text : "# of phenotypes",
				dataIndex : 'phenotypeNos',
				flex : 1
			}],

	/**
	 * init
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
		
		this.selectAllButton = Ext.create('Ext.Button', {
			itemId : 'selectAll',
			text : 'Select All',
			handler : this.selectAllHandler,
			scope : this
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
		this.toolbar.add(this.selectAllButton);
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
		//this.on('select', me.onSelectHandler, me);
		
		ASPIREdb.EVENT_BUS.on('label_change', function() {
			me.getView().refresh();
		});
	},

	/**
	 * 
	 * @param visibleLabels
	 */
	createVisibleLabels : function() {
		var visibleLabels = [];
		var suggestionContext = new SuggestionContext();
		suggestionContext.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
		
		// load all labels created by this user
		SubjectService.suggestLabels(suggestionContext, {
			callback : function(labels) {
				for ( var idx in labels) {
					var label = labels[idx];
					visibleLabels[label.id] = label;
				}
			}
		});	
		
		return visibleLabels;
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
		me.visibleLabels = me.createVisibleLabels();
			
		
		QueryService.querySubjects(filterConfigs, {
			callback : function(pageLoad) {
				me.valueObjects = pageLoad.items;
				
				var data = [];
				
				console.log(me.valueObjects.length + " subjects being processed into value objects");
				
				ProjectService.numSubjects(filterConfigs[0].projectIds, {
					callback : function(NoOfSubjects){
						if (NoOfSubjects > me.valueObjects.length) {
							me.setTitle( "Subject :"+me.valueObjects.length+" of "+NoOfSubjects +" filtered");
						}
						else if  (NoOfSubjects == me.valueObjects.length)
								me.setTitle( "Subject") ;
					}
				});
				
				for ( var i = 0; i < me.valueObjects.length; i++) {
					var val = me.valueObjects[i];
									
					// create only one unique label instance
					var labelIds = [];
					
					for ( var j = 0; j < val.labels.length; j++) {
						var aLabel = me.visibleLabels[val.labels[j].id];
						
						// this happens when a label has been assigned
						// by the admin and the user has no permissions
						// to modify the label
						if (aLabel == undefined) {
							aLabel = val.labels[j];
						}
						
						labelIds.push(aLabel.id);
					}
					
					//create summary of number of variants
					val.numOfPhenotypes;
					
					var row = [ val.id, val.patientId, labelIds, val.variants,  val.numOfPhenotypes];
					data.push(row);
				}

				me.store.loadData(data);
				
				me.setLoading(false);

				// refresh grid
				me.getView().refresh();

				var ids = [];
				for ( var i = 0; i < me.valueObjects.length; i++) {
					var o = me.valueObjects[i];
					ids.push(o.id);
				}
				
				console.log(ids.length + " subjects loaded");
				ASPIREdb.EVENT_BUS.fireEvent('subjects_loaded', ids);

			}
		});
	},

	selectionChangeHandler : function() {
		console.log("on selection  chnage Handler");
		this.selSubjects = this.getSelectionModel().getSelection();

		if (this.selSubjects.length == 0) {
			this.down('#makeLabel').disable();
			return;
		} else {
			this.down('#makeLabel').enable();
		}
				
		if (this.selSubjects.length>=1){
			console.log("fire subject_selected event");
			var ids=[];
			
			for ( var i = 0; i < this.selSubjects.length; i++) {
				ids.push(this.selSubjects[i].data.id);
			}
			ASPIREdb.EVENT_BUS.fireEvent('subject_selected',ids );
		}else
		{
			ASPIREdb.EVENT_BUS.fireEvent('subject_selected', null);
			
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
				
				var labelCombo = this.down("#labelCombo");
				var vo = this.getLabel();
				if (vo== null){
					return;
				}
				var labelIndex=labelCombo.getStore().findExact('display',vo.name);
				if ( labelIndex!=-1){
							//activate confirmation window
							Ext.MessageBox.confirm('Label already exist', 'Label already exist. Add into it ?', function(btn){
							   if(btn === 'yes'){
								   me.addLabelHandler(vo,selSubjectIds);
								   this.hide();
							   }
							   
							   
							 }, this);

				}
				else{
					me.addLabelHandler(vo,selSubjectIds);
					this.hide();
				}
					
				

			}
		});

		var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
		labelWindow.show();

	},
	
	addLabelHandler: function(vo,selSubjectIds) {
		 
		 var me=this;
		 		   
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
					me.getView().refresh();
				}
			});
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
	
	selectAllHandler : function() {

		//boolean true to suppressEvent
		this.getSelectionModel().selectAll(true);		
		this.selectionChangeHandler();

	}

});
