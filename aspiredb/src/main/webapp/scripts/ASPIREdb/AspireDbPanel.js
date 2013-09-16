Ext.require([
    'ASPIREdb.MainPanel',
    'ASPIREdb.EVENT_BUS',
    'ASPIREdb.view.filter.FilterWindow',
    'ASPIREdb.ActiveProjectSettings',
    'ASPIREdb.view.DashboardWindow'
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

        var aspireDbPanel = this;

        // TODO: finish me
        ASPIREdb.EVENT_BUS.on('login',
            function(event) {
                aspireDbPanel.getLoginForm().hide();
                aspireDbPanel.getComponent('topToolbar').getComponent('logoutForm').show();

                ASPIREdb.view.DashboardWindow.show();
                
            }
        );

        // TODO: finish me
        ASPIREdb.EVENT_BUS.on('logout',
            function(event) {
                /*
                 loginForm.setVisible( true );
                 logoutForm.setVisible( false );

                 toolPanel.setVisible( false );
                 mainPanel.setVisible( false );
                 dashboard.hide();
                 */
            }
        );
    },

    parseUrlParametersAndRedirect: function () {
        var parsedParams = Ext.Object.fromQueryString(location.search);
        var variantId = parsedParams.variantId;
        if ( variantId != null && !variantId.isEmpty() ) {
            // Grab genomic range
            VariantService.getVariant(Long.parseLong(variantId), function callback(vo) {
                    var filterConfig = new VariantFilterConfig();
                    var genomicRangeRestriction = new SimpleRestriction();
                    genomicRangeRestriction.propery = new GenomicLocationProperty();
                    genomicRangeRestriction.operator = 'IS_IN';
                    genomicRangeRestriction.value = vo.genomicRange;
                    filterConfig.restriction(genomicRangeRestriction);
                    ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfig);
//                    mainPanel.resizeMe();
            });
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
                height: 30,
                handler: function() {
                    ASPIREdb.view.filter.FilterWindow.show();
                }
            },
            {
                xtype: 'button',
                text: 'Clear filter',
                itemId: 'clearFilterButton',
                height: 30,
                handler: function() {
                	var filterConfigs = [];
                    var activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
                    var projectFilter = new ProjectFilterConfig;
                    projectFilter.projectIds = activeProjectIds;
                    filterConfigs.push(projectFilter);
                    ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);
                }
            },
            {
                xtype: 'button',
                text: 'Dashboard',
                itemId: 'dashboardButton',
                height: 30,
                handler: function() {
                	ASPIREdb.view.DashboardWindow.show();
                }
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