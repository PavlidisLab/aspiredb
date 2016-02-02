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
 * Drawing of variants as pileups beside the chromosome.
 */
Ext.define( 'ASPIREdb.view.ideogram.VariantLayer', {
	   /**
	    * @memberOf ASPIREdb.view.ideogram.VariantLayer
	    */

	constructor : function(config) {
		/**
		 * @param {CanvasRenderingContext2D}
		 *           config.ctx
		 * @param {number}
		 *           config.leftX
		 * @param {number}
		 *           config.displayScaleFactor
		 * @param {ChromosomeLayer}
		 *           config.chromosomeLayer
		 * @param {number}
		 *           config.zoom
		 */
		this.initConfig( config );

		/**
		 * @type {Array.<TrackLayer>}
		 */
		this.trackLayers = [];

		this.numberOfTracks = (this.displayWidth != null) ? Math.floor((this.zoom * this.chromosomeBaseGap - this.displayWidth - 5) / (this.variantSeparationFactor * this.zoom)) : 2;
		this.createTracks( this.numberOfTracks );
		this.missingVariants = [];

		return this;
	},

	config : {
		displayScaleFactor : null,
		ctx : null,
		zoom : null,
		leftX : null,
		chromosomeLayer : null,
		displayWidth : null,
		variantSeparationFactor : null,
		chromosomeBaseGap : null,
		globalEmphasis : 1,
	},




	betterDrawVariant : function(variant) {
		this.renderVariant(variant, variant.colour);
	},



	renderVariant : function(variant, color) {
		/* VariantSegment */
		var segment = {
				start : variant.genomicRange.baseStart,
				end : variant.genomicRange.baseEnd,
				color : color,
				emphasize : false
		};
		// pick track layer
		var trackFound = false;
		for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
			var layer = this.trackLayers[trackIndex];
			if ( layer.doesFit( segment ) ) {
				this.drawLineSegment( layer.layerIndex, segment, this.ctx, this.displayScaleFactor );
				layer.insert( segment );
				trackFound = true;
				break;
			}
		}
		if (!trackFound) {
			this.missingVariants.push(segment)
		}
	},

	/**
	 * @public
	 */
	clearTracks : function() {
		this.trackLayers = [];
		// this.createTracks(10);
		this.createTracks( this.numberOfTracks );
	},

	/**
	 * numberOfTracks : the maximum variant pileup depth
	 * 
	 * @private
	 * 
	 */
	createTracks : function(numberOfTracks) {
		
		// Define helper classes
		/**
		 * @param {number}
		 *           start
		 * @param {number}
		 *           end
		 * @param {string}
		 *           color
		 * @param {boolean}
		 *           emphasize
		 * @struct
		 */
		var VariantSegment = {
				start : null,
				end : null,
				color : null,
				emphasize : false
		};

		var TrackLayer = function(layerIndex) {
			
			/* @type {Array.<VariantSegment>} */
			this.segments = [];
			this.layerIndex = layerIndex;
		};

		/**
		 * @param {{start:number,
		 *           end:number}} segment
		 * @returns {boolean}
		 */
		TrackLayer.prototype.doesFit = function(segment) {
			/**
			 * @param {{start:number,
			 *           end:number}} segment
			 * @param {number}
			 *           point
			 * @returns {boolean}
			 */
			function isWithin(segment, point) {
				return (segment.start <= point && segment.end >= point);
			}

			/**
			 * @param {{start:number,
			 *           end:number}} segment
			 * @param {{start:number,
			 *           end:number}} existingSegment
			 * @returns {boolean}
			 */
			function doesOverlap(segment, existingSegment) {
				return (isWithin( existingSegment, segment.start ) || isWithin( existingSegment, segment.end )
						|| isWithin( segment, existingSegment.start ) || isWithin( segment, existingSegment.end ));
			}

			for (var i = 0; i < this.segments.length; i++) {
				var existingSegment = this.segments[i];
				if ( doesOverlap( segment, existingSegment ) )
					return false;
			}
			return true;
		};

		/**
		 * @param {VariantSegment}
		 *           segment
		 */
		TrackLayer.prototype.insert = function(segment) {
			this.segments.push( segment );
		};

		for (var i = 0; i < numberOfTracks; i++) {
			this.trackLayers.push( new TrackLayer( i ) );
		}
	},

	/**
	 * 
	 * @param {number}
	 *           layerIndex
	 * @param {VariantSegment}
	 *           segment
	 * @param {CanvasRenderingContext2D}
	 *           ctx
	 * @param {number}
	 *           displayScaleFactor
	 */
	drawLineSegment : function(layerIndex, segment, ctx, displayScaleFactor) {
		var start = segment.start;
		var end = segment.end;

		// var x = this.leftX + 7;
		var x = this.leftX + this.displayWidth;
		x += this.variantSeparationFactor * this.zoom * layerIndex + 3.5;

		var yStart = this.chromosomeLayer.convertToDisplayCoordinates( start, displayScaleFactor );
		var yEnd = this.chromosomeLayer.convertToDisplayCoordinates( end, displayScaleFactor );

		if ( Math.round( yStart ) === Math.round( yEnd ) ) { // Too
			// small
			// to
			// display?
			// bump
			// to 1
			// pixel
			// size.
			yEnd += 1;
		}

		ctx.strokeStyle = segment.color;
		if ( segment.emphasize ) {
			ctx.lineWidth = 5 * this.zoom * this.globalEmphasis;
		} else {
			ctx.lineWidth = 1 * this.zoom * this.globalEmphasis;
		}

		ctx.beginPath();
		ctx.moveTo( x, yStart );
		ctx.lineTo( x, yEnd );
		ctx.stroke();
		ctx.lineWidth = 1 * this.zoom * this.globalEmphasis;
	}
} );
