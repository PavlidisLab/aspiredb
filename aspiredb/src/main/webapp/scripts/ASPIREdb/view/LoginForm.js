Ext.require( [ 'Ext.layout.container.*' ] );

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
            xtype : 'textfield',
            itemId : 'username',
            fieldLabel : 'Username',
            allowBlank : false,
            value : 'administrator',
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
            value : 'changemeadmin',
            listeners : {
               specialkey : function(field, e) {
                  if ( e.getKey() == e.ENTER ) {
                     ref.submitHandler();
                  }
               }
            }
         }, {
            xtype : 'label',
            itemId : 'message',
            style : 'font-family: sans-serif; font-size: 10px; color: red;',
            text : 'Login failed. Username or password incorrect.',
            hidden : true
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

      var textPanel = Ext.create( 'Ext.form.Panel', {
         id : 'aspireRegistrationForm',
         border : false,

         layout : 'hbox',
         defaults : {
            anchor : '100%'
         },

         // The fields
         items : [ {
            xtype : 'panel',
            padding : '5 5 30 20',
            html : "Are you a new user? <a href ='/aspiredb/register.html'>Register</a> to become a user",

         } ]
      } );

      this.add( textPanel );

      this.doLayout();

   },

   submitHandler : function() {

      var me = this;
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
            var messageLabel = me.down( '#message' );
            var usernameTextfield = me.down( '#username' );
            var passwordTextfield = me.down( '#password' );

            usernameTextfield.reset();
            passwordTextfield.reset();

            if (response.responseText.indexOf("success:true") > 0) {
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
