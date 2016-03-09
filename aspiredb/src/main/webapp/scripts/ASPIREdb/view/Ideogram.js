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
Ext.require( [ 'Ext.panel.Panel', 'Ext.Component', 'ASPIREdb.view.ideogram.VariantLayer', 'ASPIREdb.ActiveProjectSettings' ] );

/**
 * Display variants in a karyotype display.
 */
Ext.define( 'ASPIREdb.view.Ideogram', {
	   /**
	    * @memberOf ASPIREdb.view.Ideogram
	    */
   extend : 'Ext.panel.Panel',
   alias : 'widget.ideogram',
   width : 600,
   height : 600,
   autoScroll : true,
   title : 'Ideogram',
   tooltip: 'A visual representation of variants\' location across the genome.',  
   closable : false,
   // resizable : true,
   layout : 'absolute',
   bodyCls : 'no-selection',
   style : {'cursor':'crosshair'},
   config : {
      selectedView : '',
      selectedSubjectIds : [],
   },
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
      itemId : 'canvasBoxSelection',
      x : 0,
      y : 0,
      style : {
         'z-index' : '1'
      }
   }, {
      xtype : 'component',
      autoEl : 'canvas',
      itemId : 'canvasBoxOverlay',
      x : 0,
      y : 0,
      style : {
    	  'z-index' : '2'
      }
   }, {
	   xtype : 'component',
	   autoEl : 'canvas',
	   itemId : 'canvasBoxLegend',
	   x : 0,
	   y : 0,
	   style : {
		   'z-index' : '0'
	   }
   } ],

   initComponent : function() {
      this.callParent();

      this.on( 'afterrender', this.registerMouseEventListeners, this );
      this.on( 'afterrender', this.initAfterRender, this );
      this.on( 'resize', this.afterResize, this );
      
      ASPIREdb.EVENT_BUS.on( 'property_changed', this.setDisplayedProperty, this);

      ASPIREdb.EVENT_BUS.on( 'subject_selected', this.subjectsSelected, this );
      
      ASPIREdb.EVENT_BUS.on( 'subject_selection_cleared', this.subjectSelectionClearedHandler, this );

   },
   
   afterResize : function() {
	   if ( !this.doneDrawing )
		   return;
	   console.log('resize');
	   this.initCanvasSize();
	   this.redraw();
   },

   initAfterRender : function() {
	   this.ctx = this.getComponent( "canvasBox" ).getEl().dom.getContext( '2d' );
	   this.ctxOverlay = this.getComponent( "canvasBoxOverlay" ).getEl().dom.getContext( '2d' );
	   this.ctxSelection = this.getComponent( "canvasBoxSelection" ).getEl().dom.getContext( '2d' );
	   this.ctxLegend = this.getComponent( "canvasBoxLegend" ).getEl().dom.getContext( '2d' );
	   
//	   this.width = Math.round( this.boxWidth * this.zoom );
	   
	   this.colourLegend = new ColourLegend(this.ctxLegend);

	   // deafult : set the display property to variant type property
	   this.setDisplayedProperty( new VariantTypeProperty() );

	   this.colourLegend.setColourCode(this.displayedProperty);

	   // Default chromosomes
	   this.chromosomeOrder = this.baseChromosomeOrder.slice();

	   this.fetchChromosomeInfo();

	   var me = this;
	   ChromosomeService.getChromosomes( {
		   callback : function(chromosomeValueObjects) {
			   me.chromosomeValueObjects = chromosomeValueObjects;
			   me.initCanvasSize();
		   }
	   } );
   },

   subjectSelectionClearedHandler : function() {
       this.selectedSubjectIds = [];
       this.redraw();
   },
   
   subjectsSelected : function(subjectIds) {
	   var projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
	   var ref = this;

	   // heighlight the selected subject in ideogram
	   SubjectService.getSubjects( projectIds[0], subjectIds, {
		   callback : function(subjectValueObjects) {

			   if ( subjectValueObjects == null ) {
				   return;
			   }

			   var subjectIDS = [];
			   var patientIDS = [];
			   for (var i = 0; i < subjectValueObjects.length; i++) {
				   subjectIDS.push( subjectValueObjects[i].id );
				   patientIDS.push( subjectValueObjects[i].patientId );
			   }
			   ref.selectedSubjectIds = subjectIDS;
			   ref.redraw();

		   }
	   } );
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
    * @type {CanvasRenderingContext2D}
    */
   ctxSelection : null,

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
    * @private
    */
   boxWidth : 900,

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
   
   baseChromosomeOrder : [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y" ],
   
   chromosomeOrder  : null,

   chromosomeBaseGap : 35,
   
   chromosomeBaseWidth : 13,
   
   variantSeparationFactor : 1.2,
   
   globalVariantEmphasis : 1,
   
   isChromosomeIsolated : false,
   
   currentTransform : {x : 0, y : 0},
   
   /**
    * @private
    */
   registerMouseEventListeners : function() {
      var me = this;
      var canvasBoxOverlay = me.getComponent( "canvasBoxOverlay" );

      // Register event listeners
      me.mon( canvasBoxOverlay.getEl(), 'mouseout', me.onMouseOut, me );
      canvasBoxOverlay.getEl().on( 'mousemove', this.onMouseMove, this );
      canvasBoxOverlay.getEl().on( 'mouseup', this.onMouseUp, this );
      canvasBoxOverlay.getEl().on( 'mousedown', this.onMouseDown, this );
      canvasBoxOverlay.getEl().on( 'dblclick', this.onDblClick, this );
   },
   
   onDblClick : function(event) {	   
	      if ( !this.doneDrawing )
	          return;
	       
	       // Determine chromosome
	       var offset = this.getOffset( event.browserEvent );
	       var chromosomeIdeogram = this.findChromosomeIdeogram( offset.x, offset.y );
	       
	       if (chromosomeIdeogram == null) {
	    	   return;
	       }
	       
	       if (this.chromosomeOrder.length == 1) {
	    	   this.chromosomeOrder = this.baseChromosomeOrder.slice();
	    	   this.chromosomeBaseWidth = 13;
	    	   this.variantSeparationFactor = 1.2;
	    	   this.globalVariantEmphasis = 1;
	    	   this.isChromosomeIsolated = false;
	       } else {
	    	   this.chromosomeOrder = [chromosomeIdeogram.name];
    	       this.chromosomeBaseWidth = 26;
	    	   this.variantSeparationFactor = 3;
	    	   this.globalVariantEmphasis = 2;
	    	   this.isChromosomeIsolated = true;
	       }  
	       this.chromosomeBaseGap = (this.boxWidth - 5) / this.chromosomeOrder.length;
    	   this.changeZoom(1, this.variants, true);
    	   this.up().zoomOutButton.setVisible( false );
   },
   
//   getOffset :  function(e) {
//	   var x = e.offsetX==undefined?e.layerX:e.offsetX;
//	   var y = e.offsetY==undefined?e.layerY:e.offsetY;
//
//	     return { x: x, y: y };
//	   },

	getViewCenter : function (e) {
	     var x = e.target.parentElement.clientWidth/2;
	     var y = e.target.parentElement.clientHeight/2;
	     return { x: x, y: y };
	   },

   /**
    */
   onMouseOut : function() {
      if ( this.previousChromosome !== null ) {
         this.previousChromosome.clearCursor();
      }
      this.previousChromosome = null;
   },

   // Needed because chrome uses offsetX in its event and firefox uses
   // something else
   getOffset : function(evt) {
      if ( evt.offsetX != undefined )
         return {
            x : evt.offsetX - this.currentTransform.x,
            y : evt.offsetY - this.currentTransform.y
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

      offset.x = evt.pageX - offset.x - this.currentTransform.x;
      offset.y = evt.pageY - offset.y - this.currentTransform.y;

      return offset;
   },

   /**
    */
   onMouseMove : function(event, target) {
      if ( !this.doneDrawing )
         return;

      // Determine chromosome we are in.

      var offset = this.getOffset( event.browserEvent );

      /* ChromosomeIdeogram */
      var chromosomeIdeogram = this.findChromosomeIdeogram( offset.x, offset.y );

      // If we moved from one chromosome to another, clear cursor from the
      // previous one.
      if ( chromosomeIdeogram !== null ) {
         if ( this.previousChromosome !== null ) {
            if ( this.previousChromosome !== chromosomeIdeogram ) {
               this.previousChromosome.clearCursor();
            }
         }
         chromosomeIdeogram.drawCursor( offset.y );
         if (this.isChromosomeIsolated) {
        	 chromosomeIdeogram.drawVariantInfo(offset, event.browserEvent);
         }
      } else {
         if ( this.previousChromosome !== null ) {
            this.previousChromosome.clearCursor();
         }
      }
      this.previousChromosome = chromosomeIdeogram;
   },

   /**
    */
   onMouseDown : function(event) {
      if ( !this.doneDrawing )
         return;
      
      if (event.button == 2) {
    	     return;
      }
      
      // Determine chromosome
      var offset = this.getOffset( event.browserEvent );
      var chromosomeIdeogram = this.findChromosomeIdeogram( offset.x, offset.y );

      if ( chromosomeIdeogram !== null ) {
         for ( var chromosomeName in this.chromosomeIdeograms) {
            var otherChromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
            otherChromosomeIdeogram.clearCursor();
            otherChromosomeIdeogram.clearSelection();
         }
         chromosomeIdeogram.startSelection( offset.y );
      }
   },

   /**
    * @public MouseUpEvent
    */
   onMouseUp : function(event) {
      if ( !this.doneDrawing )
         return;

      // Determine chromosome
      var offset = this.getOffset( event.browserEvent );
      /* ChromosomeIdeogram */
      var chromosomeIdeogram = this.findChromosomeIdeogram( offset.x, offset.y );
      if ( chromosomeIdeogram !== null ) {
         chromosomeIdeogram.finishSelection( offset.y );

         var selection = chromosomeIdeogram.getGenomicSelection();
         if (selection!=null) {
        	 this.fireEvent( 'GenomeRegionSelectionEvent', selection );
         }
      }
   },

   /**
    * @public
    * @param {VariantValueObject[]}
    *           variants
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
    * TODO: update callers (zoom() -> changeZoom())
    * 
    * @public
    * @param {number}
    *           newZoom
    */
   // changeZoom : function(newZoom, variants, property) {
   changeZoom : function(newZoom, variants, resetTransform) {
      this.zoom = newZoom;
      this.updateLengths();
      
      if (resetTransform) {
    	  this.clearTransform();
      }
      
      this.redraw( variants );
      // this.redraw(variants, property);
   },
   
   updateLengths : function() {
	      // Scale chromosomeBaseGap for the number of chromosome being displayed
	   	  this.chromosomeBaseGap = (this.boxWidth - 5) / this.chromosomeOrder.length;
	      this.width = Math.round( 5 + this.chromosomeBaseGap * this.chromosomeOrder.length * this.zoom );
	      this.height = Math.round( this.boxHeight * this.zoom );  
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
      ChromosomeService.getChromosomes( {
         onFailure : function(error) {
            // throw new ChromosomeServiceException;
         },

         onSuccess : function(/*
                               * @type {Object.<string, ChromosomeValueObject>}
                               */result) {
            this.chromosomeValueObjects = result;
         }
      } );
   },

   /**
    * @private
    */
   initChromosomeIdeograms : function() {
      
      // Pre-loop to get max chromosome size
      var longestChromosome = 0;
      for (var index = 0; index < this.chromosomeOrder.length; index++) {
    	  var cLabel = this.chromosomeOrder[index];
    	  var cInfo = this.chromosomeValueObjects[cLabel];
    	  if (cInfo != null) {
    		  longestChromosome = Math.max(longestChromosome, cInfo.size);
    	  } else {
    		  console.log("Unknown chromosome:", cLabel);
    	  }
    	  
      }
      
      this.displayScaleFactor = Math.round( longestChromosome / (this.height - 30) );

      var topY = 15;
      this.chromosomeIdeograms = {};
      for (var index = 0; index < this.chromosomeOrder.length; index++) {
         var name = this.chromosomeOrder[index];
         /* ChromosomeValueObject */
         var chromosomeInfo = this.chromosomeValueObjects[name];
         /* Map < String, ChromosomeBand > */
         var leftX = Math.round( 5 + index * this.chromosomeBaseGap * this.zoom );
         /* ChromosomeIdeogram */
         var chromosomeIdeogram = new ChromosomeIdeogram( this, name, topY, leftX, chromosomeInfo );
         this.chromosomeIdeograms[name] = chromosomeIdeogram;
      }
   },
   /**
    * Create the Ideogram overlay
    * 
    * @private
    */
   initCanvasSize : function() {
	   var me = this;
	   this.boxHeight = this.body.lastBox.height - 20;
	   this.boxWidth = Math.max(500, this.body.lastBox.width - 200);
	   
	   function prepCanvas(ctx, padding) {
	      if (padding == undefined) {
	         padding = 0;
	      }
		   ctx.canvas.height = me.boxHeight; // + "px";
		   ctx.canvas.width = me.boxWidth + padding; // + "px";

		   return ctx;
	   }

	   prepCanvas( this.ctx );
	   prepCanvas( this.ctxOverlay, 70 );
	   prepCanvas( this.ctxSelection );
	   
	   // Setting canvas widths resets transformation matrix, put it back to where it was
	   this.ctx.translate(this.currentTransform.x, this.currentTransform.y);
	   this.ctxOverlay.translate(this.currentTransform.x, this.currentTransform.y);
	   this.ctxSelection.translate(this.currentTransform.x, this.currentTransform.y);

	   this.ctxLegend.canvas.height = 400;
	   this.ctxLegend.canvas.width = 200;
	   this.ctxLegend.canvas.style.left = this.boxWidth;
	   this.ctxLegend.canvas.style.top = 20;
	   
	   this.updateLengths();
	   
	   
   },

   /**
    * @private
    * @return ChromosomeIdeogram
    */
   findChromosomeIdeogram : function(x, y) {
      if ( x < 5 )
         return null;
      var index = Math.floor( (x - 5) / (this.chromosomeBaseGap * this.zoom) );
      
      if (index > this.chromosomeOrder.length - 1) {
    	  return null;
      }

      var name = this.chromosomeOrder[index]   
      
//      if ( index > 24 )
//         return null;
//
//      if ( index == 23 ) {
//         name = "X";
//      } else if ( index == 24 ) {
//         name = "Y";
//      } else {
//         name = index;
//      }

      /* ChromosomeIdeogram */
      var chromosomeIdeogram = this.chromosomeIdeograms[name];
      var padding = 4;
      if ( chromosomeIdeogram.getTopY() - padding > y )
         return null;
      if ( chromosomeIdeogram.getTopY() + chromosomeIdeogram.getDisplaySize() + padding < y )
         return null;

      return chromosomeIdeogram;
   },
   
   drawVariants : function(variantValueObjects) {
	   
	   this.variants = variantValueObjects.slice(); // make a copy
	   this.sortVariantsBySize( this.variants );
	   
	   this.colourLegend.setColourCode(this.displayedProperty);
	   
	   for (var i = 0; i < this.variants.length; i++) {
		   var variant = this.variants[i];
		   var chrName = variant.genomicRange.chromosome;
		   
		   /* ChromosomeIdeogram */
		   var chrIdeogram = this.chromosomeIdeograms[chrName];
		   if (chrIdeogram!=null) {
			   
			   if (this.selectedSubjectIds.length != 0) {
				   // apply subject selection
				   variant.selected = ( this.selectedSubjectIds.indexOf( variant.subjectId ) != -1 );
				   variant.bandEmphasis = variant.selected;
			   } else {
				   variant.selected = true;
				   variant.bandEmphasis = false;
			   }
			   
			// apply color code
			   variant.colour = variant.selected ? this.colourLegend.getColour(variant) : "#989898";
			   
			   // draw variant
			   chrIdeogram.betterDrawVariant( variant );
		   }

	   }
	   
	   // draw legend
	   this.colourLegend.refresh();
   },

   /**
    * @private
    * @param {VariantValueObject[]}
    *           variants
    */
   sortVariantsBySize : function(variants) {
      variants.sort( function compare(/* @type VariantValueObject */variant1, /*
                                                                               * @type VariantValueObject
                                                                               */variant2) {
         var size1 = variant1.genomicRange.baseEnd - variant1.genomicRange.baseStart;
         var size2 = variant2.genomicRange.baseEnd - variant2.genomicRange.baseStart;
         return size2 - size1;
      } );
   },

   /**
    * @public
    * @param {PropertyValueObject}
    *           displayedProperty
    */
   setDisplayedProperty : function(displayedProperty) {
      this.displayedProperty = displayedProperty;
      
   },

   /**
    * @public Redraw when zoomed the ideogram
    */
   redraw : function(variants) {
	   if (variants == undefined || variants == null) {
		   variants = this.variants == null ? [] : this.variants;
	   }
	   
	   this.clear(this.ctx);
	   this.clear(this.ctxOverlay);
	   this.clear(this.ctxSelection);
	   
	   this.drawChromosomes();
	   this.drawVariants(variants);
   },

   /**
    * @private
    */
   drawChromosomes : function() {
      this.doneDrawing = false;

      this.initChromosomeIdeograms();

      for ( var chromosomeName in this.chromosomeIdeograms) {
         var chromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
         chromosomeIdeogram.drawChromosome();
      }

      this.doneDrawing = true;
   },
   
   clearTransform : function() {
	   this.ctx.setTransform(1, 0, 0, 1, 0, 0);
	   this.ctxOverlay.setTransform(1, 0, 0, 1, 0, 0);
	   this.ctxSelection.setTransform(1, 0, 0, 1, 0, 0);
	   this.currentTransform.x = 0;
	   this.currentTransform.y = 0;
   },
   
   clear : function(ctx) {
	   ctx.save();
	   ctx.setTransform(1, 0, 0, 1, 0, 0);

	   ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

	   ctx.restore();
   },

   /**
    * @public
    * @return GenomicRange
    */
   getGenomicSelection : function() {
      for ( var chromosomeName in this.chromosomeIdeograms) {
         if ( this.chromosomeIdeograms.hasOwnProperty( chromosomeName ) ) {
            var chromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
            if ( chromosomeIdeogram.getGenomicSelection() !== null ) {
               return chromosomeIdeogram.getGenomicSelection();
            }
         }
      }
      return null;
   },

   /**
    * @public
    */
   clearRangeSelection : function() {
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

} );
