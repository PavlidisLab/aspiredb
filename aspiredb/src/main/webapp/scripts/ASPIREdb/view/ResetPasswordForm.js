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
Ext.require( [ 'Ext.form.*', 'Ext.tip.QuickTipManager' ] );

/**
 * Reset user account password.
 */
Ext
   .define(
      'ASPIREdb.view.ResetPasswordForm',
      {
         extend : 'Ext.Window',
         id : 'resetPasswordFormPanel',
         singleton : true,
         title : 'Reset Password',
         closable : true,
         closeAction : 'hide',
         resizable : false,
         layout : {
            type : 'vbox',
            padding : '5 5 5 5'

         },
         items : [ {
            xtype : 'component',
            layout : {
               type : 'vbox',
               padding : '5 5 5 5',
               align : 'center',
               pack : 'center'

            },
         } ],

         initAndShow : function() {
            this.show();
         },

         initComponent : function() {
            this.callParent();

            var ref = this;

            var textPanel = Ext
               .create(
                  'Ext.form.Panel',
                  {
                     id : 'aspireResetPasswordTextForm',
                     border : false,
                     layout : 'hbox',
                     defaults : {
                        anchor : '100%'
                     },

                     // The fields
                     items : [
                              {
                                 xtype : 'displayfield',
                                 fieldLabel : '',
                                 value : 'Register to use use features of ASPIREDB like data upload. You might want to review the Terms and conditions (which includes our privacy policy) before signing up. After submitting the form, you will be sent an email with your account details.',
                              }, ]
                  } );

            var panel = Ext.create( 'Ext.form.Panel', {
               id : 'aspireResetPasswordForm',
               bodyPadding : 5,
               padding : '5 5 5 5',
               layout : 'anchor',
               defaults : {
                  anchor : '100%'
               },

               // The fields
               defaultType : 'textfield',
               items : [ {
                  xtype : 'label',
                  itemId : 'message',
                  style : 'font-family: sans-serif; color: red;',
                  text : 'Reset password failed.',
                  hidden : true
               }, {
                  xtype : 'textfield',
                  id : 'resetPasswordUsername',
                  fieldLabel : 'Username',
                  labelWidth : 200,
                  allowBlank : false,
                  style : 'margin-top:5px',
                  minLength : 4
               }, {
                  xtype : 'textfield',
                  id : 'resetPasswordEmail',
                  fieldLabel : 'Email',
                  labelWidth : 200,
                  vtype : 'email',
                  allowBlank : false
               }, ],

               buttons : [ {
                  xtype : 'button',
                  itemId : 'helpButton',
                  text : 'Help',
                  style : 'float: left;',
                  handler : function() {
                     window.open( "http://aspiredb.sites.olt.ubc.ca/", "_blank", "" );
                  }
               }, {
                  xtype : 'button',
                  itemId : 'submitButton',
                  text : 'Submit',
                  handler : ref.submitHandler,
                  scope : ref
               } ],

            } );

            this.add( panel );

            this.doLayout();

         },

         submitHandler : function() {

            var me = this;
            var form = me.down( '#aspireResetPasswordForm' ).getForm();

            if ( !form.isValid() ) {
               var messageLabel = me.down( '#message' );
               messageLabel.setText( 'Form contains missing or invalid fields', false );
               messageLabel.show();
               return;
            }

            Ext.Ajax.request( {
               url : 'resetPassword.html',
               method : 'POST',
               scope : me,
               params : Ext.Object.toQueryString( {
                  'resetPasswordId' : me.down( '#resetPasswordUsername' ).value,
                  'resetPasswordEmail' : me.down( '#resetPasswordEmail' ).value,
                  'ajaxLoginTrue' : true
               } ),
               success : function(response) {
                  var json = Ext.util.JSON.decode( response.responseText );
                  var messageLabel = me.down( '#message' );
                  var usernameTextfield = me.down( '#resetPasswordUsername' );
                  var emailTextfield = me.down( '#resetPasswordEmail' );

                  usernameTextfield.reset();
                  emailTextfield.reset();

                  if ( json.success === 'success' ) {
                     messageLabel.setText( json.message, false );
                     messageLabel.show();
                  } else {
                     console.log( json.message );
                     messageLabel.setText( json.message, false );
                     messageLabel.show();
                  }
               },
               failure : function(response, opts) {
                  var json = Ext.util.JSON.decode( response.responseText );
                  var messageLabel = me.down( '#message' );
                  messageLabel.setValue( json.message, false );
                  messageLabel.show();
                  console.log( json.message );
               }
            } );

         }

      } );
