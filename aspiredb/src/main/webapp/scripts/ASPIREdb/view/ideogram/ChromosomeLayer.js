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
 * Drawing of chromosomes.
 * 
 * @constructor
 * @param {number}
 *           topY
 * @param {number}
 *           displayScaleFactor
 * @param {number}baseSize
 * @param {Object.
 *           <string, ChromosomeBand>} bands
 * @param {string}
 *           name
 * @param {CanvasRenderingContext2D}
 *           ctx
 * @param {number}
 *           zoom
 * @param {number}
 *           leftX
 * @param {number}
 *           centromerePosition
 */
var ChromosomeLayer = function(name, baseSize, centromerePosition, topY, leftX, displayScaleFactor, ctx, bands, zoom,
   displayWidth) {
   this.name = name;
   this.baseSize = baseSize;
   this.centromerePosition = centromerePosition;

   this.yPosition = topY;
   this.xPosition = leftX;

   this.zoom = zoom;

   this.displayScaleFactor = displayScaleFactor;
   this.ctx = ctx;
   this.bands = bands;

   // this.displayWidth = 7;
   this.displayWidth = displayWidth;
   this.centromereDisplaySize = 10;

};

/**
 * Chromosome stain to color mappings.
 * 
 * @type {Object<string,string>}
 */
ChromosomeLayer.stainToColor = {
   "gpos25" : "rgba(0,0,0, 0.5)",
   "gpos50" : "rgba(0,0,0, 0.8)",
   "gpos" : "rgba(0,0,0, 0.8)",
   "gpos75" : "rgba(0,0,0, 0.9)",
   "gpos100" : "rgba(0,0,0, 1)",
   "stalk" : "rgba(0,0,255, 0.5)"
};

/**
 * @public
 * @returns {number}
 */
ChromosomeLayer.prototype.getyPosition = function() {
   return this.yPosition;
};

/**
 * @public
 * @returns {number}
 */
ChromosomeLayer.prototype.getDisplaySize = function() {
   return this.displaySize;
};

/**
 * @public
 * @param {number}
 *           baseCoordinate
 * @param {number}
 *           displayScaleFactor
 * @returns {number}
 */
ChromosomeLayer.prototype.convertToDisplayCoordinates = function(baseCoordinate, displayScaleFactor) {
	if (displayScaleFactor == undefined) {
		displayScaleFactor = this.displayScaleFactor;
	}
   var coordinate = baseCoordinate / displayScaleFactor;
   // is it on short or long arm?
   if ( baseCoordinate > this.centromerePosition ) {
      coordinate += this.centromereDisplaySize;
   }

   coordinate += this.yPosition;
   return coordinate;
};

/**
 * @public
 * @returns {number}
 */
ChromosomeLayer.prototype.getLeftX = function() {
   return this.xPosition;
};

/**
 * @public
 * @returns {string}
 */
ChromosomeLayer.prototype.getName = function() {
   return this.name;
};

ChromosomeLayer.prototype.drawChromosome = function() {
   this.shortArmDisplaySize = this.centromerePosition / this.displayScaleFactor;
   this.longArmDisplaySize = (this.baseSize - this.centromerePosition) / this.displayScaleFactor;
   this.displaySize = this.shortArmDisplaySize + this.longArmDisplaySize + this.centromereDisplaySize;

   this.ctx.save();
   this.ctx.strokeStyle = "rgba(0,0,0,1)";
   var textAlignBase = this.ctx.textAlign;
   this.ctx.textAlign = "center";
   this.ctx.strokeText( this.name, this.xPosition + this.displayWidth / 2, this.yPosition - 5 );
   this.ctx.textAlign = textAlignBase;

   // this.ctx.strokeStyle = "rgba(0,0,0,0.2)";
   this.ctx.strokeStyle = "rgba(0,0,0,1)";
   this.ctx.translate( this.xPosition, this.yPosition );
   this.ctx.translate( 0.5, 0.5 );
   this.ctx.beginPath();

   this.ctx.moveTo( 0, 0 );
   // Draw short arm
   this.ctx.lineTo( this.displayWidth, 0 );
   this.ctx.lineTo( this.displayWidth, this.shortArmDisplaySize );

   // Draw centromere
   this.ctx.lineTo( 0, this.shortArmDisplaySize + this.centromereDisplaySize );
   this.ctx.lineTo( 0, this.shortArmDisplaySize + this.centromereDisplaySize + this.longArmDisplaySize );

   // Draw long arm
   this.ctx.lineTo( this.displayWidth, this.shortArmDisplaySize + this.centromereDisplaySize + this.longArmDisplaySize );
   this.ctx.lineTo( this.displayWidth, this.shortArmDisplaySize + this.centromereDisplaySize );
   this.ctx.lineTo( 0, this.shortArmDisplaySize );
   this.ctx.lineTo( 0, 0 );

   this.ctx.stroke();
   this.ctx.restore();

   this.drawBands( this.ctx );
};

ChromosomeLayer.prototype.drawBands = function(ctx) {
   for (/* ChromosomeBand */var bandName in this.bands) {
      var band = this.bands[bandName];
      var yStart = this.convertToDisplayCoordinates( band.start, this.displayScaleFactor );
      var yEnd = this.convertToDisplayCoordinates( band.end, this.displayScaleFactor );

      if ( band.staining === "acen" || band.staining === "gneg" )
         continue; // skip
      if ( band.staining === "gvar" ) {
         this.drawVarBand( yStart, yEnd, ctx );
         continue;
      }

      var color = ChromosomeLayer.stainToColor[band.staining];
      ctx.save();
      ctx.fillStyle = color;
      ctx.translate( this.getLeftX(), yStart );
      ctx.fillRect( 0.5, 0.5, this.displayWidth, yEnd - yStart );
      ctx.restore();
   }
};

ChromosomeLayer.prototype.drawVarBand = function(yStart, yEnd, ctx) {
   ctx.save();
   // ctx.strokeStyle = "rgba(0,0,0,0.2)";
   ctx.strokeStyle = "rgba(0,0,0,1)";
   ctx.translate( this.getLeftX(), 0 );
   for (var y = yStart; y < yEnd; y += 3) {
      ctx.beginPath();
      ctx.moveTo( 0.5, y );
      ctx.lineTo( this.displayWidth, y );
      ctx.stroke();
   }
   ctx.restore();
};

ChromosomeLayer.prototype.convertToBaseCoordinate = function(yCoordinate) {
   // move to chromosome start
   yCoordinate -= this.yPosition;

   yCoordinate = Math.max( yCoordinate, 0 ); // snap to start

   if ( yCoordinate > this.shortArmDisplaySize && yCoordinate < this.shortArmDisplaySize + this.centromereDisplaySize ) {
      return this.shortArmDisplaySize * this.displayScaleFactor; // snap to centromere
   }

   // is it on short or long arm?
   if ( yCoordinate > this.shortArmDisplaySize ) {
      yCoordinate -= this.centromereDisplaySize;
   }

   var baseCoordinate = Math.min( yCoordinate * this.displayScaleFactor, this.baseSize ); // snap to end
   return baseCoordinate;
};
