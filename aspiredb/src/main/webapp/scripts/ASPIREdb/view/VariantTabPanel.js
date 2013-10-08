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

Ext.require([ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel', 'Ext.selection.RowModel', 'ASPIREdb.view.GeneHitsByVariantWindow', 'ASPIREdb.ActiveProjectSettings' ]);

// TODO js documentation
// TODO labels

// TODO button functions
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

	storeFields : [ 'id', 'patientId', 'variantType', 'genomeCoordinates', 'chromosome', 'baseStart', 'baseEnd', 'labelIds', 'type', 'copyNumber', 'cnvLength', 'dbSNPID', 'observedBase', 'referenceBase', 'indelLength' ],

	config : {

		// member variables

		// labels that are displayable
		// { label.id : label.valueObject }
		visibleLabels : {},

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

		// adding buttons to toolbar in filterSubmitHandler because extJS was
		// bugging out when we added the dynamically created grid afterwords
		ASPIREdb.EVENT_BUS.on('filter_submit', this.filterSubmitHandler, this);

		this.saveButton.on('click', function() {
			ref.saveButtonHandler();

		});

	},

	saveButtonHandler : function() {

		var grid = this.getComponent('variantGrid');

		if (grid) {
			ASPIREdb.TextDataDownloadWindow.showVariantsDownload(grid.getStore().getRange(), grid.columnHeaders);
		}

	},

	viewInUCSCHandler : function() {

		UCSCConnector.constructCustomTracksFile(this.getSpanningGenomicRange(this.selectedVariants), ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), function(searchPhrase) {

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

		GeneService.getGenesInsideVariants(this.getSelectedVariantIds(this.selectedVariants), {
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

					var fieldData = [];

					for ( var i = 0; i < ref.storeFields.length; i++) {

						if (ref.storeFields[i] == 'chromosome' || ref.storeFields[i] == 'baseStart' || ref.storeFields[i] == 'baseEnd' || ref.storeFields[i] == 'indelLength') {
							fieldData.push({
								name : ref.storeFields[i],
								type : 'int'
							});
						} else {
							fieldData.push({
								name : ref.storeFields[i],
								type : 'string'
							});
						}
					}

					characteristicNames = [];

					for ( var i = 0; i < properties.length; i++) {

						if (properties[i].characteristic) {
							fieldData.push(properties[i].displayName);
							characteristicNames.push(properties[i].name);
						}

					}

					ref.remove('variantGrid', true);

					var grid = ref.createVariantGrid(ref.constructVariantStoreData(vvos, characteristicNames), fieldData, characteristicNames);

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

	createVariantGrid : function(storeData, fieldData, characteristicNames) {

		var store = Ext.create('Ext.data.ArrayStore', {
			fields : fieldData,
			data : storeData,
			groupField : 'patientId'
		});

		var columnHeaders = [ 'Patient Id', 'Type', 'Genome Coordinates', 'Copy Number', 'CNV Type', 'CNV Length', 'DB SNP ID', 'Observed Base', 'Reference Base', 'Indel Length' ];
		var columnConfig = [];

		columnConfig.push({
			text : 'Patient Id',
			flex : 1,
			dataIndex : 'patientId'
		});

		columnConfig.push({
			text : 'Type',
			flex : 1,
			dataIndex : 'variantType'
		});

		columnConfig.push({
			text : 'Genome Coordinates',
			flex : 1,
			dataIndex : 'genomeCoordinates'
		});

		columnConfig.push({
			text : 'Chromosome',
			flex : 1,
			dataIndex : 'chromosome',
			hidden : true
		});

		columnConfig.push({
			text : 'Base Start',
			flex : 1,
			dataIndex : 'baseStart',
			hidden : true
		});

		columnConfig.push({
			text : 'Base End',
			flex : 1,
			dataIndex : 'baseEnd',
			hidden : true
		});

		columnConfig.push({
			text : "Labels",
			dataIndex : 'labelIds',
			renderer : function(value) {
				var ret = "";
				for ( var i = 0; i < value.length; i++) {
					var label = this.up('#variantTabPanel').visibleLabels[value[i]];
					if (label == undefined) {
						continue;
					}
					if (label.isShown) {

						ret += "<span style='background-color: " + label.colour + "'>" + label.name + "</span>&nbsp;";
					}
				}
				return ret;
			},
			flex : 1
		});

		columnConfig.push({
			text : 'Copy Number',
			flex : 1,
			dataIndex : 'copyNumber',
			hidden : true

		});

		columnConfig.push({
			text : 'CNV Type',
			flex : 1,
			dataIndex : 'type',
			hidden : true
		});

		columnConfig.push({
			text : 'CNV Length',
			flex : 1,
			dataIndex : 'cnvLength',
			hidden : true
		});

		columnConfig.push({
			text : 'DB SNP ID',
			flex : 1,
			dataIndex : 'dbSNPID',
			hidden : true
		});

		columnConfig.push({
			text : 'Observed Base',
			flex : 1,
			dataIndex : 'observedBase',
			hidden : true
		});

		columnConfig.push({
			text : 'Reference Base',
			flex : 1,
			dataIndex : 'referenceBase',
			hidden : true
		});

		columnConfig.push({
			text : 'Indel Length',
			flex : 1,
			dataIndex : 'indelLength',
			hidden : true
		});

		for ( var i = 0; i < characteristicNames.length; i++) {

			var config = {};

			config.text = characteristicNames[i];
			config.flex = 1;
			config.dataIndex = characteristicNames[i];
			config.hidden = true;

			columnConfig.push(config);

			columnHeaders.push(characteristicNames[i]);

		}

		// TODO styling
		grid = Ext.create('Ext.grid.Panel', {
			store : store,
			itemId : 'variantGrid',
			columns : columnConfig,
			columnHeaders : columnHeaders,
			selModel : Ext.create('Ext.selection.RowModel', {
				mode : 'MULTI'
			}),
			stripeRows : true,
			height : 180,
			width : 500,
			title : 'Table View',
			requires : [ 'Ext.grid.feature.Grouping' ],
			features : [ Ext.create('Ext.grid.feature.Grouping', {
				groupHeaderTpl : '{name} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
			}) ]

		});

		return grid;
	},

	constructVariantStoreData : function(vvos, characteristicNames) {

		var storeData = [];
		this.visibleLabels = {};

		for ( var i = 0; i < vvos.length; i++) {

			var vvo = vvos[i];

			var dataRow = [];

			dataRow.push(vvo.id);

			dataRow.push(vvo.patientId);

			dataRow.push(vvo.variantType);
			dataRow.push(vvo.genomicRange.chromosome + ":" + vvo.genomicRange.baseStart + "-" + vvo.genomicRange.baseEnd);
			dataRow.push(vvo.genomicRange.chromosome);
			dataRow.push(vvo.genomicRange.baseStart);
			dataRow.push(vvo.genomicRange.baseEnd);

			// create only one unique label instance
			var labels = [];
			for ( var j = 0; j < vvo.labels.length; j++) {
				var aLabel = this.visibleLabels[vvo.labels[j].id];
				if (aLabel == undefined) {
					aLabel = vvo.labels[j];
					this.visibleLabels[aLabel.id] = aLabel;
				}
				labels.push(aLabel.id);
			}

			dataRow.push(labels);

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

			storeData.push(dataRow);
		}

		return storeData;

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

						addedLabel.isShown = true;
						LabelService.updateLabel(addedLabel);

						var existingLab = me.visibleLabels[addedLabel.id];
						if (existingLab == undefined) {
							me.visibleLabels[addedLabel.id] = addedLabel;
						} else {
							existingLab.isShown = true;
						}

						// update local store
						for ( var i = 0; i < me.selectedVariants.length; i++) {
							me.selectedVariants[i].get('labelIds').push(addedLabel.id);
						}

						// refresh grid
						me.down('#variantGrid').store.sync();
						me.down('#variantGrid').getView().refresh();
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
			visibleLabels : me.visibleLabels,
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
