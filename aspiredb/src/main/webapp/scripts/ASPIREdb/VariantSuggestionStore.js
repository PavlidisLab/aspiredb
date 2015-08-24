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
Ext.require( [ 'Ext.data.Store', 'ASPIREdb.model.PropertyValue', 'ASPIREdb.ActiveProjectSettings' ] );

/**
 * Collection of Variant Filter options like Variant Type, Variant Label, Location, Gene, etc.
 */
Ext.define( 'ASPIREdb.VariantSuggestionStore', {
   extend : 'Ext.data.Store',
   model : 'ASPIREdb.model.VariantProperty',

   suggestionContext : null,

   constructor : function(config) {
      config.proxy = {
         type : 'dwr',
         dwrFunction : VariantService.suggestVariantLocationProperties,
         reader : {
            type : 'json',
            root : 'data',
            totalProperty : 'count'
         }
      };
      this.callParent( arguments );
   },

} );