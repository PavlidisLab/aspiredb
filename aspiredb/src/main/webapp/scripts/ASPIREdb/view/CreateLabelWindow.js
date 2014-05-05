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
   /**
    * @memberOf ASPIREdb.view.CreateLabelWindow
    */
	extend : 'Ext.Window',
	alias : 'widget.createLabelWindow',
	title : 'Make label',
	closable : true,
	closeAction : 'destroy',
	layout : 'border',
	bodyStyle : 'padding: 5px;',
	flex : 1,
	layout : {
		type : 'hbox',
		defaultMargins : {
			top : 5,
			right : 5,
			left : 5,
			bottom : 5,
		},
	},

	config : {
		isSubjectLabel : false,
	},

	constructor : function(cfg) {
		this.initConfig(cfg);
		this.callParent(arguments);
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

		if (me.isSubjectLabel) {
			SubjectService.suggestLabels(null, {
				callback : function(vos) {
					me.createSuggestLabelCombo(vos, me);
				}
			});
		} else {
			VariantService.suggestLabels(null, {
				callback : function(vos) {
					me.createSuggestLabelCombo(vos, me);
				}
			});
		}

		this.callParent();

	},

		
	createSuggestLabelCombo : function(vos, me) {
		var data = [];
		for ( var i = 0; i < vos.length; i++) {
			data.push([ vos[i], vos[i].name ]);
		}

		var suggestLabelStore = Ext.create('Ext.data.ArrayStore', {
			fields : [ 'value', 'display' ],
			data : data,
			autoLoad : true,
			autoSync : true,
			
		});

		var labelCombo = Ext.create('Ext.form.ComboBox', {
			itemId : 'labelCombo',
			store : suggestLabelStore,
			queryMode : 'local',
			displayField : 'display',
			valueField : 'value',
			renderTo : Ext.getBody(),
		
		});
					
		labelCombo.on('select', function(combo, records, eOpts) {							
			//Bug 3917 fixed
			var vo = records[0].data.value;
			if (vo != null && vo.colour != null) {
				me.down('#colorPicker').select(vo.colour);
			}
		});

		me.insert(0, labelCombo);

	},

	onOkButtonClick : function() {
		
			this.hide();
	},

	getLabel : function() {
		var colorPicker = this.down("#colorPicker");
		var labelCombo = this.down("#labelCombo");

		// vo will be a ValueObject if it already exists
		// otherwise, it's just a name of type string
		var vo = labelCombo.getValue();
		if (vo == null || vo.length == "") {
			return null;
		}
		if (vo.id == undefined) {
			vo = new LabelValueObject();
			vo.name = labelCombo.getValue();
			vo.colour = colorPicker.getValue();
		}
		vo.isShown = true;
		return vo;
	},
	
	
});