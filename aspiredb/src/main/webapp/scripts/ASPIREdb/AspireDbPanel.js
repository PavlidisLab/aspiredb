Ext.require([ 'ASPIREdb.MainPanel', 'ASPIREdb.EVENT_BUS', 'ASPIREdb.view.filter.FilterWindow', 'ASPIREdb.ActiveProjectSettings', 'ASPIREdb.view.DashboardWindow' ]);

/**
 * Events: - login - logout
 */
Ext.define('ASPIREdb.AspireDbPanel', {
	itemId : 'aspireDbPanel',
	extend : 'Ext.container.Viewport',
	layout : 'border',
	config : {
		loginForm : null
	},
	initComponent : function() {
		this.callParent();

		var aspireDbPanel = this;

		ASPIREdb.EVENT_BUS.on('login', function(event) {
			
			aspireDbPanel.getComponent('topToolbar').getComponent('logoutForm').show();
			
			aspireDbPanel.disableToolbarButtonsForDashboard(true);

			ASPIREdb.view.DashboardWindow.show();

			var runner = new Ext.util.TaskRunner();

			// poll keep_alive page so session doesn't reset
			var task = runner.start({
				run : function() {
					Ext.Ajax.request({
						url : 'keep_alive.html',

						success : function(response) {
							var json = Ext.JSON.decode(response.responseText);

							// is we are logged out then redirect to login page
							if (!json.success) {
								runner.destroy();

								Ext.Msg.alert("You have been logged out", "You have been logged out due to inactivity, please login again.", function() {
									location.href = "/aspiredb/home.html";
								});

							}
						},
						
						failure : function(response, opts){
							runner.destroy();

							Ext.Msg.alert("You have been logged out", "You have been logged out due to inactivity, please login again.", function() {
								location.href = "/aspiredb/home.html";
							});
						}
					});
				},
				interval : 60000*10
			});

		});
		
		ASPIREdb.view.DashboardWindow.on('beforeclose', function(event) {
			aspireDbPanel.disableToolbarButtonsForDashboard(false);
		});

		// TODO: finish me
		ASPIREdb.EVENT_BUS.on('logout', function(event) {
			/*
			 * loginForm.setVisible( true ); logoutForm.setVisible( false );
			 * 
			 * toolPanel.setVisible( false ); mainPanel.setVisible( false );
			 * dashboard.hide();
			 */
		});

		ASPIREdb.EVENT_BUS.fireEvent('login');
		
		LoginStatusService.getCurrentUsername( {
			callback : function( username ) {
				aspireDbPanel.down('#message').setText('You are logged in as ' + username);
			}
		} );
	},
	
	disableToolbarButtonsForDashboard: function(yes){
		
		if (yes){
			this.down('#filterButton').disable();
			this.down('#clearFilterButton').disable();
		}else{
			this.down('#filterButton').enable();
			this.down('#clearFilterButton').enable();
		}
	},

	parseUrlParametersAndRedirect : function() {
		var parsedParams = Ext.Object.fromQueryString(location.search);
		var variantId = parsedParams.variantId;
		if (variantId !== null && !variantId.isEmpty()) {
			// Grab genomic range
			VariantService.getVariant(Long.parseLong(variantId), function callback(vo) {
				var filterConfig = new VariantFilterConfig();
				var genomicRangeRestriction = new SimpleRestriction();
				genomicRangeRestriction.propery = new GenomicLocationProperty();
				genomicRangeRestriction.operator = 'IS_IN';
				genomicRangeRestriction.value = vo.genomicRange;
				filterConfig.restriction(genomicRangeRestriction);
				ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfig);
				// mainPanel.resizeMe();
			});
		}
	},

	items : [ {
		region : 'north',
		itemId : 'topToolbar',
		height : 50,
		xtype : 'container',
		layout : 'column',
		items : [ {
			xtype : 'component',
			margin : '5 5 5 5',
			autoEl : {
				tag : 'img',
				src : 'scripts/ASPIREdb/resources/images/aspiredb-logo-smaller.png'
			}
		}, {
			xtype : 'button',
			text : 'Filter...',
			itemId : 'filterButton',
			height : 30,
			margin : '5 5 5 5',
			handler : function() {
				ASPIREdb.view.filter.FilterWindow.show();
			}
		}, {
			xtype : 'button',
			text : 'Clear filter',
			itemId : 'clearFilterButton',
			height : 30,
			margin : '5 5 5 5',
			handler : function() {
				var filterConfigs = [];
				var activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
				var projectFilter = new ProjectFilterConfig;
				projectFilter.projectIds = activeProjectIds;
				filterConfigs.push(projectFilter);
				ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);
			}
		}, {
			xtype : 'button',
			text : 'Dashboard',
			itemId : 'dashboardButton',
			height : 30,
			margin : '5 5 5 5',
			handler : function() {
				this.up('#aspireDbPanel').disableToolbarButtonsForDashboard(true);
				ASPIREdb.view.DashboardWindow.show();
			}
		}, {
			xtype : 'button',
			text : 'Help',
			itemId : 'helpButton',
			margin : '5 5 5 5',
			height : 30,
			handler : function() {
				window.open("http://aspiredb.sites.olt.ubc.ca/", "_blank", "");
			}
		}, {
			xtype : 'label',
			itemId : 'message',
			style : 'text-align: right; vertical-align : middle; padding-top : 10px',
			height : 30,
			margin : '5 5 5 5',
			columnWidth : 1,
		}, {
			xtype : 'button',
			text : 'Logout',
			itemId : 'logoutButton',
			height : 30,
			margin : '5 5 5 5',
			handler : function() {

				window.location.href = 'j_spring_security_logout';

			}
		}, {
			xtype : 'container',
			itemId : 'logoutForm',
			hidden : true,
			layout : 'hbox',
			items : [ {
				xtype : 'button',
				text : 'Admin Tools',
				height : 30,			
				margin : '5 5 5 5',
				itemId : 'adminToolsButton',
				hidden : true
			} ]
		} ]
	}, {
		region : 'center',
		xtype : 'ASPIREdb_mainpanel'
	} ]
});