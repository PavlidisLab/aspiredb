/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import java.util.Date;
import java.util.HashSet;

import ubc.pavlab.aspiredb.client.aspiredb;
import ubc.pavlab.aspiredb.client.events.LoginEvent;
import ubc.pavlab.aspiredb.client.events.LogoutEvent;
import ubc.pavlab.aspiredb.client.events.SubjectFilterEvent;
import ubc.pavlab.aspiredb.client.events.VariantFilterEvent;
import ubc.pavlab.aspiredb.client.handlers.LoginEventHandler;
import ubc.pavlab.aspiredb.client.handlers.LogoutEventHandler;
import ubc.pavlab.aspiredb.client.handlers.SubjectFilterHandler;
import ubc.pavlab.aspiredb.client.handlers.VariantFilterHandler;
import ubc.pavlab.aspiredb.client.service.LoginStatusService;
import ubc.pavlab.aspiredb.client.service.LoginStatusServiceAsync;
import ubc.pavlab.aspiredb.client.service.VariantService;
import ubc.pavlab.aspiredb.client.service.VariantServiceAsync;
import ubc.pavlab.aspiredb.client.util.GemmaURLUtils;
import ubc.pavlab.aspiredb.client.view.common.LoginForm;
import ubc.pavlab.aspiredb.client.view.filter.FilterWindow;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.GenomicLocationProperty;
import ubc.pavlab.aspiredb.shared.query.RestrictionFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SetOperator;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

/**
 * Main panel for AspireDb
 * 
 * @author mly
 * @version $Id: AspireDbPanel.java,v 1.14 2013/07/12 19:38:45 anton Exp $
 */
public class AspireDbPanel extends Composite implements RequiresResize, ProvidesResize {

    // UIBinder boilerplate.
    interface MyUIBinder extends UiBinder<Widget, AspireDbPanel> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
    
    @UiField
    HTMLPanel header;

    @UiField
    HTMLPanel toolPanel;
    
    @UiField(provided = true)
    MainPanel mainPanel;

    private DashboardDialog dashboard;

    private LoginForm loginForm;

    @UiField
    TextButton clearFilterButton;

    @UiField
    Widget logoutForm;
    
    @UiField
    HTML copyRight;

    private final LoginStatusServiceAsync loginStatusService = GWT.create( LoginStatusService.class );
    private final VariantServiceAsync variantService = GWT.create( VariantService.class );

    public AspireDbPanel() {
        mainPanel = new MainPanel();

        initWidget( uiBinder.createAndBindUi( this ) );
        addHandlers();
        
    	loginForm = new LoginForm();
        checkIfAlreadyLoggedIn();
        
        Date date = new Date();
        DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy");
        
        String copyRightText= "Copyright &copy; "+dtf.format( date )+" University of British Columbia";
        copyRight.setHTML( copyRightText );

		dashboard = new DashboardDialog();
		dashboard.setCancelButtonVisible(false);
		dashboard.addOkButtonSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
			    toolPanel.setVisible( true );

			    mainPanel.initialize();
			    mainPanel.setVisible(true);

			    dashboard.setCancelButtonVisible(true);
			}
		});
    }

    private void checkIfAlreadyLoggedIn() {
        loginStatusService.isLoggedIn( new AsyncCallback<Boolean>() {
            @Override
            public void onFailure( Throwable e ) {
                // does this mean they are not logged in?
                // Answer: no, this means that the call failed.
                // log.info( "Not logged in: " + e.getMessage() );
            }

            @Override
            public void onSuccess( Boolean result ) {
                if ( result ) {
                    aspiredb.EVENT_BUS.fireEvent( new LoginEvent() );
                    loginForm.hide();
                } else { // this means that they are not logged in                	
                	loginForm.center();
                }
            }
        } );
    }
    
    private void addHandlers() {
    	final AspireDbPanel me = this;
    	
        aspiredb.EVENT_BUS.addHandler( LoginEvent.TYPE, new LoginEventHandler() {
            @Override
            public void onLogin( LoginEvent event ) {
                loginForm.setVisible( false );
                logoutForm.setVisible( true );

                toolPanel.setVisible( false );
                mainPanel.setVisible( false );                
                dashboard.show();
                me.parseUrlParametersAndRedirect();
            }
        } );

        aspiredb.EVENT_BUS.addHandler( LogoutEvent.TYPE, new LogoutEventHandler() {
            @Override
            public void onLogout( LogoutEvent event ) {
                loginForm.setVisible( true );
                logoutForm.setVisible( false );

                toolPanel.setVisible( false );
                mainPanel.setVisible( false );
                dashboard.hide();
            }
        } );

        //FIXME: think about moving this
        aspiredb.EVENT_BUS.addHandler( VariantFilterEvent.TYPE, new VariantFilterHandler() {
            @Override
            public void onFilter( VariantFilterEvent event ) {
                mainPanel.variantGrid.applyFilter( event );
            }
        } );

        //FIXME: think about moving this
        aspiredb.EVENT_BUS.addHandler( SubjectFilterEvent.TYPE, new SubjectFilterHandler() {
            @Override
            public void onFilter( SubjectFilterEvent event ) {
                mainPanel.subjectGrid.applyFilter( event );
            }
        } );
    }

    /**
     * To support use-case where variantId is passed in the url parameters from UCSC browser.
     */
    void parseUrlParametersAndRedirect() {
    	String variantId = Location.getParameter("variantId");    	
    	if ( variantId != null && !variantId.isEmpty() ) {
    		// Grab genomic range
    		variantService.getVariant(Long.parseLong(variantId), new AsyncCallback<VariantValueObject>() {

                @Override
                public void onFailure(Throwable caught) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onSuccess(VariantValueObject vo) {
                    VariantFilterConfig filterConfig = new VariantFilterConfig();
                    SimpleRestriction genomicRangeRestriction =
                            new SimpleRestriction(new GenomicLocationProperty(), SetOperator.IS_IN, vo.getGenomicRange());
                    filterConfig.setRestriction(genomicRangeRestriction);
                    mainPanel.filterWindow.fireEvent(new SubjectFilterEvent(filterConfig));

                    mainPanel.resizeMe();
                }
            });
    	}
	}

	@Override
    public void onResize() {
	    mainPanel.onResize();
    }

    /**
     * @param event
     */
    @UiHandler("clearFilterButton")
    public void onClearFilterClick( SelectEvent event ) {
        FilterWindow filterWindow = this.mainPanel.filterWindow;

        filterWindow.clearFilter();

        SubjectFilterEvent e = new SubjectFilterEvent( filterWindow.getFilterConfigs() );
        filterWindow.fireEvent( e );
    }

    /**
     * @param event
     */
    @UiHandler("filterButton")
    protected void onFilterButtonClick( SelectEvent event ) {
        this.mainPanel.showFilterWindow();
    }

    /**
     * @param event
     */
    @UiHandler("dashboardButton")
    protected void onDashboardButtonClick( SelectEvent event ) {
        this.dashboard.show();
    }

    /**
     * @param event
     */
    @UiHandler("helpButton")
    protected void onHelpButtonClick( SelectEvent event ) {
        Window.open( GemmaURLUtils.getHelpPageURL( ) , "_blank", "" );
    }
}
