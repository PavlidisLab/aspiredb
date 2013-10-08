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

//This grid is created dynamically based on 'suggested properties'( see VariantService.suggestProperties) this dynamic nature is why this grid is constructed this way
// TODO js documentation
Ext.define('ASPIREdb.view.VariantGridCreator', {
	extend : 'Ext.util.Observable',
	singleton: true,

	storeFields : [ 'id', 'patientId', 'variantType', 'genomeCoordinates', 'chromosome', 'baseStart', 'baseEnd', 'labelIds', 'type', 'copyNumber', 'cnvLength', 'dbSNPID', 'observedBase', 'referenceBase', 'indelLength' ],

	initComponent : function() {
		this.callParent();

	},

	createVariantGrid : function(vvos, properties) {
		
		var fieldData = [];
		
		var dataIndexes = this.storeFields;
		
		var visibleLabels = {};

		for ( var i = 0; i < dataIndexes.length; i++) {

			if (dataIndexes[i] == 'chromosome' || dataIndexes[i] == 'baseStart' || dataIndexes[i] == 'baseEnd' || dataIndexes[i] == 'indelLength') {
				fieldData.push({
					name : dataIndexes[i],
					type : 'int'
				});
			} else {
				fieldData.push({
					name : dataIndexes[i],
					type : 'auto'
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
		
		var storeData = this.constructVariantStoreData(vvos, characteristicNames, visibleLabels);
		
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
					
					var label = this.visibleLabels[value[i]];
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
			}) ],
			visibleLabels : visibleLabels

		});

		return grid;
	},

	constructVariantStoreData : function(vvos, characteristicNames, visibleLabels) {

		var storeData = [];
		

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
				var aLabel = visibleLabels[vvo.labels[j].id];
				if (aLabel == undefined) {
					aLabel = vvo.labels[j];
					visibleLabels[aLabel.id] = aLabel;
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

	}
	
});