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
						visibleLabels : [],
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
									dataIndex : 'labelId',
									width : 180,
									renderer : function(labelId) {

										var label = this
												.up('#labelControlWindow').visibleLabels[labelId];
										var ret = "";
										ret += "<span style='background-color: "
												+ label.colour
												+ "'>"
												+ label.name + "</span>&nbsp;";
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

						var loadData = [];

						if (me.isSubjectLabel) {
							me.service = SubjectService;
							
						} else {
							me.service = VariantService;
						}
						
						// populate grid from suggestLabels()
						// don't use me.visibleLabels because it contains other user's labels
						me.service.suggestLabels(new SuggestionContext, {
							callback : function(labels) {
								for ( var idx in labels) {
									var label = labels[idx];
									loadData.push([ label.id, label.isShown ]);
								}
								me.down('#labelSettingsGrid').store.loadData(loadData);
							}
						});
						
						me.down('#labelCheckColumn').on('checkchange',
								me.onLabelCheckChange, this);
						me.down('#labelActionColumn').on('itemclick',
								me.onLabelActionColumnClick, this);
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

						var me = this;

						var rec = view.store.getAt(rowIndex);
						var labelId = rec.get('labelId');
						var label = this.visibleLabels[labelId];

						if (action == 'removeLabel') {
							Ext.MessageBox.confirm('Delete', 'Remove label "'
									+ label.name + '"?', function(btn) {
								if (btn === 'yes') {
									me.down('#labelSettingsGrid').store
											.removeAt(rowIndex);
									delete me.visibleLabels[labelId];
									if (me.isSubjectLabel) {
										LabelService.deleteSubjectLabel(label);
									} else {
										LabelService.deleteVariantLabel(label);
									}

									ASPIREdb.EVENT_BUS.fireEvent('label_change');
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
						var labelId = this.down('#labelSettingsGrid').store.data.items[rowIndex].data.labelId;
						var label = this.visibleLabels[labelId];
						label.isShown = checked;
						LabelService.updateLabel(label);
						ASPIREdb.EVENT_BUS.fireEvent('label_change');
					},
				});