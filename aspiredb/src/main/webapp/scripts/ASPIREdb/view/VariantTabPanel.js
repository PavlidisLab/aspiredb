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

Ext.require([ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel' ]);

// TODO js documentation
// TODO labels

// TODO button functions
Ext.define('ASPIREdb.view.VariantTabPanel', {
	extend : 'Ext.tab.Panel',
	alias : 'widget.variantTabPanel',
	title : 'Variant',

	tbar : [ {
		xtype : 'button',
		text : 'Labels'
	}, {
		xtype : 'button',
		text : 'Actions'
	}, {
		xtype : 'button',
		text : 'Select All'
	}, {
		xtype : 'button',
		text : 'Ideogram settings'
	}, {
		xtype : 'button',
		text : 'Save'
	}, ],

	items : [ {
		xtype : 'ideogram',
		itemId : 'ideogram'
	} ],

	defaultGridFields : [ 'patientId', 'variantType', 'genomeCoordinates', 'type', 'copyNumber', 'cnvLength', 'dbSNPID', 'observedBase', 'referenceBase', 'indelLength' ],

	initComponent : function() {
		this.callParent();

		var ref = this;

		ASPIREdb.EVENT_BUS.on('filter_submit', function(filterConfigs) {

			VariantService.suggestProperties(function(properties) {

				QueryService.queryVariants(filterConfigs, {
					callback : function(pageLoad) {

						var vvos = pageLoad.items;

						var ideogram = ref.getComponent('ideogram');
						ideogram.drawChromosomes();
						ideogram.drawVariants(vvos);

						var fieldData = [];

						for ( var i = 0; i < ref.defaultGridFields.length; i++) {
							fieldData.push(ref.defaultGridFields[i]);
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

						ref.add(grid);

					}
				});

			});

		});
	},

	createVariantGrid : function(storeData, fieldData, characteristicNames) {

		var store = Ext.create('Ext.data.ArrayStore', {
			fields : fieldData,
			data : storeData,
			groupField : 'patientId'
		});

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

		}

		// TODO styling
		grid = Ext.create('Ext.grid.Panel', {
			store : store,
			itemId : 'variantGrid',
			columns : columnConfig,
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
