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
Ext.require( [ 'Ext.layout.container.*', 'Ext.form.*', 'Ext.Img', 'Ext.tip.QuickTipManager' ] );

/**
 * User registration form.
 */
Ext
   .define(
      'ASPIREdb.view.RegistrationForm',
      {
         extend : 'Ext.Window',
         title : 'Please Register!.',
         id : 'registerFormPanel',

         singleton : true,
         title : 'User Registration',
         closable : true,
         resizable : false,
         closeAction : 'hide',

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

         resetFields : function() {
            var me = this;
            var messageLabel = me.down( '#message' );
            var usernameTextfield = me.down( '#username' );
            var password1Textfield = me.down( '#password1' );
            var password2Textfield = me.down( '#password2' );
            var emailTextfield = me.down( '#email' );
            var reCaptcha = me.down( '#reCaptcha' );

            usernameTextfield.reset();
            password1Textfield.reset();
            password2Textfield.reset();
            emailTextfield.reset();
            me.showCaptcha( reCaptcha.getEl() );
         },

         initAndShow : function() {
            this.show();
         },

         initComponent : function() {
            this.callParent();

            this.on( 'hide', this.resetFields );

            var ref = this;

            var textPanel = Ext
               .create(
                  'Ext.form.Panel',
                  {
                     id : 'aspireRegistrationTextForm',
                     border : false,

                     // height : 300,

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

            // this.add(textPanel);

            var panel = Ext.create( 'Ext.form.Panel', {
               id : 'aspireRegistrationForm',
               // title : 'Registration Form',
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
                  text : 'Registration failed.',
                  hidden : true
               }, {
                  xtype : 'textfield',
                  id : 'username',
                  fieldLabel : 'Username',
                  labelWidth : 200,
                  allowBlank : false,
                  style : 'margin-top:5px',
                  minLength : 6
               }, {
                  xtype : 'textfield',
                  id : 'email',
                  fieldLabel : 'Email',
                  labelWidth : 200,
                  vtype : 'email',
                  allowBlank : false
               }, {
                  xtype : 'textfield',
                  id : 'password1',
                  fieldLabel : 'Password',
                  labelWidth : 200,
                  inputType : 'password',
                  // style : 'margin-top:15px',
                  allowBlank : false,
                  minLength : 6
               }, {
                  xtype : 'textfield',
                  id : 'password2',
                  labelWidth : 200,
                  fieldLabel : 'Confirm Password',
                  inputType : 'password',
                  allowBlank : false,
                  /**
                   * Custom validator implementation - checks that the value matches what was entered into the password1
                   * field.
                   */
                  validator : function(value) {
                     var password1 = this.previousSibling( '[id=password1]' );
                     return (value === password1.getValue()) ? true : 'Passwords do not match.'
                  }
               }, {
                  xtype : 'panel',
                  itemId : 'reCaptcha',
                  border : true,
                  width : 440,
                  height : 120,
                  style : 'margin-top:10px',
                  html : '<div id="recaptcha"></div>',
                  listeners : {
                     afterrender : function() {

                        this.up( '#registerFormPanel' ).showCaptcha( this.body );
                     }
                  }
               },

               ],

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
                  itemId : 'registerButton',
                  text : 'Register',
                  handler : ref.registerHandler,
                  scope : ref
               } ],

            } );

            this.add( panel );

            this.doLayout();

         },

         showCaptcha : function(ele) {
            // console.log(Ext.getDom(this.body));
            Recaptcha.create( "6Lf4KAkAAAAAADFjpOSiyfHhlQ1pkznapAnmIvyr", Ext.getDom( ele ), {
               theme : "clean",
               callback : Recaptcha.focus_response_field
            } );
         },

         registerHandler : function() {

            var me = this;
            var form = me.down( '#aspireRegistrationForm' ).getForm();
            var recaptchaText = Recaptcha.get_response();

            if ( !form.isValid() || recaptchaText.length == 0 ) {
               // Ext.Msg.alert( 'Error', 'Form is not valid' );
               var messageLabel = me.down( '#message' );
               messageLabel.setText( 'Form contains missing or invalid fields', false );
               messageLabel.show();
               return;
            }

            Ext.Ajax.request( {
               url : 'signup.html',
               method : 'POST',
               scope : me,
               params : Ext.Object.toQueryString( {
                  'username' : me.down( '#username' ).value,
                  'password' : me.down( '#password1' ).value,
                  'passwordConfirm' : me.down( '#password2' ).value,
                  'email' : me.down( '#email' ).value,
                  'recaptcha_challenge_field' : Recaptcha.get_challenge(),
                  'recaptcha_response_field' : Recaptcha.get_response(),
                  'ajaxLoginTrue' : true
               } ),
               success : function(response) {
                  var json = Ext.util.JSON.decode( response.responseText );
                  var messageLabel = me.down( '#message' );
                  var usernameTextfield = me.down( '#username' );
                  var password1Textfield = me.down( '#password1' );
                  var password2Textfield = me.down( '#password2' );
                  var emailTextfield = me.down( '#email' );
                  var reCaptcha = me.down( '#reCaptcha' );

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
