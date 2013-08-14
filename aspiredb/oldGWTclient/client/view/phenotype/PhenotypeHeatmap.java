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
package ubc.pavlab.aspiredb.client.view.phenotype;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import ubc.pavlab.aspiredb.client.ActiveProjectSettings;
import ubc.pavlab.aspiredb.client.callback.AspireAsyncCallback;
import ubc.pavlab.aspiredb.client.service.SubjectService;
import ubc.pavlab.aspiredb.client.service.SubjectServiceAsync;
import ubc.pavlab.aspiredb.shared.SubjectValueObject;
import ubc.pavlab.aspiredb.shared.Table;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhenotypeHeatmap extends Composite {
	
    interface MyUIBinder extends UiBinder<Widget, PhenotypeHeatmap> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );
	
    @UiField(provided=true)
    Canvas canvasBox;
    
    @UiField
    SimpleContainer panel;
    
    Context2d ctx;
	
    private final SubjectServiceAsync subjectService = GWT.create( SubjectService.class );
//    private final PhenotypeServiceAsync phenotypeService = GWT.create( PhenotypeService.class );

	private Table<SubjectValueObject, String, String> table;
	
	public PhenotypeHeatmap () {

		canvasBox = Canvas.createIfSupported();
		
		initWidget( uiBinder.createAndBindUi( this ) );        
    
		ctx = canvasBox.getContext2d();
    
	}

	public void loadMatrix (List<AspireDbFilterConfig> subjectFilters) {
		AspireAsyncCallback<Table<SubjectValueObject, String, String>> callback = new AspireAsyncCallback<Table<SubjectValueObject, String, String>>(
				new AsyncCallback<Table<SubjectValueObject, String, String>>() {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onSuccess(
						Table<SubjectValueObject, String, String> result) {
					table = result;
					drawHeatmap();
					panel.unmask();
				}    			   
		}); 
 	         
		panel.mask("Drawing...");
		
		Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
		filters.add ( new ProjectFilterConfig( ActiveProjectSettings.getActiveProjects() ) );
		filters.addAll( subjectFilters );
		
		// subjectService.getPhenotypeMatrix( filters, callback );
	}
	
	protected void drawHeatmap() {
		Collection<SubjectValueObject> rows = table.rowKeySet();
		Collection<String> columns = table.columnKeySet();

		int width = columns.size() * 10 + 100;
		int height = rows.size() * 10 + 200;
		canvasBox.setHeight( height + "px" );
		canvasBox.setWidth( width + "px" );
		canvasBox.setCoordinateSpaceHeight( height );
		canvasBox.setCoordinateSpaceWidth( width );		
		
		int x = 0,y = 0;
		boolean firstTime=true;
		for (SubjectValueObject row : rows) {
			drawLabel( y, row.getPatientId() );
			x = 60;
			for (String column : columns) {
				String value = table.get( row, column );
				if (firstTime) drawVerticalLabel( x, height, column );
				if (value != null ) {
					if (value.equals("1")) {
						drawCell(x,y);
					} else {
						drawBox(x,y);
					}
				}
				x = x + 10;
			}
			firstTime = false;
			y = y + 10;
		}
	}

	private void drawCell(int x, int y) {
		ctx.setFillStyle("black");
		ctx.fillRect( x + 5/2, y + 5/2, 5, 5 );
	}
	

	private void drawBox(int x, int y) {
		ctx.setStrokeStyle("black");
		ctx.strokeRect( x + 5/2, y + 5/2, 5, 5 );
	}
	
	private void drawLabel(int y, String label) {
		ctx.setStrokeStyle("black");
		ctx.setFont("10px sans-serif");
		ctx.strokeText(label, 0, y+10);
	}
	
	private void drawVerticalLabel(int x, int y, String label) {
		ctx.save();
		ctx.translate(x, y);
		ctx.rotate(3*Math.PI/2);
		
		drawLabel(0, label);
		ctx.restore();
	}
	
	
}

