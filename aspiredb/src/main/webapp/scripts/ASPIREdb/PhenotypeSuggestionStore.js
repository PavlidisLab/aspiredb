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
    'Ext.data.Store',
    'ASPIREdb.model.PropertyValue',
    'ASPIREdb.ActiveProjectSettings'
]);

Ext.define('ASPIREdb.PhenotypeSuggestionStore', {
    extend:'Ext.data.Store',
    model: 'ASPIREdb.model.PhenotypeProperty',

    suggestionContext: null,

    config : {
       projectIds : ASPIREdb.ActiveProjectSettings.getActiveProjectIds(),
    },
    
    constructor: function (config) {
       
       if ( config != null ) {
           config.proxy = {
               type: 'dwr',
               dwrFunction: PhenotypeService.suggestPhenotypes,
               reader: {
                   type: 'json',
                   root: 'data',
                   totalProperty: 'count'
               }
           };
       }
        this.callParent(arguments);
    },

    load: function(options) {
        this.suggestionContext = new SuggestionContext();
        if ( this.projectIds === undefined ) {
           this.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
        }
        this.suggestionContext.activeProjectIds = this.projectIds;
        this.suggestionContext.valuePrefix = options.params.query;
        this.proxy.dwrParams = [this.suggestionContext];
        this.callParent(options);
    }
});