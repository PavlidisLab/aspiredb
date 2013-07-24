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
package ubc.pavlab.aspiredb.client.view.subject;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.service.LabelService;
import ubc.pavlab.aspiredb.client.service.LabelServiceAsync;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author anton
 * @version $Id: LabelControlWindow.java,v 1.8 2013/07/12 21:21:06 anton Exp $
 */
public class LabelControlWindow extends Window {
    interface MyUIBinder extends UiBinder<Widget, LabelControlWindow> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    private class LabelDisplaySettings {
        private LabelValueObject label;
        private boolean isShown = false;

        private LabelDisplaySettings( LabelValueObject label ) {
            this.label = label;
        }

        private LabelValueObject getLabel() {
            return label;
        }

        private boolean isShown() {
            return isShown;
        }

        private void setShown( boolean shown ) {
            isShown = shown;
        }
    }

    @UiField(provided = true)
    protected Grid<LabelDisplaySettings> labelGrid;

    private ListStore<LabelDisplaySettings> labelDisplaySettingsStore;

    private final Set<Long> visibleLabelIds;

    private final LabelServiceAsync labelService = GWT.create( LabelService.class );

    public LabelControlWindow( Set<Long> visibleLabels, GridView view, final boolean isSubjectLabel ) {
        visibleLabelIds = visibleLabels;
        final GridView gridView = view;

        labelDisplaySettingsStore = new ListStore<LabelDisplaySettings>( new ModelKeyProvider<LabelDisplaySettings>() {
            @Override
            public String getKey( LabelDisplaySettings item ) {
                return item.getLabel().getId().toString();
            }
        } );

        List<ColumnConfig<LabelDisplaySettings, ?>> columnConfigs = new ArrayList<ColumnConfig<LabelDisplaySettings, ?>>();
        ColumnConfig<LabelDisplaySettings, Boolean> checkBoxColumn = new ColumnConfig<LabelDisplaySettings, Boolean>(
                new ValueProvider<LabelDisplaySettings, Boolean>() {
                    @Override
                    public Boolean getValue( LabelDisplaySettings object ) {
                        return object.isShown();
                    }

                    @Override
                    public void setValue( LabelDisplaySettings object, Boolean value ) {
                        object.setShown( value );
                    }

                    @Override
                    public String getPath() {
                        return "checkBox";
                    }
                }, 70, "Show" );

        CheckBoxCell checkBoxCell = new CheckBoxCell() {
            private boolean delayedGridRefreshScheduled = false;

            @Override
            public void onBrowserEvent( final Context context, Element parent, Boolean value, NativeEvent event,
                    ValueUpdater<Boolean> valueUpdater ) {
                valueUpdater = new ValueUpdater<Boolean>() {
                    @Override
                    public void update( Boolean value ) {
                        LabelDisplaySettings labelDisplaySettings = labelDisplaySettingsStore.get( context.getIndex() );
                        labelDisplaySettings.setShown( value );
                        if ( value ) {
                            visibleLabelIds.add( labelDisplaySettings.getLabel().getId() );
                        } else {
                            visibleLabelIds.remove( labelDisplaySettings.getLabel().getId() );
                        }

                        // Experimental: Trying to see if this improves UI responsiveness.
                        // Refresh the grid after a delay so that if multiple checkboxes are checked quickly
                        // only on refresh request is made.
                        if ( !delayedGridRefreshScheduled ) {
                            delayedGridRefreshScheduled = true;
                            Timer timer = new Timer() {
                                @Override
                                public void run() {
                                    delayedGridRefreshScheduled = false;
                                    gridView.refresh( false );
                                }
                            };
                            timer.schedule( 1000 );
                        }
                    }
                };
                super.onBrowserEvent( context, parent, value, event, valueUpdater );
            }
        };
        checkBoxColumn.setCell( checkBoxCell );

        ColumnConfig<LabelDisplaySettings, String> nameColumn = new ColumnConfig<LabelDisplaySettings, String>(
                new ValueProvider<LabelDisplaySettings, String>() {
                    @Override
                    public String getValue( LabelDisplaySettings object ) {
                        return object.getLabel().getName();
                    }

                    @Override
                    public void setValue( LabelDisplaySettings object, String value ) {
                    }

                    @Override
                    public String getPath() {
                        return "name";
                    }
                }, 70, "Label" );

        ColumnConfig<LabelDisplaySettings, String> removeButtonColumn = new ColumnConfig<LabelDisplaySettings, String>(
                new ValueProvider<LabelDisplaySettings, String>() {
                    @Override
                    public String getValue( LabelDisplaySettings object ) {
                        return "Delete";
                    }

                    @Override
                    public void setValue( LabelDisplaySettings object, String value ) {
                    }

                    @Override
                    public String getPath() {
                        return null;
                    }
                } );
        removeButtonColumn.setHeader("Actions");
        TextButtonCell textButtonCell = new TextButtonCell();
        textButtonCell.addSelectHandler( new SelectEvent.SelectHandler() {
            @Override
            public void onSelect( SelectEvent event ) {
                Cell.Context c = event.getContext();
                final int index = c.getIndex();
                final LabelDisplaySettings labelDisplaySettings = labelDisplaySettingsStore.get( index );
                final AspireAsyncCallback<Void> updateLabelGridCallback = new AspireAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        labelGrid.unmask();
                        labelDisplaySettingsStore.remove(index);
                        visibleLabelIds.remove(labelDisplaySettings.getLabel().getId());
                        gridView.refresh(false);
                    }
                };
                labelGrid.mask("Deleting...");
                if (isSubjectLabel) {
                    labelService.deleteSubjectLabel( labelDisplaySettings.getLabel(), updateLabelGridCallback );
                } else {
                    labelService.deleteVariantLabel( labelDisplaySettings.getLabel(), updateLabelGridCallback );
                }

            }
        } );
        removeButtonColumn.setCell( textButtonCell );

        columnConfigs.add( nameColumn );
        columnConfigs.add( checkBoxColumn );
        columnConfigs.add( removeButtonColumn );
        ColumnModel<LabelDisplaySettings> columnModel = new ColumnModel<LabelDisplaySettings>( columnConfigs );

        labelGrid = new Grid<LabelDisplaySettings>( labelDisplaySettingsStore, columnModel );
        labelGrid.setWidth( 300 );
        labelGrid.setHeight( 300 );

        setWidget( uiBinder.createAndBindUi( this ) );
        
        this.setHeadingText("Label settings");
        this.setResizable( false );
    }

    public void initializeStore( Collection<LabelValueObject> labels ) {
        labelDisplaySettingsStore.clear();
        for ( LabelValueObject label : labels ) {
            LabelDisplaySettings settings = new LabelDisplaySettings( label );
            if ( visibleLabelIds.contains( label.getId() ) ) {
                settings.setShown( true );
            }
            labelDisplaySettingsStore.add( settings );
        }
    }
}
