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
 * Edit user profile form for tasks like changing user password.
 */
Ext
   .define(
      'ASPIREdb.view.EditProfileForm',
      {
         extend : 'Ext.Window',
         id : 'editProfileFormPanel',
         singleton : true,
         title : 'Edit your profile',
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
                     id : 'aspireEditProfileTextForm',
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
               id : 'aspireEditProfileForm',
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
                  id : 'currentPassword',
                  fieldLabel : 'Current password',
                  labelWidth : 200,
                  inputType : 'password',
                  allowBlank : false,
                  minLength : 6
               }, {
                  xtype : 'textfield',
                  id : 'newPassword',
                  fieldLabel : 'New password',
                  labelWidth : 200,
                  inputType : 'password',
                  allowBlank : false,
                  minLength : 6
               }, {
                  xtype : 'textfield',
                  id : 'newPasswordConfirm',
                  fieldLabel : 'Confirm new password',
                  labelWidth : 200,
                  inputType : 'password',
                  allowBlank : false,
                  minLength : 6,
                  /**
                   * Custom validator implementation - checks that the value matches what was entered into the password1
                   * field.
                   */
                  validator : function(value) {
                     var newPassword = this.previousSibling( '[id=newPassword]' );
                     return (value === newPassword.getValue()) ? true : 'Passwords do not match.'
                  }
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

            this.on( 'hide', this.closeActionHandler );

            this.doLayout();

         },

         closeActionHandler : function() {
            var me = this;

            var messageLabel = me.down( '#message' );
            var currentPassword = me.down( '#currentPassword' );
            var newPassword = me.down( '#newPassword' );
            var newPasswordConfirm = me.down( '#newPasswordConfirm' );

            currentPassword.reset();
            newPassword.reset();
            newPasswordConfirm.reset();
            messageLabel.setText( "" );

            messageLabel.hide();
         },

         submitHandler : function() {

            var me = this;
            var form = me.down( '#aspireEditProfileForm' ).getForm();

            if ( !form.isValid() ) {
               var messageLabel = me.down( '#message' );
               messageLabel.setText( 'Form contains missing or invalid fields', false );
               messageLabel.show();
               return;
            }

            Ext.Ajax.request( {
               url : 'editUser.html',
               method : 'POST',
               scope : me,
               params : Ext.Object.toQueryString( {
                  'oldPassword' : me.down( '#currentPassword' ).value,
                  'password' : me.down( '#newPassword' ).value,
                  'passwordConfirm' : me.down( '#newPasswordConfirm' ).value,
                  'ajaxLoginTrue' : true
               } ),
               success : function(response) {
                  var json = Ext.util.JSON.decode( response.responseText );
                  var messageLabel = me.down( '#message' );

                  me.closeActionHandler();

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

                  me.closeActionHandler();

                  var json = Ext.util.JSON.decode( response.responseText );
                  var messageLabel = me.down( '#message' );
                  messageLabel.setValue( json.message, false );
                  messageLabel.show();
                  console.log( json.message );

               }
            } );

         }

      } );
