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

Ext
		.define(
				'ASPIREdb.view.ideogram.VariantLayer',
				{
					/**
					 * @param {CanvasRenderingContext2D}
					 *            config.ctx
					 * @param {number}
					 *            config.leftX
					 * @param {number}
					 *            config.displayScaleFactor
					 * @param {ChromosomeLayer}
					 *            config.chromosomeLayer
					 * @param {number}
					 *            config.zoom
					 */
					constructor : function(config) {
						this.initConfig(config);

						/**
						 * @type {Array.<TrackLayer>}
						 */
						this.trackLayers = [];
						this.createTracks(10);

						return this;
					},

					config : {
						displayScaleFactor : null,
						ctx : null,
						zoom : null,
						leftX : null,
						chromosomeLayer : null,
						selectedVariants : [],
					},

					statics : {
						colors : [ "red", "blue", "green", "purple", "brown",
								"black", "olive", "maroon" ],
						defaultColour : "rgba(0,0,0,0.5)",
						nextColourIndex : 0,
						/** @type {Object.<string,string>} */
						valueToColourMap : [],
						characteristicList : [],
						resetDisplayProperty : function(property) {

							if (property.displayType != undefined) {

								// if variant type property : CNV, SNV, indel,
								// translocation, inversion
								if (property instanceof VariantTypeProperty) {
									this.nextColourIndex = 0;
									if (property.displayType.indexOf('CNV') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[this.nextColourIndex]
														+ "'>" + "CNV"
														+ "</font><br>\n" ]);
									if (property.displayType.indexOf('SNV') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[++this.nextColourIndex]
														+ "'>" + "SNV"
														+ "</font><br>\n" ]);
									if (property.displayType.indexOf('indel') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[++this.nextColourIndex]
														+ "'>" + "Indel"
														+ "</font><br>\n" ]);
									if (property.displayType
											.indexOf('translocation') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[++this.nextColourIndex]
														+ "'>"
														+ "Translocation"
														+ "</font><br>\n" ]);
									if (property.displayType
											.indexOf('inversion') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[++this.nextColourIndex]
														+ "'>" + "Inversion"
														+ "</font><br>\n" ]);
								}

								// if CNV type : LOSS, GAIN
								if (property instanceof CNVTypeProperty) {
									this.nextColourIndex = 0;
									if (property.displayType.indexOf('LOSS') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[this.nextColourIndex]
														+ "'>" + "LOSS"
														+ "</font><br>\n" ]);
									if (property.displayType.indexOf('GAIN') != -1)
										this.valueToColourMap
												.push([ "<font color='"
														+ this.colors[++this.nextColourIndex]
														+ "'>" + "GAIN"
														+ "</font><br>\n" ]);
								}

								// if Characteristic type : benign, pathogenic,
								// unknown
								if (property instanceof CharacteristicProperty) {

									if (property.name == 'Characteristics') {

										this.nextColourIndex = 0;
										this.characteristicList = [];
										for (var i = 0; i < property.displayType.length; i++) {
											var value = property.displayType[i];
											if (this.characteristicList.length == 0
													|| this.characteristicList
															.indexOf(value) == -1) {
												this.valueToColourMap
														.push([ "<font color='"
																+ this.colors[this.nextColourIndex]
																+ "'>"
																+ value
																+ "</font><br>\n" ]);
												this.characteristicList
														.push(value);
												++this.nextColourIndex;
											}
										}
									}

									if (property.name == 'Inheritance') {
										this.nextColourIndex = 0;
										this.characteristicList = [];
										for (var i = 0; i < property.displayType.length; i++) {
											var value = property.displayType[i];
											if (this.characteristicList.length == 0
													|| this.characteristicList
															.indexOf(value) == -1) {
												this.valueToColourMap
														.push([ "<font color='"
																+ this.colors[this.nextColourIndex]
																+ "'>"
																+ value
																+ "</font><br>\n" ]);
												this.characteristicList
														.push(value);
												++this.nextColourIndex;
											}
										}
									}

									if (property.name == 'Common CNV') {
										this.nextColourIndex = 0;
										this.characteristicList = [];
										for (var i = 0; i < property.displayType.length; i++) {
											var value = property.displayType[i];
											if (this.characteristicList.length == 0
													|| this.characteristicList
															.indexOf(value) == -1) {
												this.valueToColourMap
														.push([ "<font color='"
																+ this.colors[this.nextColourIndex]
																+ "'>"
																+ value
																+ "</font><br>\n" ]);
												this.characteristicList
														.push(value);
												++this.nextColourIndex;
											}
										}
									}

									if (property.name == 'Array Report') {
										this.nextColourIndex = 0;
										this.characteristicList = [];
										for (var i = 0; i < property.displayType.length; i++) {
											var value = property.displayType[i];
											if (this.characteristicList.length == 0
													|| this.characteristicList
															.indexOf(value) == -1) {
												this.valueToColourMap
														.push([ "<font color='"
																+ this.colors[this.nextColourIndex]
																+ "'>"
																+ value
																+ "</font><br>\n" ]);
												this.characteristicList
														.push(value);
												++this.nextColourIndex;
											}
										}

									}

									if (property.name == 'Array Platform') {
										this.nextColourIndex = 0;
										this.characteristicList = [];
										for (var i = 0; i < property.displayType.length; i++) {
											var value = property.displayType[i];
											if (this.characteristicList.length == 0
													|| this.characteristicList
															.indexOf(value) == -1) {
												this.valueToColourMap
														.push([ "<font color='"
																+ this.colors[this.nextColourIndex]
																+ "'>"
																+ value
																+ "</font><br>\n" ]);
												this.characteristicList
														.push(value);
												++this.nextColourIndex;
											}
										}
									}

								}

							} else
								this.valueToColourMap = [];

						}
					},

					/**
					 * TODO: color legend function is broken/ not complete check
					 * getPropertyStringValue and property
					 * 
					 * @param {VariantValueObject}
					 *            variant
					 * @param {PropertyValueObject}
					 *            property
					 * @returns {string}
					 */
					pickColor : function(variant, property) {
						if (property == null)
							return this.self.defaultColour;

						var value = null;
						var colorIndex = 0;
						/**var sVstat = 'No';

						for (var j = 0; j < this.selectedVariants.length; j++) {
							if (this.selectedVariants[j] == variant)
								sVstat = 'Yes';
						}
						if (sVstat == 'Yes') {*/

							// if variant type property : CNV, SNV, indel,
							// translocation, inversion
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
									// this.self.valueToColourMap.push([value,"
									// : <font
									// color='"+color+"'>"+color+"</font>\n"]);
								}
							}

							// if CNV type : LOSS, GAIN
							if (property instanceof CNVTypeProperty) {
								value = variant.type;

								var color = this.self.valueToColourMap[value];
								if (color == null) {
									if (value.toLowerCase() === "loss") {
										color = this.self.colors[colorIndex];
									} else if (value.toLowerCase() === "gain") {
										color = this.self.colors[++colorIndex];
									}
									// this.self.valueToColourMap.push([value,"
									// : <font
									// color='"+color+"'>"+color+"</font>\n"]);
								}
							}

							// if variant labels
							if (property instanceof VariantLabelProperty) {

								if (variant.labels.length > 0) {
									value = variant.labels[0].name;
									var color = variant.labels[0].colour;
									var pushvalue = "<font color='" + color
											+ "'>" + value + "</font><br>\n";
									var vtcmStat = 'No';

									if (this.self.valueToColourMap.length == 0) {
										this.self.valueToColourMap
												.push([ pushvalue ]);
									}

									for (var i = 0; i < this.self.valueToColourMap.length; i++) {
										if (this.self.valueToColourMap[i] == pushvalue) {
											vtcmStat = 'Yes';
										}
									}
									if (vtcmStat == 'No') {
										this.self.valueToColourMap
												.push([ pushvalue ]);
									}

								} else {
									color = "grey";
									console.log('no subject label (default) :'
											+ color);
								}
							}

							// if subject labels
							if (property instanceof SubjectLabelProperty) {
								subject = variant.subject;

								if (subject.labels.length > 0) {
									value = subject.labels[0].name;
									var color = subject.labels[0].colour;
									var pushvalue = "<font color='" + color
											+ "'>" + value + "</font><br>\n";
									var vtcmStat = 'No';

									if (this.self.valueToColourMap.length == 0) {
										this.self.valueToColourMap
												.push([ pushvalue ]);
									}

									for (var i = 0; i < this.self.valueToColourMap.length; i++) {
										if (this.self.valueToColourMap[i] == pushvalue) {
											vtcmStat = 'Yes';
										}
									}
									if (vtcmStat == 'No') {
										this.self.valueToColourMap
												.push([ pushvalue ]);
									}

								} else {
									color = "grey";
									console.log('no subject label (default) :'
											+ color);
								}
							}

							// if Characteristic type : benign, pathogenic,
							// unknown
							if (property instanceof CharacteristicProperty) {
								var characteristicValueObject = variant.characteristics[property.name];

								if (characteristicValueObject !== null) {
									if (characteristicValueObject == undefined) {
										console
												.log('characteristic is undefined : '
														+ property.name
														+ '  !!!!!');
										value = 'undefined';
									} else
										value = characteristicValueObject.value;

									if (property.name == 'Characteristics') {
										for (var i = 0; i < this.self.valueToColourMap.length; i++) {
											var color = this.self.valueToColourMap[i][value];
										}
										if (color == null) {
											/**
											 * // for (var i=0; i<this.self.characteristicList.length;i++){ //
											 * if (value.toLowerCase() ===
											 * this.self.characteristicList[i]) {
											 * value=property.displayValue;
											 * this.self.characteristicList.push(value);
											 * color =
											 * this.self.colors[colorIndex];
											 * this.self.valueToColourMap.push([value," :
											 * <font color='"+color+"'>"+color+"</font>\n"]);
											 * colorIndex++; // } // }
											 */

											if (value.toLowerCase() === "pathogenic") {
												color = this.self.colors[colorIndex];
											} else if (value.toLowerCase() === "benign") {
												color = this.self.colors[colorIndex + 1];
											} else if (value.toLowerCase() === "unknown") {
												color = this.self.colors[colorIndex + 2];
											}

										}
									}

									if (property.name == 'Inheritance') {
										var color = this.self.valueToColourMap[value];
										if (color == null) {
											if (value.toLowerCase() === "de novo") {
												color = this.self.colors[colorIndex];
											} else if (value.toLowerCase() === "maternal") {
												color = this.self.colors[colorIndex + 1];
											} else if (value.toLowerCase() === "paternal") {
												color = this.self.colors[colorIndex + 2];
											} else if (value.toLowerCase() === "unclassified") {
												color = this.self.colors[colorIndex + 3];
												console.log('unclassified : '
														+ color);
											}

										}
									}

									if (property.name == 'Common CNV') {
										var color = this.self.valueToColourMap[value];
										if (color == null) {
											if (value.toLowerCase() === "y") {
												color = this.self.colors[colorIndex];
											} else if (value.toLowerCase() === "n") {
												color = this.self.colors[colorIndex + 1];
											}
										}
									}

									if (property.name == 'Array Report') {
										var color = this.self.valueToColourMap[value];
										if (color == null) {
											if (value.toLowerCase() === "normal") {
												color = this.self.colors[colorIndex];
											} else if (value.toLowerCase() === "abnormal") {
												color = this.self.colors[colorIndex + 1];
											} else if (value.toLowerCase() === "uncertain") {
												color = this.self.colors[colorIndex + 2];
											}

										}
									}

									if (property.name == 'Array Platform') {

										var color = this.self.valueToColourMap[value];
										if (color == null) {

											if (value.toLowerCase() === 'undefined') {
												color = 'darkgray';
											} else if (value.toLowerCase() === "normal") {
												color = this.self.colors[colorIndex];
											} else if (value.toLowerCase() === "abnormal") {
												color = this.self.colors[colorIndex + 1];
											} else if (value.toLowerCase() === "uncertain") {
												color = this.self.colors[colorIndex + 2];
											}
										}
									}
								}
							}

					//	}else color ="grey";
						return color;
					},

					/**
					 * hexToColorName : function(hex){ var colours = {"#f0f8ff":
					 * "aliceblue","#faebd7":"antiquewhite","#00ffff":"aqua","#7fffd4":"aquamarine","#f0ffff":"azure",
					 * "#f5f5dc":"beige","#ffe4c4":"bisque","#000000":"black","#ffebcd":"blanchedalmond","#0000ff":"blue","#8a2be2":"blueviolet","#a52a2a":"brown","#deb887":"burlywood",
					 * "#5f9ea0":"cadetblue","#7fff00":"chartreuse","#d2691e":"chocolate","#ff7f50":"coral","#6495ed":"cornflowerblue","#fff8dc":"cornsilk","#dc143c":"crimson","#00ffff":"cyan",
					 * "#00008b":"darkblue","#008b8b":"darkcyan","#b8860b":"darkgoldenrod","#a9a9a9":"darkgray","#006400":"darkgreen","#bdb76b":"darkkhaki","#8b008b":"darkmagenta","#556b2f":"darkolivegreen",
					 * "#ff8c00":"darkorange","#9932cc":"darkorchid","#8b0000":"darkred","#e9967a":"darksalmon","#8fbc8f":"darkseagreen","#483d8b":"darkslateblue","#2f4f4f":"darkslategray","#00ced1":"darkturquoise",
					 * "#9400d3":"darkviolet","#ff1493":"deeppink","#00bfff":"deepskyblue","#696969":"dimgray","#1e90ff":"dodgerblue",
					 * "#b22222":"firebrick","#fffaf0":"floralwhite","#228b22":"forestgreen","#ff00ff":"fuchsia",
					 * "#dcdcdc":"gainsboro","#f8f8ff":"ghostwhite","#ffd700":"gold","#daa520":"goldenrod","#808080":"gray","#008000":"green","#adff2f":"greenyellow",
					 * "#f0fff0":"honeydew","#ff69b4":"hotpink",
					 * "#cd5c5c":"indianred","#4b0082":"indigo","#fffff0":"ivory","#f0e68c":"khaki",
					 * "#e6e6fa":"lavender","#fff0f5":"lavenderblush","#7cfc00":"lawngreen","#fffacd":"lemonchiffon","#add8e6":"lightblue","#f08080":"lightcoral","#e0ffff":"lightcyan","#fafad2":"lightgoldenrodyellow",
					 * "#d3d3d3":"lightgrey","#90ee90":"lightgreen","#ffb6c1":"lightpink","#ffa07a":"lightsalmon","#20b2aa":"lightseagreen","#87cefa":"lightskyblue","#778899":"lightslategray","#b0c4de":"lightsteelblue",
					 * "#ffffe0":"lightyellow","#00ff00":"lime","#32cd32":"limegreen","#faf0e6":"linen",
					 * "#ff00ff":"magenta","#800000":"maroon","#66cdaa":"mediumaquamarine","#0000cd":"mediumblue","#ba55d3":"mediumorchid","#9370d8":"mediumpurple","#3cb371":"mediumseagreen","#7b68ee":"mediumslateblue",
					 * "#00fa9a":"mediumspringgreen","#48d1cc":"mediumturquoise","#c71585":"mediumvioletred","#191970":"midnightblue","#f5fffa":"mintcream","#ffe4e1":"mistyrose","#ffe4b5":"moccasin",
					 * "#ffdead":"navajowhite","#000080":"navy",
					 * "#fdf5e6":"oldlace","#808000":"olive","#6b8e23":"olivedrab","#ffa500":"orange","#ff4500":"orangered","#da70d6":"orchid",
					 * "#eee8aa":"palegoldenrod","#98fb98":"palegreen","#afeeee":"paleturquoise","#d87093":"palevioletred","#ffefd5":"papayawhip","#ffdab9":"peachpuff","#cd853f":"peru","#ffc0cb":"pink","#dda0dd":"plum","#b0e0e6":"powderblue","#800080":"purple",
					 * "#ff0000":"red","#bc8f8f":"rosybrown","#4169e1":"royalblue",
					 * "#8b4513":"saddlebrown","#fa8072":"salmon","#f4a460":"sandybrown","#2e8b57":"seagreen","#fff5ee":"seashell","#a0522d":"sienna","#c0c0c0":"silver","#87ceeb":"skyblue","#6a5acd":"slateblue","#708090":"slategray","#fffafa":"snow","#00ff7f":"springgreen","#4682b4":"steelblue",
					 * "#d2b48c":"tan","#008080":"teal","#d8bfd8":"thistle","#ff6347":"tomato","#40e0d0":"turquoise",
					 * "#ee82ee":"violet",
					 * "#f5deb3":"wheat","#ffffff":"white","#f5f5f5":"whitesmoke",
					 * "#ffff00":"yellow","#9acd32":"yellowgreen","#99CC00"
					 * :"pistachio", "3366FF":"primary(blue)" };
					 * 
					 * if (typeof colours[hex.toLowerCase()] != 'undefined')
					 * return colours[hex.toLowerCase()]; else return hex; },
					 */

					/**
					 * @public
					 * @param {VariantValueObject}
					 *            variant
					 */
					drawDimmedVariant : function(variant) {
						/* VariantSegment */
						var segment = {
							start : variant.genomicRange.baseStart,
							end : variant.genomicRange.baseEnd,
							color : "rgb(128,128,128)",// "rgba(0,0,0,
														// 0.4)",//grey
							emphasize : false
						};

						// pick track layer
						for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
							var layer = this.trackLayers[trackIndex];
							if (layer.doesFit(segment)) {
								this.drawLineSegment(layer.layerIndex, segment,
										this.ctx, this.displayScaleFactor);
								layer.insert(segment);
								break;
							}
						}
					},

					/**
					 * @public
					 * @param {VariantValueObject}
					 *            variant
					 * @param {PropertyValueObject}
					 *            property
					 */
					drawHighlightedVariant : function(variant, property) {
						/* VariantSegment */
						var segment = {
							start : variant.genomicRange.baseStart,
							end : variant.genomicRange.baseEnd,
							color : this.pickColor(variant, property),// red
																		// "rgb(255,0,0)"
							emphasize : false
						};
						this.selectedVariants.push(variant);
						// pick track layer
						for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
							var layer = this.trackLayers[trackIndex];
							if (layer.doesFit(segment)) {
								this.drawLineSegment(layer.layerIndex, segment,
										this.ctx, this.displayScaleFactor);
								layer.insert(segment);
								break;
							}
						}
					},

					/**
					 * @public
					 * @param {VariantValueObject}
					 *            variant
					 * @param {PropertyValueObject}
					 *            property
					 */
					drawVariant : function(variant, property) {
						/* VariantSegment */
						var segment = {
							start : variant.genomicRange.baseStart,
							end : variant.genomicRange.baseEnd,
							color : this.pickColor(variant, property),
							emphasize : false
						};
						// pick track layer
						for (var trackIndex = 0; trackIndex < this.trackLayers.length; trackIndex++) {
							var layer = this.trackLayers[trackIndex];
							if (layer.doesFit(segment)) {
								this.drawLineSegment(layer.layerIndex, segment,
										this.ctx, this.displayScaleFactor);
								layer.insert(segment);
								break;
							}
						}
					},

					/**
					 * @public
					 */
					clearTracks : function() {
						this.trackLayers = [];
						this.createTracks(10);
					},

					/**
					 * @private
					 */
					createTracks : function(numberOfTracks) {
						// Define helper classes
						/**
						 * @param {number}
						 *            start
						 * @param {number}
						 *            end
						 * @param {string}
						 *            color
						 * @param {boolean}
						 *            emphasize
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
						 *            end:number}} segment
						 * @returns {boolean}
						 */
						TrackLayer.prototype.doesFit = function(segment) {
							/**
							 * @param {{start:number,
							 *            end:number}} segment
							 * @param {number}
							 *            point
							 * @returns {boolean}
							 */
							function isWithin(segment, point) {
								return (segment.start <= point && segment.end >= point);
							}

							/**
							 * @param {{start:number,
							 *            end:number}} segment
							 * @param {{start:number,
							 *            end:number}} existingSegment
							 * @returns {boolean}
							 */
							function doesOverlap(segment, existingSegment) {
								return (isWithin(existingSegment, segment.start)
										|| isWithin(existingSegment,
												segment.end)
										|| isWithin(segment,
												existingSegment.start) || isWithin(
										segment, existingSegment.end));
							}

							for (var i = 0; i < this.segments.length; i++) {
								var existingSegment = this.segments[i];
								if (doesOverlap(segment, existingSegment))
									return false;
							}
							return true;
						};

						/**
						 * @param {VariantSegment}
						 *            segment
						 */
						TrackLayer.prototype.insert = function(segment) {
							this.segments.push(segment);
						};

						for (var i = 0; i < numberOfTracks; i++) {
							this.trackLayers.push(new TrackLayer(i));
						}
					},

					/**
					 * 
					 * @param {number}
					 *            layerIndex
					 * @param {VariantSegment}
					 *            segment
					 * @param {CanvasRenderingContext2D}
					 *            ctx
					 * @param {number}
					 *            displayScaleFactor
					 */
					drawLineSegment : function(layerIndex, segment, ctx,
							displayScaleFactor) {
						var start = segment.start;
						var end = segment.end;

						var x = this.leftX + 7;
						x += 2 * this.zoom * layerIndex + 3.5;

						var yStart = this.chromosomeLayer
								.convertToDisplayCoordinates(start,
										displayScaleFactor);
						var yEnd = this.chromosomeLayer
								.convertToDisplayCoordinates(end,
										displayScaleFactor);

						if (Math.round(yStart) === Math.round(yEnd)) { // Too
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
