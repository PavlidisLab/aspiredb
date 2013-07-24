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
import com.google.gwt.canvas.dom.client.ImageData;
import ubc.pavlab.aspiredb.shared.ChromosomeValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;

/**
 * author: anton
 * date: 26/05/13
 */
public class IdeogramCursorLayer {

    private class SavedImage {
        private int yTop = -1;
        private ImageData imageData;

        public void restore() {
            if (imageData != null) {
                ctx.putImageData(imageData, getLeftX(), yTop);
            }
        }

        public void save(int start, int finish) {
            int boxSize = Math.abs( start - finish ); // padding
            yTop = Math.min( start, finish );
            imageData = ctx.getImageData(getLeftX(), yTop, 100, boxSize);
        }

        public void clear() {
            imageData = null;
            yTop = -1;
        }
    }

    private class Selection {
        private GenomicRange selectedRange = null;

        private int start;
        private int end;

        public GenomicRange getSelectedRange() {
            return selectedRange;
        }

        private void setSelectedRange(GenomicRange selectedRange) {
            this.selectedRange = selectedRange;
        }

        public int getTop() {
            return Math.min(start, end);
        }

        public int getBottom() {
            return Math.max(start, end);
        }

        private void setStart(int start) {
            this.start = start;
        }

        private void setCurrent(int current) {
            end = current;
        }

        private void setEnd(int end) {
            this.end = end;

            if (Math.abs(start - end) <=1 ) {
                clearSelection();
            } else {
                int selectedStartBase;
                int selectedEndBase;

                selectedStartBase = chromosomeLayer.convertToBaseCoordinate(getTop());
                selectedEndBase = chromosomeLayer.convertToBaseCoordinate(getBottom());

                String selectedChromosome = chromosomeData.getName();
                setSelectedRange(new GenomicRange(selectedChromosome, selectedStartBase, selectedEndBase));
            }
        }

        public void render() {
            // Render selection
            ctx.setFillStyle( "rgba(0,0,255,0.1)" );
            ctx.fillRect( getLeftX(), getTop(), 29, getBottom() - getTop() );

            renderCursor( getTop() );
            renderCursor( getBottom() );
        }

        public void clear() {
            start = 0;
            end = 0;
        }
    }

    private ChromosomeValueObject chromosomeData;
    private ChromosomeLayer chromosomeLayer;

    private Context2d ctx;
    private int xPosition;

    private Selection selection = new Selection();
    private SavedImage cursorBackground = new SavedImage();
    private SavedImage selectionBackground = new SavedImage();

    public GenomicRange getSelectedRange() {
        return selection.getSelectedRange();
    }

    public IdeogramCursorLayer(Context2d ctx, int leftX, ChromosomeValueObject chromosomeData, ChromosomeLayer chromosomeLayer) {
        this.ctx = ctx;
        this.xPosition = leftX;
        this.chromosomeData = chromosomeData;
        this.chromosomeLayer = chromosomeLayer;
    }

    public int getLeftX() {
        return this.xPosition;
    }

    private void renderCursor( int y ) {
        ctx.setFillStyle("red");
        ctx.fillRect(this.getLeftX(), y, 29, 1);

        String cursorLabel = this.chromosomeData.getName()+":"+getBandName(y);

        ctx.setStrokeStyle("black");
        ctx.strokeText(cursorLabel, this.getLeftX() + 30, y);
    }

    private String getBandName(int y) {
        int base = chromosomeLayer.convertToBaseCoordinate(y);
        return chromosomeData.getBandName(base);
    }

    public void drawCursor(int y) {
        if (isSelectionMode)  {
            drawSelection(y);
        } else {
            cursorBackground.restore();
            cursorBackground.save(y - 10, y + 3);
            renderCursor( y );
        }
    }

    public void clearCursor() {
        if (isSelectionMode && selection.getSelectedRange() == null) {
            clearSelection();
        } else {
            cursorBackground.restore();
            cursorBackground.clear();
        }
    }

    private void drawSelection(int y) {
        selectionBackground.restore();

        selection.setCurrent(y);
        selectionBackground.save(selection.getTop() - 10, selection.getBottom() + 3);
        selection.render();
    }

    boolean isSelectionMode = false;
    public void startSelection(int y) {
        cursorBackground.clear();
//        cursorBackground.restore();

        isSelectionMode = true;

        selection.setSelectedRange(null);
        selection.setStart(y);
    }

    public void clearSelection() {
        selectionBackground.restore();
        selectionBackground.clear();

        selection.setSelectedRange(null);
        selection.clear();
        isSelectionMode = false;
    }

    public void finishSelection(int y) {
        if (isSelectionMode) {
            isSelectionMode = false;
            selection.setEnd( y );
            cursorBackground.clear();
        }
    }
}
