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

Ext.define('ASPIREdb.view.DeleteQueryWindow', {
	extend : 'Ext.Window',
	alias : 'widget.deleteQueryWindow',
	singleton : true,
	title : 'Delete Query',
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
		items : [{
					xtype : 'label',
					text : 'Select Query: '
				},
				{
					xtype : 'combo',
					itemId : 'deleteQueryComboBox',
					editable : false,
					forceSelection : true,
					value : 'FILTER_PLACEHOLDER',
					store : [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ]
				}],

		buttons : [ {
			xtype : 'button',
			itemId : 'deleteButton',
			text : 'Delete'
		} ]

	} ],

	initComponent : function() {
		this.callParent();

		var ref = this;

		this.updateDeleteQueryCombo();
		this.down('#deleteButton').on('click', ref.deleteButtonHandler, ref);
		this.on('query_deleted', this.updateDeleteQueryCombo, this);
		
		
	},

	initAndShow : function(filters) {

		this.filterConfigs = filters;
		this.show();

	},
	
	deleteButtonHandler : function() {
		
		var ref = this;
		
		Ext.Msg.show({
		    title:'Are you sure you want to delete query?',
		    msg:'This operation is not reversible. Would you like to continue?',
		    buttons:Ext.Msg.YESNOCANCEL,
		    icon:Ext.Msg.QUESTION,
		    fn:function(btn){
		        if(btn=='cancel'){
		            //do nothing
		        }
		        //display the query name and delete the selected query
		        if(btn=='yes'){
		        	var queryName = ref.down('#deleteQueryComboBox').getValue();

		    		QueryService.deleteQuery(queryName, {
		    			callback : function() {			
		    				ASPIREdb.view.DeleteQueryWindow.close();
		    				ASPIREdb.view.DeleteQueryWindow.fireEvent('query_deleted');
		    			}
		    		});
		    		ref.down('#deleteQueryComboBox').clearValue();
		    		Ext.Msg.alert('Status', 'Query Deleted successfully.');
		        }
		    }
		});
		
		

	},
	
	updateDeleteQueryCombo : function() {

		var deleteQueryComboBox = this.down('#deleteQueryComboBox');

		QueryService.getSavedQueryNames({
			callback : function(names) {

				var storedata = [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ];

				for ( var i = 0; i < names.length; i++) {

					storedata.push([ names[i], names[i] ]);

				}

				deleteQueryComboBox.getStore().loadData(storedata);

			}
		});

	},
	
			

});