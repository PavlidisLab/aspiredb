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

Ext.define('ASPIREdb.view.SaveQueryWindow', {
	extend : 'Ext.Window',
	alias : 'widget.saveQueryWindow',
	singleton : true,
	title : 'Save Query',
	closable : true,
	closeAction : 'hide',
	width : 400,
	height : 200,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',

	filterConfigs : [],

	items : [ {

		bodyPadding : 5,
		width : 350,

		layout : 'anchor',
		defaults : {
			anchor : '100%'
		},

		defaultType : 'textfield',
		items : [ {
			fieldLabel : 'Query Name',
			name : 'last',
			allowBlank : false,
			itemId : 'queryName'
		} ],

		buttons : [ {
			xtype : 'button',
			itemId : 'saveButton',
			text : 'OK'
		} ]

	} ],

	initComponent : function() {
		this.callParent();

		var ref = this;

		this.down('#saveButton').on('click', ref.saveButtonHandler, ref);
	},

	initAndShow : function(filters) {

		this.filterConfigs = filters;
		this.show();

	},

	saveButtonHandler : function() {
		var ref=this;
		var queryName = this.down('#queryName').getValue();
			
		//check whether the query name exist in the database
		QueryService.isQueryName(queryName,{
			callback : function(qvoSta) {
				if (qvoSta){
					//Ext.Msg.alert('Status', 'Query name already exist. Choose another name');}
					Ext.Msg.show({
						title:'Save query overwrite',
						msg:'Query name already exist. Do you want to overwrite it?',
						buttons:Ext.Msg.YESNOCANCEL,
						icon:Ext.Msg.QUESTION,
						fn:function(btn){
							if(btn=='cancel'){
				            //do something
							}
							if(btn=='yes'){
				        	
								QueryService.saveQuery(queryName, ref.filterConfigs, {
									callback : function(qvoId) {

										ASPIREdb.view.SaveQueryWindow.down('#queryName').setValue('');
										ASPIREdb.view.SaveQueryWindow.close();
										ASPIREdb.view.SaveQueryWindow.fireEvent('new_query_saved');

									}
								});
								ref.down('#queryName').clearValue();
				    		
							}
							
						}
						
					});	 
						
				
				}
				else {
					QueryService.saveQuery(queryName, ref.filterConfigs, {
						callback : function(qvoId) {

							ASPIREdb.view.SaveQueryWindow.down('#queryName').setValue('');
							ASPIREdb.view.SaveQueryWindow.close();
							ASPIREdb.view.SaveQueryWindow.fireEvent('new_query_saved');
							ASPIREdb.view.DeleteQueryWindow.updateDeleteQueryCombo();

						}
					});
				}
			}
		
		});
		
	}

});