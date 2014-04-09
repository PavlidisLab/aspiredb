Ext.require([ 'Ext.window.*', 'Ext.layout.container.Border', 'ASPIREdb.view.filter.AndFilterContainer', 'ASPIREdb.view.filter.VariantFilterPanel', 'ASPIREdb.view.filter.SubjectFilterPanel', 'ASPIREdb.view.filter.PhenotypeFilterPanel', 'ASPIREdb.view.SaveQueryWindow', 'ASPIREdb.view.filter.ProjectOverlapFilterPanel', 'ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel', 'ASPIREdb.view.filter.DgvProjectOverlapFilterPanel','ASPIREdb.view.DeleteQueryWindow', 'ASPIREdb.TextDataDownloadWindow']);


Ext.define('ASPIREdb.view.filter.FilterWindow', {
	extend : 'Ext.Window',
	alias : 'widget.filterwindow',
	singleton : true,
	title : 'Filter',
	closable : true,
	closeAction : 'hide',
	width : 1000,
	height : 500,
	layout : 'border',
	bodyStyle : 'padding: 5px;',
	border: false,
	constrain:true,
	config : {
		isOverlapedProjects :'No',
	},

	initComponent : function() {
		var me = this;
		this.items = [ {
			region : 'north',
			width : 900,
			items : [ {
				xtype : 'container',
				layout : {
					type : 'hbox',
					defaultMargins : {
						top : 5,
						right : 5,
						left : 5,
						bottom : 5
					}
				},
				items : [ {
					xtype : 'label',
					text : 'Add new: '
				}, {
					xtype : 'combo',
					itemId : 'filterTypeComboBox',
					editable : false,
					forceSelection : true,
					value : 'FILTER_PLACEHOLDER',
					store : [ [ 'FILTER_PLACEHOLDER', '<Filter>' ], [ 'ASPIREdb.view.filter.SubjectFilterPanel', 'Subject Filter' ], [ 'ASPIREdb.view.filter.VariantFilterPanel', 'Variant Filter' ], [ 'ASPIREdb.view.filter.PhenotypeFilterPanel', 'Phenotype Filter' ], [ 'ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel', 'Decipher Overlap' ], [ 'ASPIREdb.view.filter.DgvProjectOverlapFilterPanel', 'DGV Overlap' ],  [ 'ASPIREdb.view.filter.ProjectOverlapFilterPanel', 'Project Overlap' ] ]
				}, {
					xtype : 'label',
					text : 'or load saved query: '
				}, 
				{
					xtype : 'combo',
					itemId : 'savedQueryComboBox',
					editable : false,
					forceSelection : true,
					value : 'FILTER_PLACEHOLDER',
					store : [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ]
				},
				{
					xtype : 'button',
					flex : 1,
					text : 'Query Manager',
					id: 'querymanager',
					shrinkWrap: 1,
					width: 30,
					disabled: true,
					itemId : 'deleteQueryButton',
					handler : me.deleteQueryHandler,
					scope : me
				}]
			} ]
		}, {
			region : 'center',
			xtype : 'container',
			itemId : 'filterContainer',
			overflowY : 'auto',
			layout : {
				type : 'vbox'
			},
			items : [
			// {
			// xtype: 'filter_variant'
			// }
			]
		}, {
			region : 'south',
			/*
			 * xtype: 'container',
			 */
			layout : {
				type : 'hbox',
				defaultMargins : {
					top : 5,
					right : 5,
					left : 5,
					bottom : 5
				}
			},
			items : [ {
				xtype : 'container',
				flex : 1,
				layout : {
					type : 'hbox',
					defaultMargins : {
						top : 5,
						right : 5,
						left : 5,
						bottom : 5
					}
				},
				items : [ {
					xtype : 'label',
					itemId : 'numberOfSubjectsLabel',
				}, {
					xtype : 'label',
					text : ' subjects and ',
					itemId : 'numberOfSubjectsLabelText'
				}, {
					xtype : 'label',
					itemId : 'numberOfVariantsLabel',
					
				}, {
					xtype : 'label',
					text : ' variants will be returned.',
					itemId : 'numberOfVariantsLabelText'
				} ]
			}, {
				xtype : 'container',
				flex : 1,
				layout : {
					type : 'hbox',
					defaultMargins : {
						top : 5,
						right : 5,
						left : 5,
						bottom : 5
					}
				},
				items : [ {
					xtype : 'button',
					flex : 2,
					text : 'Preview query',
					itemId : 'previewQueryButton',
					handler : me.previewQueryHandler,
					scope : me
				},{
					xtype : 'button',
					flex : 1,
					text : 'Submit',
					itemId : 'applyButton',
					handler : function() {
						var filterConfigs = me.getFilterConfigs();
						console.log("filter_submit event from FilterWindow");
						ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);
						me.close();
					}
				}, {
					xtype : 'button',
					flex : 2,
					text : 'Save query',
					itemId : 'saveQueryButton',
					handler : me.saveQueryHandler,
					scope : me
				}, 
				{
					xtype : 'button',
					flex : 1,
					text : 'Clear',
					itemId : 'clearButton',
					handler : me.clearButtonHandler,
					scope : me
				}, {
					xtype : 'button',
					flex : 1,
					text : 'Cancel',
					itemId : 'cancelButton',
					handler : me.cancelButtonHandler,
					scope : me
				} ]
			}

			]
		} ];

		this.callParent();
		var filterTypeComboBox = this.down('#filterTypeComboBox');
		var filterContainer = this.down('#filterContainer');

		this.updateSavedQueryCombo();
		this.enableDisableQueryManager();

		this.down('#savedQueryComboBox').on('select', this.savedQueryComboBoxSelectHandler, this);
		
		ASPIREdb.EVENT_BUS.on('project_select', this.clearButtonHandler, this);	
			

		filterTypeComboBox.on('select', function(combo, records) {
			var record = records[0];
			filterContainer.add(Ext.create(record.raw[0]));
			filterTypeComboBox.setValue('FILTER_PLACEHOLDER');
			me.invalidateResultCounts();
		});
		
		ASPIREdb.view.DeleteQueryWindow.on('query_deleted', this.updateSavedQueryCombo, this);
		ASPIREdb.view.DeleteQueryWindow.on('query_deleted', this.enableDisableQueryManager, this);
		ASPIREdb.view.SaveQueryWindow.on('new_query_saved', this.updateSavedQueryCombo, this);
		ASPIREdb.view.SaveQueryWindow.on('new_query_saved', this.enableDisableQueryManager, this);		
		
		ASPIREdb.EVENT_BUS.on('query_update', function(event) {
			me.invalidateResultCounts();
		});
		
		//ASPIREdb.EVENT_BUS.on('overlapProject_selected', this.updateOverlappedProject, this);
		
		this.updateSpecialProjectValues(); 
		
				
	},
	
	updateOverlappedProject : function(projectId){
		this.getOverlappedFilterConfigs(projectId); 
		console.log('overlapped project config updated');
	},

	loadQueryHandler : function(filters) {

		// ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filters);

		var filterContainer = this.down('#filterContainer');
		filterContainer.doLayout();

		filterContainer.removeAll(true);

		// first is filterconfig
		for ( var i = 0; i < filters.length; i++) {

			if (filters[i].restriction || filters[i].restriction1) {

				if (filters[i] instanceof SubjectFilterConfig) {

					var subjectFilterPanel = Ext.create('ASPIREdb.view.filter.SubjectFilterPanel');
					filterContainer.add(subjectFilterPanel);
					filterContainer.doLayout();
					subjectFilterPanel.setFilterConfig(filters[i]);

				} else if (filters[i] instanceof PhenotypeFilterConfig) {

					var phenotypeFilterPanel = Ext.create('ASPIREdb.view.filter.PhenotypeFilterPanel');
					filterContainer.add(phenotypeFilterPanel);
					filterContainer.doLayout();
					phenotypeFilterPanel.setFilterConfig(filters[i]);

				} else if (filters[i] instanceof VariantFilterConfig) {
					
					var variantFilterPanel = Ext.create('ASPIREdb.view.filter.VariantFilterPanel');					
					
					filterContainer.add(variantFilterPanel);
					filterContainer.doLayout();
					variantFilterPanel.setFilterConfig(filters[i]);

				}else if (filters[i] instanceof ProjectOverlapFilterConfig) {
					
					//overlapProjectIds will only have 1 entry currently, however it is an array to allow for extension to multiple projects later
					var overlapProjectId =filters[i].overlapProjectIds[0];
					
					if (overlapProjectId == this.decipherProjectValueObject.id){
						
						var decipherProjectOverlapFilterPanel = Ext.create('ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel');					
						
						filterContainer.add(decipherProjectOverlapFilterPanel);
						filterContainer.doLayout();
						decipherProjectOverlapFilterPanel.setFilterConfig(filters[i]);
						
					}else if (overlapProjectId == this.dgvProjectValueObject.id){
						
						var dgvProjectOverlapFilterPanel = Ext.create('ASPIREdb.view.filter.DgvProjectOverlapFilterPanel');					
						
						filterContainer.add(dgvProjectOverlapFilterPanel);
						filterContainer.doLayout();
						dgvProjectOverlapFilterPanel.setFilterConfig(filters[i]);
						
					}else{
						//This will be the user project overlap functionality
						var projectOverlapFilterPanel = Ext.create('ASPIREdb.view.filter.ProjectOverlapFilterPanel');					
						this.isOverlapedProjects ='Yes';
						filterContainer.add(projectOverlapFilterPanel);
						filterContainer.doLayout();
						projectOverlapFilterPanel.setFilterConfig(filters[i]);
						
					}

				}

			}

		}
		
		ASPIREdb.EVENT_BUS.fireEvent('query_update');

	},

	
	savedQueryComboBoxSelectHandler : function() {

		var combo = this.down('#savedQueryComboBox');
		
		if (combo.getValue() && combo.getValue()!=''){

			//Ext.getCmp('querymanager').enable();

			QueryService.loadQuery(combo.getValue(), {
				callback : this.loadQueryHandler,
				scope : this
			});
		
		}

	},

	updateSavedQueryCombo : function() {

		var savedQueryComboBox = this.down('#savedQueryComboBox');

		QueryService.getSavedQueryNames({
			callback : function(names) {

				var storedata = [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ];

				for ( var i = 0; i < names.length; i++) {

					storedata.push([ names[i], names[i] ]);

				}

				savedQueryComboBox.getStore().loadData(storedata);
				if (names.length >1){
				savedQueryComboBox.select(savedQueryComboBox.getStore().getAt(names.length));}
				
			}
		});
		
		this.down('#savedQueryComboBox').clearValue();
		
	},
	
	enableDisableQueryManager: function(){
		QueryService.getSavedQueryNames({
			callback : function(names) {
				
			if (names.length != 0)
				 Ext.getCmp('querymanager').enable();
			 else 
				 Ext.getCmp('querymanager').disable();
			}
		});
		
	},
	
	saveQueryHandler : function() {

		ASPIREdb.view.SaveQueryWindow.initAndShow(this.getFilterConfigs());

	},
	
	deleteQueryHandler : function() {

		ASPIREdb.view.DeleteQueryWindow.initAndShow(this.getFilterConfigs());

	},
	
	enableOverlappButton: function(){
    	//var projectOverlapFilterContainer = ASPIREdb.view.filter.ProjectOverlapFilterPanel.getComponent('projectOverlapFilterContainer');
    	var projectOverlapFilterPanel = Ext.create('ASPIREdb.view.filter.ProjectOverlapFilterPanel');
    	projectOverlapFilterPanel.down('#overlappedVariants').enable();
    },
	
	previewQueryHandler : function() {
		if (this.isOverlapedProjects == 'Yes'){
			//if project overlap filter panel, then enable the overlapped variants button in filter container
			var filterContainer = this.down('#filterContainer');
			filterContainer.down('#overlappedVariants').enable();
		}
								
		if (this.down('#numberOfSubjectsLabel').getEl() && this.down('#numberOfVariantsLabel').getEl()){
			this.down('#numberOfSubjectsLabel').getEl().setOpacity(1, true);
			this.down('#numberOfVariantsLabel').getEl().setOpacity(1, true);
			this.down('#numberOfSubjectsLabelText').getEl().setOpacity(1, true);
			this.down('#numberOfVariantsLabelText').getEl().setOpacity(1, true);
		}

		var me = this;		
		
		
		var SUBJECT_IDS_KEY = 0;
		var VARIANT_IDS_KEY = 1;
		
		me.setLoading(true);
		
		QueryService.getSubjectVariantCounts(this.getFilterConfigs(), {
		    callback : function(totalCounts) {
                me.down('#numberOfSubjectsLabel').setText(totalCounts[SUBJECT_IDS_KEY].toString());
                me.down('#numberOfVariantsLabel').setText(totalCounts[VARIANT_IDS_KEY].toString());
                me.setLoading(false);
            }
		});

	},

	cancelButtonHandler : function() {

		this.hide();

	},
	
	
	clearButtonHandler : function() {
		
		this.down('#filterContainer').removeAll();
		this.down('#savedQueryComboBox').clearValue();
		ASPIREdb.EVENT_BUS.fireEvent('query_update');

	},
	
	
	getFilterConfigs : function() {
		/**
		 * @type {Array.RestrictionFilterConfig}
		 */
		var filterConfigs = [];
		var projectFilter = new ProjectFilterConfig();
		projectFilter.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
		filterConfigs.push(projectFilter);
		var filterContainer = this.down('#filterContainer');
		filterContainer.items.each(function(item, index, length) {
			filterConfigs.push(item.getFilterConfig());
		});

		return filterConfigs;
	},
	
	getOverlappedFilterConfigs : function(projectId) {
		/**
		 * @type {Array.RestrictionFilterConfig}
		 * Here we need to edit the project id to overlapped project id 
		 */
		var filterConfigs = [];
		
		var projectIds=[];
		projectIds.push(projectId);
		
		var projectFilter = new ProjectFilterConfig();
		projectFilter.projectIds = projectIds;
		filterConfigs.push(projectFilter);
		
		var filterContainer = this.down('#filterContainer');
		filterContainer.items.each(function(item, index, length) {
			
			var newFilterConfig = item.getFilterConfig();
			newFilterConfig.projectIds=projectIds;
			filterConfigs.push(newFilterConfig);
		});

		return filterConfigs;
	},

	invalidateResultCounts: function() {
		
		if (this.down('#numberOfSubjectsLabel').getEl() && this.down('#numberOfVariantsLabel').getEl()){
			this.down('#numberOfSubjectsLabel').getEl().setOpacity(0.5, true);
			this.down('#numberOfVariantsLabel').getEl().setOpacity(0.5, true);
			this.down('#numberOfSubjectsLabelText').getEl().setOpacity(0.5, true);
			this.down('#numberOfVariantsLabelText').getEl().setOpacity(0.5, true);
		}
		
		
	},
	
	/**
	 * Populate the overlapped variants
	 */
	overlappedVariantsHandler : function(projectId){
		
		var me=this;
				
		VariantService.suggestProperties(function(properties) {
			
			
		  QueryService.queryVariants(me.getOverlappedFilterConfigs(projectId), {
			callback : function(pageLoad) {
				
				var vvos = pageLoad.items;
				characteristicNames = [];

				for ( var i = 0; i < properties.length; i++) {

					if (properties[i].characteristic) {
						characteristicNames.push(properties[i].name);
					}

				}
				
			
					
				var data=[];
				var headers=['Id','Patient Id','Type', 'Genome Coordinates','Chromosome','Base Start', 'Base End','CNV Type','Copy Number','CNV Length'];
				for ( var j = 0; j < characteristicNames.length; j++) {
					headers.push(characteristicNames[j]);
				}
				
				for ( var i = 0; i < vvos.length; i++) {

					var vvo = vvos[i];
					console.log('filter window set variant load overlap filter vvo '+vvos[i]);

					var dataRow = [];

					dataRow.push(vvo.id);

					dataRow.push(vvo.patientId);

					dataRow.push(vvo.variantType);
					dataRow.push(vvo.genomicRange.chromosome + ":" + vvo.genomicRange.baseStart + "-" + vvo.genomicRange.baseEnd);
					dataRow.push(vvo.genomicRange.chromosome);
					dataRow.push(vvo.genomicRange.baseStart);
					dataRow.push(vvo.genomicRange.baseEnd);
					
					/**var visibleLabels = [];
					var suggestionContext = new SuggestionContext();
					
					suggestionContext.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
					
					// load all labels created by this user
					VariantService.suggestLabels(suggestionContext, {
						callback : function(labels) {
							for ( var idx in labels) {
								var label = labels[idx];
								visibleLabels[label.id] = label;
							}
						}
					});
					
					// create only one unique label instance
					var labels = [];
					for (var j = 0; j < vvo.labels.length; j++) {
						var aLabel = visibleLabels[vvo.labels[j].id];

						// this happens when a label has been assigned
						// by the admin and the user has no permissions
						// to modify the label
						if (aLabel == undefined) {
							aLabel = vvo.labels[j];
						}

						labels.push(aLabel.id);
					}

					dataRow.push(labels);*/

					if (vvo.variantType == "CNV") {
						dataRow.push(vvo.type);
						dataRow.push(vvo.copyNumber);
						dataRow.push(vvo.cnvLength);
					} else {
						dataRow.push("");
						dataRow.push("");
						dataRow.push("");
					}

					if (vvo.variantType == "SNV") {
						dataRow.push(vvo.dbSNPID);
						dataRow.push(vvo.observedBase);
						dataRow.push(vvo.referenceBase);
					} else {
						dataRow.push("");
						dataRow.push("");
						dataRow.push("");
					}

					if (vvo.variantType == "INDEL") {
						dataRow.push(vvo.length);
					} else {
						dataRow.push("");
					}
					
					
					for ( var j = 0; j < characteristicNames.length; j++) {

						var dataRowValue = "";

						for ( var char in vvo.characteristics) {
							if (char == characteristicNames[j]) {
								dataRowValue = vvo.characteristics[char].value;								
								break;
							}
						}
						
						
						dataRow.push(dataRowValue);
					}

					data.push(dataRow);
				}
				
				ASPIREdb.TextDataDownloadWindow.initAndShow (data, headers);
				
			}
		
			});
		});
		
	},
	
	
	/**
	 * Triggered by Preview query button in Filter Window
	 * Updates the result count in the filter window
	 */
	updateSpecialProjectValues : function() {

		var ref = this;
		
		ProjectService.getDecipherProject({
			
			callback : function(pvo) {

				ref.decipherProjectValueObject = pvo;

			}
		});
		
		ProjectService.getDgvProject({
			
			callback : function(pvo) {

				ref.dgvProjectValueObject = pvo;

			}
		});

	}
	
});
