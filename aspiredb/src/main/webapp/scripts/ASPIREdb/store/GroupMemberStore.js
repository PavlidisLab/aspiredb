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

Ext.require( [ 'Ext.data.ArrayStore' ] );

Ext.define( 'ASPIREdb.store.GroupMemberStore', {
   extend : 'Ext.data.ArrayStore',
   alias : 'store.groupMemberStore',
   autoSync : true,
   autoLoad : true,

   fields : [ {
      name : 'memberName',
      type : 'string'
   }, {
      name : 'memberEmail',
      type : 'string'
   } ],

   storeId : 'GroupMemberStore'

} );