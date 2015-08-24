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

/**
 * Delegates drawing of variants along chromosomes.
 * 
 * @constructor
 * @param {number}
 *           topY
 * @param {number}
 *           displayScaleFactor
 * @param {number}
 *           baseSize
 * @param {string}
 *           name
 * @param {number}
 *           zoom
 * @param {CanvasRenderingContext2D}
 *           ctx
 * @param {ChromosomeValueObject}
 *           chromosomeData
 * @param {CanvasRenderingContext2D}
 *           overlayCtx
 * @param {number}
 *           leftX
 * @param {number}
 *           centromerePosition
 */
var ChromosomeIdeogram = function(name, baseSize, centromerePosition, topY, leftX, displayScaleFactor, ctx, overlayCtx,
   chromosomeData, zoom) {

   this.zoom = zoom;
   this.topY = topY;
   this.displayWidth = 13; // width of each ideogram

   /* ChromosomeLayer */
   this.chromosomeLayer = new ChromosomeLayer( name, baseSize, centromerePosition, topY, leftX, displayScaleFactor,
      ctx, chromosomeData.bands, zoom, this.displayWidth );
   this.chromosomeData = chromosomeData;
   this.zoom = zoom;

   /* @type {VariantLayer} */
   this.variantLayer = Ext.create( 'ASPIREdb.view.ideogram.VariantLayer', {
      ctx : ctx,
      leftX : leftX,
      displayScaleFactor : displayScaleFactor,
      chromosomeLayer : this.chromosomeLayer,
      zoom : zoom,
      displayWidth : this.displayWidth
   } );
   /* @type {IdeogramCursorLayer} */
   this.cursorLayer = new IdeogramCursorLayer( overlayCtx, leftX, chromosomeData, this.chromosomeLayer );
};

/**
 * @public
 * @returns {*}
 */
ChromosomeIdeogram.prototype.getSelection = function() {
   return this.cursorLayer.getSelectedRange();
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.clearCursor = function() {
   this.cursorLayer.clearCursor();
};

/**
 * @public
 * @param y
 */
ChromosomeIdeogram.prototype.drawCursor = function(y) {
   this.cursorLayer.drawCursor( y );
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.clearSelection = function() {
   this.cursorLayer.clearSelection();
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.startSelection = function(y) {
   this.cursorLayer.startSelection( y );
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.finishSelection = function(y) {
   this.cursorLayer.finishSelection( y );
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.getTopY = function() {
   return this.topY;
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.getDisplaySize = function() {
   return this.chromosomeLayer.getDisplaySize();
};

/**
 * @public
 * @param {VariantValueObject}
 *           variant
 * @param {PropertyValueObject}
 *           property
 */
ChromosomeIdeogram.prototype.drawVariant = function(variant, property) {
   this.variantLayer.drawVariant( variant, property );
};

/**
 * @public
 * @param {VariantValueObject}
 *           variant
 */
ChromosomeIdeogram.prototype.drawDimmedVariant = function(variant) {
   this.variantLayer.drawDimmedVariant( variant );
};

/**
 * @public
 * @param {VariantValueObject}
 *           variant
 * @param {PropertyValueObject}
 *           property
 */
ChromosomeIdeogram.prototype.drawHighlightedVariant = function(variant, property) {
   this.variantLayer.drawHighlightedVariant( variant, property );
};

/**
 * @public
 */
ChromosomeIdeogram.prototype.drawChromosome = function() {
   this.variantLayer.clearTracks();
   this.chromosomeLayer.drawChromosome();
};
