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
Ext.require( [ 'ASPIREdb.MainPanel', 'ASPIREdb.EVENT_BUS', 'ASPIREdb.view.filter.FilterWindow',
              'ASPIREdb.ActiveProjectSettings', 'ASPIREdb.view.DashboardWindow', 'ASPIREdb.view.GeneManagerWindow',
              'ASPIREdb.view.EditProfileForm' ] );

/**
 * For sorting fractions in data grids.
 */
Ext.apply( Ext.data.SortTypes, {
   asFraction : function(fraction) {
      var tokens = fraction.split( '/' );
      if ( tokens.length != 2 ) {
         return fraction;
      }
      return parseInt( tokens[0] ) / parseInt( tokens[1] ) * 1.0;
   }
} );

/**
 * Main Panel which trigger the Main Panel after user's successful login Events: - login - logout
 */
Ext.define( 'ASPIREdb.AspireDbPanel', {
   id : 'aspireDbPanel',
   extend : 'Ext.container.Viewport',
   layout : 'border',

   config : {
      loginForm : null
   },

   initComponent : function() {
      this.callParent();

      document.title = "ASPIREdb";

      var aspireDbPanel = this;

      ConfigUtils.getAppVersion( {
         callback : function(version) {

            if ( version == null ) {
               version = "";
            }
            aspireDbPanel.getComponent( 'statusbar' ).update(
               "ASPIREdb " + version + " Copyright " + new Date().getFullYear() );

         }
      } );

      ASPIREdb.EVENT_BUS.on( 'login', function(event) {

         aspireDbPanel.getComponent( 'topToolbar' ).getComponent( 'logoutForm' ).show();

         aspireDbPanel.disableToolbarButtonsForDashboard( true );

         ASPIREdb.view.DashboardWindow.show();

         var runner = new Ext.util.TaskRunner();

         // poll keep_alive page so session doesn't reset
         var task = runner.start( {
            run : function() {
               Ext.Ajax.request( {
                  url : 'keep_alive.html',

                  success : function(response) {
                     var json = Ext.JSON.decode( response.responseText );

                     // is we are logged out then redirect to login page
                     if ( !json.success ) {
                        runner.destroy();

                        Ext.Msg.alert( "You have been logged out",
                           "You have been logged out due to inactivity, please login again.", function() {
                              window.location.href = "home.html";
                           } );
                     }
                  },

                  failure : function(response, opts) {
                     runner.destroy();

                     Ext.Msg.alert( "You have been logged out",
                        "You have been logged out due to inactivity, please login again.", function() {
                           window.location.href = "home.html";
                        } );
                  }
               } );
            },
            interval : 60000 * 10
         } );

      } );

      ASPIREdb.view.DashboardWindow.on( 'beforeclose', function(event) {
         aspireDbPanel.disableToolbarButtonsForDashboard( false );
      } );

      ASPIREdb.EVENT_BUS.fireEvent( 'login' );

      LoginStatusService.getCurrentUsername( {
         callback : function(username) {
            aspireDbPanel.down( '#userBtn' ).setText( username );
         }
      } );

      ASPIREdb.EVENT_BUS.on( 'project_select', function(event) {

         var projecttitle = ASPIREdb.ActiveProjectSettings.getActiveProjectName();
         document.title = "ASPIREdb Project " + projecttitle;
      } );

      ASPIREdb.EVENT_BUS.on( 'filter_submit', function(filterConfigs) {
         var isFilter = false;
         for (var i = 0; i < filterConfigs.length; i++) {
            if ( !(filterConfigs[i] instanceof ProjectFilterConfig) ) {
               isFilter = true;
            }

         }
      } );

   },

   /**
    * Disable the filter button and clear filter button before closing the dashboard window
    * 
    * @param: 'yes' or 'no'
    */
   disableToolbarButtonsForDashboard : function(yes) {

      if ( yes ) {
         this.down( '#filterButton' ).disable();
         this.down( '#clearFilterButton' ).disable();
      } else {
         this.down( '#filterButton' ).enable();
         this.down( '#clearFilterButton' ).enable();
      }
   },

   parseUrlParametersAndRedirect : function() {
      var parsedParams = Ext.Object.fromQueryString( location.search );
      var variantId = parsedParams.variantId;
      if ( variantId !== null && !variantId.isEmpty() ) {
         // Grab genomic range
         VariantService.getVariant( Long.parseLong( variantId ), function callback(vo) {
            var filterConfig = new VariantFilterConfig();
            var genomicRangeRestriction = new SimpleRestriction();
            genomicRangeRestriction.propery = new GenomicLocationProperty();
            genomicRangeRestriction.operator = 'IS_IN';
            genomicRangeRestriction.value = vo.genomicRange;
            filterConfig.restriction( genomicRangeRestriction );
            //
            // console.log( "filter_submit event from aspiredbpanel parseurlparameters and redirect" );
            ASPIREdb.EVENT_BUS.fireEvent( 'filter_submit', filterConfig );
         } );
      }
   },

   items : [ {
      region : 'north',
      itemId : 'topToolbar',
      height : 50,
      xtype : 'toolbar',
      layout : 'hbox',
      items : [ {
         xtype : 'component',
         margin : '5 5 5 5',
         height : 30,
         width : 126,
         autoEl : {
            tag : 'img',
            src : 'scripts/ASPIREdb/resources/images/aspiredb-logo-smaller.png'
         }
      }, {
         xtype : 'button',
         text : 'Filter...',
         itemId : 'filterButton',
         tooltip : 'Use queries to filter subjects and variants',         
         height : 30,
         margin : '5 5 5 5',
         handler : function() {
            ASPIREdb.view.filter.FilterWindow.show();
         }
      }, {
         xtype : 'button',
         text : 'Clear filter',
         itemId : 'clearFilterButton',
         tooltip : 'Remove applied filters',         
         height : 30,
         margin : '5 5 5 5',
         handler : function() {
            var filterConfigs = [];
            var activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
            var projectFilter = new ProjectFilterConfig;
            projectFilter.projectIds = activeProjectIds;
            filterConfigs.push( projectFilter );

            console.log( "filter_submit event from Aspiredbpanel clearfilterbutton" );
            ASPIREdb.EVENT_BUS.fireEvent( 'filter_submit', filterConfigs );
         }
      }, {
         xtype : 'button',
         text : 'Dashboard',
         itemId : 'dashboardButton',
         height : 30,
         margin : '5 5 5 5',
         tooltip : 'Manage or load projects',         
         handler : function() {
            Ext.getCmp( 'aspireDbPanel' ).disableToolbarButtonsForDashboard( true );
            ASPIREdb.view.DashboardWindow.show();
         }
      }, {
         xtype : 'button',
         text : 'Gene Set Manager',
         itemId : 'geneManagerButton',
         tooltip : 'Define or edit gene sets.',         
         height : 30,
         margin : '5 5 5 5',
         handler : function() {
            ASPIREdb.view.GeneManagerWindow.initGridAndShow();

         }
      }, , {
         xtype : 'button',
         text : 'Help',
         itemId : 'helpButton',
         margin : '5 5 5 5',
         height : 30,
         handler : function() {
            window.open( "http://aspiredb.sites.olt.ubc.ca/", "_blank", "" );
         }
      }, {
         xtype : 'label',
         itemId : 'filterActivated',
         style : 'vertical-align : middle; padding-top : 10px; float:auto',
         height : 30,
         margin : '5 5 5 5',
         flex : 1
      }, {
         xtype : 'splitbutton',
         itemId : 'userBtn',
         text : 'anonymous',
         menu : [ {
            text : 'Edit your profile',
            tooltip : 'Change profile details',
            handler : function() {
               ASPIREdb.view.EditProfileForm.initAndShow();
            }
         }, {
            text : 'Log out',
            tooltip : 'Log out of ASPIREdb',
            handler : function() {
               window.location.href = 'j_spring_security_logout';
            }
         } ]
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
   }, {
      xtype : 'label',
      itemId : 'statusbar',
      region : 'south',
      html : 'ASPIREdb',
      style : 'font-size : 70%; padding : 2px 2px 2px 2px'
   } ]
} );
