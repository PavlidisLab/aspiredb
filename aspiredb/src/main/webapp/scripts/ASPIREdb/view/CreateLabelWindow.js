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
Ext.require([ 'Ext.Window', 'Ext.picker.Color', 'Ext.data.ArrayStore',
		'Ext.form.ComboBox', 'Ext.button.Button' ]);

Ext.define('ASPIREdb.view.CreateLabelWindow', {
	extend : 'Ext.Window',
	alias : 'widget.createLabelWindow',
	title : 'Make label',
	closable : true,
	closeAction : 'hide',
	layout : 'border',
	bodyStyle : 'padding: 5px;',
	flex : 1,
	layout : {
		type : 'hbox',
		defaultMargins : {
			top : 5,
			right : 5,
			left : 5,
			bottom : 5
		}
	},

	initComponent : function() {
		var me = this;

		this.items = [ {
			xtype : 'colorpicker',
			itemId : 'colorPicker',
			value : '00FFFF', // default
			flex : 2,
		}, {
			xtype : 'button',
			itemId : 'okButton',
			text : 'OK',
			flex : 1,
			handler : function() {
				me.onOkButtonClick();
			}
		}, {
			xtype : 'button',
			itemId : 'cancelButton',
			text : 'Cancel',
			flex : 1,
			handler : function() {
				me.hide();
			}
		}, ];

		SubjectService.suggestLabels(null, {
			callback : function(vo) {
				var data = [];
				for ( var i = 0; i < vo.length; i++) {
					data.push([ vo[i].name ]);
				}

				var labelStore = Ext.create('Ext.data.ArrayStore', {
					fields : [ 'name' ],
					data : data,
				});

				var labelCombo = Ext.create('Ext.form.ComboBox', {
					itemId : 'labelCombo',
					store : labelStore,
					queryMode : 'local',
					displayField : 'name',
					valueField : 'name',
					renderTo : Ext.getBody()
				});

				me.insert(0, labelCombo);

			}
		});

		this.callParent();

	},

	onOkButtonClick : function() {
		this.hide();
	},

	getLabel : function() {
		var colorPicker = this.getComponent("colorPicker");
		var labelCombo = this.getComponent("labelCombo");
		var label = new LabelValueObject();
		label.name = labelCombo.getValue();
		label.colour = colorPicker.getValue();
		label.isShown = true;
		return label;
	},
});