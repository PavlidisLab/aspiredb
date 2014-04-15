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
		'Ext.grid.column.Action', 'Ext.ux.CheckColumn','Ext.form.field.*', 'Ext.picker.Color' ]);

var rowEditing = Ext.create('Ext.grid.plugin.RowEditing', {
    //clicksToMoveEditor: 1,
	clicksToEdit: 2,
     autoCancel: false
});
/**Ext.define('Ext.ux.ColorPickerCombo', {
	  extend: 'Ext.form.field.Trigger',
	  alias: 'widget.colorcbo',
	  triggerTip: 'Please select a color.',
	  onTriggerClick: function() {
	    var me = this; 
	    picker = Ext.create('Ext.picker.Color', {     
	    pickerField: this,     
	    ownerCt: this,    
	    renderTo: document.body,     
	    floating: true,    
	    hidden: true,    
	    focusOnShow: true,
	    style: {
	              backgroundColor: "#fff"
	          } ,
	    listeners: {
	              scope:this,
	              select: function(field, value, opts){
	    me.setValue('#' + value);
	    me.inputEl.setStyle({backgroundColor:value});
	    picker.hide();
	  },
	  show: function(field,opts){
	    field.getEl().monitorMouseLeave(500, field.hide, field);
	    }
	          }
	});
	       picker.alignTo(me.inputEl, 'tl-bl?');
	       picker.show(me.inputEl);
	  } 
	});*/

/**
 * For removing and showing labels
 */
