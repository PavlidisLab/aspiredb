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
    'Ext.util.Observable',
    'ASPIREdb.model.Project'
]);

// Events: login, logout
Ext.define('ASPIREdb.ActiveProjectSettings', {
    extend: 'Ext.util.Observable',
    singleton: true,

    constructor: function () {
        this.store = Ext.create('Ext.data.Store', {
            model: 'ASPIREdb.model.Project',
            proxy: {
                type: 'localstorage'
            }
        });

        this.setActiveProject([{id:1, name:'', description:''}]);

        this.callParent(arguments);
    },

    getActiveProjectIds: function () {
        var ids = [];
        this.store.each(function (record) {
            ids.push(record.data.id);
            return false;
        });
        return ids;
    },
    /**
     * ToDo: Need to be changed once we implement more than one project
     * 
     */
    getActiveProjectName: function () {
        var name = '';
        this.store.each(function (record) {
            name=record.data.name;
            return false;
        });
        return name;
    },

    setActiveProject: function (project) {
        this.store.removeAll();
        this.store.add(project);
    }
});
