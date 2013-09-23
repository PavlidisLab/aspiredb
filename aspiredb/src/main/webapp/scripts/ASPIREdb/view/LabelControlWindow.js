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
Ext.require([ 'Ext.Window' ]);

Ext.define('ASPIREdb.view.LabelControlWindow', {
	extend : 'Ext.Window',
	alias : 'widget.labelControlWindow',
	title : 'Label settings',
	id : 'labelControlWindow',
	closable : true,
	closeAction : 'hide',
	layout : 'border',
	bodyStyle : 'padding: 5px;',
	layout: 'fit',
    width : 300,
    height : 300,
	items : [{
		xtype : 'grid',
		itemId : 'labelSettingsGrid',
		store : Ext.create('ASPIREdb.store.LabelStore'),
		columns : [ {
			header : 'Label',
			dataIndex : 'label',
			flex : 1,
			renderer : function(value) {
				//value is a LabelValueObject
				
				var ret = "";
				//for ( var i = 0; i < value.length; i++) {
					ret += "<span style='background-color: "
							+ value.colour + "'>" + value.name
							+ "</span>&nbsp;";
				//}
				return ret;
			},
		}, {
			header : 'Show',
			dataIndex : 'show',
			flex : 1
		}, {
			header : 'Action',
			xtype:'actioncolumn',
            width:50,
            items: [{
                icon: 'scripts/ASPIREdb/resources/images/icons/delete.png',  // Use a URL in the icon config
                tooltip: 'Delete',
                handler: function(grid, rowIndex, colIndex) {
                    var rec = grid.getStore().getAt(rowIndex);
                    alert("Edit " + rec.get('label').name);
                }
            }]	
         } ],
	}],

	initComponent : function() {
		
		this.callParent();
		
		var me = this;

		/*var grid = Ext.create('Ext.grid.Panel', {
			itemId : 'labelSettingsGrid',
			store : Ext.create('ASPIREdb.store.LabelStore'),
			columns : [ {
				header : 'Label',
				dataIndex : 'label',
				flex : 1
			}, {
				header : 'Show',
				dataIndex : 'show',
				flex : 1
			} ],
		});
		
		this.items = [ grid ];*/

		SubjectService.suggestLabels(null, {
			callback : function(pageLoad) {
				var labelValueObjects = pageLoad;

				var data = [];
				for ( var i = 0; i < labelValueObjects.length; i++) {
					var val = labelValueObjects[i];
					var show = true;
					var row = [ val, show ];
					data.push(row);
				}
				me.down('#labelSettingsGrid').store.loadData(data);
				//this.items[0].store.loadData(data);
			}
		});
	},

});