Ext.require( [ 'Ext.layout.container.*', 'Ext.form.*', 'Ext.Img', 'Ext.tip.QuickTipManager' ] );

Ext
   .define(
      'ASPIREdb.view.RegistrationForm',
      {
         // extend : 'Ext.container.Viewport',
         extend : 'Ext.Window',
         title : 'Please Register!.',
         id : 'registerFormPanel',
         // width : 1500,
         // closable : false,
         // resizable : false,

         singleton : true,
         title : 'User Registration Form',
         closable : true,
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
         // listeners : {
         // render : function(c) {
         // c.getEl().on( 'click', function() {
         // window.location.href = 'home.html';
         // }, c );
         // }
         // },
         // autoEl : {
         // tag : 'img',
         // src : 'scripts/ASPIREdb/resources/images/aspiredb-logo-smaller.png',
         // }
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
//               title : 'Registration Form',
//               bodyPadding : 5,
               // padding : '50 50 50 50',
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
                  minLength : 8
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

               /*
                * { id : 'ajaxRegisterTrue', name : 'ajaxRegisterTrue', hidden : true, value : 'true' },
                */

               /*
                * Terms of Use acceptance checkbox. Two things are special about this: 1) The boxLabel contains a HTML
                * link to the Terms of Use page; a special click listener opens this page in a modal Ext window for
                * convenient viewing, and the Decline and Accept buttons in the window update the checkbox's state
                * automatically. 2) This checkbox is required, i.e. the form will not be able to be submitted unless the
                * user has checked the box. Ext does not have this type of validation built in for checkboxes, so we add
                * a custom getErrors method implementation.
                */
               // {
               // xtype : 'checkboxfield',
               // name : 'acceptTerms',
               // fieldLabel : 'Terms of Use',
               // hideLabel : true,
               // style : 'margin-top:15px',
               // boxLabel : 'I have read and accept the <a href="" class="terms">Terms of Use</a>.',
               //
               // // Listener to open the Terms of Use page link in a modal window
               // listeners : {
               // click : {
               // element : 'boxLabelEl',
               // fn : function(e) {
               // var target = e.getTarget( '.terms' ), win;
               // if ( target ) {
               // win = Ext.widget( 'window', {
               // title : 'Terms of Use',
               // modal : true,
               // html : '<iframe src="' + target.href
               // + '" width="950" height="500" style="border:0"></iframe>',
               // buttons : [ {
               // text : 'Decline',
               // handler : function() {
               // this.up( 'window' ).close();
               // formPanel.down( '[name=acceptTerms]' ).setValue( false );
               // }
               // }, {
               // text : 'Accept',
               // handler : function() {
               // this.up( 'window' ).close();
               // formPanel.down( '[name=acceptTerms]' ).setValue( true );
               // }
               // } ]
               // } );
               // win.show();
               // e.preventDefault();
               // }
               // }
               // }
               // },
               //
               // // Custom validation logic - requires the checkbox to be checked
               // getErrors : function() {
               // return this.getValue() ? [] : [ 'You must accept the Terms of Use' ]
               // }
               // }
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
                  // xtype : 'button',
                  // itemId : 'clearButton',
                  // text : 'Clear',
                  // handler : function() {
                  // var me = this.ownerCt.ownerCt;
                  // me.getComponent( 'username' ).setValue( '' );
                  // me.getComponent( 'password' ).setValue( '' );
                  // me.getComponent( 'email' ).setValue( '' );
                  //
                  // }
                  // }, {
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
               // headers : {
               // 'Content-Type' : 'text/html'
               // },
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

                  usernameTextfield.reset();
                  password1Textfield.reset();
                  password2Textfield.reset();
                  emailTextfield.reset();
                  me.showCaptcha( reCaptcha.getEl() );

                  if ( json.success === 'success' ) {
                     messageLabel.setText( json.message, false );
                     messageLabel.show();
//                     window.setTimeout( function() {
//                        window.location.href = "home.html";
//                     }, 6000 );
                  } else {
                     console.log( json.message );
                     messageLabel.setText( json.message );
                     messageLabel.show();
                  }
               },
               failure : function(response, opts) {
                  var json = Ext.util.JSON.decode( response.responseText );
                  var messageLabel = me.down( '#message' );
                  messageLabel.setValue( json.message );
                  messageLabel.show();
                  console.log( json.message );
               }
            } );

         }

      } );
