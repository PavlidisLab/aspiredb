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
 * Highlighting regions in the chromosome.
 * 
 * @param {CanvasRenderingContext2D}
 *           ctx
 * @param {number}
 *           leftX
 * @param {ChromosomeValueObject}
 *           chromosomeData
 * @param {ChromosomeLayer}
 *           chromosomeLayer
 * @constructor
 */
var IdeogramCursorLayer = function(ctx, leftX, chromosomeData, chromosomeLayer) {
   this.ctx = ctx;
   this.xPosition = leftX;
   this.chromosomeData = chromosomeData;
   this.chromosomeLayer = chromosomeLayer;
   this.isSelectionMode = false;

   this.cursorBackground = new SavedImage();
   this.selectionBackground = new SavedImage();

   /**
    * @public
    * @returns {number}
    */
   this.getLeftX = function() {
      return getLeftX();
   };

   /**
    * @private
    * @returns {number}
    */
   var getLeftX = function() {
      return leftX;
   };

   /**
    * Helper class
    * 
    * @constructor
    */
   function SavedImage() {
      var yTop = -1;
      /** @type ImageData */
      var imageData = null;

      this.restore = function() {
         if ( imageData != null ) {
            ctx.putImageData( imageData, getLeftX(), yTop );
         }
      };

      this.save = function(start, finish) {
         var boxSize = Math.abs( start - finish ); // padding
         yTop = Math.min( start, finish );
         imageData = ctx.getImageData( getLeftX(), yTop, 100, boxSize );
      };

      this.clear = function() {
         imageData = null;
         yTop = -1;
      };
   }

   var me = this;
   /**
    * @type {{}}
    */
   this.selection = {
      /* GenomicRange */
      selectedRange : null,
      start : null,
      end : null,

      getSelectedRange : function() {
         return this.selectedRange;
      },

      setSelectedRange : function(/* GenomicRange */selectedRange) {
         this.selectedRange = selectedRange;
      },

      getTop : function() {
         return Math.min( this.start, this.end );
      },

      getBottom : function() {
         return Math.max( this.start, this.end );
      },

      setStart : function(start) {
         this.start = start;
      },

      setCurrent : function(current) {
         this.end = current;
      },

      setEnd : function(end) {
         this.end = end;

         if ( Math.abs( this.start - end ) <= 1 ) {
            me.clearSelection();
         } else {
            var selectedStartBase;
            var selectedEndBase;

            selectedStartBase = chromosomeLayer.convertToBaseCoordinate( this.getTop() );
            selectedEndBase = chromosomeLayer.convertToBaseCoordinate( this.getBottom() );

            var selectedChromosome = chromosomeData.name;
            this.setSelectedRange( {
               chromosome : selectedChromosome,
               baseStart : selectedStartBase,
               baseEnd : selectedEndBase
            } );
         }
      },
      render : function() {
         // Render selection
         me.ctx.fillStyle = "rgba(0,0,255,0.1)";
         me.ctx.fillRect( getLeftX(), this.getTop(), 29, this.getBottom() - this.getTop() );

         me.renderCursor( this.getTop() );
         me.renderCursor( this.getBottom() );
      },
      clear : function() {
         this.start = 0;
         this.end = 0;
      }
   };
};

/**
 * @returns {GenomicRange}
 */
IdeogramCursorLayer.prototype.getSelectedRange = function() {
   return this.selection.getSelectedRange();
};

IdeogramCursorLayer.prototype.renderCursor = function(y) {
   var me = this;
   var getBandName = function(y) {
      var base = me.chromosomeLayer.convertToBaseCoordinate( y );
      for ( var bandName in me.chromosomeData.bands) {
         var band = me.chromosomeData.bands[bandName];
         if ( band.start < base && band.end > base ) {
            return band.name;
         }
      }
      return "";
   };

   this.ctx.fillStyle = "red";
   this.ctx.fillRect( this.getLeftX(), y, 29, 1 );

   var cursorLabel = this.chromosomeData.name + ":" + getBandName( y );

   this.ctx.strokeStyle = "black";
   this.ctx.strokeText( cursorLabel, this.getLeftX() + 30, y );
};

IdeogramCursorLayer.prototype.drawCursor = function(y) {
   var me = this;
   function drawSelection(y) {
      me.selectionBackground.restore();

      me.selection.setCurrent( y );
      me.selectionBackground.save( me.selection.getTop() - 10, me.selection.getBottom() + 3 );
      me.selection.render();
   }

   if ( this.isSelectionMode ) {
      drawSelection( y );
   } else {
      this.cursorBackground.restore();
      this.cursorBackground.save( y - 10, y + 3 );
      this.renderCursor( y );
   }
};

IdeogramCursorLayer.prototype.clearCursor = function() {
   if ( this.isSelectionMode && this.selection.getSelectedRange() == null ) {
      this.clearSelection();
   } else {
      this.cursorBackground.restore();
      this.cursorBackground.clear();
   }
};

IdeogramCursorLayer.prototype.startSelection = function(y) {
   this.cursorBackground.clear();

   this.isSelectionMode = true;

   this.selection.setSelectedRange( null );
   this.selection.setStart( y );
};

IdeogramCursorLayer.prototype.clearSelection = function() {
   this.selectionBackground.restore();
   this.selectionBackground.clear();

   this.selection.setSelectedRange( null );
   this.selection.clear();
   this.isSelectionMode = false;
};

IdeogramCursorLayer.prototype.finishSelection = function(y) {
   if ( this.isSelectionMode ) {
      this.isSelectionMode = false;
      this.selection.setEnd( y );
      this.cursorBackground.clear();
   }
};
