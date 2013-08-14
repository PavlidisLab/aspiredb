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
package ubc.pavlab.aspiredb.client.view.variant.ideogram;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import ubc.pavlab.aspiredb.shared.query.Property;

import java.util.Map;

/**
 * @author anton
 */
public class ColourLegend extends Window {

    interface MyUIBinder extends UiBinder<Widget, ColourLegend> {}
    private static MyUIBinder uiBinder = GWT.create(MyUIBinder.class);

    private int height = 200;
    private int width = 230;

    @UiField(provided=true)
    Canvas canvasBox;

    Context2d ctx;

    public ColourLegend() {
        this.setWidth( width+30 + "px" );
        this.setHeight( height + "px" );

        canvasBox = Canvas.createIfSupported();

        setWidget(uiBinder.createAndBindUi(this));

        ctx = canvasBox.getContext2d();
        canvasBox.setHeight( height + "px" );
        canvasBox.setWidth( width + "px" );
        canvasBox.setCoordinateSpaceHeight( height );
        canvasBox.setCoordinateSpaceWidth( width );

    }

    public void update(Map<String,String> valueToColour, Property property) {
        canvasBox.setHeight( height + "px" );
        canvasBox.setWidth( width + "px" );
        canvasBox.setCoordinateSpaceHeight( height );
        canvasBox.setCoordinateSpaceWidth( width );

        this.setHeadingText("Colour Legend: " + (property == null ? "No property selected" : property.getDisplayName()));
        drawLegend(valueToColour);
    }


    private void drawLegend(Map<String,String> valueToColour) {
        ctx.translate(5, 5);

        printLine("N/A", VariantLayer.defaultColour);
        ctx.translate(0, 15);

        for (String value : valueToColour.keySet()) {
            printLine(value, valueToColour.get(value));
            ctx.translate(0, 15);
        }
    }

    private void printLine(String value, String colour) {
        ctx.save();
        ctx.translate(3, 0);
        ctx.setFillStyle(colour);
        ctx.fillRect(0, 0, 10, 10);
        ctx.translate(13, 10);
        ctx.setFillStyle("black");
        ctx.fillText(value, 0, 0);
        ctx.restore();
    }
}
