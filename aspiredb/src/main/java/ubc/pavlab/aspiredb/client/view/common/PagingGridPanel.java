/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubc.pavlab.aspiredb.client.view.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.event.RefreshEvent.HasRefreshHandlers;
import com.sencha.gxt.widget.core.client.event.RefreshEvent.RefreshHandler;
import com.sencha.gxt.widget.core.client.event.RowClickEvent.HasRowClickHandlers;
import com.sencha.gxt.widget.core.client.event.RowClickEvent.RowClickHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.HasRowDoubleClickHandlers;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GroupingView;

/**
 * Simple paging grid. Contains Grid and PagingToolBar.
 * 
 * @author azoubare
 * @version $Id: PagingGridPanel.java,v 1.5 2013/06/11 22:30:39 anton Exp $
 * @param <M>
 */
public class PagingGridPanel<M> extends Composite implements HasRowClickHandlers, HasRowDoubleClickHandlers, HasRefreshHandlers, RequiresResize {
    
    // UIBinder boilerplate.
    @SuppressWarnings("rawtypes")
    interface MyUIBinder extends UiBinder<Widget, PagingGridPanel> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
    
    @UiField
    FlowPanel main;
    
    @UiField(provided=true)
    public Grid<M> grid;

//    @UiField
//    PagingToolBar pagingToolbar;
    
    public final GroupingView<M> view;
//    public final GridView<M> view;

    public PagingGridPanel( ListStore<M> store, ColumnModel<M> columnModel ) {
        grid = new Grid<M>( store, columnModel );
//        view = new LiveGridView<M>();

        view = new GroupingView<M>();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setEmptyText( "No results" );
     
        grid.setView(view);

        grid.getView().setAutoFill( true );
        
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public void setLoader ( PagingLoader<? extends PagingLoadConfig, PagingLoadResult<M>> loader) {
        grid.setLoader( loader );
//        pagingToolbar.bind( loader );
    }
    
    public void groupBy (ColumnConfig<M, Object> columnConfig) {   	
    	view.groupBy( columnConfig );
    }
    
    public void mask( String message ) {
        grid.mask( message );
    }
    
    public void unmask() {
        grid.unmask();
//        pagingToolbar.show(); // for unknown to me reason, seems important
    }
    
    @Override
    public HandlerRegistration addRowDoubleClickHandler( RowDoubleClickHandler handler ) {
        return grid.addRowDoubleClickHandler( handler );
    }

    @Override
    public HandlerRegistration addRowClickHandler( RowClickHandler handler ) {
        return grid.addRowClickHandler( handler );
    }

    @Override
    public HandlerRegistration addRefreshHandler( RefreshHandler handler ) {
        return grid.addRefreshHandler( handler );
    }
    
    @Override
    public void onResize() {
//        int availableWidth = this.main.getElement().getClientWidth();
//        int availableHeight = this.main.getElement().getClientHeight();
//        //FIXME: This a hack, resize is called before things are rendered and this causes grid to shrink to 0.
//        // I'll implement this the right way once I figure out how.
//        if (availableWidth == 0) return;  
//        if (availableHeight == 0) return;  
//        
//        grid.setWidth( availableWidth );
//        grid.setHeight( availableHeight - 20 );
//        
//        pagingToolbar.setWidth( availableWidth );
    }

    public void setWidth ( int width ) {
    	grid.setWidth( width );
//    	pagingToolbar.setWidth( width );
    }
    
	public void setHeight(int height) {
		grid.setHeight( height ); //- pagingToolbar.getOffsetHeight( false ) );
	}

}
