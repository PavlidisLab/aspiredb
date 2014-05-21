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
Ext.require( [ 'Ext.form.*', 'Ext.layout.container.Column', 'Ext.tab.Panel',

] );
/**
 * Upload Panel
 */
Ext.define( 'ASPIREdb.view.UploadManagerPanel', {
   extend : 'Ext.form.Panel',
   alias : 'widget.uploadManagerPanel',
   frame : true,
   bodyStyle : 'padding:5px 5px 0',
   // width: 350,
   fileUpload : true,
   fieldDefaults : {
      labelWidth : 75,
      msgTarget : 'side'
   },
   defaults : {
      anchor : '100%'
   },

   items : {
      xtype : 'tabpanel',
      activeTab : 0,
      defaults : {
         bodyStyle : 'padding:10px'
      },

      items : [ {
         title : 'New Project Upload',
         defaultType : 'textfield',
         width : 750,

         items : [ {
            xtype : 'fieldset',
            title : 'Project',
            collapsible : true,
            autoWidth : true,
            defaultType : 'textfield',
            layout : 'anchor',
            defaults : {
               anchor : '100%'
            },
            items : [ {
               fieldLabel : 'Project Name',
               name : 'projectName',
               value : '',

            }, {
               fieldLabel : 'Project Decription',
               name : 'projectDescription',

            } ]
         }, {
            xtype : 'filefield',
            id : 'variantFile',
            width : 600,
            emptyText : 'Select variant file to upload',
            fieldLabel : 'Upload Variant List',
            labelWidth : 150,
            name : 'varintUploadFile-path',
            buttonText : 'Select',
       
         } ]
      }, {
         title : 'Existing Project Upload',
         defaultType : 'textfield',
         width : 750,

         items : [ {
            xtype : 'combobox',
            fieldLabel : 'Project',
            id : 'userProjectField',
            name : 'unit',
            editable : false,
            displayField : 'name',
            allowBlank : false,
            valueField : 'id',
            store : Ext.create( 'Ext.data.Store', {
               proxy : {
                  type : 'dwr',
                  dwrFunction : ProjectService.getProjects,
                  model : 'ASPIREdb.model.Project',
                  reader : {
                     type : 'json',
                     root : 'name'
                  }
               }
            } ),
       //     typeAhead : true,
         //   queryMode : 'local',
            emptyText : 'Choose project...',
            forceSelection : true,
            msgTarget : 'qtip',

         } ]
      } ]
   },

   buttons : [ {
      text : 'Upload',
      disabled : true,
      formBind : true,
      handler : function() {
         this.up( 'form' ).getForm().submit( {
            url : 'upload.action', //'xml-form-errors.xml',
            submitEmptyText : false,
            waitMsg : 'Uploading your file...',
            success: function(fp, o) { 
               Ext.Msg.alert('Success', 'Your file has been uploaded.');
            }
               /**
                * handler: function() { var form = this.up('form').getForm(); if(form.isValid()){ form.submit({ url:
                * 'upload.action', waitMsg: 'Uploading your file...', success: function(fp, o) { Ext.Msg.alert('Success',
                * 'Your file has been uploaded.'); } }); } }
                */
         } );
         /**
          * ProjectService.createUserProject( 'New Project', 'New Project Description', { callback : function(projectId) {
          * var createdProject =projectId; }, errorHandler : function(er, exception) { Ext.Msg.alert( "create project
          * Error", er + "\n" + exception.stack ); console.log( exception.stack ); } } );
          */
      }

   }, {
      text : 'Cancel'
   } ]
} );

// fsf.render(document.body);
