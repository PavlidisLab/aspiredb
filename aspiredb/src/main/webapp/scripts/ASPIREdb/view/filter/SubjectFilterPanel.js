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
Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.AndFilterContainer',
              'ASPIREdb.view.filter.OrFilterContainer', 'ASPIREdb.view.filter.FilterPanel' ] );

/**
 * Filtering subjects
 */
Ext.define( 'ASPIREdb.view.filter.SubjectFilterPanel', {
   extend : 'ASPIREdb.view.filter.FilterPanel',
   alias : 'widget.filter_subject',
   title : 'Subject Filter',
   bodyStyle : 'background: #FFDAA3;',
   items : [ {
      xtype : 'filter_and',
      itemId : 'subjectFilterContainer',
      filterItemType : 'ASPIREdb.view.filter.PropertyFilter',
      suggestValuesRemoteFunction : SubjectService.suggestValues,
      propertyStore : {
         autoLoad : false,
         proxy : {
            type : 'dwr',
            dwrFunction : SubjectService.suggestProperties,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      }
   } ],

   getFilterConfig : function() {
      var config = new SubjectFilterConfig();
      var subjectFilterContainer = this.getComponent( 'subjectFilterContainer' );
      config.restriction = subjectFilterContainer.getRestrictionExpression();
      return config;
   },

   setFilterConfig : function(config) {

      var subjectFilterContainer = this.down( '#subjectFilterContainer' );
      subjectFilterContainer.setRestrictionExpression( config.restriction );

   },

   handleCloseImageClick : function() {
      this.close();
   },

   initComponent : function() {
      this.callParent();

   }
} );