Ext.define('ASPIREdb.view.LabelControlWindow',
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
					width : 600,
					height : 400,
					renderTo : Ext.getBody(),
					config : {
						visibleLabels : [],
						isSubjectLabel : false,
						selectedIds : []
					},
					constructor : function(cfg) {
						this.initConfig(cfg);
						this.callParent(arguments);
					},
                    
					/**dockedItems : [ {
						xtype : 'colorpicker',
						itemId : 'colorPicker',
						value : '00FFFF', // default
						dock : 'right'
					} ],*/
                    items : [ { xtype : 'container',
                                layout : {
                                    type : 'vbox',
                                    defaultMargins : {
                                        top : 5,
                                        right : 5,
                                        left : 5,
                                        bottom : 5
                                    }
                                }, 
                                
                                items : [
                                  { xtype : 'grid',
                                    flex : 1,
                                    width : 550,
                                    //height : 100,
                                    itemId : 'labelSettingsGrid',
                                    store : Ext.create('ASPIREdb.store.LabelStore'),
                                    columns : [
                                            {
                                                header : 'Label',
                                                dataIndex : 'labelId',
                                                width : 100,
                                                renderer : function(labelId) {
            
                                                    var label = this
                                                            .up('#labelControlWindow').visibleLabels[labelId];
                                                    var ret = "";
                                                    var fontcolor = (parseInt(label.colour, 16) > 0xffffff/2) ? 'black' : 'white';	
                        							ret += "<font color="+fontcolor+"><span style='background-color: "
                        									+ label.colour + "'>&nbsp&nbsp"+ label.name+"&nbsp</span></font>&nbsp&nbsp&nbsp";
                                          
                                                    return ret;
                                                },
                                                editor: {
                            		                xtype:'numberfield',
                            		                allowBlank: false,
                            		               
                            		            }
                                            },
                                            {
                                            	header : 'Name',
                                                dataIndex : 'labelName',
                                                width:100,
                                                editor: {
                                                	xtype: 'textfield',
                            		                allowBlank: false,
                            		               
                            		            }
                                            },
                                            {
                                            	header : 'Color',
                                                dataIndex : 'labelColour',
                                                width : 100,
                                                editor: {
                                                	xtype: 'textfield',
                            		                allowBlank: false,
                            		                
                                                }, 
                                              
                                            },
                                            {
                                                header : 'Show',
                                                dataIndex : 'show',
                                                xtype : 'checkcolumn',
                                                id : 'labelShowColumn',
                                                flex : 1
                                            },
                                            {
                                                header : 'Remove',
                                                dataIndex : 'remove',
                                                xtype : 'checkcolumn',
                                                id : 'labelRemoveColumn',
                                                flex : 1
                                            }, ],
                                            plugins: [rowEditing],
                                        },// end of grid
                                        // buttons
                                        { xtype : 'container',
                                            height : 25,
                                            layout : {
                                                type : 'hbox',
                                                defaultMargins : {
                                                    right : 5,
                                                    left : 5
                                                }
                                            }, 
                                            items : [ { xtype : 'button',
                                                        flex : 1,
                                                        text : 'OK',
                                                        itemId : 'okButton',
                                                    }, { xtype : 'button',
                                                        flex : 1,
                                                        text : 'Cancel',
                                                        itemId : 'cancelButton',
                                                    } ] 
                                        } // end of button container
                                     ] // items + buttons 
                                  } ],
                                  
					initComponent : function() {
						this.callParent();

						var me = this;
					
						var loadData = [];
						me.down('#labelSettingsGrid').on('edit',function(editor, e){
							var record = e.record;
							var label = me.visibleLabels[record.data.labelId];
							label.name=record.data.labelName;
							label.colour = record.data.labelColour;
							LabelService.updateLabel(label, {
	                            callback : function() {
	                            	me.down('#labelSettingsGrid').getView().refresh();
	                                if( me.isSubjectLabel ) {
	                                    ASPIREdb.EVENT_BUS.fireEvent('subject_label_changed');
	                                } else {
	                                    ASPIREdb.EVENT_BUS.fireEvent('variant_label_changed');
	                                }
	                            }
	                        });
						});
						
						me.down('#labelSettingsGrid').on('validateedit', function(editor, e) {
							var record = e.record;
							var labelcolour=record.data.labelColour;
							//TODO: check for valid color or find a better way to edit colours using color picker
							//if (labelcolour)
							});
						

						if (me.isSubjectLabel) {
							me.service = SubjectService;
							
						} else {
							me.service = VariantService;
						}
						
						var loadData = [];
						for ( var idx in me.visibleLabels ) {
							var label = me.visibleLabels[idx];
							loadData.push([ label.id, label.name,label.colour,label.isShown ]);
						}
                        
						me.down('#labelSettingsGrid').store.loadData(loadData);
						
						me.down('#labelShowColumn').on('checkchange',
								me.onLabelShowCheckChange, this);

                        me.down('#okButton').on('click', me.okButtonHandler, this);        
                        me.down('#cancelButton').on('click', me.cancelButtonHandler, this);                        
                    },

					cancelButtonHandler : function(event) {
				        this.destroy();
				    },
				    
				    
				    okButtonHandler : function(event) {
                        var me = this;
                        
				        var checkedRecords = me.down('#labelSettingsGrid').store.queryBy(function(rec) {
				            return rec.get('remove') == true;
				        }).items;
				        
				        var checkedLabels = [];
                        for(var i = 0; i < checkedRecords.length; i++) {
                            var rec = checkedRecords[i];
				            var label = this.visibleLabels[rec.get('labelId')];
				            checkedLabels.push( label );
                        }
				        this.removeLabels(checkedLabels);
				    },
				    
				    removeSubjectLabels : function(labels) {
				        var me = this;
				        if ( me.selectedIds.length == 0 ) {
                            Ext.MessageBox.confirm('Delete', 'Remove ' + labels.length + ' label(s) '
                                    + 'for all subject(s)?', function(btn) {
                                if (btn === 'yes') {
                                    LabelService.deleteSubjectLabels( labels, {
                                        callback : function() {
                                            //me.down('#labelSettingsGrid').store.removeAt(rowIndex);
                                            ASPIREdb.EVENT_BUS.fireEvent('subject_label_changed', me.selectedIds);
                                            me.destroy();
                                        }
                                    });
                                }
                            });
                        } else {
                            Ext.MessageBox.confirm('Delete', 'Remove ' + labels.length + ' label(s) '
                                    + 'for ' + me.selectedIds.length + ' subject(s)?', function(btn) {
                                if (btn === 'yes') {
                                    LabelService.removeLabelsFromSubjects( labels, me.selectedIds, {
                                        callback : function() {
                                            ASPIREdb.EVENT_BUS.fireEvent('subject_label_changed', me.selectedIds);
                                            me.destroy();
                                        }
                                    });
                                }
                            });
                        }
				    },
				    
				    removeVariantLabels : function(labels) {
				        var me = this;
				        if ( me.selectedIds.length == 0 ) {
				            Ext.MessageBox.confirm('Delete', 'Remove ' + labels.length + ' label(s) '
                                    + 'for all variant(s)?', function(btn) {
                                if (btn === 'yes') {
                                    LabelService.deleteVariantLabels( labels, {
                                        callback : function() {
                                            ASPIREdb.EVENT_BUS.fireEvent('variant_label_changed', me.selectedIds);
                                            me.destroy();
                                        }
                                    });
                                }
                            });
                        } else {
                            Ext.MessageBox.confirm('Delete', 'Remove ' + labels.length + ' label(s) '
                                    + 'for ' + me.selectedIds.length + ' variant(s)?', function(btn) {
                                if (btn === 'yes') {
                                    LabelService.removeLabelsFromVariants( labels, me.selectedIds, {
                                        callback : function() {
                                            ASPIREdb.EVENT_BUS.fireEvent('variant_label_changed', me.selectedIds);
                                            me.destroy();
                                        }
                                    } );
                                }
                            });
                        }
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
				    removeLabels : function(labels) {
						var me = this;

                        if ( labels != null && labels.length > 0 ) {
						    if ( me.isSubjectLabel ) {
						        me.removeSubjectLabels(labels);
						    } else {
						        me.removeVariantLabels(labels);
						    }
						} else {
                            me.destroy();
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
					onLabelShowCheckChange : function(checkColumn, rowIndex,
							checked, eOpts) {
                        var me = this;
						var labelId = this.down('#labelSettingsGrid').store.data.items[rowIndex].data.labelId;
						var label = this.visibleLabels[labelId];
						label.isShown = checked;
						LabelService.updateLabel(label, {
                            callback : function() {
                                if( me.isSubjectLabel ) {
                                    ASPIREdb.EVENT_BUS.fireEvent('subject_label_changed');
                                } else {
                                    ASPIREdb.EVENT_BUS.fireEvent('variant_label_changed');
                                }
                            }
                        });
					}
					
				});