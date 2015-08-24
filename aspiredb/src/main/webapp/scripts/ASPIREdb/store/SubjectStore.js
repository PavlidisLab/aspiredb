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
Ext.require( [ 'Ext.data.Store', 'ASPIREdb.model.Subject', 'ASPIREdb.ActiveProjectSettings' ] );

Ext.define( 'ASPIREdb.store.SubjectStore', {
   extend : 'Ext.data.ArrayStore',
   alias : 'store.subjectStore',
   autoLoad : true,
   autoSync : true,
   proxy : {
      type : 'localstorage',
      id : 'subjectId'
   },

   fields : [ {
      name : 'id',
      type : 'int'
   }, {
      name : 'patientId',
      type : 'string'
   }, {
      name : 'labelIds',
      type : 'array'
   }, {
      name : 'varientNos',
      type : 'int'
   }, {
      name : 'phenotypeNos',
      type : 'int'
   } ],

   storeId : 'subjectStore'

} );