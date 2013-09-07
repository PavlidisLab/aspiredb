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
// TODO just works for generic VariantValueObject, make it work for subtypes (CNV etc.)
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
		xtype : 'ideogram'
	} ],

	initComponent : function() {
		this.callParent();

		var ref = this;

		ASPIREdb.EVENT_BUS.on('filter_submit', function(filterConfigs) {
			QueryService.queryVariants(filterConfigs, {
				callback : function(pageLoad) {

					var vvos = pageLoad.items;
					
					var storeData = [];
					
					var fieldData = ['variantType', 'genomeCoordinates' ];

					var characteristicNames = [];

					for ( var char in vvos[0].characteristics) {

						characteristicNames.push(vvos[0].characteristics[char].key);
						fieldData.push(vvos[0].characteristics[char].key);
						
					}

					for ( var i = 0; i < vvos.length; i++) {

						var vvo = vvos[i];

						var dataRow = [];

						dataRow.push(vvo.variantType);
						dataRow.push(vvo.genomicRange.chromosome + ":" + vvo.genomicRange.baseStart + "-"
								+ vvo.genomicRange.baseEnd);

						for ( var char in vvo.characteristics) {

							dataRow.push(vvo.characteristics[char].value);

						}

						storeData.push(dataRow);

					}

					var grid = ref.createVariantGrid(storeData, fieldData,
							characteristicNames);

					ref.add(grid);

				}
			});
		});
	},

	createVariantGrid : function(storeData, fieldData,charColumnNames) {

		var store = Ext.create('Ext.data.ArrayStore', {
			fields : fieldData,
			data : storeData
		});

		var columnConfig = [];

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

		for ( var i = 0; i < charColumnNames.length; i++) {

			var config ={};

			config.text = charColumnNames[i];
			config.flex = 1;
			config.dataIndex = charColumnNames[i];

			columnConfig.push(config);

		}

		//TODO grouping, styling
		grid = Ext.create('Ext.grid.Panel', {
			store : store,
			columns : columnConfig,
			stripeRows : true,
			height : 180,
			width : 500,

			title : 'Table View',
			requires : [ 'Ext.grid.feature.Grouping' ]
		});
		
		return grid;
	}

});
