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
              'ASPIREdb.view.filter.OrFilterContainer', 'ASPIREdb.view.filter.FilterPanel',
              'ASPIREdb.view.filter.PhenotypeFilter', 'ASPIREdb.view.filter.VariantFilter',
              'ASPIREdb.view.filter.ProjectOverlapFilterContainer' ] );

/**
 * Contains a ProjectOverlapFilterContainer
 */
Ext.define( 'ASPIREdb.view.filter.ProjectOverlapFilterPanel', {
   extend : 'ASPIREdb.view.filter.FilterPanel',
   alias : 'widget.filter_projectoverlappanel',
   title : 'Project Overlap Filter',
   bodyStyle : 'background: #E0A3C2;',
   items : [ {
      xtype : 'filter_projectoverlap',
      itemId : 'projectOverlapFilterContainer'
   } ],

   getFilterConfig : function() {
      var projectOverlapFilterContainer = this.getComponent( 'projectOverlapFilterContainer' );
      return projectOverlapFilterContainer.getRestrictionExpression();
   },

   setFilterConfig : function(config) {

      var projectOverlapFilterContainer = this.down( '#projectOverlapFilterContainer' );
      projectOverlapFilterContainer.setRestrictionExpression( config );

   },

   handleCloseImageClick : function() {
      this.close();
   },

   initComponent : function() {
      this.callParent();

   }
} );
