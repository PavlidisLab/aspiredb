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
   constructor : function(config) {
      this.initConfig( config );

      /**
       * @type {Array.<TrackLayer>}
       */
      this.trackLayers = [];
      this.numberOfTracks = (this.displayWidth != null) ? this.displayWidth - 5 : 2;
      this.createTracks( this.numberOfTracks );

      return this;
   },

   config : {
      displayScaleFactor : null,
      ctx : null,
      zoom : null,
      leftX : null,
      chromosomeLayer : null,
      // selectedVariants : [],
      displayWidth : null,
   },

   statics : {
      colors : [ "red", "blue", "black", "purple", "brown", "olive", "maroon", "orange" ],
      defaultColour : "rgba(0,0,0,0.5)",
      nextColourIndex : 0,
      /** @type {Object.<string,string>} */
      valueToColourMap : [],
      characteristicList : {},
      selectedVariants : [],
      resetDisplayProperty : function(property) {
         this.selectedVariants = [];

         if ( property.displayType != undefined ) {

            // if variant type property : CNV, SNV, indel,translocation, inversion
            if ( property instanceof VariantTypeProperty ) {

               for (var i = 0; i < property.displayType.length; i++) {
                  var value = property.displayType[i];
                  if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                     this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>" + value
                        + "</font><br>\n" ] );
                     this.characteristicList[value] = this.colors[this.nextColourIndex];
                     this.nextColourIndex++;
                  }
               }

            }

            // if CNV type : LOSS, GAIN
            if ( property instanceof CNVTypeProperty ) {
               for (var i = 0; i < property.displayType.length; i++) {
                  var value = property.displayType[i];
                  if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                     this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>" + value
                        + "</font><br>\n" ] );
                     this.characteristicList[value] = this.colors[this.nextColourIndex];
                     this.nextColourIndex++;
                  }
               }
            }

            // if Characteristic type : benign, pathogenic, unknown
            if ( property instanceof CharacteristicProperty ) {

               if ( property.name == 'Characteristics' ) {

                  for (var i = 0; i < property.displayType.length; i++) {
                     var value = property.displayType[i];
                     if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                        this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>"
                           + value + "</font><br>\n" ] );
                        this.characteristicList[value] = this.colors[this.nextColourIndex];
                        this.nextColourIndex++;
                     }
                  }
               }

               if ( property.name == 'Inheritance' ) {

                  for (var i = 0; i < property.displayType.length; i++) {
                     var value = property.displayType[i];
                     if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                        this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>"
                           + value + "</font><br>\n" ] );
                        this.characteristicList[value] = this.colors[this.nextColourIndex];
                        this.nextColourIndex++;
                     }
                  }
               }

               if ( property.name == 'Common CNV' ) {

                  for (var i = 0; i < property.displayType.length; i++) {
                     var value = property.displayType[i];
                     if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                        this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>"
                           + value + "</font><br>\n" ] );
                        this.characteristicList[value] = this.colors[this.nextColourIndex];
                        this.nextColourIndex++;
                     }
                  }
               }

               if ( property.name == 'Array Report' ) {

                  for (var i = 0; i < property.displayType.length; i++) {
                     var value = property.displayType[i];
                     if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                        this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>"
                           + value + "</font><br>\n" ] );
                        this.characteristicList[value] = this.colors[this.nextColourIndex];
                        this.nextColourIndex++;
                     }
                  }

               }

               if ( property.name == 'Array Platform' ) {

                  for (var i = 0; i < property.displayType.length; i++) {
                     var value = property.displayType[i];

                     if ( this.characteristicList.length == 0 || this.characteristicList[value] == undefined ) {
                        this.valueToColourMap.push( [ "<font color='" + this.colors[this.nextColourIndex] + "'>"
                           + value + "</font><br>\n" ] );
                        this.characteristicList[value] = this.colors[this.nextColourIndex];
                        this.nextColourIndex++;
                     }
                  }
               }

            }

         } else {
            this.characteristicList = {};
            this.valueToColourMap = [];
            this.nextColourIndex = 0;
         }

      }
   },

   /**
    * TODO: color legend function is broken/ not complete check getPropertyStringValue and property
    * 
    * @param {VariantValueObject}
    *           variant
    * @param {PropertyValueObject}
    *           property
    * @returns {string}
    */
   pickColor : function(variant, property) {
      if ( property == null )
         return this.self.defaultColour;

      var value = null;
      var colorIndex = 0;

      // if variant type property : CNV, SNV, indel,translocation, inversion
      if ( property instanceof VariantTypeProperty ) {
         property.name = variant.variantType;
         value = variant.variantType;

         var color = this.self.characteristicList[value];
         if ( color == null ) {
            var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
               + "</font><br>\n";
            color = this.self.colors[this.self.nextColourIndex];
            this.self.valueToColourMap.push( [ pushvalue ] );
            this.self.characteristicList[value] = color;
            this.self.nextColourIndex++;

         }
      }

      // if CNV type : LOSS, GAIN
      if ( property instanceof CNVTypeProperty ) {
         value = variant.type;

         var color = this.self.characteristicList[value];
         if ( color == null ) {

            var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
               + "</font><br>\n";
            color = this.self.colors[this.self.nextColourIndex];
            this.self.valueToColourMap.push( [ pushvalue ] );
            this.self.characteristicList[value] = color;
            this.self.nextColourIndex++;

         }
      }

      // if variant labels
      if ( property instanceof VariantLabelProperty ) {

         if ( variant.labels.length > 0 ) {
            value = variant.labels[0].name;
            var color = '#' + variant.labels[0].colour;
            var pushvalue = "<font color='" + color + "'>" + value + "</font><br>\n";
            var vtcmStat = 'No';

            if ( this.self.valueToColourMap.length == 0 ) {
               this.self.valueToColourMap.push( [ pushvalue ] );
               this.self.characteristicList[value] = color;
            }

            for (var i = 0; i < this.self.valueToColourMap.length; i++) {
               if ( this.self.valueToColourMap[i] == pushvalue ) {
                  vtcmStat = 'Yes';
               }
            }
            if ( vtcmStat == 'No' ) {
               this.self.valueToColourMap.push( [ pushvalue ] );
               this.self.characteristicList[value] = color;
            }

         } else {
            value = "No Label";
            var color = this.self.characteristicList[value];
            if ( color == null ) {
               color = "#303030";

               var pushvalue = "<font color=" + color + ">" + value + "</font><br>\n";
               this.self.valueToColourMap.push( [ pushvalue ] );
               this.self.characteristicList[value] = color;
               console.log( 'no subject label (default) :' + color );
            }

         }
      }

      // if subject labels
      if ( property instanceof SubjectLabelProperty ) {
         subject = variant.subject;

         if ( subject.labels.length > 0 ) {
            value = subject.labels[0].name;
            var color = '#' + subject.labels[0].colour;
            var pushvalue = "<font color='" + color + "'>" + value + "</font><br>\n";
            var vtcmStat = 'No';

            if ( this.self.valueToColourMap.length == 0 ) {
               this.self.valueToColourMap.push( [ pushvalue ] );
               this.self.characteristicList[value] = color;
            }

            for (var i = 0; i < this.self.valueToColourMap.length; i++) {
               if ( this.self.valueToColourMap[i] == pushvalue ) {
                  vtcmStat = 'Yes';
               }
            }
            if ( vtcmStat == 'No' ) {
               this.self.valueToColourMap.push( [ pushvalue ] );
               this.self.characteristicList[value] = color;
            }

         } else {
            value = "No Label";
            var color = this.self.characteristicList[value];
            if ( color == null ) {
               color = "#303030";

               var pushvalue = "<font color=" + color + ">" + value + "</font><br>\n";
               this.self.valueToColourMap.push( [ pushvalue ] );
               this.self.characteristicList[value] = color;
               console.log( 'no subject label (default) :' + color );
            }

         }
      }

      // if Characteristic type : benign, pathogenic,
      // unknown
      if ( property instanceof CharacteristicProperty ) {
         var characteristicValueObject = variant.characteristics[property.name];

         if ( characteristicValueObject !== null ) {
            if ( characteristicValueObject == undefined ) {
               console.log( 'characteristic is undefined : ' + property.name + '  !!!!!' );
               value = 'No data';
               var color = this.self.characteristicList[value];

               if ( color == null ) {
                  color = '#303030';
                  var pushvalue = "<font color=" + color + ">" + value + "</font><br>\n";

                  this.self.valueToColourMap.push( [ pushvalue ] );
                  this.self.characteristicList[value] = color;
               }

            } else {

               value = characteristicValueObject.value;

               if ( property.name == 'Characteristics' ) {

                  var color = this.self.characteristicList[value];

                  if ( color == null ) {

                     var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
                        + "</font><br>\n";
                     color = this.self.colors[this.self.nextColourIndex];
                     this.self.valueToColourMap.push( [ pushvalue ] );
                     this.self.characteristicList[value] = color;
                     this.self.nextColourIndex++;

                  }
               }

               if ( property.name == 'Inheritance' ) {

                  var color = this.self.characteristicList[value];

                  if ( color == null ) {

                     var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
                        + "</font><br>\n";
                     color = this.self.colors[this.self.nextColourIndex];
                     this.self.valueToColourMap.push( [ pushvalue ] );
                     this.self.characteristicList[value] = color;
                     this.self.nextColourIndex++;

                  }
               }

               if ( property.name == 'Common CNV' ) {
                  var color = this.self.characteristicList[value];
                  if ( color == null ) {
                     var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
                        + "</font><br>\n";
                     color = this.self.colors[this.self.nextColourIndex];
                     this.self.valueToColourMap.push( [ pushvalue ] );
                     this.self.characteristicList[value] = color;
                     this.self.nextColourIndex++;
                  }
               }

               if ( property.name == 'Array Report' ) {
                  var color = this.self.characteristicList[value];
                  if ( color == null ) {
                     var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
                        + "</font><br>\n";
                     color = this.self.colors[this.self.nextColourIndex];
                     this.self.valueToColourMap.push( [ pushvalue ] );
                     this.self.characteristicList[value] = color;
                     this.self.nextColourIndex++;

                  }
               }

               if ( property.name == 'Array Platform' ) {

                  var color = this.self.characteristicList[value];
                  if ( color == null ) {

                     var pushvalue = "<font color='" + this.self.colors[this.self.nextColourIndex] + "'>" + value
                        + "</font><br>\n";
                     color = this.self.colors[this.self.nextColourIndex];
                     this.self.valueToColourMap.push( [ pushvalue ] );
                     this.self.characteristicList[value] = color;
                     this.self.nextColourIndex++;
                  }
               }
            }
         }

      }

      return color;
   },

   /**
    * @public
    * @param {VariantValueObject}
    *           variant
    */
   drawDimmedVariant : function(variant) {
      /* VariantSegment */
      var segment = {
         start : variant.genomicRange.baseStart,
         end : variant.genomicRange.baseEnd,
         color : "#989898", // "rgb(128,128,128)",// "rgba(0,0,0,0.4)",//grey
         emphasize : false
      };

      // pick track layer
      for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
         var layer = this.trackLayers[trackIndex];
         if ( layer.doesFit( segment ) ) {
            this.drawLineSegment( layer.layerIndex, segment, this.ctx, this.displayScaleFactor );
            layer.insert( segment );
            break;
         }
      }
   },

   /**
    * @public
    * @param {VariantValueObject}
    *           variant
    * @param {PropertyValueObject}
    *           property
    */
   drawHighlightedVariant : function(variant, property) {
      this.self.selectedVariants.push( variant );
      /* VariantSegment */
      var segment = {
         start : variant.genomicRange.baseStart,
         end : variant.genomicRange.baseEnd,
         color : this.pickColor( variant, property ),// red "rgb(255,0,0)"
         emphasize : false
      };

      // pick track layer
      for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
         var layer = this.trackLayers[trackIndex];
         if ( layer.doesFit( segment ) ) {
            this.drawLineSegment( layer.layerIndex, segment, this.ctx, this.displayScaleFactor );
            layer.insert( segment );
            break;
         }
      }
   },

   /**
    * @public
    * @param {VariantValueObject}
    *           variant
    * @param {PropertyValueObject}
    *           property
    */
   drawVariant : function(variant, property) {
      /* VariantSegment */
      var segment = {
         start : variant.genomicRange.baseStart,
         end : variant.genomicRange.baseEnd,
         color : this.pickColor( variant, property ),
         emphasize : false
      };
      // pick track layer
      for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
         var layer = this.trackLayers[trackIndex];
         if ( layer.doesFit( segment ) ) {
            this.drawLineSegment( layer.layerIndex, segment, this.ctx, this.displayScaleFactor );
            layer.insert( segment );
            break;
         }
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
      x += 2 * this.zoom * layerIndex + 3.5;

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
         ctx.lineWidth = 5 * this.zoom;
      } else {
         ctx.lineWidth = 1 * this.zoom;
      }

      ctx.beginPath();
      ctx.moveTo( x, yStart );
      ctx.lineTo( x, yEnd );
      ctx.stroke();
      ctx.lineWidth = 1 * this.zoom;
   }
} );
