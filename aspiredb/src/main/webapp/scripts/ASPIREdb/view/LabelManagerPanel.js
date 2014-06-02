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
    'ASPIREdb.view.LabelGrid',
    'ASPIREdb.view.LabelOwnerGrid', 
]);
/**
 * Label panel includes LabelSetGrid and LabelGrid 
 */
Ext.define('ASPIREdb.LabelManagerPanel',{
    extend: 'Ext.panel.Panel',
    alias: 'widget.ASPIREdb_Labelmanagerpanel',
    layout: 'border',
    items:[
        {
            region: 'west',
            xtype:'LabelGrid',
            id : 'LabelGrid',
            width: 480,
            collapsible: true,
            split: true,
            title:'Label'
        },
        {
            region: 'east',
            xtype:'LabelOwnerGrid',
            id :'LabelOwnerGrid',
            width: 480,
            collapsible: true,
            split: true,
            title:'Label Owner'
        }
    ],



});