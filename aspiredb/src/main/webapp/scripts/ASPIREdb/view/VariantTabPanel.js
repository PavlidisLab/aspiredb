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

Ext.require([ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel', 'Ext.selection.RowModel', 'ASPIREdb.view.GeneHitsByVariantWindow', 'ASPIREdb.ActiveProjectSettings', 'ASPIREdb.view.VariantGridCreator' , 'ASPIREdb.IdeogramDownloadWindow']);

// TODO js documentation

Ext.define('ASPIREdb.view.VariantTabPanel', {
	extend : 'Ext.tab.Panel',
	alias : 'widget.variantTabPanel',
	title : 'Variant',

	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'variantTabPanelToolbar',
		dock : 'top'

	} ],

	id : 'variantTabPanel',

	items : [ {
		xtype : 'ideogram',
		itemId : 'ideogram'
	} ],

	config : {
		// selected subjects records in the grid
		selectedVariants : [],
		loadedSubjects : [],
		selectedSubjectVariants: [],
		
	},

	constructor : function(cfg) {
		this.initConfig(cfg);
		this.callParent(arguments);
	},

	initComponent : function() {
		this.callParent();

		var ref = this;

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

		this.actionsMenu = Ext.create('Ext.menu.Menu', {
			items : [ {
				itemId : 'viewInUCSC',
				text : 'View in UCSC',
				disabled : true,
				handler : this.viewInUCSCHandler,
				scope : this
			}, {
				itemId : 'viewGenes',
				text : 'View Genes',
				disabled : true,
				handler : this.viewGenesHandler,
				scope : this
			} ]
		});

		this.actionsButton = Ext.create('Ext.Button', {
			text : '<b>Actions</b>',
			itemId : 'actionsButton',
			menu : this.actionsMenu
		});
		
		this.selectAllButton = Ext.create('Ext.Button', {
			itemId : 'selectAll',
			text : 'Select All',
			disabled : true,
			handler : this.selectAllHandler,
			scope : this
		});

		this.saveButton = Ext.create('Ext.Button', {
			id : 'saveButton',
			text : '',
			tooltip : 'Download table contents as text',
			icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'

		});
		
		this.exportButton = Ext.create('Ext.Button', {
			id : 'exportButton',
			text : '',
			tooltip : 'Download ideogram as png',
			icon : 'scripts/ASPIREdb/resources/images/icons/export.png'

		});

		// adding buttons to toolbar in filterSubmitHandler with the grid
		// because extJS was
		// bugging out when we added the dynamically created grid afterwords
		ASPIREdb.EVENT_BUS.on('filter_submit', this.filterSubmitHandler, this);

		ASPIREdb.EVENT_BUS.on('label_change', function() {
			ref.down('#variantGrid').getView().refresh();
		});
		
		ASPIREdb.EVENT_BUS.on('subjects_loaded', function(subjectIds) {
			ref.loadedSubjects =subjectIds;
		});
			
		//when subjects selected it is focused in variant grid
		ASPIREdb.EVENT_BUS.on('subject_selected', this.subjectSubmitHandler, this); 
		
		
		this.saveButton.on('click', function() {
			ref.saveButtonHandler();

		});
		
		this.exportButton.on('click', function() {
			ref.exportButtonHandler();

		});

		// selection is GenomicRange{baseEnd, baseStart, chromosome}
		this.getComponent('ideogram').on('GenomeRegionSelectionEvent', function(selection) {
			ref.ideogramSelectionChangeHandler(null, ref.getVariantRecordSelection());
		});

		// activate/deactiveButtons based on activeTab
		this.on('beforetabchange', function(tabPanel, newCard, oldCard, eOpts) {

			var currentlySelectedRecords = [];

			if (newCard.itemId == 'ideogram') {

				currentlySelectedRecords = this.getIdeogramVariantRecordSelection();
				this.selectAllButton.disable();

			} else {
				// newCard is the grid
				currentlySelectedRecords = this.selectedVariants;
				this.selectAllButton.enable();
				
			}

			this.enableActionButtonsBySelectedRecords(currentlySelectedRecords);

		});

	},

	filterSubmitHandler : function(filterConfigs) {

		var ref = this;

		ref.setLoading(true);
		
		VariantService.suggestProperties(function(properties) {

			QueryService.queryVariants(filterConfigs, {
				callback : function(pageLoad) {

					
					var vvos = pageLoad.items;
					
					ProjectService.numVariants(filterConfigs[0].projectIds, {
						callback : function(NoOfVariants){
							if (NoOfVariants > vvos.length){
								ref.setTitle( "Variant :"+vvos.length+" of "+NoOfVariants +" filtered");
							}
							else if  (NoOfVariants == vvos.length)
								ref.setTitle( "Variant") ;
						}
					});
					
					var ideogram = ref.getComponent('ideogram');
					ideogram.drawChromosomes();
					ideogram.drawVariants(vvos);
					
					
					
					var grid = ASPIREdb.view.VariantGridCreator.createVariantGrid(vvos, properties);	
					
					
					
					ref.remove('variantGrid', true);

					grid.on('selectionchange', ref.selectionChangeHandler, ref);
					
					grid.on('show', function() {

						if (ref.newIdeogramLabel) {
							grid.getView().refresh();
							ref.newIdeogramLabel = undefined;
						}
												
					});
					
													
					ref.add(grid);
					

					var toolbar = ref.getDockedComponent('variantTabPanelToolbar');

					toolbar.add(ref.actionsButton);
					toolbar.add(ref.labelsButton);
					toolbar.add(ref.selectAllButton);
					toolbar.add(ref.saveButton);
					toolbar.add(ref.exportButton);
					
					ref.setLoading(false);

				}
			});

		});

	},
	
	subjectSubmitHandler :function(subjectIds) {
		
		var projectIds= ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
		
		var grid = this.down('#variantGrid');
		
		//when variant table view is selected
		if (grid.isVisible()){
			
						
			//collapse all the records already expanded		
			SubjectService.getSubjects(projectIds[0],this.loadedSubjects, {
				callback : function(subjectValueObjects) {
				
					for ( var i = 0; i < subjectValueObjects.length ; i++) {
						var subjectValueObject = subjectValueObjects[i];
						grid.features[0].collapse(subjectValueObject.patientId); 
					}
				
					//expand only the selected subjects
					SubjectService.getSubjects(projectIds[0],subjectIds, {
						callback : function(subjectValueObjects) {
							for ( var i = 0; i < subjectValueObjects.length ; i++) {
								var subjectValueObject = subjectValueObjects[i];
								grid.features[0].expand(subjectValueObject.patientId, true);
								this.selectedSubjectVariants=subjectValueObject.patientId;
							}
						}
					});
				}
			});
		}
		//When Ideogram view selected
		else {
			var ideogram = this.getComponent('ideogram');
			var selectedSubjectVariants=[];
			//heighlight the selected subject in ideogram
			SubjectService.getSubjects(projectIds[0],subjectIds, {
				callback : function(subjectValueObjects) {
					for ( var i = 0; i < subjectValueObjects.length ; i++) {
						var subjectValueObject = subjectValueObjects[i];
								 
						VariantService.getSubjectsVariants(subjectValueObject.patientId, {
								callback : function(vvo) {
									ideogram.drawVariantsWithSubjectHighlighted(subjectValueObject.id, vvo);
									}
						});
					}
				}
			});
			
			
			
		}
			
			
		grid.getView().refresh();
		
		
	},

	selectionChangeHandler : function(model, records) {

		this.selectedVariants = records;

		this.enableActionButtonsBySelectedRecords(records);

	},
	
	selectAllHandler : function() {

		//boolean true to suppressEvent
		this.getComponent('variantGrid').getSelectionModel().selectAll(true);
		
		this.selectionChangeHandler(this.getComponent('variantGrid').getSelectionModel(), this.getComponent('variantGrid').getSelectionModel().getSelection());

	},

	ideogramSelectionChangeHandler : function(model, records) {

		this.enableActionButtonsBySelectedRecords(records);

	},

	saveButtonHandler : function() {

		var grid = this.getComponent('variantGrid');

		if (grid) {
			ASPIREdb.TextDataDownloadWindow.showVariantsDownload(grid.getStore().getRange(), grid.columnHeaders);
		}

	},
	
	exportButtonHandler : function() {

		var ideogram = this.getComponent('ideogram');
		var canvas=ideogram.getComponent('canvasBox');
		var imgsrc    = canvas.el.dom.toDataURL('image/png');
        
		if (imgsrc) {
			ASPIREdb.IdeogramDownloadWindow.showIdeogramDownload(imgsrc);
		}

	},

	viewGenesHandler : function() {
		ASPIREdb.view.GeneHitsByVariantWindow.clearGridAndMask();
		ASPIREdb.view.GeneHitsByVariantWindow.initGridAndShow(this.getSelectedVariantIds(this.getVariantRecordSelection()));
	},

	viewInUCSCHandler : function() {

		UCSCConnector.constructCustomTracksFile(this.getSpanningGenomicRange(this.getVariantRecordSelection()), ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), function(searchPhrase) {

			var ucscForm = Ext.create('Ext.form.Panel', {

			});

			ucscForm.submit({
				target : '_blank',
				url : "http://genome.ucsc.edu/cgi-bin/hgCustom",
				standardSubmit : true,
				method : "POST",
				params : {
					clade : 'mammal',
					org : 'Human',
					db : 'hg19',
					hgct_customText : searchPhrase
				},
				success : function() {
					console.log("ok");
				},
				failure : function(response, opts) {
					console.log("failed");
				},
				headers : {
					'Content-Type' : 'multipart/form-data'
				}
			});

		});

	},

	enableActionButtonsBySelectedRecords : function(records) {

		if (records.length > 0) {

			this.down('#viewGenes').enable();
			this.down('#makeLabel').enable();
		} else {

			this.down('#viewGenes').disable();
			this.down('#viewInUCSC').disable();
			this.down('#makeLabel').disable();
			return;
		}

		if (this.areOnSameChromosome(records)) {
			this.down('#viewInUCSC').enable();
		} else {
			this.down('#viewInUCSC').disable();
		}

	},

	getVariantRecordSelection : function() {

		if (this.getActiveTab().itemId == 'ideogram') {

			return this.getIdeogramVariantRecordSelection();

		} else {
			return this.selectedVariants;
		}

	},

	getIdeogramVariantRecordSelection : function() {

		var ideogram = this.getComponent('ideogram');

		var ideogramGenomicRange = ideogram.getSelection();

		if (ideogramGenomicRange == null) {
			return [];
		}

		var grid = this.getComponent('variantGrid');

		var records = grid.getStore().getRange();

		var variantRecordsInsideRange = [];

		for ( var i = 0; i < records.length; i++) {

			var genomicRange = {
				chromosome : records[i].data.chromosome,
				baseStart : records[i].data.baseStart,
				baseEnd : records[i].data.baseEnd
			};

			if (this.secondGenomicRangeIsWithinFirst(genomicRange, ideogramGenomicRange)) {
				variantRecordsInsideRange.push(records[i]);
			}

		}

		return variantRecordsInsideRange;

	},

	/**
	 * Assigns a Label
	 * 
	 */
	makeLabelHandler : function(event) {

		var me = this;

		Ext.define('ASPIREdb.view.CreateLabelWindowVariant', {
			isSubjectLabel : false,
			extend : 'ASPIREdb.view.CreateLabelWindow',

			// override
			onOkButtonClick : function() {
				this.callParent();

				var vo = this.getLabel();

				var idsToLabel = [];

				idsToLabel = me.getSelectedVariantIds(me.getVariantRecordSelection());

				// store in database
				VariantService.addLabel(idsToLabel, vo, {
					errorHandler : function(message) {
						alert('Error adding variant label. ' + message);
					},
					callback : function(addedLabel) {

						var grid = me.down('#variantGrid');

						addedLabel.isShown = true;
						LabelService.updateLabel(addedLabel);

						var existingLab = grid.visibleLabels[addedLabel.id];
						if (existingLab === undefined) {
							grid.visibleLabels[addedLabel.id] = addedLabel;
						} else {
							existingLab.isShown = true;
						}

						var currentlySelectedRecords = me.getVariantRecordSelection();

						// update local store
						for ( var i = 0; i < currentlySelectedRecords.length; i++) {
							var labelIds = currentlySelectedRecords[i].get('labelIds');
							labelIds.push(addedLabel.id);
						}

						if (me.getActiveTab().itemId == 'ideogram') {
							// refreshing grid doesn't work if it is not the
							// active tab so set flag to refresh on grid 'show'
							// event
							me.newIdeogramLabel = true;

						} else {
							// refresh grid
							grid.getView().refresh();
						}

					}
				});
			},
		});

		var labelWindow = new ASPIREdb.view.CreateLabelWindowVariant();
		labelWindow.show();
	},

	/**
	 * Display LabelSettingsWindow
	 */
	labelSettingsHandler : function(event) {
		var me = this;

		var labelControlWindow = Ext.create('ASPIREdb.view.LabelControlWindow', {
			visibleLabels : me.down('#variantGrid').visibleLabels,
			isSubjectLabel : false,
		});

		labelControlWindow.show();
	},

	getSelectedVariantIds : function(selectedVariantRecords) {

		var selectedVariantIds = [];

		for ( var i = 0; i < selectedVariantRecords.length; i++) {
			selectedVariantIds.push(selectedVariantRecords[i].data.id);
		}

		return selectedVariantIds;
	},

	areOnSameChromosome : function(records) {

		if (records.length < 1)
			return false;

		var chromosome = records[0].data.chromosome;

		for ( var i = 0; i < records.length; i++) {

			var otherChromosome = records[i].data.chromosome;

			if (chromosome !== otherChromosome) {
				return false;
			}

		}

		return true;

	},

	secondGenomicRangeIsWithinFirst : function(first, other) {
		if (first.chromosome == other.chromosome) {
			if (first.baseStart >= other.baseStart && first.baseEnd <= other.baseEnd) {
				return true;
			}
		}
		return false;
	},

	getSpanningGenomicRange : function(selectedVariants) {
		if (!this.areOnSameChromosome(selectedVariants))
			return;

		var start = 2147483647;
		var end = -2147483648;
		var chromosome = selectedVariants[0].data.chromosome;
		for ( var i = 0; i < selectedVariants.length; i++) {

			var variant = selectedVariants[i].data;

			if (variant.chromosome == chromosome) {
				if (variant.baseStart < start)
					start = variant.baseStart;
				if (variant.baseEnd > end)
					end = variant.baseEnd;
			}
		}

		// Genomic Range
		return {
			chromosome : chromosome,
			baseStart : start,
			baseEnd : end
		};

	}

});
