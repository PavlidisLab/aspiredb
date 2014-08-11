Ext.require( [ 'Ext.layout.container.*', 'Ext.form.*', 'Ext.Img', 'Ext.tip.QuickTipManager' ] );

Ext
   .define(
      'ASPIREdb.view.RegistrationForm',
      {
         extend : 'Ext.container.Viewport',
         title : 'Please Register!.',
         id : 'registerFormPanel',
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
            this.callParent();

            var ref = this;

            var textPanel = Ext.create('Ext.form.Panel',{
                     id : 'aspireRegistrationTextForm',
                     border : false,

                     layout : 'hbox',
                     defaults : {
                        anchor : '100%'
                     },

                     // The fields
                     items : [{  xtype : 'displayfield',
                                 fieldLabel : '',
                                 value : 'Register to use use features of ASPIREDB like data upload. You might want to review the Terms and conditions (which includes our privacy policy) before signing up. After submitting the form, you will be sent an email with your account details.',
                              }, ]
                  } );

            // this.add(textPanel);

            var panel = Ext.create( 'Ext.form.Panel', {
               id : 'aspireRegistrationForm',
               title : 'User Registration Form',
               bodyPadding : 5,
               padding : '50 50 50 50',
               layout : 'anchor',
               defaults : {
                  anchor : '100%'
               },

               // The fields
               defaultType : 'textfield',
               items : [{
                           xtype : 'textfield',
                           name : 'username',
                           fieldLabel : 'User Name',
                           labelWidth : 200,
                           allowBlank : false,
                           minLength : 6
                        },
                        {
                           xtype : 'textfield',
                           name : 'email',
                           fieldLabel : 'Email',
                           labelWidth : 200,
                           vtype : 'email',
                           allowBlank : false
                        },
                        {
                           xtype : 'textfield',
                           name : 'password1',
                           fieldLabel : 'Password',
                           labelWidth : 200,
                           inputType : 'password',
                           style : 'margin-top:15px',
                           allowBlank : false,
                           minLength : 8
                        },
                        {
                           xtype : 'textfield',
                           name : 'password2',
                           labelWidth : 200,
                           fieldLabel : 'Confirm Password',
                           inputType : 'password',
                           allowBlank : false,
                           /**
                            * Custom validator implementation - checks that the value matches what was entered into the
                            * password1 field.
                            */
                           validator : function(value) {
                              var password1 = this.previousSibling( '[name=password1]' );
                              return (value === password1.getValue()) ? true : 'Passwords do not match.'
                           }
                        },
                        {
                           xtype : 'panel',
                           itemId : 'reCaptcha',
                           border : true,
                           width : 420,
                           height : 80,
                           html : '<div id="recaptcha">adsf</div>',
                           listeners : {
                              afterrender : function() {
                                 // console.log(Ext.getDom(this.body));
                                 Recaptcha.create( "6Lf4KAkAAAAAADFjpOSiyfHhlQ1pkznapAnmIvyr", Ext.getDom( this.body ),
                                    {
                                       theme : "clean",
                                       callback : Recaptcha.focus_response_field
                                    } );
                              }
                           }
                        },

                        /*
                         * { id : 'ajaxRegisterTrue', name : 'ajaxRegisterTrue', hidden : true, value : 'true' },
                         */

                        /*
                         * Terms of Use acceptance checkbox. Two things are special about this: 1) The boxLabel contains
                         * a HTML link to the Terms of Use page; a special click listener opens this page in a modal Ext
                         * window for convenient viewing, and the Decline and Accept buttons in the window update the
                         * checkbox's state automatically. 2) This checkbox is required, i.e. the form will not be able
                         * to be submitted unless the user has checked the box. Ext does not have this type of
                         * validation built in for checkboxes, so we add a custom getErrors method implementation.
                         */
                        {
                           xtype : 'checkboxfield',
                           name : 'acceptTerms',
                           fieldLabel : 'Terms of Use',
                           hideLabel : true,
                           style : 'margin-top:15px',
                           boxLabel : 'I have read and accept the <a href="" class="terms">Terms of Use</a>.',

                           // Listener to open the Terms of Use page link in a modal window
                           listeners : {
                              click : {
                                 element : 'boxLabelEl',
                                 fn : function(e) {
                                    var target = e.getTarget( '.terms' ), win;
                                    if ( target ) {
                                       win = Ext.widget( 'window', {
                                          title : 'Terms of Use',
                                          modal : true,
                                          html : '<iframe src="' + target.href
                                             + '" width="950" height="500" style="border:0"></iframe>',
                                          buttons : [ {
                                             text : 'Decline',
                                             handler : function() {
                                                this.up( 'window' ).close();
                                                formPanel.down( '[name=acceptTerms]' ).setValue( false );
                                             }
                                          }, {
                                             text : 'Accept',
                                             handler : function() {
                                                this.up( 'window' ).close();
                                                formPanel.down( '[name=acceptTerms]' ).setValue( true );
                                             }
                                          } ]
                                       } );
                                       win.show();
                                       e.preventDefault();
                                    }
                                 }
                              }
                           },

                           // Custom validation logic - requires the checkbox to be checked
                           getErrors : function() {
                              return this.getValue() ? [] : [ 'You must accept the Terms of Use' ]
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
                  itemId : 'clearButton',
                  text : 'Clear',
                  handler : function() {
                     var me = this.ownerCt.ownerCt;
                     me.getComponent( 'username' ).setValue( '' );
                     me.getComponent( 'password' ).setValue( '' );
                     me.getComponent( 'email' ).setValue( '' );

                  }
               }, {
                  xtype : 'button',
                  itemId : 'loginButton',
                  text : 'Login',
                  handler : ref.submitHandler,
                  scope : ref
               } ],
               
            } );

            this.add( panel );

            this.doLayout();

         },

         submitHandler : function() {
                      
            var me = this;
          //  var form = this.up( 'registerFormPanel' ).getForm();
            
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
                  'j_email' : me.down( '#email' ).getValue(),
                  'ajaxLoginTrue' : true
               } ),
               success : function(response) {
                  var messageLabel = me.down( '#message' );
                  var usernameTextfield = me.down( '#username' );
                  var passwordTextfield = me.down( '#password' );
                  var emailTextfield = me.down( '#email' );

                  usernameTextfield.reset();
                  passwordTextfield.reset();
                  emailTextfield.reset();

                  if ( response.responseText === 'success' ) {
                     window.location.href = "home.html";
                     messageLabel.hide();
                  } else {
                     messageLabel.show();
                  }
               },
               failure : function(response, opts) {
                  var messageLabel = me.down( '#message' );
                  messageLabel.show();
               }
            } );

         }

      } );
