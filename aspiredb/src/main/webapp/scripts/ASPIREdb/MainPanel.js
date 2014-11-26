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
    'ASPIREdb.view.Ideogram',
    'ASPIREdb.view.SubjectGrid',
    'ASPIREdb.view.PhenotypeGrid',
    'ASPIREdb.view.VariantTabPanel'
]);
/**
 * Main panel contains grid Panels "subjectGrid", "variantTabPanel" and "phenotypeGrid'
 */
Ext.define('ASPIREdb.MainPanel',{
    extend: 'Ext.panel.Panel',
    alias: 'widget.ASPIREdb_mainpanel',
    layout: 'border',
    items:[
        {
            region: 'west',
            xtype:'subjectGrid',
            width: 300,
            collapsible: true,
            split: true,
            title:'Subject'
        },
        {
            region: 'center',
            xtype: 'variantTabPanel',
            title:'Variant',
            flex : 1
        },
        {
            region: 'east',
            xtype:'phenotypeGrid',
            width: 350,
            collapsible: true,
            split: true,
            title:'Phenotype'
        }
    ]
});