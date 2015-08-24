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
Ext.require( [ 'Ext.container.Container', 'Ext.Component' ] );

/**
 * Multicombo item which contains an 'x' for deleting the item.
 */
Ext.define( 'ASPIREdb.view.filter.multicombo.Item', {
   config : {
      text : null,
      value : null
   },
   extend : 'Ext.container.Container',
   autoEl : 'li',
   cls : 'multiValueSuggestBox-token',
   resizable : false,

   initComponent : function() {
      this.items = [ {
         xtype : 'component',
         autoEl : {
            tag : 'p',
            html : this.getText()
         },
         cls : 'multiValueSuggestBox-token-label'
      }, {
         xtype : 'component',
         autoEl : {
            tag : 'span',
            html : 'x'
         },
         listeners : {
            click : {
               element : 'el',
               fn : function() {
                  this.fireEvent( 'remove', this );
                  ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
               },
               scope : this
            }
         },
         cls : 'multiValueSuggestBox-token-close'
      } ];

      this.callParent();
   }
} );