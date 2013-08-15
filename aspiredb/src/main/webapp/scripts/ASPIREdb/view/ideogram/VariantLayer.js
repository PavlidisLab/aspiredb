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
            "rgb(0,125,0)",
            "rgb(0,0,255)",
            "rgb(0.255,255)",
            "rgb(255,0,255)",
            "rgb(125,125,0)"
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

        var value = variant.getPropertyStringValue(property);

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
            start: variant.getGenomicRange().getBaseStart(),
            end: variant.getGenomicRange().getBaseEnd(),
            color: "rgba(0,0,0, 0.4)",
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
            start: variant.getGenomicRange().getBaseStart(),
            end: variant.getGenomicRange().getBaseEnd(),
            color: this.pickColor(variant, property),
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
    drawVariant: function (variant, property) {
        /*VariantSegment*/
        var segment = {
            start: variant.getGenomicRange().getBaseStart(),
            end: variant.getGenomicRange().getBaseEnd(),
            color: this.pickColor(variant, property),
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
/*

 */
/**
 *
 * @param {CanvasRenderingContext2D} ctx
 * @param {number} leftX
 * @param {number} displayScaleFactor
 * @param {ChromosomeLayer} chromosomeLayer
 * @param {number} zoom
 * @constructor
 *//*

 var VariantLayer = function (ctx, leftX, displayScaleFactor, chromosomeLayer, zoom) {
 this.displayScaleFactor = displayScaleFactor;
 this.ctx = ctx;
 this.zoom = zoom;
 this.leftX = leftX;
 this.chromosomeLayer = chromosomeLayer;
 */
/**
 * @type {Array.<TrackLayer>}
 *//*

 this.trackLayers = [];
 this.createTracks(10);

 */
/**
 * @param {number} start
 * @param {number} end
 * @param {string} color
 * @constructor
 *//*

 this.VariantSegment = function (start, end, color) {
 this.start = start;
 this.end = end;
 this.color = color;
 this.emphasize = false;
 };

 this.VariantSegment.prototype.isEmphasize = function () {
 return this.emphasize;
 };

 this.VariantSegment.prototype.setEmphasize = function (emphasize) {
 this.emphasize = emphasize;
 };

 var TrackLayer = function (layerIndex) {
 */
/* @type {Array.<VariantSegment>} *//*

 this.segments = [];
 this.layerIndex = layerIndex;
 };

 */
/**
 * @param {VariantSegment} segment
 * @returns {boolean}
 *//*

 TrackLayer.prototype.doesFit = function (segment) {
 */
/**
 * @param {VariantSegment} segment
 * @param {number} point
 * @returns {boolean}
 *//*

 function isWithin(segment, point) {
 return (segment.start <= point && segment.end >= point);
 }

 */
/**
 * @param {VariantSegment} segment
 * @param {VariantSegment} existingSegment
 * @returns {boolean}
 *//*

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

 */
/**
 * @param {VariantSegment} segment
 *//*

 TrackLayer.prototype.insert = function (segment) {
 this.segments.add(segment);
 };
 };

 VariantLayer.colors = [
 "rgb(255,0,0)",
 "rgb(0,125,0)",
 "rgb(0,0,255)",
 "rgb(0.255,255)",
 "rgb(255,0,255)",
 "rgb(125,125,0)"
 ];

 VariantLayer.defaultColour = "rgba(0,0,0,0.5)";
 VariantLayer.nextColourIndex = 0;
 */
/**
 * @type {Object.<string,string>}
 *//*

 VariantLayer.valueToColourMap = {};

 VariantLayer.resetDisplayProperty = function () {
 VariantLayer.nextColourIndex = 0;
 VariantLayer.valueToColourMap = {};
 };

 */
/**
 *
 * @param {VariantValueObject} variant
 * @param {PropertyValueObject} property
 * @returns {string}
 *//*

 VariantLayer.prototype.pickColor = function (variant, property) {
 if (property == null) return VariantLayer.defaultColour;

 var value = variant.getPropertyStringValue(property);

 if (value == null) return VariantLayer.defaultColour;

 var color = VariantLayer.valueToColourMap[value];
 if (color == null) {
 // Special cases
 if (value.toLowerCase() === "loss") {
 color = "red";
 } else if (value.toLowerCase() === "gain") {
 color = "blue";
 } else {
 color = VariantLayer.colors[VariantLayer.nextColourIndex];
 VariantLayer.nextColourIndex++;
 if (VariantLayer.nextColourIndex >= VariantLayer.colors.length) {
 VariantLayer.nextColourIndex = 0; //TODO: for now just wrap around, think of a better way
 }
 }
 VariantLayer.valueToColourMap[value] = color;
 }
 return color;
 };

 */
/**
 * @public
 * @param {VariantValueObject} variant
 *//*

 VariantLayer.prototype.drawDimmedVariant = function (variant) {
 var start = variant.getGenomicRange().getBaseStart();
 var end = variant.getGenomicRange().getBaseEnd();
 var color = "rgba(0,0,0, 0.4)";

 */
/*VariantSegment*//*

 var segment = new this.VariantSegment(start, end, color);

 // pick track layer
 for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
 var layer = this.trackLayers[trackIndex];
 if (layer.doesFit(segment)) {
 this.drawLineSegment(layer.layerIndex, segment, this.ctx, this.displayScaleFactor);
 layer.insert(segment);
 break;
 }
 }
 };

 */
/**
 * @public
 * @param {VariantValueObject} variant
 * @param {PropertyValueObject} property
 *//*

 VariantLayer.prototype.drawHighlightedVariant = function (variant, property) {
 var start = variant.getGenomicRange().getBaseStart();
 var end = variant.getGenomicRange().getBaseEnd();
 var color = this.pickColor(variant, property);

 */
/* VariantSegment *//*

 var segment = new this.VariantSegment(start, end, color);
 segment.setEmphasize(true);
 // pick track layer
 for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
 var layer = this.trackLayers[trackIndex];
 if (layer.doesFit(segment)) {
 this.drawLineSegment(layer.layerIndex, segment, this.ctx, this.displayScaleFactor);
 layer.insert(segment);
 break;
 }
 }
 };

 */
/**
 * @public
 * @param {VariantValueObject} variant
 * @param {PropertyValueObject} property
 *//*

 VariantLayer.prototype.drawVariant = function (variant, property) {
 var start = variant.getGenomicRange().getBaseStart();
 var end = variant.getGenomicRange().getBaseEnd();
 var color = this.pickColor(variant, property);

 */
/*VariantSegment*//*

 var segment = new VariantSegment(start, end, color);
 // pick track layer
 for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
 var layer = this.trackLayers[trackIndex];
 if (layer.doesFit(segment)) {
 this.drawLineSegment(layer.layerIndex, segment, this.ctx, this.displayScaleFactor);
 layer.insert(segment);
 break;
 }
 }
 };

 */
/**
 * @public
 *//*

 VariantLayer.prototype.clearTracks = function () {
 this.trackLayers = [];
 this.createTracks(10);
 };

 */
/**
 * @private
 *//*

 VariantLayer.prototype.createTracks = function (numberOfTracks) {
 for (var i = 0; i < numberOfTracks; i++) {
 this.trackLayers.add(new TrackLayer(i));
 }
 };

 */
/**
 *
 * @param {number} layerIndex
 * @param {VariantSegment} segment
 * @param {CanvasRenderingContext2D} ctx
 * @param {number} displayScaleFactor
 *//*

 VariantLayer.prototype.drawLineSegment = function (layerIndex, segment, ctx, displayScaleFactor) {
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
 if (segment.isEmphasize()) {
 ctx.lineWidth = 5 * this.zoom;
 } else {
 ctx.lineWidth = 1 * this.zoom;
 }

 ctx.beginPath();
 ctx.moveTo(x, yStart);
 ctx.lineTo(x, yEnd);
 ctx.stroke();
 ctx.lineWidth = 1 * this.zoom;
 };*/
