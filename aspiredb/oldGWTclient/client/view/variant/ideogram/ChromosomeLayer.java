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
import ubc.pavlab.aspiredb.shared.ChromosomeBand;

import java.util.HashMap;
import java.util.Map;

/**
 * author: anton
 * date: 26/05/13
 */
public class ChromosomeLayer {

    private final Context2d ctx;
    private double zoom;

    public ChromosomeLayer(String name, int baseSize, int centromerePosition, int topY, int leftX,
                           int displayScaleFactor, Context2d ctx, Map<String, ChromosomeBand> bands, double zoom) {
        this.name = name;
        this.baseSize = baseSize;
        this.centromerePosition = centromerePosition;

        this.yPosition = topY;
        this.xPosition = leftX;

        this.zoom = zoom;

        this.displayScaleFactor = displayScaleFactor;
        this.ctx = ctx;
        this.bands = bands;

        this.displayWidth = (int) (7); // * zoom);
        this.centromereDisplaySize = (int) (10);// * zoom);
    }

    // Stain to color mapping
    private static Map<String,String> stainToColor;
    static {
        stainToColor = new HashMap<String, String>();
        stainToColor.put("gpos25","rgba(0,0,0, 0.05)");
        stainToColor.put("gpos50","rgba(0,0,0, 0.1)");
        stainToColor.put("gpos","rgba(0,0,0, 0.1)");
        stainToColor.put("gpos75","rgba(0,0,0, 0.2)");
        stainToColor.put("gpos100","rgba(0,0,0, 0.3)");
        stainToColor.put("stalk","rgba(0,0,255, 0.05)");
        //stainToColor.put("acen","rgba(0,0,100,1)");
        //stainToColor.put("gvar","rgba(0,100,0, 0.3)");
        //stainToColor.put("gneg","white");
    }

    String name;

    Map<String, ChromosomeBand> bands;

    public int getyPosition() {
        return yPosition;
    }

    // Display coordinates
    int yPosition;
    int xPosition;

    public int getDisplaySize() {
        return displaySize;
    }

    int displaySize;

    int shortArmDisplaySize;
    int longArmDisplaySize;
    int displayWidth;
    int centromereDisplaySize;

    int baseSize;
    int centromerePosition; // to tell short arm and long arm apart

    public double convertToDisplayCoordinates(int baseCoordinate, int displayScaleFactor) {
        double coordinate = (double) baseCoordinate / (double) displayScaleFactor;
        // is it on short or long arm?
        if (baseCoordinate > this.centromerePosition) {
            coordinate += centromereDisplaySize;
        }

        coordinate += yPosition;
        return coordinate;
    }

    public int getLeftX() {
        return this.xPosition;
    }

    public String getName() {
        return this.name;
    }

    int displayScaleFactor;
    public void drawChromosome() {
        this.shortArmDisplaySize = centromerePosition / displayScaleFactor;
        this.longArmDisplaySize = (baseSize - centromerePosition) / displayScaleFactor;
        this.displaySize = this.shortArmDisplaySize + this.longArmDisplaySize + this.centromereDisplaySize;

        ctx.save();
        ctx.setStrokeStyle("rgba(0,0,0,1)");
        ctx.strokeText(name, this.xPosition, this.yPosition - 5);

        ctx.setStrokeStyle("rgba(0,0,0,0.2)");
        ctx.translate(this.xPosition, this.yPosition);
        ctx.translate(0.5, 0.5);
        ctx.beginPath();

        ctx.moveTo(0, 0);
        // Draw short arm
        ctx.lineTo(displayWidth, 0);
        ctx.lineTo(displayWidth, shortArmDisplaySize);

        // Draw centromere
        ctx.lineTo(0, shortArmDisplaySize + centromereDisplaySize);
        ctx.lineTo(0, shortArmDisplaySize + centromereDisplaySize + longArmDisplaySize);

        // Draw long arm
        ctx.lineTo(displayWidth, shortArmDisplaySize + centromereDisplaySize + longArmDisplaySize);
        ctx.lineTo(displayWidth, shortArmDisplaySize + centromereDisplaySize);
        ctx.lineTo(0, shortArmDisplaySize);
        ctx.lineTo(0, 0);

        ctx.stroke();
        ctx.restore();

        drawBands(ctx, displayScaleFactor);
    }

    private void drawBands(Context2d ctx, int displayScaleFactor) {
        for (ChromosomeBand band : bands.values()) {

            double yStart = this.convertToDisplayCoordinates(band.getStart(), displayScaleFactor);
            double yEnd = this.convertToDisplayCoordinates(band.getEnd(), displayScaleFactor);

            if (band.getStaining().equals("acen") || band.getStaining().equals("gneg")) continue; //skip
            if (band.getStaining().equals("gvar")) {
                drawVarBand(yStart, yEnd, ctx);
                continue;
            }

            String color = stainToColor.get(band.getStaining());
            ctx.save();
            ctx.setFillStyle(color);
            ctx.translate(this.getLeftX(), yStart);
            ctx.fillRect(0.5, 0.5, displayWidth, yEnd - yStart);
            ctx.restore();
        }
    }

    private void drawVarBand(double yStart, double yEnd, Context2d ctx) {
        ctx.save();
        ctx.setStrokeStyle("rgba(0,0,0,0.2)");
        ctx.translate(this.getLeftX(), 0);
        for (double y = yStart; y < yEnd; y += 3) {
            ctx.beginPath();
            ctx.moveTo(0.5, y);
            ctx.lineTo(displayWidth, y);
            ctx.stroke();
        }
        ctx.restore();
    }

    public int convertToBaseCoordinate(int yCoordinate) {
        // move to chromosome start
        yCoordinate -= yPosition;

        yCoordinate = Math.max(yCoordinate, 0); // snap to start

        if (yCoordinate > this.shortArmDisplaySize && yCoordinate < this.shortArmDisplaySize + this.centromereDisplaySize) {
            return this.shortArmDisplaySize * displayScaleFactor; // snap to centromere
        }

        // is it on short or long arm?
        if (yCoordinate > this.shortArmDisplaySize) {
            yCoordinate -= this.centromereDisplaySize;
        }

        int baseCoordinate = Math.min( yCoordinate * displayScaleFactor, this.baseSize ); // snap to end

        return baseCoordinate;
    }

}
