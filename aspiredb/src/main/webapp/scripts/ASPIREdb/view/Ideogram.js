Ext.require([ 'Ext.panel.Panel', 'Ext.Component', 'ASPIREdb.view.ideogram.ColourLegend', 'ASPIREdb.view.ideogram.VariantLayer' ]);

// TODO: events: GenomeRegionSelectionEvent
//

// TODO: valueobjects from backend
Ext.define('ASPIREdb.view.Ideogram', {
	extend : 'Ext.panel.Panel',
	alias : 'widget.ideogram',
	width : 600,
	height : 600,
	autoScroll : true,
	title : 'Ideogram',
	closable : false,
	layout : 'absolute',
	items : [ {
		xtype : 'component',
		autoEl : 'canvas',
		itemId : 'canvasBox',
		x : 0,
		y : 0,
		style : {
			'z-index' : '0'
		}
	}, {
		xtype : 'component',
		autoEl : 'canvas',
		itemId : 'canvasBoxOverlay',
		x : 0,
		y : 0,
		style : {
			'z-index' : '1'
		}
	} ],

	initComponent : function() {
		this.callParent();

		this.width = Math.round(850 * this.zoom);
		this.height = Math.round(this.boxHeight * this.zoom);

		this.colourLegend = Ext.create('ASPIREdb.view.ideogram.ColourLegend');

		this.fetchChromosomeInfo();

		this.setDisplayedProperty(new VariantTypeProperty());

		this.on('afterrender', this.registerMouseEventListeners, this);

		var me = this;
		ChromosomeService.getChromosomes({
			callback : function(chromosomeValueObjects) {
				me.chromosomeValueObjects = chromosomeValueObjects;
				me.drawChromosomes();
			}
		});

	},

	/**
	 * @private
	 * @type {PropertyValueObject}
	 */
	displayedProperty : null,

	/**
	 * @private
	 * @type {VariantValueObject[]}
	 */
	variants : null,

	/**
	 * @private
	 * @type {ColourLegend }
	 */
	colourLegend : null,

	/**
	 * @private
	 * @type {CanvasRenderingContext2D}
	 */
	ctx : null,

	/**
	 * @private
	 * @type {CanvasRenderingContext2D}
	 */
	ctxOverlay : null,

	/**
	 * @private
	 * @type {number}
	 */
	zoom : 1,

	/**
	 * @private
	 */
	boxHeight : 700,

	/**
	 * bases per pixel
	 * 
	 * @private
	 * @type {number}
	 */
	displayScaleFactor : 0,

	/**
	 * @private
	 */
	doneDrawing : false,

	/**
	 * @private
	 * @type {Object.<string, ChromosomeValueObject>}
	 */
	chromosomeValueObjects : {},

	/**
	 * @private
	 * @type {Object.<string, ChromosomeIdeogram>}
	 */
	chromosomeIdeograms : {},

	/**
	 * @private
	 * @type {ChromosomeIdeogram}
	 */
	previousChromosome : null,

	/**
	 * @private
	 */
	registerMouseEventListeners : function() {
		var me = this;
		var canvasBoxOverlay = me.getComponent("canvasBoxOverlay");

		// Register event listeners
		me.mon(canvasBoxOverlay.getEl(), 'mouseout', me.onMouseOut, me);
		canvasBoxOverlay.getEl().on('mousemove', this.onMouseMove, this);
		canvasBoxOverlay.getEl().on('mouseup', this.onMouseUp, this);
		canvasBoxOverlay.getEl().on('mousedown', this.onMouseDown, this);
	},

	/**
	 */
	onMouseOut : function() {
		if (this.previousChromosome !== null) {
			this.previousChromosome.clearCursor();
		}
		this.previousChromosome = null;
	},

	// Needed because chrome uses offsetX in its event and firefox uses
	// something else
	getOffset : function(evt) {
		if (evt.offsetX != undefined)
			return {
				x : evt.offsetX,
				y : evt.offsetY
			};

		var el = evt.target;
		var offset = {
			x : 0,
			y : 0
		};

		while (el.offsetParent) {
			offset.x += el.offsetLeft;
			offset.y += el.offsetTop;
			el = el.offsetParent;
		}

		offset.x = evt.pageX - offset.x;
		offset.y = evt.pageY - offset.y;

		return offset;
	},

	/**
	 */
	onMouseMove : function(event, target) {
		if (!this.doneDrawing)
			return;

		// Determine chromosome we are in.

		var offset = this.getOffset(event.browserEvent);

		/* ChromosomeIdeogram */
		var chromosomeIdeogram = this.findChromosomeIdeogram(offset.x, offset.y);

		// If we moved from one chromosome to another, clear cursor from the
		// previous one.
		if (chromosomeIdeogram !== null) {
			if (this.previousChromosome !== null) {
				if (this.previousChromosome !== chromosomeIdeogram) {
					this.previousChromosome.clearCursor();
				}
			}
			chromosomeIdeogram.drawCursor(offset.y);
		} else {
			if (this.previousChromosome !== null) {
				this.previousChromosome.clearCursor();
			}
		}
		this.previousChromosome = chromosomeIdeogram;
	},

	/**
	 */
	onMouseDown : function(event) {
		if (!this.doneDrawing)
			return;

		// Determine chromosome
		var offset = this.getOffset(event.browserEvent);
		var chromosomeIdeogram = this.findChromosomeIdeogram(offset.x, offset.y);

		if (chromosomeIdeogram !== null) {
			for ( var chromosomeName in this.chromosomeIdeograms) {
				var otherChromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
				otherChromosomeIdeogram.clearCursor();
				otherChromosomeIdeogram.clearSelection();
			}
			chromosomeIdeogram.startSelection(offset.y);
		}
	},

	/**
	 * @public MouseUpEvent
	 */
	onMouseUp : function(event) {
		if (!this.doneDrawing)
			return;

		// Determine chromosome
		var offset = this.getOffset(event.browserEvent);
		/* ChromosomeIdeogram */
		var chromosomeIdeogram = this.findChromosomeIdeogram(offset.x, offset.y);
		if (chromosomeIdeogram !== null) {
			chromosomeIdeogram.finishSelection(offset.y);

			var selection = chromosomeIdeogram.getSelection();
			this.fireEvent('GenomeRegionSelectionEvent', selection);
		}
	},

	/**
	 * @public
	 * @param {VariantValueObject[]}
	 *            variants
	 */
	setVariants : function(variants) {
		this.variants = variants;
	},

	/**
	 * @public
	 * @return {VariantValueObject[]}
	 */
	getVariants : function() {
		return this.variants;
	},

	/**
	 * @public
	 */
	showColourLegend : function() {
		this.colourLegend.show();
	},

	/**
	 * @public
	 */
	hideColourLegend : function() {
		this.colourLegend.hide();
	},

	/**
	 * TODO: update callers (zoom() -> changeZoom())
	 * 
	 * @public
	 * @param {number}
	 *            newZoom
	 */
	changeZoom : function(newZoom) {
		this.zoom = newZoom;
		this.width = Math.round(850 * this.zoom);
		this.height = Math.round(this.boxHeight * this.zoom);
		this.redraw();
	},

	/**
	 * TODO: once service is exposed through DWR
	 * 
	 * @public
	 */
	fetchChromosomeInfo : function() {
		// TODO: fixme
		var ChromosomeService = {
			getChromosomes : function(cb) {
			}
		};
		ChromosomeService.getChromosomes({
			onFailure : function(error) {
				// throw new ChromosomeServiceException;
			},

			onSuccess : function(/*
									 * @type {Object.<string,
									 * ChromosomeValueObject>}
									 */result) {
				this.chromosomeValueObjects = result;
			}
		});
	},

	/**
	 * @private
	 */
	initChromosomeIdeograms : function() {
		var longestChromosome = 250000000; // longest chromosome (# bases)
		var displayScaleFactor = Math.round(longestChromosome / (this.height - 30));

		var chromosomeOrder = [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y" ];

		var topY = 15;
		for ( var index = 0; index < chromosomeOrder.length; index++) {
			var name = chromosomeOrder[index];
			/* ChromosomeValueObject */
			var chromosomeInfo = this.chromosomeValueObjects[name];
			/* Map < String, ChromosomeBand > */
			var size = chromosomeInfo.size;
			var centromereLocation = chromosomeInfo.centromereLocation;
			var leftX = Math.round(5 + index * 35 * this.zoom);
			/* ChromosomeIdeogram */
			var chromosomeIdeogram = new ChromosomeIdeogram(name, size, centromereLocation, topY, leftX, displayScaleFactor, this.ctx, this.ctxOverlay, chromosomeInfo, this.zoom);
			this.chromosomeIdeograms[name] = chromosomeIdeogram;
		}
	},
	/**
	 * @private
	 */
	initCanvasSize : function() {
		var canvasBox = this.getComponent("canvasBox");
		var overlayCanvasBox = this.getComponent("canvasBoxOverlay");

		this.ctx = canvasBox.getEl().dom.getContext('2d');
		this.ctxOverlay = overlayCanvasBox.getEl().dom.getContext('2d');

		canvasBox.getEl().dom.height = this.height; // + "px";
		canvasBox.getEl().dom.width = this.width; // + "px";

		overlayCanvasBox.getEl().dom.height = this.height;// + "px";
		overlayCanvasBox.getEl().dom.width = this.width;// + "px";

		this.ctx.clearRect(0, 0, this.width, this.height);
		this.ctxOverlay.clearRect(0, 0, this.width, this.height);
	},

	/**
	 * @private
	 * @return ChromosomeIdeogram
	 */
	findChromosomeIdeogram : function(x, y) {
		if (x < 5)
			return null;
		var name;
		var index = Math.round((x - 20) / (35 * this.zoom) + 1);
		if (index > 24)
			return null;

		if (index == 23) {
			name = "X";
		} else if (index == 24) {
			name = "Y";
		} else {
			name = index;
		}

		/* ChromosomeIdeogram */
		var chromosomeIdeogram = this.chromosomeIdeograms[name];
		var padding = 4;
		if (chromosomeIdeogram.getTopY() - padding > y)
			return null;
		if (chromosomeIdeogram.getTopY() + chromosomeIdeogram.getDisplaySize() + padding < y)
			return null;

		return chromosomeIdeogram;
	},

	/**
	 * @private
	 * @param {VariantValueObject[]}
	 *            variantValueObjects
	 */
	drawVariants : function(variantValueObjects) {
		/* List<VariantValueObject> */
		var variants = variantValueObjects.slice(); // make a copy
		this.sortVariantsBySize(variants);
		for ( var i = 0; i < variants.length; i++) {
			var variant = variants[i];
			var chrName = variant.genomicRange.chromosome;
			/* ChromosomeIdeogram */
			var chrIdeogram = this.chromosomeIdeograms[chrName];
			chrIdeogram.drawVariant(variant, this.displayedProperty);
		}
	},

	/**
	 * @private
	 * @param {VariantValueObject[]}
	 *            variants
	 */
	sortVariantsBySize : function(variants) {
		variants.sort(function compare(/* @type VariantValueObject */variant1, /*
																				 * @type
																				 * VariantValueObject
																				 */variant2) {
			var size1 = variant1.genomicRange.baseEnd - variant1.genomicRange.baseStart;
			var size2 = variant2.genomicRange.baseEnd - variant2.genomicRange.baseStart;
			return size2 - size1;
		});
	},

	/**
	 * @private
	 * @param subjectId
	 * @param {VariantValueObject[]}
	 *            variantValueObjects
	 */
	drawVariantsWithSubjectHighlighted : function(subjectId, variantValueObjects) {
		/* List<VariantValueObject> */
		var variants = variantValueObjects.slice(); // copy array
		this.sortVariantsBySize(variants);

		for ( var i = 0; i < variants.length; i++) {
			var variant = variants[i];
			var chrName = variant.genomicRange.chromosome;
			/* ChromosomeIdeogram */
			var chrIdeogram = this.chromosomeIdeograms[chrName];
			if (variant.subjectId === subjectId) {
				chrIdeogram.drawHighlightedVariant(variant, this.displayedProperty);
			} else {
				chrIdeogram.drawDimmedVariant(variant);
			}
		}
	},

	/**
	 * @public
	 * @param {PropertyValueObject}
	 *            displayedProperty
	 */
	setDisplayedProperty : function(displayedProperty) {
		this.displayedProperty = displayedProperty;
		ASPIREdb.view.ideogram.VariantLayer.resetDisplayProperty();
	},

	/**
	 * @public
	 * @return {Object.<string,string>}
	 */
	getColourLegend : function() {
		return ASPIREdb.view.ideogram.VariantLayer.valueToColourMap;
	},

	/**
	 * @public
	 */
	redraw : function() {
		this.drawChromosomes();
		this.drawVariants(variants);
		this.colourLegend.update(ASPIREdb.view.ideogram.VariantLayer.valueToColourMap, this.displayedProperty);
	},
	/**
	 * @public
	 */
	redrawHighlightedSubjects : function(subjectIds,vvo) {
		this.drawChromosomes();
		this.drawVariantsWithSubjectHighlighted(subjectIds,vvo);
		this.colourLegend.update(ASPIREdb.view.ideogram.VariantLayer.valueToColourMap, this.displayedProperty);
	},
	

	/**
	 * @private
	 */
	drawChromosomes : function() {
		this.doneDrawing = false;

		this.initCanvasSize();
		this.initChromosomeIdeograms();

		for ( var chromosomeName in this.chromosomeIdeograms) {
			var chromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
			chromosomeIdeogram.drawChromosome();
		}

		this.doneDrawing = true;
	},

	/**
	 * @public
	 * @param {Array}
	 *            variants
	 * @param subjectId
	 */
	highlightSubject : function(/* Long */subjectId, /* List<VariantValueObject> */variants) {
		this.doneDrawing = false;
		this.initCanvasSize();

		this.drawChromosomes();
		this.drawVariantsWithSubjectHighlighted(subjectId, variants);

		this.doneDrawing = true;
	},

	/**
	 * @public
	 */
	removeHighlight : function() {
		this.redraw();
	},

	/**
	 * @public
	 * @return GenomicRange
	 */
	getSelection : function() {
		for ( var chromosomeName in this.chromosomeIdeograms) {
			if (this.chromosomeIdeograms.hasOwnProperty(chromosomeName)) {
				var chromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
				if (chromosomeIdeogram.getSelection() !== null) {
					return chromosomeIdeogram.getSelection();
				}
			}
		}
		return null;
	},

	/**
	 * @public
	 */
	deselectAll : function() {
		for ( var chromosomeName in this.chromosomeIdeograms) {
			var chromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
			chromosomeIdeogram.clearSelection();
		}
	},

	/**
	 * @public
	 */
	setWidth : function(width) {
		this.width = width;
	},

	/**
	 * @public
	 * @param height
	 */
	setHeight : function(height) {
		this.boxHeight = height;
	}

});
