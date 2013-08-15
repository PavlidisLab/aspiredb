Ext.require([
    'ASPIREdb.MainPanel'
]);

/**
 * Events:
 *  - login
 *  - logout
 */
Ext.define('ASPIREdb.AspireDbPanel', {
    extend: 'Ext.container.Viewport',
    layout: 'border',
    config: {
        loginForm: null
    },
    initComponent: function() {
        this.callParent();
        this.relayEvents(this.getLoginForm(), ['login','logout']);
    },
    listeners: {
        login: {
            fn:function(event) {
                this.getLoginForm().hide();
                this.getComponent('topToolbar').getComponent('logoutForm').show();
/*
                loginForm.setVisible( false );
                logoutForm.setVisible( true );

                toolPanel.setVisible( false );
                mainPanel.setVisible( false );
                dashboard.show();
                me.parseUrlParametersAndRedirect();
*/
            }
        },
        logout: {
            fn:function(event) {
/*
                loginForm.setVisible( true );
                logoutForm.setVisible( false );

                toolPanel.setVisible( false );
                mainPanel.setVisible( false );
                dashboard.hide();
*/
            }
        }
    },
    items: [{
        region: 'north',
        itemId: 'topToolbar',
        height: 50,
        xtype: 'container',
        layout: 'column',
        items: [
            {
                xtype: 'component',
                autoEl : {
                    tag:'img',
                    src:'scripts/ASPIREdb/resources/images/aspiredb-logo-smaller.png'
                }
            },
            {
                xtype: 'button',
                text: 'Filter...',
                itemId: 'filterButton',
                height: 30
            },
            {
                xtype: 'button',
                text: 'Clear filter',
                itemId: 'clearFilterButton',
                height: 30
            },
            {
                xtype: 'button',
                text: 'Dashboard',
                itemId: 'dashboardButton',
                height: 30
            },
            {
                xtype: 'button',
                text: 'Help',
                itemId: 'helpButton',
                height: 30
            },
            {
                xtype:'container',
                itemId: 'logoutForm',
                hidden: true,
                layout:'hbox',
                items:[
                    {
                        xtype:'label',
                        text:'Logged in as...',
                        itemId:'message'
                    },
                    {
                        xtype:'button',
                        text:'Logout',
                        itemId:'logoutButton'
                    },
                    {
                        xtype:'button',
                        text:'Admin Tools',
                        itemId:'adminToolsButton',
                        hidden: true
                    }
                ]
            }
        ]
    }, {
        region: 'center',
        xtype: 'ASPIREdb_mainpanel'
    }]
});