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


Ext.define('ASPIREdb.view.ideogram.VariantLayer', {
    /**
     * @param {CanvasRenderingContext2D} config.ctx
     * @param {number} config.leftX
     * @param {number} config.displayScaleFactor
     * @param {ChromosomeLayer} config.chromosomeLayer
     * @param {number} config.zoom
     */
    constructor: function (config) {
        this.initConfig(config);

        /**
         * @type {Array.<TrackLayer>}
         */
        this.trackLayers = [];
        this.createTracks(10);

        return this;
    },

    config: {
        displayScaleFactor: null,
        ctx: null,
        zoom: null,
        leftX: null,
        chromosomeLayer: null
    },

    statics: {
        colors: [
            "rgb(255,0,0)",
            "rgb(0,0,255)",
            "rgb(0.255,255)",
            "rgb(255,0,255)",
            "rgb(125,125,0)",
            "rgb(0,125,0)",
        ],
        defaultColour: "rgba(0,0,0,0.5)",
        nextColourIndex: 0,
        /** @type {Object.<string,string>} */
        valueToColourMap: {},
        resetDisplayProperty: function () {
            this.nextColourIndex = 0;
            this.valueToColourMap = {};
        }
    },

    /**
     *
     * @param {VariantValueObject} variant
     * @param {PropertyValueObject} property
     * @returns {string}
     */
    pickColor: function (variant, property) {
        if (property == null) return this.self.defaultColour;

//        var value = getPropertyStringValue(property);
        var value = null;

        if (property instanceof VariantTypeProperty) {
            value = variant.variantType;
        } else if (property instanceof CharacteristicProperty) {
            var characteristicValueObject = variant.characteristics[property.name];
            if (characteristicValueObject !== null) {
                value = characteristicValueObject.value;
            }
        }

        if (value == null) return this.self.defaultColour;

        var color = this.self.valueToColourMap[value];
        if (color == null) {
            // Special cases
            if (value.toLowerCase() === "loss") {
                color = "red";
            } else if (value.toLowerCase() === "gain") {
                color = "blue";
            } else {
                color = this.self.colors[this.self.nextColourIndex];
                this.self.nextColourIndex++;
                if (this.self.nextColourIndex >= this.self.colors.length) {
                    this.self.nextColourIndex = 0; //TODO: for now just wrap around, think of a better way
                }
            }
            this.self.valueToColourMap[value] = color;
        }
        return color;
    },

    /**
     * @public
     * @param {VariantValueObject} variant
     */
    drawDimmedVariant: function (variant) {
        /*VariantSegment*/
        var segment = {
            start: variant.genomicRange.baseStart,
            end: variant.genomicRange.baseEnd,
            color: "rgb(128,128,128)",//"rgba(0,0,0, 0.4)",
            emphasize: false
        };

        // pick track layer
        for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
            var layer = this.trackLayers[trackIndex];
            if (layer.doesFit(segment)) {
                this.drawLineSegment(layer.layerIndex, segment, this.ctx, this.displayScaleFactor);
                layer.insert(segment);
                break;
            }
        }
    },

    /**
     * @public
     * @param {VariantValueObject} variant
     * @param {PropertyValueObject} property
     */
    drawHighlightedVariant: function (variant, property) {
        /* VariantSegment */
        var segment = {
            start: variant.genomicRange.baseStart,
            end: variant.genomicRange.baseEnd,
            color: "rgb(255,0,0)",//this.pickColor(variant, property),
            emphasize: true
        };
        // pick track layer
        for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
            var layer = this.trackLayers[trackIndex];
            if (layer.doesFit(segment)) {
                this.drawLineSegment(layer.layerIndex, segment, this.ctx, this.displayScaleFactor);
                layer.insert(segment);
                break;
            }
        }
    },

    /**
     * @public
     * @param {VariantValueObject} variant
     * @param {PropertyValueObject} property
     */
    drawVariant: function (variant, property,color) {
        /*VariantSegment*/
        var segment = {
            start: variant.genomicRange.baseStart,
            end: variant.genomicRange.baseEnd,
            color: color, //this.pickColor(variant, property),
            emphasize: false
        };
        // pick track layer
        for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
            var layer = this.trackLayers[trackIndex];
            if (layer.doesFit(segment)) {
                this.drawLineSegment(layer.layerIndex, segment, this.ctx, this.displayScaleFactor);
                layer.insert(segment);
                break;
            }
        }
    },

    /**
     * @public
     */
    clearTracks: function () {
        this.trackLayers = [];
        this.createTracks(10);
    },

    /**
     * @private
     */
    createTracks: function (numberOfTracks) {
        // Define helper classes
        /**
         * @param {number} start
         * @param {number} end
         * @param {string} color
         * @param {boolean} emphasize
         * @struct
         */
        var VariantSegment = {
            start: null,
            end: null,
            color: null,
            emphasize: false
        };

        var TrackLayer = function (layerIndex) {
            /* @type {Array.<VariantSegment>} */
            this.segments = [];
            this.layerIndex = layerIndex;
        };

        /**
         * @param {{start:number, end:number}} segment
         * @returns {boolean}
         */
        TrackLayer.prototype.doesFit = function (segment) {
            /**
             * @param {{start:number, end:number}} segment
             * @param {number} point
             * @returns {boolean}
             */
            function isWithin(segment, point) {
                return (segment.start <= point && segment.end >= point);
            }

            /**
             * @param {{start:number, end:number}} segment
             * @param {{start:number, end:number}} existingSegment
             * @returns {boolean}
             */
            function doesOverlap(segment, existingSegment) {
                return (isWithin(existingSegment, segment.start) ||
                    isWithin(existingSegment, segment.end) ||
                    isWithin(segment, existingSegment.start) ||
                    isWithin(segment, existingSegment.end));
            }

            for (var i = 0; i < this.segments.length; i++) {
                var existingSegment = this.segments[i];
                if (doesOverlap(segment, existingSegment)) return false;
            }
            return true;
        };

        /**
         * @param {VariantSegment} segment
         */
        TrackLayer.prototype.insert = function (segment) {
            this.segments.push(segment);
        };

        for (var i = 0; i < numberOfTracks; i++) {
            this.trackLayers.push(new TrackLayer(i));
        }
    },

    /**
     *
     * @param {number} layerIndex
     * @param {VariantSegment} segment
     * @param {CanvasRenderingContext2D} ctx
     * @param {number} displayScaleFactor
     */
    drawLineSegment: function (layerIndex, segment, ctx, displayScaleFactor) {
        var start = segment.start;
        var end = segment.end;

        var x = this.leftX + 7;
        x += 2 * this.zoom * layerIndex + 3.5;

        var yStart = this.chromosomeLayer.convertToDisplayCoordinates(start, displayScaleFactor);
        var yEnd = this.chromosomeLayer.convertToDisplayCoordinates(end, displayScaleFactor);

        if (Math.round(yStart) === Math.round(yEnd)) { //Too small to display? bump to 1 pixel size.
            yEnd += 1;
        }

        ctx.strokeStyle = segment.color;
        if (segment.emphasize) {
            ctx.lineWidth = 5 * this.zoom;
        } else {
            ctx.lineWidth = 1 * this.zoom;
        }

        ctx.beginPath();
        ctx.moveTo(x, yStart);
        ctx.lineTo(x, yEnd);
        ctx.stroke();
        ctx.lineWidth = 1 * this.zoom;
    }
});
