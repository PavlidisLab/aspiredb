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
Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.RegistrationForm', 'ASPIREdb.view.ResetPasswordForm' ] );

/**
 * User login form for authenticating users.
 */
Ext.define( 'ASPIREdb.view.LoginForm', {
   extend : 'Ext.container.Viewport',
   title : 'Welcome, please login.',
   width : 1500,
   closable : false,
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
      autoEl : {
         tag : 'img',
         src : 'scripts/ASPIREdb/resources/images/aspiredb-logo-smaller.png'
      }
   } ],

   initComponent : function() {

      document.title = "ASPIREdb";

      this.callParent();

      var ref = this;

      var panel = Ext.create( 'Ext.form.Panel', {
         id : 'aspireLoginForm',
         title : 'Welcome, please login',
         bodyPadding : 5,
         padding : '50 50 50 50',

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
            text : 'Login failed. Username or password incorrect.',
            hidden : true
         }, {
            xtype : 'textfield',
            itemId : 'username',
            fieldLabel : 'Username',
            style : 'margin-top:5px',
            allowBlank : false,
            listeners : {
               specialkey : function(field, e) {
                  if ( e.getKey() == e.ENTER ) {
                     ref.submitHandler();
                  }
               }
            }
         }, {
            xtype : 'textfield',
            itemId : 'password',
            fieldLabel : 'Password',
            inputType : 'password',
            allowBlank : false,
            listeners : {
               specialkey : function(field, e) {
                  if ( e.getKey() == e.ENTER ) {
                     ref.submitHandler();
                  }
               }
            }
         }, {
            xtype : 'label',
            // html : '<a href="/aspiredb/resetPassword.html">Forgot your password?</a>',
            html : 'Forgot your password?',
            style : 'font-size:12px; color: blue; text-decoration: underline; ',
            listeners : {
               render : function(obj) {
                  obj.getEl().on( 'click', function() {
                     ASPIREdb.view.ResetPasswordForm.initAndShow();
                  }, this );
               }
            }
         } ],

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
            handler : function(obj) {
               ASPIREdb.view.RegistrationForm.initAndShow();
            }
         }, {
            xtype : 'button',
            itemId : 'loginButton',
            text : 'Login',
            handler : ref.submitHandler,
            scope : ref
         } ]
      } );

      this.add( panel );

      this.doLayout();

   },

   submitHandler : function() {

      var me = this;
      me.setLoading( true );

      Ext.Ajax.request( {
         url : 'j_spring_security_check',
         method : 'POST',
         headers : {
            'Content-Type' : 'application/x-www-form-urlencoded'
         },
         scope : me,
         params : Ext.Object.toQueryString( {
            'j_username' : me.down( '#username' ).getValue(),
            'j_password' : me.down( '#password' ).getValue(),
            'ajaxLoginTrue' : true
         } ),
         success : function(response) {
            me.setLoading( false );
            var messageLabel = me.down( '#message' );
            var usernameTextfield = me.down( '#username' );
            var passwordTextfield = me.down( '#password' );

            usernameTextfield.reset();
            passwordTextfield.reset();

            if ( response.responseText.indexOf( "success:true" ) > 0 ) {
               window.location.href = "home.html";
               messageLabel.hide();
            } else {
               messageLabel.show();
            }

         },
         failure : function(response, opts) {
            me.setLoading( false );
            var messageLabel = me.down( '#message' );
            messageLabel.show();
         }
      } );

   }

} );
