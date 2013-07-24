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

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.events.GenomeRegionSelectionEvent;
import ubc.pavlab.aspiredb.client.handlers.GenomeRegionSelectionHandler;
import ubc.pavlab.aspiredb.client.handlers.HasGenomeRegionSelectionHandler;
import ubc.pavlab.aspiredb.shared.ChromosomeValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.Property;

/**
 * author: anton
 * date: 21/02/13
 */
public class ChromosomeIdeogram
        implements HasGenomeRegionSelectionHandler,
                   HasMouseMoveHandlers,
                   HasMouseOutHandlers,
                   HasMouseDownHandlers,
                   HasMouseUpHandlers {

    private HandlerManager handlerManager = new HandlerManager(this);
    private ChromosomeLayer chromosomeLayer;
    private ChromosomeValueObject chromosomeData;
    private VariantLayer variantLayer;
    private IdeogramCursorLayer cursorLayer;
    private int topY;
    private double zoom;

    public ChromosomeIdeogram(String name, int baseSize, int centromerePosition,
                              int topY, int leftX, int displayScaleFactor, Context2d ctx, Context2d overlayCtx,
                              ChromosomeValueObject chromosomeData, double zoom ) {
        this.zoom = zoom;
        this.topY = topY;
        this.chromosomeLayer = new ChromosomeLayer(name, baseSize, centromerePosition, topY, leftX,
                displayScaleFactor, ctx,
                chromosomeData.getBands(), zoom);
        this.chromosomeData = chromosomeData;
        this.zoom = zoom;

        variantLayer = new VariantLayer(ctx, leftX, displayScaleFactor, chromosomeLayer, zoom);
        cursorLayer = new IdeogramCursorLayer(overlayCtx, leftX, chromosomeData, chromosomeLayer);
    }

    public GenomicRange getSelection() {
        return cursorLayer.getSelectedRange();
    }

    @Override
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return handlerManager.addHandler(MouseMoveEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return handlerManager.addHandler(MouseOutEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return handlerManager.addHandler(MouseDownEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return handlerManager.addHandler(MouseUpEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addGenomeRegionSelectionHandler(GenomeRegionSelectionHandler handler) {
        return handlerManager.addHandler(GenomeRegionSelectionEvent.TYPE, handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        handlerManager.fireEvent( event );
    }

    public void clearCursor() {
        this.cursorLayer.clearCursor();
    }

    public void drawCursor(int y) {
        this.cursorLayer.drawCursor(y);
    }

    public void clearSelection() {
        this.cursorLayer.clearSelection();
    }

    public void startSelection(int y) {
        this.cursorLayer.startSelection(y);
    }

    public void finishSelection(int y) {
        this.cursorLayer.finishSelection(y);
    }

    public int getTopY() {
        return topY;
    }

    public int getDisplaySize() {
        return chromosomeLayer.getDisplaySize();
    }

    public void drawVariant(VariantValueObject variant, Property property) {
        this.variantLayer.drawVariant(variant, property);
    }

    public void drawDimmedVariant(VariantValueObject variant) {
        this.variantLayer.drawDimmedVariant(variant);
    }

    public void drawHighlightedVariant(VariantValueObject variant, Property property) {
        this.variantLayer.drawHighlightedVariant(variant, property);
    }

    public void drawChromosome() {
        this.variantLayer.clearTracks();
        this.chromosomeLayer.drawChromosome();
    }
}
