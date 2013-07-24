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
package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.*;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.service.*;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author anton
 * @version $Id: ClearLabelWindow.java,v 1.1 2013/06/19 18:23:35 anton Exp $
 */
public class ClearLabelWindow extends Window {
    interface MyUIBinder extends UiBinder<Widget, ClearLabelWindow> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField(provided = true)
    protected Grid<LabelValueObject> labelGrid;

    private ListStore<LabelValueObject> labelStore;

    private final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );
    private final VariantServiceAsync variantService = GWT.create( VariantService.class );

    public ClearLabelWindow( final Set<LabelValueObject> possibleLabels, final Collection<Long> ids, final GridView view ) {
        final GridView gridView = view;

        labelStore = new ListStore<LabelValueObject>(new ModelKeyProvider<LabelValueObject>() {
            @Override
            public String getKey(LabelValueObject item) {
                return item.getId().toString();
            }
        });

        labelStore.addAll(possibleLabels);

        List<ColumnConfig<LabelValueObject, ?>> columnConfigs = new ArrayList<ColumnConfig<LabelValueObject, ?>>();

        ColumnConfig<LabelValueObject, String> nameColumn = new ColumnConfig<LabelValueObject, String>(
                new ValueProvider<LabelValueObject, String>() {
                    @Override
                    public String getValue( LabelValueObject label ) {
                        return label.getName();
                    }

                    @Override
                    public void setValue( LabelValueObject label, String value ) {
                    }

                    @Override
                    public String getPath() {
                        return null;
                    }
                }, 70, "Label" );

        ColumnConfig<LabelValueObject, String> clearButtonColumn = new ColumnConfig<LabelValueObject, String>(
                new ValueProvider<LabelValueObject, String>() {
                    @Override
                    public String getValue( LabelValueObject object ) {
                        return "Clear";
                    }

                    @Override
                    public void setValue( LabelValueObject object, String value ) {
                    }

                    @Override
                    public String getPath() {
                        return null;
                    }
                } );
        TextButtonCell textButtonCell = new TextButtonCell();
        textButtonCell.addSelectHandler( new SelectEvent.SelectHandler() {

            @Override
            public void onSelect( SelectEvent event ) {
                Cell.Context c = event.getContext();
                final int index = c.getIndex();
                final LabelValueObject label = labelStore.get( index );

                subjectService.removeLabel(ids, label, new AspireAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        labelStore.remove(label);
                        gridView.refresh(false);
                    }
                });

                variantService.removeLabel(ids, label, new AspireAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        labelStore.remove(label);
                        gridView.refresh(false);
                    }
                });
            }
        } );
        clearButtonColumn.setCell(textButtonCell);


        columnConfigs.add( nameColumn );
        ColumnModel<LabelValueObject> columnModel = new ColumnModel<LabelValueObject>( columnConfigs );

        labelGrid = new Grid<LabelValueObject>( labelStore, columnModel );
        labelGrid.setWidth( 300 );
        labelGrid.setHeight( 300 );

        setWidget( uiBinder.createAndBindUi( this ) );
    }
}
