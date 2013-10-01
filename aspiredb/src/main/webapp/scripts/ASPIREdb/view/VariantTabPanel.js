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

	items : [ {
		xtype : 'ideogram',
		itemId : 'ideogram'
	} ],

	storeFields : [ 'id', 'patientId', 'variantType', 'genomeCoordinates', 'type', 'copyNumber', 'cnvLength', 'dbSNPID', 'observedBase', 'referenceBase', 'indelLength' ],

	initComponent : function() {
		this.callParent();

		var ref = this;

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

		if (records.length > 0) {
			viewGenesButton.enable();
		} else {
			viewGenesButton.disable();
			viewInUCSCButton.disable();
			return;
		}

		var viewInUCSCButton = this.actionsMenu.getComponent('viewInUCSC');

		if (this.areOnSameChromosome(records)) {
			viewInUCSCButton.enable();
		} else {
			viewInUCSCButton.disable();
		}

		this.selectedVariantIds = [];

		// needed for 'View Genes' button
		for ( var i = 0; i < records.length; i++) {
			this.selectedVariantIds.push(records[i].data.id);
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

					var characteristicNames = [];

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

		for ( var i = 0; i < vvos.length; i++) {

			var vvo = vvos[i];

			var dataRow = [];

			dataRow.push(vvo.id);

			dataRow.push(vvo.patientId);

			dataRow.push(vvo.variantType);
			dataRow.push(vvo.genomicRange.chromosome + ":" + vvo.genomicRange.baseStart + "-" + vvo.genomicRange.baseEnd);

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

	}

});
