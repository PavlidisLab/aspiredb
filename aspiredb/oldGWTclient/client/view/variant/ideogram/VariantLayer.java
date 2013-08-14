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
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: anton
 * date: 26/05/13
 */
public class VariantLayer {

    private static class VariantSegment {
        int start;
        int end;
        String color;
        private boolean emphasize;

        public VariantSegment(int start, int end, String color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }

        public boolean isEmphasize() {
            return emphasize;
        }

        private void setEmphasize(boolean emphasize) {
            this.emphasize = emphasize;
        }
    }

    private static class TrackLayer {
        List<VariantSegment> segments = new ArrayList<VariantSegment>();
        int layerIndex;

        public TrackLayer(int layerIndex) {
            this.layerIndex = layerIndex;
        }

        public boolean doesFit(VariantSegment segment) {
            for (VariantSegment existingSegment : segments) {
                if (doesOverlap(segment, existingSegment)) return false;
            }
            return true;
        }

        public void insert(VariantSegment segment) {
            segments.add(segment);
        }

        private boolean doesOverlap(VariantSegment segment, VariantSegment existingSegment) {
            return (isWithin(existingSegment, segment.start) ||
                    isWithin(existingSegment, segment.end) ||
                    isWithin(segment, existingSegment.start) ||
                    isWithin(segment, existingSegment.end));
        }

        private boolean isWithin(VariantSegment segment, int point) {
            return (segment.start <= point && segment.end >= point);
        }
    }

    private final int leftX;
    private int displayScaleFactor;
    private ChromosomeLayer chromosomeLayer;
    private List<TrackLayer> trackLayers = new ArrayList<TrackLayer>();

    private Context2d ctx;
    private double zoom;

    public VariantLayer(Context2d ctx, int leftX, int displayScaleFactor, ChromosomeLayer chromosomeLayer, double zoom) {
        this.displayScaleFactor = displayScaleFactor;
        this.ctx = ctx;
        this.zoom = zoom;
        this.leftX = leftX;
        this.chromosomeLayer = chromosomeLayer;
        createTracks(10);
    }

    private static final String[] colors = {
            "rgb(255,0,0)",
            "rgb(0,125,0)",
            "rgb(0,0,255)",
            "rgb(0.255,255)",
            "rgb(255,0,255)",
            "rgb(125,125,0)"
    };
    static String defaultColour = "rgba(0,0,0,0.5)";

    private static int nextColourIndex = 0;
    static Map<String, String> valueToColourMap = new HashMap<String,String>();
    public static void resetDisplayProperty() {
        nextColourIndex = 0;
        valueToColourMap = new HashMap<String,String>();
    }

    private String pickColor(VariantValueObject variant, Property property) {
        if (property == null) return defaultColour;

        String value = variant.getPropertyStringValue(property);

        if (value == null) return defaultColour;

        String color = valueToColourMap.get(value);
        if (color == null) {
            // Special cases
            if (value.toLowerCase().equals("loss")) {
                color = "red";
            } else if (value.toLowerCase().equals("gain")) {
                color = "blue";
            } else {
                color = colors[nextColourIndex];
                nextColourIndex++;
                if (nextColourIndex >= colors.length) {
                    nextColourIndex = 0; //TODO: for now just wrap around, think of a better way
                }
            }
            valueToColourMap.put(value, color);
        }

        return color;
    }

    public void drawDimmedVariant(VariantValueObject variant) {
        int start = variant.getGenomicRange().getBaseStart();
        int end = variant.getGenomicRange().getBaseEnd();
        String color = "rgba(0,0,0, 0.4)";

        VariantSegment segment = new VariantSegment(start, end, color);

        // pick track layer
        for (TrackLayer layer : trackLayers) {
            if (layer.doesFit(segment)) {
                drawLineSegment(layer.layerIndex, segment, ctx, displayScaleFactor);
                layer.insert(segment);
                break;
            }
        }
    }

    public void drawHighlightedVariant(VariantValueObject variant, Property property) {
        int start = variant.getGenomicRange().getBaseStart();
        int end = variant.getGenomicRange().getBaseEnd();
        String color = pickColor(variant, property);

        VariantSegment segment = new VariantSegment(start, end, color);
        segment.setEmphasize(true);
        // pick track layer
        for (TrackLayer layer : trackLayers) {
            if (layer.doesFit(segment)) {
                drawLineSegment(layer.layerIndex, segment, ctx, displayScaleFactor);
                layer.insert(segment);
                break;
            }
        }
    }

    public void drawVariant(VariantValueObject variant, Property property) {
        int start = variant.getGenomicRange().getBaseStart();
        int end = variant.getGenomicRange().getBaseEnd();
        String color = pickColor(variant, property);

        VariantSegment segment = new VariantSegment(start, end, color);
        // pick track layer
        for (TrackLayer layer : trackLayers) {
            if (layer.doesFit(segment)) {
                drawLineSegment(layer.layerIndex, segment, ctx, displayScaleFactor);
                layer.insert(segment);
                break;
            }
        }
    }

    public void clearTracks() {
        this.trackLayers = new ArrayList<TrackLayer>();
        createTracks(10);
    }

    private void createTracks(int numberOfTracks) {
        for (int i = 0; i<numberOfTracks; i++) {
            this.trackLayers.add(new TrackLayer(i));
        }
    }

    public void drawLineSegment(int layerIndex, VariantSegment segment, Context2d ctx, int displayScaleFactor) {
        int start = segment.start;
        int end = segment.end;

        double x = this.leftX + 7;
        x += 2 * zoom * layerIndex + 3.5;

        double yStart = chromosomeLayer.convertToDisplayCoordinates(start, displayScaleFactor);
        double yEnd = chromosomeLayer.convertToDisplayCoordinates(end, displayScaleFactor);

        if ((int) yStart == (int) yEnd) { //Too small to display? bump to 1 pixel size.
            yEnd += 1;
        }

        ctx.setStrokeStyle(segment.color);
        if (segment.isEmphasize()) {
            ctx.setLineWidth(5 * zoom);
        } else {
            ctx.setLineWidth(1 * zoom);
        }

        ctx.beginPath();
        ctx.moveTo(x, yStart);
        ctx.lineTo(x, yEnd);
        ctx.stroke();
        ctx.setLineWidth(1 * zoom);
    }
}
