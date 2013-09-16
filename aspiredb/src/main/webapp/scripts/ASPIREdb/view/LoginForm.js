Ext.require([ 'Ext.layout.container.*' ]);

Ext.define('ASPIREdb.view.LoginForm', {
	extend : 'Ext.container.Viewport',
	title : 'Welcome, please login.',
	width : 800,
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

		var panel = Ext.create('Ext.form.Panel', {
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
				allowBlank : false
			}, {
				xtype : 'textfield',
				itemId : 'password',
				fieldLabel : 'Password',
				inputType : 'password',
				allowBlank : false
			}, {
				xtype : 'label',
				itemId : 'message',
				style : 'font-family: sans-serif; font-size: 10px; color: red;',
				text : 'Login failed. Username or password incorrect.',
				hidden : true
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
					window.open("http://aspiredb.sites.olt.ubc.ca/", "_blank", "");
				}
			}, {
				xtype : 'button',
				itemId : 'clearButton',
				text : 'Clear',
				handler : function() {
					var me = this.ownerCt.ownerCt;
					me.getComponent('username').setValue('');
					me.getComponent('password').setValue('');
					
				}
			}, {
				xtype : 'button',
				itemId : 'loginButton',
				text : 'Login',
				handler : function() {

					var me = this.ownerCt.ownerCt;
					Ext.Ajax.request({
						url : 'j_spring_security_check',
						method : 'POST',
						headers : {
							'Content-Type' : 'application/x-www-form-urlencoded'
						},
						params : Ext.Object.toQueryString({
							'j_username' : me.getComponent('username').getValue(),
							'j_password' : me.getComponent('password').getValue(),
							'ajaxLoginTrue' : true
						}),
						success : function(response) {
							var messageLabel = me.getComponent('message');
							var usernameTextfield = me.getComponent('username');
							var passwordTextfield = me.getComponent('password');

							usernameTextfield.reset();
							passwordTextfield.reset();

							if (response.responseText === 'success') {
								window.location.href = "home.html";
								messageLabel.hide();
							} else {
								messageLabel.show();
							}
						},
						failure : function(response, opts) {
							var messageLabel = me.getComponent('message');
							messageLabel.show();
						}
					});

				}
			} ]
		});

		this.add(panel);

		this.doLayout();

	}

});
