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
Ext.require( [ 'Ext.layout.container.*' ] );

/**
 * Contains filter containers.
 */
Ext.define( 'ASPIREdb.view.filter.FilterPanel', {
   extend : 'Ext.Panel',
   closable : true,
   collapsible : true,
   header : false,
   width : 950,
   padding : '2 2 0 2',
   layout : {
      type : 'vbox',
      align : 'stretch'
   },

   initComponent : function() {
      this.callParent();

      var ref = this;

      this.insert( 0, {
         xtype : 'container',
         layout : {
            type : 'hbox',
            align : 'right'
         },
         items : [ {
            xtype : 'label',
            text : this.title,
            padding : '2 0 0 2' // top, right, bottom, left
         }, {
            xtype : 'tbspacer',
            flex : 1,
         }, {
            xtype : 'image',
            itemId : 'closeImage',
            margin : '2 2 2 2',
            src : 'scripts/ASPIREdb/resources/images/icons/cross.png',
            listeners : {
               render : function(c) {
                  c.getEl().on( 'click', function(e) {
                     ref.close();
                     ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
                  }, c );
               }
            }

         } ]
      } );

   },

   closeEmptyFilter : function() {
      var filterContainer = this.getComponent( 'filterContainer' );

      if ( filterContainer.items.length == 0 ) {
         this.close();
      }

      ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
   }
} );
