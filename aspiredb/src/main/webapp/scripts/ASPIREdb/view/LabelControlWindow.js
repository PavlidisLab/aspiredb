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
Ext.require([ 'Ext.Window', 'ASPIREdb.store.LabelStore',
              'Ext.grid.column.Action', 'Ext.ux.CheckColumn' ]);

/**
 * For removing and showing labels
 */
Ext
.define(
		'ASPIREdb.view.LabelControlWindow',
		{
			extend : 'Ext.Window',
			alias : 'widget.labelControlWindow',
			title : 'Label settings',
			id : 'labelControlWindow',
			frame : 'true',
			closable : true,
			closeAction : 'destroy',
			layout : 'border',
			bodyStyle : 'padding: 5px;',
			layout : 'fit',
			width : 300,
			height : 320,
			renderTo : Ext.getBody(),
			config : {
				visibleLabelIds : [],
				isSubjectLabel : false,
			},
			constructor : function(cfg) {
				this.initConfig(cfg);
				this.callParent(arguments);
			},
			items : [ {
				xtype : 'grid',
				itemId : 'labelSettingsGrid',
				store : Ext.create('ASPIREdb.store.LabelStore'),
				columns : [
				           {
				        	   header : 'Label',
				        	   dataIndex : 'label',
				        	   width : 180,
				        	   renderer : function(value) {
				        		   // value is a LabelValueObject
				        		   var ret = "";
				        		   ret += "<span style='background-color: "
				        			   + value.colour
				        			   + "'>"
				        			   + value.name + "</span>&nbsp;";
				        		   return ret;
				        	   },
				           },
				           {
				        	   header : 'Show',
				        	   dataIndex : 'show',
				        	   xtype : 'checkcolumn',
				        	   id : 'labelCheckColumn',
				        	   flex : 1,
				           },
				           {
				        	   header : '',
				        	   xtype : 'actioncolumn',
				        	   id : 'labelActionColumn',
				        	   handler : function(view, rowIndex,
				        			   colIndex, item, e) {
				        		   var action = 'removeLabel';
				        		   this.fireEvent('itemclick', this,
				        				   action, view, rowIndex,
				        				   colIndex, item, e);
				        	   },

				        	   width : 30,
				        	   items : [ {
				        		   icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
				        		   tooltip : 'Remove label',

				        	   } ]
				           } ],
			} ],

			initComponent : function() {
				this.callParent();

				var me = this;

				var ids = [];
				var loadData = [];
				for ( var i = 0; i < me.visibleLabelIds.length; i++) {
					var label = me.visibleLabelIds[i];
					var isShown = label.isShown;
					if (ids.indexOf(label.id) == -1) {
						ids.push(label.id);
						loadData.push([ label, isShown ]);
					}
				}
				me.down('#labelSettingsGrid').store.loadData(loadData);

				me.down('#labelCheckColumn').on('checkchange',
						me.onLabelCheckChange);
				me.down('#labelActionColumn').on('itemclick',
						me.onLabelActionColumnClick);
			},

			/**
			 * Remove label?
			 * 
			 * @param column
			 * @param action
			 * @param view
			 * @param rowIndex
			 * @param colIndex
			 * @param item
			 * @param e
			 */
			onLabelActionColumnClick : function(column, action, view,
					rowIndex, colIndex, item, e) {

				var rec = view.store.getAt(rowIndex);
				var label = rec.get('label');
				var me = this;
				if (action == 'removeLabel') {
					Ext.MessageBox
					.confirm(
							'Delete',
							'Remove label "' + label.name
							+ '"?',
							function(btn) {
								if (btn === 'yes') {
									me.up('#labelSettingsGrid').store
									.removeAt(rowIndex);
									if (me
											.up('#labelControlWindow').isSubjectLabel) {
										LabelService
										.deleteSubjectLabel(
												label,
												{
													timeout : 500,
													errorHandler : function(
															message) {
														alert("Error deleting subject label: "
																+ message);
													}
												});
									} else {
										LabelService
										.deleteVariantLabel(
												label,
												{
													timeout : 500,
													errorHandler : function(
															message) {
														alert("Error deleting variant label: "
																+ message);
													}
												});
									}
								}
							});
				}
			},

			/**
			 * Show label?
			 * 
			 * @param checkColumn
			 * @param rowIndex
			 * @param checked
			 * @param eOpts
			 */
			onLabelCheckChange : function(checkColumn, rowIndex,
					checked, eOpts) {
				var label = this.up('#labelSettingsGrid').store.data.items[rowIndex].data.label;
				label.isShown = checked;
				LabelService.updateLabel(label, {
					timeout : 500,
					errorHandler : function(message) {
						alert("Error updating label: " + message);
					}
				});
			},
		});