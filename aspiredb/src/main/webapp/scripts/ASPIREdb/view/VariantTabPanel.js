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

Ext.require([ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel', 'Ext.selection.RowModel', 'ASPIREdb.view.GeneHitsByVariantWindow' ]);

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

	id    : 'variantTabPanel',


	items : [ {
		xtype : 'ideogram',
		itemId : 'ideogram'
	} ],

	storeFields : [ 'id', 'patientId', 'variantType', 'genomeCoordinates', 'labelIds', 'type', 'copyNumber', 'cnvLength', 'dbSNPID', 'observedBase', 'referenceBase', 'indelLength' ],

	config : {
	
		// member variables 
		
		// labels that are displayable
		// { label.id : label.valueObject }
		visibleLabels : {},
		
		// selected subjects ids the grid
		selectedVariantIds : [],
		
		// selected subjects records in the grid
		selectedVariants : [],
		
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
				handler : this.viewGenesHandler,
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
		
		this.saveButton = Ext.create('Ext.Button',{			
			id : 'saveButton',
			text : '',
			tooltip : 'Download table contents as text',
			icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'
						
		} );

		//adding buttons to toolbar in filterSubmitHandler because extJS was bugging out when we added the dynamically created grid afterwords
		ASPIREdb.EVENT_BUS.on('filter_submit', this.filterSubmitHandler, this);		
		
		this.saveButton.on('click', function(){
			ref.saveButtonHandler();
						
		});
		
		
	},
	
	saveButtonHandler : function() {
		
		var grid = this.getComponent('variantGrid');
		
		if (grid){
		
			ASPIREdb.TextDataDownloadWindow.showVariantsDownload(grid.getStore().getRange(), grid.columnHeaders);
		
		}
		

	},
	
	viewGenesHandler : function() {
		
		ASPIREdb.view.GeneHitsByVariantWindow.clearGridAndMask();		
		ASPIREdb.view.GeneHitsByVariantWindow.show();
		
		GeneService.getGenesInsideVariants(this.selectedVariantIds, {
			callback : function(vos) {
				ASPIREdb.view.GeneHitsByVariantWindow.populateGrid(vos);
			}
		});

	},

	selectionChangeHandler : function(model, records) {

		var grid = this.getComponent('variantGrid');
		if (!grid)
			return;

		var viewGenesButton = this.actionsMenu.getComponent('viewGenes');
		var viewInUCSCButton = this.actionsMenu.getComponent('viewInUCSC');

		if (records.length > 0) {
			viewGenesButton.enable();
			this.down('#makeLabel').enable();
		} else {
			viewGenesButton.disable();
			viewInUCSCButton.disable();
			this.down('#makeLabel').disable();
			return;
		}

		if (this.areOnSameChromosome(records)) {
			viewInUCSCButton.enable();
		} else {
			viewInUCSCButton.disable();
		}

		this.selectedVariantIds = [];
		this.selectedVariants = [];
		
		// needed for 'View Genes' button
		for ( var i = 0; i < records.length; i++) {
			this.selectedVariantIds.push(records[i].data.id);
			this.selectedVariants.push(records[i]);
		}

	},

	areOnSameChromosome : function(records) {

		if (records.length < 1)
			return false;

		var chromosome = this.getChromosomeFromGenomicRangeString(records[0].data.genomeCoordinates);

		for ( var i = 0; i < records.length; i++) {

			var otherChromosome = this.getChromosomeFromGenomicRangeString(records[i].data.genomeCoordinates);

			if (chromosome !== otherChromosome) {
				return false;
			}

		}

		return true;

	},

	getChromosomeFromGenomicRangeString : function(grString) {

		return grString.slice(0, grString.indexOf(':'));

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
						fieldData.push(ref.storeFields[i]);
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

		
		var columnHeaders = ['Patient Id','Type','Genome Coordinates','Copy Number','CNV Type','CNV Length','DB SNP ID','Observed Base','Reference Base','Indel Length'];
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
						
						ret += "<span style='background-color: "
								+ label.colour + "'>" + label.name
								+ "</span>&nbsp;";
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
				VariantService.addLabel(me.selectedVariantIds, vo, {
					errorHandler : function( message ) {
						alert('Error adding variant label. ' + message);
					},
					callback : function(addedLabel) {
						
						addedLabel.isShown = true;
						LabelService.updateLabel(addedLabel);
		
						var existingLab = me.visibleLabels[addedLabel.id];
						if ( existingLab == undefined ) {
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

});
