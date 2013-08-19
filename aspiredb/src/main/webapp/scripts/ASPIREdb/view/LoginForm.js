Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.EVENT_BUS'
]);

Ext.define('ASPIREdb.view.LoginForm', {
    extend: 'Ext.window.Window',
    title: 'Welcome, please login.',
    width: 290,
    closable: false,
    resizable: false,
    layout: {
        type: 'vbox',
        padding: '5'
    },
    items: [
        {
            xtype: 'textfield',
            itemId: 'username',
            fieldLabel: 'Username',
            allowBlank: false
        },
        {
            xtype: 'textfield',
            itemId: 'password',
            fieldLabel: 'Password',
            inputType: 'password',
            allowBlank: false
        },
        {
            xtype: 'label',
            itemId: 'message',
            style: 'font-family: sans-serif; font-size: 10px; color: red;',
            text: 'Login failed. Username or password incorrect.',
            hidden: true
        }
    ],

    buttons: [
        {
            xtype: 'button',
            itemId: 'helpButton',
            text: 'Help',
            style: 'float: left;',
            handler : function() {
                window.open( "http://aspiredb.sites.olt.ubc.ca/" , "_blank", "" );
            }
        },
        {
            xtype: 'button',
            itemId: 'clearButton',
            text: 'Clear'
        },
        {
            xtype: 'button',
            itemId: 'loginButton',
            text: 'Login',
            handler: function() {
                var me = this.ownerCt.ownerCt;
                Ext.Ajax.request({
                    url: 'j_spring_security_check',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    params: Ext.Object.toQueryString(
                        {
                            'j_username' : me.getComponent('username').getValue(),
                            'j_password' : me.getComponent('password').getValue(),
                            'ajaxLoginTrue' : true
                        }
                    ),
                    success: function(response) {
                        var messageLabel = me.getComponent('message');
                        var usernameTextfield = me.getComponent('username');
                        var passwordTextfield = me.getComponent('password');

                        usernameTextfield.reset();
                        passwordTextfield.reset();

                        if ( response.responseText === 'success' ) {
                            ASPIREdb.EVENT_BUS.fireEvent('login');
                            messageLabel.hide();
                        } else {
                            messageLabel.show();
                        }
                    },
                    failure: function(response, opts) {

                    }
                });
            }
        }
    ],

    initComponent: function () {
        this.callParent();
    }

});
