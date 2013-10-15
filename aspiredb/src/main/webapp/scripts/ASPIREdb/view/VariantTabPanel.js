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

Ext.require([ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel', 'Ext.selection.RowModel', 'ASPIREdb.view.GeneHitsByVariantWindow', 'ASPIREdb.ActiveProjectSettings', 'ASPIREdb.view.VariantGridCreator' ]);

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
		selectedVariants : []

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

		this.saveButton = Ext.create('Ext.Button', {
			id : 'saveButton',
			text : '',
			tooltip : 'Download table contents as text',
			icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'

		});

		// adding buttons to toolbar in filterSubmitHandler with the grid because extJS was
		// bugging out when we added the dynamically created grid afterwords
		ASPIREdb.EVENT_BUS.on('filter_submit', this.filterSubmitHandler, this);

		this.saveButton.on('click', function() {
			ref.saveButtonHandler();

		});
		
		//selection is GenomicRange{baseEnd, baseStart, chromosome}
		this.getComponent('ideogram').on('GenomeRegionSelectionEvent', function(selection){
			ref.selectionChangeHandler(null,ref.getVariantRecordSelection());
		});
		
		
		

	},

	saveButtonHandler : function() {

		var grid = this.getComponent('variantGrid');

		if (grid) {
			ASPIREdb.TextDataDownloadWindow.showVariantsDownload(grid.getStore().getRange(), grid.columnHeaders);
		}

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

	},

	viewGenesHandler : function() {

		ASPIREdb.view.GeneHitsByVariantWindow.clearGridAndMask();
		ASPIREdb.view.GeneHitsByVariantWindow.show();

		GeneService.getGenesInsideVariants(this.getSelectedVariantIds(this.getVariantRecordSelection()), {
			callback : function(vos) {
				
				ASPIREdb.view.GeneHitsByVariantWindow.populateGrid(vos);
			}
		});

	},

	selectionChangeHandler : function(model, records) {

		this.selectedVariants = records;

		var grid = this.getComponent('variantGrid');
		if (!grid)
			return;

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
	
	secondGenomicRangeIsWithinFirst : function(first, other) {
        if (first.chromosome == other.chromosome) {
            if (first.baseStart >= other.baseStart && first.baseEnd <= other.baseEnd) {
                return true;
            }
        }
        return false;
    },
	
	getVariantRecordSelection : function() {
		
		if (this.getActiveTab().itemId == 'ideogram'){
			
			var ideogram = this.getComponent('ideogram');
			
			var ideogramGenomicRange = ideogram.getSelection();
			
			if (ideogramGenomicRange == null){
				return [];
			}
			
			var grid = this.getComponent('variantGrid');
			
			var records= grid.getStore().getRange();
			
			var variantRecordsInsideRange = [];
			
			for (var i = 0 ; i < records.length ; i++){
				
				var genomicRange = {chromosome : records[i].data.chromosome,
									baseStart : records[i].data.baseStart,
									baseEnd : records[i].data.baseEnd};
				
				if (this.secondGenomicRangeIsWithinFirst(genomicRange, ideogramGenomicRange )){
					variantRecordsInsideRange.push(records[i]);
				}
				
			}
			
			return variantRecordsInsideRange;
			
			
		}else{
			return this.selectedVariants;
		}
		
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

	filterSubmitHandler : function(filterConfigs) {

		var ref = this;

		VariantService.suggestProperties(function(properties) {

			QueryService.queryVariants(filterConfigs, {
				callback : function(pageLoad) {

					var vvos = pageLoad.items;

					var ideogram = ref.getComponent('ideogram');
					ideogram.drawChromosomes();
					ideogram.drawVariants(vvos);

					var grid = ASPIREdb.view.VariantGridCreator.createVariantGrid(vvos, properties);

					ref.remove('variantGrid', true);

					grid.on('selectionchange', ref.selectionChangeHandler, ref);

					ref.add(grid);

					var toolbar = ref.getDockedComponent('variantTabPanelToolbar');

					toolbar.add(ref.actionsButton);
					toolbar.add(ref.labelsButton);
					toolbar.add(ref.saveButton);

				}
			});

		});

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

				// store in database
				VariantService.addLabel(me.getSelectedVariantIds(me.selectedVariants), vo, {
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

						// update local store
						for ( var i = 0; i < me.selectedVariants.length; i++) {							
							var labelIds = me.selectedVariants[i].get('labelIds');
							labelIds.push(addedLabel.id);
						}

						// refresh grid
						grid.store.sync();
						grid.getView().refresh();
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

		labelControlWindow.on('destroy', function(btn, e, eOpts) {
			me.down('#variantGrid').getView().refresh();
		}, this);

		labelControlWindow.show();
	},

	getSelectedVariantIds : function(selectedVariantRecords) {

		var selectedVariantIds = [];

		for ( var i = 0; i < selectedVariantRecords.length; i++) {
			selectedVariantIds.push(selectedVariantRecords[i].data.id);
		}

		return selectedVariantIds;
	}

});
