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
		this.previousEmphasizedSegment = null;
		
		this.self.hideTipCtx();

		return this;
	},
	
	statics : {
		
		tipCtx : null,
		
		tipHidden: true,
		
		tipBaseHeight : 15,
		
		tipBaseWidth : 100,
		
		createTipCtx : function() {
			var ttipCanvas =  document.createElement("canvas");
			ttipCanvas.style.position = "absolute";
			ttipCanvas.width = ASPIREdb.view.ideogram.VariantLayer.tipBaseWidth;  // 275 min
			ttipCanvas.height = ASPIREdb.view.ideogram.VariantLayer.tipBaseHeight; //115 for 5
			
			ttipCanvas.style.left = "-300px";
			ttipCanvas.style.top = "100px";
			ttipCanvas.style.backgroundColor = "ivory";
			ttipCanvas.style.border = "1px solid black";
			ttipCanvas.style.zIndex = 3;
			
			var body = document.getElementsByTagName("body")[0];
			body.appendChild(ttipCanvas);
			
			ASPIREdb.view.ideogram.VariantLayer.tipCtx = ttipCanvas.getContext("2d");
			ASPIREdb.view.ideogram.VariantLayer.tipCtx.textBaseline = "top";
			ASPIREdb.view.ideogram.VariantLayer.tipCtx.strokeStyle = "black";
			ASPIREdb.view.ideogram.VariantLayer.tipCtx.fillStyle = "black";
			ASPIREdb.view.ideogram.VariantLayer.tipCtx.font = "14px sans-serif";
			ASPIREdb.view.ideogram.VariantLayer.tipCtx.lineWidth = 1;
		},
		
		getTipCtx : function() {
			
			if (ASPIREdb.view.ideogram.VariantLayer.tipCtx == null ) {
				ASPIREdb.view.ideogram.VariantLayer.createTipCtx();
			}
			
			return ASPIREdb.view.ideogram.VariantLayer.tipCtx;
		},
		
		hideTipCtx : function() {
			if (!ASPIREdb.view.ideogram.VariantLayer.tipHidden) {
				var ctx = ASPIREdb.view.ideogram.VariantLayer.getTipCtx();
				ctx.canvas.style.left = -1 * ( ctx.canvas.width + 50 ) + "px";
				ASPIREdb.view.ideogram.VariantLayer.tipHidden = true;
			}
		},
		
		renderTip : function(variant, x, y) {
			var ctx = ASPIREdb.view.ideogram.VariantLayer.getTipCtx();
			ASPIREdb.view.ideogram.VariantLayer.tipHidden = false;
						
			var entries = []

			entries.push( new TipEntry('Subject Id:', ctx, ", ", 1, variant.patientId) );
			
			if (variant.type != undefined) {
				entries.push( new TipEntry('Type:', ctx, ", ", 1, variant.type) );
			}
			
			entries.push( new TipEntry('Variant Type:', ctx, ", ", 1, variant.variantType) );
			
			entries.push( new TipEntry('Coordinates:', ctx, ", ", 1, variant.genomeCoordinates) );

			// Labels Section

			var entry = new TipEntry('Labels:', ctx, ", ", 1);
			for (var i = 0; i < variant.labels.length; i++) {
				var l = variant.labels[i];
				entry.addEntry(l.name, l.colour);
			}
			if (variant.labels.length > 0) {
				entries.push( entry );
			}
			
			// Genes section

			entry = new TipEntry('Genes:', ctx, ", ", 4);
			for (var i = 0; i < variant.overlappingGenes.length; i++) {
				var g = variant.overlappingGenes[i];
				entry.addEntry(g.symbol);				
			}
			
			if (variant.overlappingGenes.length > 0) {
				entries.push( entry );
			}
			
			// Calculate canvas width
			var maxWidth = 0;
			var entryCount = 0;
			for (var i = 0; i < entries.length; i++) {
				var e = entries[i];
				maxWidth = Math.max(maxWidth, e.maxWidth());
				entryCount += e.groups.length;
			}

			var entryHeight = 20;
			
			var tipWidth = maxWidth + ASPIREdb.view.ideogram.VariantLayer.tipBaseWidth + 5;
			var tipHeight = ASPIREdb.view.ideogram.VariantLayer.tipBaseHeight + entryCount * entryHeight;
			
			ctx.canvas.width = tipWidth;
			ctx.canvas.height = tipHeight;

			var cnt = 0;
			for (var i = 0; i < entries.length; i++) {
				var e = entries[i];
				ctx.fillText(e.label, 5, 20 + entryHeight*cnt);
				for (var k = 0; k < e.groups.length; k++) {
					var group = e.groups[k];
					
					var xText = 100;
					for (var m = 0; m < group.length; m++) {
						var t = group[m];
						
						if (t.colour) {
							ctx.save();
							ctx.fillStyle = "#" + t.colour;
							ctx.fillRect(xText, 20 + entryHeight*cnt - 10, t.width, 12);
							ctx.fillStyle = ASPIREdb.view.LabelControlWindow.getContrastYIQ(t.colour);
						}
						
						ctx.fillText(t.text + (m != group.length - 1 ? e.split : ""), xText, 20 + entryHeight*cnt);
						xText += t.width + e.splitWidth;
						if (t.colour) {
							ctx.restore();
						}
					}
					cnt++;
				}

			}

			ctx.canvas.style.left = x + "px";
			ctx.canvas.style.top = (y - ctx.canvas.height - 20) + "px";
		}

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
		previousEmphasizedSegment : null,
		previousClosest : null,
		mouseoverBuffer : 200000,
		previousTrackIndex : -1,
	},




	betterDrawVariant : function(variant) {
		this.renderVariant(variant, variant.colour);
	},



	renderVariant : function(variant, color) {
		/* VariantSegment */
		if (variant.bandEmphasis) {
			this.emphasizeBand(variant);
		}
		var segment = new VariantSegment(variant, color, false);
//		var segment = {
//				start : variant.genomicRange.baseStart,
//				end : variant.genomicRange.baseEnd,
//				color : color,
//				emphasize : false
//		};
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
	
	emphasizeBand : function(variant) {
		var start = variant.genomicRange.baseStart;
		var end = variant.genomicRange.baseEnd;

		var yStart = Math.round(this.chromosomeLayer.convertToDisplayCoordinates( start, this.displayScaleFactor ));
		var yEnd =  Math.round(this.chromosomeLayer.convertToDisplayCoordinates( end, this.displayScaleFactor ));
		
		// Too small?
		yEnd = yEnd - yStart < 1 ? yEnd + 1: yEnd;
		
//		this.ctx.fillStyle = '#FFFBCC';
		this.ctx.fillStyle = variant.colour;
		
//		var padding = 3;
		var padding = 1;
		
		this.ctx.fillRect( this.leftX + 1 + padding, yStart, this.displayWidth - 1 - 2 * padding, yEnd-yStart );
	},

	drawVariantInfo : function(offset, event) {
		var trackIndex = this.getTrackIndex(offset.x);
		var layer = this.trackLayers[trackIndex];
		
		if ( layer != undefined) {
			
			if (layer.segments.length == 0) {
				return;
			}
			
			// check to see if we are still within the previous segment, if so then do nothing
			var baseY = this.chromosomeLayer.convertToBaseCoordinate( offset.y );
			if ( this.previousClosest != null && this.previousTrackIndex == trackIndex && this.previousClosest.segment.isWithin(baseY) ) {
//				console.log('same segment');
				return;
			}
			
			// check to see if we've moved far enough to hit something
			if ( this.previousClosest != null && this.previousTrackIndex == trackIndex ) {
				var moved = Math.abs(baseY - this.previousClosest.y);
				if ( this.previousClosest.distance - moved > this.mouseoverBuffer) {
					// Not close enough!
					return;
				}
			}
			
			
			var closest = layer.closestVariant(baseY);
			
			if (closest.distance < this.mouseoverBuffer) {
				
//				// Don't recolor the same segment
//				if (this.previousEmphasizedSegment != null && this.previousEmphasizedSegment.layerIndex == trackIndex && 
//						this.previousEmphasizedSegment.segment.start == closest.variant.start && 
//						this.previousEmphasizedSegment.segment.end == closest.variant.end) {
//					console.log(1);
//					return;
//				}
				
				this.deemphasizeSegment();
				
				this.emphasizeSegment(trackIndex, closest.segment);
				
				
				var start = closest.segment.start;

				// var x = this.leftX + 7;
				var x = this.getTrackLeftX(trackIndex);

				var yStart = Math.round(this.chromosomeLayer.convertToDisplayCoordinates( start, this.displayScaleFactor ));
				
				var tipY = Math.max(event.pageY - offset.y, event.pageY + (yStart - offset.y));
				var tipX = event.pageX + (x - offset.x);
				this.self.renderTip(closest.segment.variant, tipX, tipY);
			} else {
				this.deemphasizeSegment();
				this.self.hideTipCtx();
			}
			
			this.previousClosest = closest;
			this.previousTrackIndex = trackIndex;
		}
	},
	
	getTrackLeftX : function(trackIndex) {
		return Math.round(this.leftX + this.displayWidth + this.variantSeparationFactor * this.zoom * trackIndex + 3.5 + this.zoom * this.globalEmphasis / 2);
	},
	
	getTrackIndex : function(x) {
		return Math.round((x - 3.5 - this.leftX - this.displayWidth - this.zoom * this.globalEmphasis / 2) / ( this.variantSeparationFactor * this.zoom ));
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
		for (var i = 0; i < numberOfTracks; i++) {
			this.trackLayers.push( new TrackLayer( i ) );
		}
	},
	
	
	
	emphasizeSegment : function(layerIndex, segment) {

//		this.deemphasizeSegment();
		var segmentClone = segment.clone();
		segmentClone.color = '#00ff00';
		this.drawLineSegment(layerIndex, segmentClone, this.ctx, this.displayScaleFactor);
		this.previousEmphasizedSegment = {layerIndex : layerIndex, segment : segment};
	},
	
	deemphasizeSegment : function() {
		if (this.previousEmphasizedSegment != null) {
			this.drawLineSegment(this.previousEmphasizedSegment.layerIndex, this.previousEmphasizedSegment.segment, this.ctx, this.displayScaleFactor);
			this.previousEmphasizedSegment = null;
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
		var x = this.getTrackLeftX(layerIndex);

		var yStart = Math.round(this.chromosomeLayer.convertToDisplayCoordinates( start, displayScaleFactor ));
		var yEnd =  Math.round(this.chromosomeLayer.convertToDisplayCoordinates( end, displayScaleFactor ));
		
		// Too small?
		yEnd = yEnd - yStart < 1 ? yEnd + 1: yEnd;
		
		ctx.save();
		ctx.strokeStyle = segment.color;
//		if ( segment.emphasize ) {
//			ctx.lineWidth = 5 * this.zoom * this.globalEmphasis;
//		} else {
			ctx.lineWidth = 1 * this.zoom * this.globalEmphasis;
//		}

		ctx.beginPath();
		ctx.moveTo( x, yStart );
		ctx.lineTo( x, yEnd );
		ctx.stroke();
		ctx.restore();
	}
} );

