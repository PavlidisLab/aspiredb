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
/**
 * @author gayacharath
 * @widget saveUserGeneSetWindow
 */
Ext.require( [ 'Ext.Window' ] );

Ext.define( 'ASPIREdb.view.SaveUserGeneSetWindow', {
   extend : 'Ext.Window',
   alias : 'widget.saveUserGeneSetWindow',
   singleton : true,
   title : 'Save User Gene Set',
   closable : true,
   closeAction : 'hide',
   width : 400,
   height : 200,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',
   border : false,

   geneSetValueobjects : [],

   items : [ {

      bodyPadding : 5,
      width : 350,

      layout : 'anchor',
      defaults : {
         anchor : '100%'
      },

      defaultType : 'textfield',
      items : [ {
         fieldLabel : 'Gene Set Name',
         name : 'last',
         allowBlank : false,
         itemId : 'geneSetName'
      } ],

      buttons : [ {
         xtype : 'button',
         itemId : 'saveButton',
         text : 'OK'
      } ]

   } ],

   initComponent : function() {
      this.callParent();

      var ref = this;

      this.down( '#saveButton' ).on( 'click', ref.saveButtonHandler, ref );
   },

   initAndShow : function(genes) {

      this.geneSetValueobjects = genes;
      this.show();

   },

   saveButtonHandler : function() {
      var ref = this;
      var geneSetName = this.down( '#geneSetName' ).getValue();

      // check whether the query name exist in the database
      UserGeneSetService.isGeneSetName( geneSetName, {
         callback : function(gvoSta) {
            if ( gvoSta ) {

               Ext.Msg.show( {
                  title : 'Save gene set overwrite',
                  msg : 'Gene set name already exist. Do you want to overwrite it?',
                  buttons : Ext.Msg.YESNOCANCEL,
                  icon : Ext.Msg.QUESTION,
                  fn : function(btn) {
                     if ( btn == 'cancel' ) {
                        // do something
                     }
                     if ( btn == 'yes' ) {

                        GeneService.saveUserGeneSet( geneSetName, ref.geneSetValueobjects, {
                           callback : function(gvoId) {

                              ASPIREdb.view.SaveUserGeneSetWindow.down( '#geneSetName' ).setValue( '' );
                              ASPIREdb.view.SaveUserGeneSetWindow.close();
                              ASPIREdb.EVENT_BUS.fireEvent( 'new_geneSet_saved' );

                           }
                        } );
                        ref.down( '#geneSetName' ).clearValue();

                     } else if ( btn == 'no' ) {
                        // if the user wish to add to the existing geneset
                        Ext.Msg.show( {
                           title : 'Add to gene set',
                           msg : 'Do you want to add to geneset then?',
                           buttons : Ext.Msg.YESNOCANCEL,
                           icon : Ext.Msg.QUESTION,
                           fn : function(btn) {
                              if ( btn == 'cancel' ) {
                                 // do something
                              }
                              if ( btn == 'yes' ) {
                                 var geneSymbols = [];
                                 for (var i = 0; i < ref.geneSetValueobjects.length; i++)
                                    geneSymbols.push( ref.geneSetValueobjects[i].symbol );
                                 UserGeneSetService.addGenesToGeneSet( geneSetName, geneSymbols, {
                                    callback : function() {
                                       ASPIREdb.view.SaveUserGeneSetWindow.down( '#geneSetName' ).setValue( '' );
                                       ASPIREdb.view.SaveUserGeneSetWindow.close();
                                       ASPIREdb.EVENT_BUS.fireEvent( 'new_geneSet_saved' );

                                    }
                                 } );

                                 

                              }

                           }

                        } );
                        ref.down( '#geneSetName' ).clearValue();

                     }

                  }

               } );

            } else {
               UserGeneSetService.saveUserGeneSet( geneSetName, ref.geneSetValueobjects, {
                  callback : function(gvoId) {

                     console.log('Successfully saved ' + ref.geneSetValueobjects.length + ' unique genes to gene set "' + geneSetName + '"');
                     ASPIREdb.view.SaveUserGeneSetWindow.down( '#geneSetName' ).setValue( '' );
                     ASPIREdb.view.SaveUserGeneSetWindow.close();
                     ASPIREdb.EVENT_BUS.fireEvent( 'new_geneSet_saved', geneSetName );
                     // ASPIREdb.view.DeleteQueryWindow.updateDeleteQueryCombo();

                  }
               } );
            }
         }

      } );

   }

} );