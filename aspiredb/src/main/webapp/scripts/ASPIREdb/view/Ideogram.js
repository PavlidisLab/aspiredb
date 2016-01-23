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
Ext.require( [ 'Ext.panel.Panel', 'Ext.Component', 'ASPIREdb.view.ideogram.ColourLegend',
              'ASPIREdb.view.ideogram.VariantLayer', 'ASPIREdb.ActiveProjectSettings' ] );

/**
 * Display variants in a karyotype display.
 */
Ext.define( 'ASPIREdb.view.Ideogram', {
   extend : 'Ext.panel.Panel',
   alias : 'widget.ideogram',
   width : 600,
   height : 600,
   autoScroll : true,
   title : 'Ideogram',
   tooltip: 'A quick overview of where the variants are located in the genome.',  
   closable : false,
   // resizable : true,
   layout : 'absolute',
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
      itemId : 'canvasBoxOverlay',
      x : 0,
      y : 0,
      style : {
         'z-index' : '1'
      }
   } ],

   initComponent : function() {
      this.callParent();

      this.width = Math.round( 850 * this.zoom );
      this.height = Math.round( this.boxHeight * this.zoom );

      this.colourLegend = Ext.create( 'ASPIREdb.view.ideogram.ColourLegend' );

      this.fetchChromosomeInfo();

      // deafult : set the display property to variant type property
      this.setDisplayedProperty( new VariantTypeProperty() );

      this.on( 'afterrender', this.registerMouseEventListeners, this );

      var me = this;
      ChromosomeService.getChromosomes( {
         callback : function(chromosomeValueObjects) {
            me.chromosomeValueObjects = chromosomeValueObjects;
            me.drawChromosomes();
         }
      } );

      ASPIREdb.EVENT_BUS.on( 'colorCoding_selected', this.selectDrawingType, this );

      ASPIREdb.EVENT_BUS.on( 'subject_selected', this.selectDrawingType, this );

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
      var canvasBoxOverlay = me.getComponent( "canvasBoxOverlay" );

      // Register event listeners
      me.mon( canvasBoxOverlay.getEl(), 'mouseout', me.onMouseOut, me );
      canvasBoxOverlay.getEl().on( 'mousemove', this.onMouseMove, this );
      canvasBoxOverlay.getEl().on( 'mouseup', this.onMouseUp, this );
      canvasBoxOverlay.getEl().on( 'mousedown', this.onMouseDown, this );
      canvasBoxOverlay.getEl().on( 'dblclick', this.onDblClick, this );
   },
   
   onDblClick : function(evt) {
//	   var zoomRatio = this.zoom+1/this.zoom;
	   
	   this.changeZoom(this.zoom+1, this.variants);
	   
	   var center = this.getViewCenter(evt.browserEvent)
	   var pt = this.getOffset(evt.browserEvent);
	   this.body.dom.scrollTop = pt.y - center.y;
	   this.body.dom.scrollLeft = pt.x - center.x;
	   
//	   console.log(this);
	  
	   
//	   this.body.dom.scrollTop = Math.floor(zoomRatio*(pt.y - center.y));
//	   this.body.dom.scrollLeft = Math.floor(zoomRatio*(pt.x - center.x));
//	   
//	   console.log(evt);
//	   console.log(center);
//	   console.log(pt);
//	   console.log(pt.y - center.y, pt.x - center.x);
//	   console.log(Math.floor(zoomRatio*(pt.y - center.y)), Math.floor(zoomRatio*(pt.x - center.x)));
	   
   },
   
   getOffset :  function(e) {
	   var x = e.offsetX==undefined?e.layerX:e.offsetX;
	   var y = e.offsetY==undefined?e.layerY:e.offsetY;

	     return { x: x, y: y };
	   },

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

         var selection = chromosomeIdeogram.getSelection();
         this.fireEvent( 'GenomeRegionSelectionEvent', selection );
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

   refreshColourLegend : function() {
      this.colourLegend.update( ASPIREdb.view.ideogram.VariantLayer.valueToColourMap, this.displayedProperty );
      this.colourLegend.show();

   },

   /**
    * TODO: update callers (zoom() -> changeZoom())
    * 
    * @public
    * @param {number}
    *           newZoom
    */
   // changeZoom : function(newZoom, variants, property) {
   changeZoom : function(newZoom, variants) {
      this.zoom = newZoom;
      this.width = Math.round( 850 * this.zoom );
      this.height = Math.round( this.boxHeight * this.zoom );
      this.redraw( variants );
      // this.redraw(variants, property);
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
      var longestChromosome = 250000000; // longest chromosome (# bases)
      var displayScaleFactor = Math.round( longestChromosome / (this.height - 30) );

      var chromosomeOrder = [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
                             "17", "18", "19", "20", "21", "22", "X", "Y" ];

      var topY = 15;
      for (var index = 0; index < chromosomeOrder.length; index++) {
         var name = chromosomeOrder[index];
         /* ChromosomeValueObject */
         var chromosomeInfo = this.chromosomeValueObjects[name];
         /* Map < String, ChromosomeBand > */
         var size = chromosomeInfo.size;
         var centromereLocation = chromosomeInfo.centromereLocation;
         var leftX = Math.round( 5 + index * 35 * this.zoom );
         /* ChromosomeIdeogram */
         var chromosomeIdeogram = new ChromosomeIdeogram( name, size, centromereLocation, topY, leftX,
            displayScaleFactor, this.ctx, this.ctxOverlay, chromosomeInfo, this.zoom );
         this.chromosomeIdeograms[name] = chromosomeIdeogram;
      }
   },
   /**
    * Create the Ideogram overlay
    * 
    * @private
    */
   initCanvasSize : function() {
      var canvasBox = this.getComponent( "canvasBox" );
      var overlayCanvasBox = this.getComponent( "canvasBoxOverlay" );

      this.ctx = canvasBox.getEl().dom.getContext( '2d' );
      this.ctxOverlay = overlayCanvasBox.getEl().dom.getContext( '2d' );

      canvasBox.getEl().dom.height = this.height; // + "px";
      canvasBox.getEl().dom.width = this.width; // + "px";

      overlayCanvasBox.getEl().dom.height = this.height;// + "px";
      overlayCanvasBox.getEl().dom.width = this.width;// + "px";

      this.ctx.clearRect( 0, 0, this.width, this.height );
      this.ctxOverlay.clearRect( 0, 0, this.width, this.height );
   },

   /**
    * @private
    * @return ChromosomeIdeogram
    */
   findChromosomeIdeogram : function(x, y) {
      if ( x < 5 )
         return null;
      var name;
      var index = Math.round( (x - 20) / (35 * this.zoom) + 1 );
      if ( index > 24 )
         return null;

      if ( index == 23 ) {
         name = "X";
      } else if ( index == 24 ) {
         name = "Y";
      } else {
         name = index;
      }

      /* ChromosomeIdeogram */
      var chromosomeIdeogram = this.chromosomeIdeograms[name];
      var padding = 4;
      if ( chromosomeIdeogram.getTopY() - padding > y )
         return null;
      if ( chromosomeIdeogram.getTopY() + chromosomeIdeogram.getDisplaySize() + padding < y )
         return null;

      return chromosomeIdeogram;
   },

   /**
    * @private
    * @param {VariantValueObject[]}
    *           variantValueObjects
    */
   drawVariants : function(variantValueObjects) {

      /* List<VariantValueObject> */
      var variants = variantValueObjects.slice(); // make a copy
      this.variants = variants;
      this.sortVariantsBySize( variants );
      for (var i = 0; i < variants.length; i++) {
         var variant = variants[i];
         var chrName = variant.genomicRange.chromosome;
         /* ChromosomeIdeogram */
         var chrIdeogram = this.chromosomeIdeograms[chrName];
         chrIdeogram.drawVariant( variant, this.displayedProperty );

      }

   },

   /**
    * @private
    * @param {VariantValueObject[]}
    *           variantValueObjects
    */
   drawColouredVariants : function(variantValueObjects, repeat) {

      /* List<VariantValueObject> */
	  var variants = variantValueObjects.slice(); // make a copy
	  this.variants = variants;
      this.sortVariantsBySize( variants );
      var propertyValues = [];

      for (var i = 0; i < variants.length; i++) {
         var variant = variants[i];
         if ( variant == null ) {
            console.log( "variant is null" );
            continue;
         }
         var chrName = variant.genomicRange.chromosome;
         /* ChromosomeIdeogram */
         var chrIdeogram = this.chromosomeIdeograms[chrName];
         // populating the displayed properties
         var property = this.displayedProperty;

         // if variant type property : CNV, SNV, indel, translocation, inversion
         if ( property instanceof VariantTypeProperty ) {
            propertyValues.push( variant.variantType );
         }
         // if CNV type : LOSS, GAIN
         if ( property instanceof CNVTypeProperty ) {
            propertyValues.push( variant.type );
         }
         // if Characteristic type : benign, pathogenic, unknown
         if ( property instanceof CharacteristicProperty ) {
            var characteristicValueObject = variant.characteristics[property.name];
            if ( characteristicValueObject != null ) {
               if ( characteristicValueObject == undefined ) {
                  // considering only the first label given to the subject
               } else
                  propertyValues.push( characteristicValueObject.value );
            }
         }
         // if variant labels
         if ( property instanceof VariantLabelProperty ) {
            if ( variant.labels != null && variant.labels.length > 0 ) {
               // considering only the first label given to the subject
               propertyValues.push( variant.labels[0].name );
            } else {
               propertyValues.push( 'No Label' );
            }
         }
         // if subject labels
         if ( property instanceof SubjectLabelProperty ) {
            subject = variant.subject;
            if ( subject != null && subject.labels.length > 0 ) {
               propertyValues.push( subject.labels[0].name );
               // console.log( 'variant label name :' + subject.labels[0].name );
            } else {
               propertyValues.push( 'No Label' );
            }

         }
         this.displayedProperty = property;
         this.displayedProperty.displayType = propertyValues;
         this.setDisplayedProperty( this.displayedProperty );

         if ( this.selectedSubjectIds.length == 0 ) {
            // default case for color coding [color coding selected, but no subject selected means all subjects
            // selected]
            chrIdeogram.drawVariant( variant, this.displayedProperty );
         } else {
            // if subject and color coding both selected case
            if ( this.selectedSubjectIds.indexOf( variant.subjectId ) == -1 ) {
               // color not selected subject variants in grey
               chrIdeogram.drawDimmedVariant( variant );
            } else {
               // color selected subject variants in color based on selected property
               chrIdeogram.drawHighlightedVariant( variant, this.displayedProperty );
            }
         }

      }

      var valuetoColourArray = [];
      var displayText = '';

      for (var i = 0; i < ASPIREdb.view.ideogram.VariantLayer.valueToColourMap.length; i++) {
         valuetoColourArray.push( ASPIREdb.view.ideogram.VariantLayer.valueToColourMap[i] );
         displayText = displayText + ASPIREdb.view.ideogram.VariantLayer.valueToColourMap[i];
      }

      // setting the colors for the ideogram ledgent
      if ( repeat ) {
         // do not update the ledgend
      } else {
         this.colourLegend.setTitle( 'Ideogram legend : ' + this.displayedProperty.displayName );
         this.colourLegend.update( displayText );
      }
      // this.colourLegend.update(valuetoColourArray,this.displayedProperty);
      // console.log( 'colour legend ' + valuetoColourArray );

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

   drawVariantsWithSubjectHighlighted : function(subjectId, variantValueObjects) {
      // List<VariantValueObject>
	  var variants = variantValueObjects.slice(); // copy array
	  this.variants = variants;
      this.sortVariantsBySize( variants );

      for (var i = 0; i < variants.length; i++) {
         var variant = variants[i];
         var chrName = variant.genomicRange.chromosome;
         // ChromosomeIdeogram
         var chrIdeogram = this.chromosomeIdeograms[chrName];
         if ( variant.subjectId === subjectId ) {
            console.log( 'drawing red variants of subject id :' + subjectId + ' in dieogam view' );
            chrIdeogram.drawHighlightedVariant( variant, this.displayedProperty );
         } else {
            chrIdeogram.drawDimmedVariant( variant );
         }
      }
   },

   /**
    * @private
    * @param subjectIds
    * @param {VariantValueObject[]}
    *           variantValueObjects
    */
   drawVariantsWithSubjectsHighlighted : function(subjectIds, variantValueObjects) {
      /* List<VariantValueObject> */
	  var variants = variantValueObjects.slice(); // copy array
	  this.variants = variants;
      this.sortVariantsBySize( variants );
      this.displayedProperty.displayType = null;
      this.setDisplayedProperty( this.displayedProperty );

      for (var i = 0; i < variants.length; i++) {
         var variant = variants[i];
         var chrName = variant.genomicRange.chromosome;
         /* ChromosomeIdeogram */
         var chrIdeogram = this.chromosomeIdeograms[chrName];
         if ( subjectIds.indexOf( variant.subjectId ) == -1 ) {
            chrIdeogram.drawDimmedVariant( variant );
         } else {
            // colour heighlighted variants in red
            chrIdeogram.drawHighlightedVariant( variant, this.displayedProperty );
         }

      }

      var valuetoColourArray = [];
      var displayText = '';

      for (var i = 0; i < ASPIREdb.view.ideogram.VariantLayer.valueToColourMap.length; i++) {
         valuetoColourArray.push( ASPIREdb.view.ideogram.VariantLayer.valueToColourMap[i] );
         displayText = displayText + ASPIREdb.view.ideogram.VariantLayer.valueToColourMap[i];
      }

      // setting the colors for the ideogram ledgend
      this.colourLegend.update( displayText );

   },

   drawDimmedVariants : function(variantValueObjects) {
      /* List<VariantValueObject> */
      var variants = variantValueObjects.slice(); // make a copy
      this.variants = variants;
      this.sortVariantsBySize( variants );
      for (var i = 0; i < variants.length; i++) {
         var variant = variants[i];
         var chrName = variant.genomicRange.chromosome;
         /* ChromosomeIdeogram */
         var chrIdeogram = this.chromosomeIdeograms[chrName];
         chrIdeogram.drawDimmedVariant( variant );
      }
   },

   /**
    * @public
    * @param {PropertyValueObject}
    *           displayedProperty
    */
   setDisplayedProperty : function(displayedProperty) {
      this.displayedProperty = displayedProperty;
      ASPIREdb.view.ideogram.VariantLayer.resetDisplayProperty( displayedProperty );
   },

   /**
    * @public
    * @return {Object.<string,string>}
    */
   getColourLegend : function() {
      return ASPIREdb.view.ideogram.VariantLayer.valueToColourMap;
   },

   selectDrawingType : function(subjectIds) {

      if ( subjectIds != null ) {
         this.selectedView = 'subject_selected';
         this.selectedSubjectIds = subjectIds;
      } else {
         this.selectedView = 'colourCoding_selected';
      }
   },

   /**
    * @public Redraw when zoomed the ideogram
    */
   redraw : function(variants) {
      // this.setDisplayedProperty(this.displayedProperty);
      this.drawChromosomes();
      if ( this.selectedView == 'subject_selected' ) {
         this.drawVariantsWithSubjectsHighlighted( this.selectedSubjectIds, variants );
      } else if ( this.selectedView == 'colourCoding_selected' ) {
         this.drawColouredVariants( variants, true );
         this.showColourLegend();
      } else
         this.drawVariants( variants );

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
    *           variants
    * @param subjectId
    */
   highlightSubject : function(/* Long */subjectId, /* List<VariantValueObject> */variants) {
      this.doneDrawing = false;
      this.initCanvasSize();

      this.drawChromosomes();
      this.drawVariantsWithSubjectHighlighted( subjectId, variants );

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
         if ( this.chromosomeIdeograms.hasOwnProperty( chromosomeName ) ) {
            var chromosomeIdeogram = this.chromosomeIdeograms[chromosomeName];
            if ( chromosomeIdeogram.getSelection() !== null ) {
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

} );