var TipEntry = function(label, ctx, split, entriesPerLine, entry, colour) {
	this.label = label;
	this.entriesPerLine = entriesPerLine;
	this.groups = [[]];
	this.entries = 0;
	this.split = split;
	this.ctx = ctx;
	this.splitWidth = ctx.measureText(split).width;
	
	if (entry != undefined) {
		this.addEntry(entry, colour);
	}

}

TipEntry.prototype.addEntry = function(entry, colour) {
	if (entry.trim() == "" ) {
		return;
	}
	
	this.entries++;
	
	var peek = this.groups[this.groups.length - 1];
	
	if (peek.length == this.entriesPerLine) {
		// Need a new group
		peek = [];
		this.groups.push(peek);
	}
	
	peek.push({text: entry, colour: colour, width: this.ctx.measureText(entry).width});
};

TipEntry.prototype.maxWidth = function() {
	var maxWidth = 0;
	for (var i = 0; i < this.groups.length; i++) {
		var g = this.groups[i];
		var width = 0;
		for (var k = 0; k < g.length; k++) {
			var e = g[k];
			width += e.width;
			
		}
		
		width += (g.length - 1) * this.splitWidth; // Account for split characters
		
		maxWidth = Math.max(maxWidth, width);
		
	}
	return maxWidth;
}



