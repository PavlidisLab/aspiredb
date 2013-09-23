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

Ext.require([ 'Ext.grid.Panel', 'ASPIREdb.store.GeneStore' ]);

// TODO js documentation
Ext.define('ASPIREdb.view.GeneHitsByVariantGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.geneHitsByVariantGrid',

	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'geneHitsByVariantGridToolbar',
		dock : 'top',
		items : [ {
			xtype : 'button',
			text : 'Save'
		} ]

	} ],

	columns : [ {
		header : 'Gene Symbol',
		dataIndex : 'symbol',
		flex : 1
	}, {
		header : 'Type',
		dataIndex : 'geneBioType',
		flex : 1
	}, {
		header : 'Gene Name',
		dataIndex : 'name',
		flex : 1
	}, {
		header : 'View in Gemma',
		dataIndex : 'linkToGemma',
		flex : 1,
		renderer : function(value) {

			if (value == '') {
				return;
			}

			return '<a href="' + value + '" target="_blank" > <img src="scripts/ASPIREdb/resources/images/gemmaTiny.gif" /> </a>';
		}
	} ],

	store : Ext.create('ASPIREdb.store.GeneStore'),

	initComponent : function() {
		this.callParent();

	},

	enableViewCoexpressionLink : function(vos) {

		if (vos.length < 1) {
			return;
		}

		var geneSymbols = [];

		for ( var i = 0; i < vos.length; i++) {
			var vo = vos[i];
			geneSymbols.push(vo.symbol);
		}

		var url = ASPIREdb.GemmaURLUtils.makeViewGeneNetworkInGemmaURL(geneSymbols);

		//This kind of weird technique is being used because the baked in extjs button href config way was not working
		var viewCoexpressionNetworkInGemmaLink = {
			xtype : 'box',
			itemId : 'viewCoexpressionNetworkButton',
			autoEl : {
				tag : 'a',
				href : url,
				target : '_blank',
				cn : 'View Coexpression Network in Gemma'
			}
		};

		this.getDockedComponent('geneHitsByVariantGridToolbar').add(viewCoexpressionNetworkInGemmaLink);

	}
});
