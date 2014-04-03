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
            "red",
            "blue",
            "green",
            "purple",
            "brown",
            "black",
            "olive",
            "maroon"
        ],
        defaultColour: "rgba(0,0,0,0.5)",
        nextColourIndex: 0,
        /** @type {Object.<string,string>} */
        valueToColourMap: [],
        characteristicList :[],
        resetDisplayProperty: function (property) {
      
        if (property.displayType!=undefined){
        	
        	//if variant type property : CNV, SNV, indel, translocation, inversion
    		if (property instanceof VariantTypeProperty) {
    			this.nextColourIndex = 0;
    			if (property.displayType.indexOf('CNV')!=-1)
    				this.valueToColourMap.push(["CNV"," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
    			if (property.displayType.indexOf('SNV')!=-1)
    				this.valueToColourMap.push(["SNV"," : <font color='"+this.colors[++this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]); 
    			if (property.displayType.indexOf('indel')!=-1)
    				this.valueToColourMap.push(["Indel"," : <font color='"+this.colors[++this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
    			if (property.displayType.indexOf('translocation')!=-1)
    				this.valueToColourMap.push(["Translocation"," : <font color='"+this.colors[++this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
    			if (property.displayType.indexOf('inversion')!=-1)
    				this.valueToColourMap.push(["Inversion"," : <font color='"+this.colors[++this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
    		}
    		
        	//if CNV type : LOSS, GAIN
    		if (property instanceof CNVTypeProperty) {
    			this.nextColourIndex = 0;
    			if (property.displayType.indexOf('LOSS')!=-1)
    				this.valueToColourMap.push(["LOSS"," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>"+"\n"]);
    			if (property.displayType.indexOf('GAIN')!=-1)
    				this.valueToColourMap.push(["GAIN"," : <font color='"+this.colors[++this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>"+"\n"]);    			
    		} 
    		
    	    		
    		//if Characteristic type : benign, pathogenic, unknown
    		if (property instanceof CharacteristicProperty) {
    			
    			if (property.name == 'Characteristics'){    
    				
    					  this.nextColourIndex = 0;
    					  this.characteristicList=[];
    					  for (var i=0; i<property.displayType.length;i++){
    						  var value =property.displayType[i];     						  
    						  if (this.characteristicList.length ==0 || this.characteristicList.indexOf(value)==-1 ){
    							  this.valueToColourMap.push([value," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
    							  this.characteristicList.push(value);  
    							  ++this.nextColourIndex;
    						  }
    					  }    		
    			}	    			
    		
				if (property.name == 'Inheritance'){
					this.nextColourIndex = 0;
					  this.characteristicList=[];
					  for (var i=0; i<property.displayType.length;i++){
						  var value =property.displayType[i];     						  
						  if (this.characteristicList.length ==0 || this.characteristicList.indexOf(value)==-1 ){
							  this.valueToColourMap.push([value," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
							  this.characteristicList.push(value);  
							  ++this.nextColourIndex;
						  }
					  }		
			    }	 			
						
				if (property.name == 'Common CNV'){
					this.nextColourIndex = 0;
					  this.characteristicList=[];
					  for (var i=0; i<property.displayType.length;i++){
						  var value =property.displayType[i];     						  
						  if (this.characteristicList.length ==0 || this.characteristicList.indexOf(value)==-1 ){
							  this.valueToColourMap.push([value," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
							  this.characteristicList.push(value);  
							  ++this.nextColourIndex;
						  }
					  }		
			   }	    			
		      		
				if (property.name == 'Array Report'){
					this.nextColourIndex = 0;
					  this.characteristicList=[];
					  for (var i=0; i<property.displayType.length;i++){
						  var value =property.displayType[i];     						  
						  if (this.characteristicList.length ==0 || this.characteristicList.indexOf(value)==-1 ){
							  this.valueToColourMap.push([value," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
							  this.characteristicList.push(value);  
							  ++this.nextColourIndex;
						  }
					  }
		
				}	    			
    		
				if (property.name == 'Array Platform'){
					this.nextColourIndex = 0;
					  this.characteristicList=[];
					  for (var i=0; i<property.displayType.length;i++){
						  var value =property.displayType[i];     						  
						  if (this.characteristicList.length ==0 || this.characteristicList.indexOf(value)==-1 ){
							  this.valueToColourMap.push([value," : <font color='"+this.colors[this.nextColourIndex]+"'>"+this.colors[this.nextColourIndex]+"</font>\n"]);
							  this.characteristicList.push(value);  
							  ++this.nextColourIndex;
						  }
					  }		
				}	    			
		  			
    		} 
    		
    		
        } else  this.valueToColourMap=[];
        	
        }
    },
    
       
    /**
     * TODO: color legend function is broken/ not complete check getPropertyStringValue and property
     * @param {VariantValueObject} variant
     * @param {PropertyValueObject} property
     * @returns {string}
     */
    pickColor: function (variant, property) {
        if (property == null) return this.self.defaultColour;

        var value = null;
        var colorIndex=0;        
            
        //if variant type property : CNV, SNV, indel, translocation, inversion
        if (property instanceof VariantTypeProperty) {
        	property.name = variant.variantType;
            value = variant.variantType;
            
            var color = this.self.valueToColourMap[value];
             if (color == null) {            	
            	if (value.toLowerCase() === "cnv") {            
                    color = this.self.colors[colorIndex];                    
                } else if (value.toLowerCase() === "snv") {            	
                    color = this.self.colors[++colorIndex];
                } else if (value.toLowerCase() === "indel") { 
                	 colorIndex++;
                    color = this.self.colors[++colorIndex];
                } else if (value.toLowerCase() === "translocation") {  
                	 colorIndex++;
                    color = this.self.colors[++colorIndex];
                } else if (value.toLowerCase() === "inversion") {   
                	 colorIndex++;
                    color = this.self.colors[++colorIndex];
                }
            	//this.self.valueToColourMap.push([value," : <font color='"+color+"'>"+color+"</font>\n"]);
            }         
        } 
        
        //if CNV type : LOSS, GAIN
        if (property instanceof CNVTypeProperty) {
             value = variant.type;
                         
             var color = this.self.valueToColourMap[value];
             if (color == null) {
            	 if (value.toLowerCase() === "loss") {            
                     color = this.self.colors[colorIndex];                      	
                 } else if (value.toLowerCase() === "gain") {            	
                     color = this.self.colors[++colorIndex];
                 }
            	 //this.self.valueToColourMap.push([value," : <font color='"+color+"'>"+color+"</font>\n"]);
            }
        }
        
        //if  variant labels
        if (property instanceof VariantLabelProperty) {
        	
           	if (variant.labels.length >0){
        		value = variant.labels[0].name;        		
          		var color = variant.labels[0].colour;
        		console.log('variant label color :'+color);
        		this.self.valueToColourMap.push([value," : <font color='"+color+"'>"+color+"</font>\n"]); 	                         
           	} 
        }
        
        //if  subject labels
        if (property instanceof SubjectLabelProperty) {
             subject = variant.subject;
             if (subject.labels.length >0){
         		value = subject.labels[0].name;        		
           		var color = subject.labels[0].colour;
         		console.log('subject label color :'+color);
         		this.self.valueToColourMap.push([value," : <font color='"+color+"'>"+color+"</font>\n"]); 	                         
            	}            
        }
        
        //if Characteristic type : benign, pathogenic, unknown
        if (property instanceof CharacteristicProperty) {
           var characteristicValueObject = variant.characteristics[property.name];
           
            if (characteristicValueObject !== null) {
            	//if (characteristicValueObject!= undefined){
            //	console.log('characteristic is undefined : '+ property.name+'  !!!!!');
            	//}else{
                value = characteristicValueObject.value;
                
                if (property.name =='Characteristics'){                	                	
                     var color = this.self.valueToColourMap[value];
                     if (color == null) {                   	 
                    	 
                    	 if (value.toLowerCase() === "pathogenic") {            
                             color = this.self.colors[colorIndex];                              
                         } else if (value.toLowerCase() === "benign") {   
                        	 color = this.self.colors[colorIndex+1];                          
                         }
                         else if (value.toLowerCase() === "unknown") { 
                        	 color = this.self.colors[colorIndex+2];                             
                         }
                	  
                     }
                }
                
                if (property.name =='Inheritance'){                	
                    var color = this.self.valueToColourMap[value];
                    if (color == null) {
                   	 if (value.toLowerCase() === "de novo") {            
                            color = this.self.colors[colorIndex];                            
                        } else if (value.toLowerCase() === "maternal") {            	
                            	color = this.self.colors[colorIndex+1];
                        }
                        else if (value.toLowerCase() === "paternal") {                         	        	
                        		color = this.self.colors[colorIndex+2];
                        }
                        else if (value.toLowerCase() === "unclassified") {     
                        		color = this.self.colors[colorIndex+3];
                        		console.log('unclassified : '+color);
                        }
               	  
                    }
               }
                
                if (property.name =='Common CNV'){                	
                    var color = this.self.valueToColourMap[value];
                    if (color == null) {
                   	 if (value.toLowerCase() === "y") {            
                            color = this.self.colors[colorIndex];                            
                        } else if (value.toLowerCase() === "n") {            	
                            	color = this.self.colors[colorIndex+1];
                        }                                      	  
                    }
               }
                
                if (property.name =='Array Report'){                	
                    var color = this.self.valueToColourMap[value];
                    if (color == null) {
                   	 if (value.toLowerCase() === "normal") {            
                            color = this.self.colors[colorIndex];                            
                        }
                   	    else if (value.toLowerCase() === "abnormal") {            	
                   	    		color = this.self.colors[colorIndex+1];
                        }
                        else if (value.toLowerCase() === "uncertain") {  
                        		color = this.self.colors[colorIndex+2];
                        }
                      
                    }
               }
               
                if (property.name =='Array Platform'){  
                	
                    var color = this.self.valueToColourMap[value];
                    if (color == null) {
                   	 if (value.toLowerCase() === "normal") {            
                            color = this.self.colors[colorIndex];                            
                        } else if (value.toLowerCase() === "abnormal") {            	
                            	color = this.self.colors[colorIndex+1];
                        }
                        else if (value.toLowerCase() === "uncertain") { 
                        	    color = this.self.colors[colorIndex+2];
                        }                      
                    }
                }
           //   }  
                
            }
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
            start: variant.genomicRange.baseStart,
            end: variant.genomicRange.baseEnd,
            color: "rgb(128,128,128)",//"rgba(0,0,0, 0.4)",//grey
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
            start: variant.genomicRange.baseStart,
            end: variant.genomicRange.baseEnd,
            color: this.pickColor(variant, property),//red "rgb(255,0,0)"
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
    drawVariant: function (variant, property) {
        /*VariantSegment*/
        var segment = {
            start: variant.genomicRange.baseStart,
            end: variant.genomicRange.baseEnd,
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
