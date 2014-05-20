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
Ext.require([
    'Ext.panel.Panel',
    'Ext.form.*',
    'Ext.layout.container.Column',
    'Ext.tab.Panel',

]);
/**
 * gene panel includes GeneSetGrid and GeneGrid 
 */
Ext.define('ASPIREdb.UploadManagerPanel',{
    extend: 'Ext.panel.Panel',
    alias: 'widget.uploadManagerPanel',
    layout: 'border',
    width :380,
    boarder: false,
    bodyBorder: false,
    fieldDefaults: {
        labelWidth: 75,
        msgTarget: 'side'
    },
    defaults: {
        anchor: '100%'
    },

    items: {
        xtype:'tabpanel',
        activeTab: 0,
        defaults:{
            bodyStyle:'padding:10px'
        },

        items:[{
            title:'New Project Upload',
            defaultType: 'textfield',

            items: [{
               xtype:'fieldset',
               title: 'Project',
               collapsible: true,
               defaultType: 'textfield',
               layout: 'anchor',
               defaults: {
                   anchor: '100%'
               },
               items :[{
                   fieldLabel: 'Project Name',
                   name: 'projectName',
                   value: ''
               },{
                   fieldLabel: 'Project Decription',
                   name: 'projectDescription'
               }]
           },{
              xtype: 'filefield',
              id: 'variantFile',
              emptyText: 'Select variant file to upload',
              fieldLabel: 'variantUploadFile',
              name: 'varintUploadFile-path',
              buttonText: '',
              buttonConfig: {
                  iconCls: 'upload-icon'
              }
          }
           ]
        },{
            title:'Existing Project Upload',
            defaultType: 'textfield',

            items: [{
                fieldLabel: 'Home',
                name: 'home',
                value: '(888) 555-1212'
            },{
                fieldLabel: 'Business',
                name: 'business'
            },{
                fieldLabel: 'Mobile',
                name: 'mobile'
            },{
                fieldLabel: 'Fax',
                name: 'fax'
            }]
        }]
    },

    buttons: [{
        text: 'Save'
    },{
        text: 'Cancel'
    }]
});