var TrackLayer = function(layerIndex) {
	
	/* @type {Array.<VariantSegment>} */
	this.segments = [];
	this.layerIndex = layerIndex;
};

TrackLayer.prototype.closestVariant = function(y) {
	var minDist = Number.MAX_VALUE;
	var closestSegment = null;
	for (var i = 0; i < this.segments.length; i++) {
		var existingSegment = this.segments[i];
		
		if ( existingSegment.isWithin(y) ) {
			return {segment: existingSegment, distance: 0, y: y}; 
		}
		
		if ( y < existingSegment.start) {
			dist = existingSegment.start - y;
		} else {
			dist = y - existingSegment.end
		}
		
		if (dist < minDist) {
			closestSegment = existingSegment;
			minDist = dist;
		}
		
	}
	
	return {segment: closestSegment, distance: minDist, y: y};
};

/**
 * @param {{start:number,
 *           end:number}} segment
 * @returns {boolean}
 */
TrackLayer.prototype.doesFit = function(segment) {

	for (var i = 0; i < this.segments.length; i++) {
		var existingSegment = this.segments[i];
		if ( segment.doesOverlap( existingSegment ) )
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

var VariantSegment = function(variant, color, emphasize) {
	this.start = variant.genomicRange.baseStart;
	this.end = variant.genomicRange.baseEnd;
	this.color = color;
	this.emphasize = emphasize;
	this.variant = variant;
}

VariantSegment.prototype.isWithin = function(point) {
	return (this.start <= point && this.end >= point);
}

VariantSegment.prototype.doesOverlap = function(segment) {
	return (this.isWithin(segment.start ) || this.isWithin( segment.end ) || segment.isWithin( this.start ) || segment.isWithin( this.end ));
}

VariantSegment.prototype.clone = function() {
	return new VariantSegment (this.variant, this.color, this.emphasize);
}