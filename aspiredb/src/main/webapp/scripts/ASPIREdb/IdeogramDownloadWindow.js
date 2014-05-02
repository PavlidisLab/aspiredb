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

Ext.define('ASPIREdb.IdeogramDownloadWindow', {
	extend : 'Ext.Window',
	singleton : true,
	closable : true,
	closeAction : 'hide',
	width : 900,
	height : 500,
	layout : 'fit',
	border: false,
	maximizable : true,
	title : 'Ideogram image in PNG format',
	renderTo: Ext.getBody(),
	
	initComponent : function() {

		var ref = this;

		Ext.apply(this, {
			tbar : [ {
				itemId : 'saveButton',
				xtype : 'button',
				text : 'Save',
				scope : this,
				handler : function() {
					Ext.MessageBox.confirm('Confirm Download', 'Would you like to download the ideogram?', function(choice){
	                    if(choice == 'yes'){
	                    	var strDownloadMime = "image/octet-stream";
	                    	var strData = ref.getComponent('imageComponent').el.dom.src;
	                    	document.location.href = strData.replace("image/png", strDownloadMime);
	                        
	                    }
	                });
				}
			} ],
			items : [ {
				xtype : 'component',
				itemId : 'imageComponent',
				autoEl:{
					tag : 'img',
					src :'',
				}
				
				
			} ],

			
			
		});
		
	
		
		this.callParent();

	},

   showIdeogramDownload : function(img) {
	  var imgComponent =this.getComponent('imageComponent');
	  imgComponent.el.dom.src =img;	 
	  this.show();

	},	
	
	
});

