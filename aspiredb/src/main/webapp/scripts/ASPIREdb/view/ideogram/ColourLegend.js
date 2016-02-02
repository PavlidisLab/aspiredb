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
 * Ideogram colour legend for colouring variants based on variant type, CNV type, etc.
 */
Ext.define( 'ASPIREdb.view.ideogram.ColourLegend', {
	/**
	 * @memberOf ASPIREdb.view.ideogram.ColourLegend
	 */
	extend : 'Ext.window.Window',
	alias : 'widget.colourlegend',
	width : 150,
	height : 150,
	autoScroll : true,
	title : 'Ideogram',
	closable : false,
	resizable : true,
	layout : 'absolute',
	items : [ {
		xtype : 'component',
		autoEl : 'canvas',
		itemId : 'canvasBox',
		x : 0,
		y : 0,
		width : 200,
		height : 200
	} ],
	tools : [ {
		type : 'restore',
		hidden : true,
		handler : function(evt, toolEl, owner, tool) {
			var window = owner.up( 'window' );
			window.expand( '', false );
			window.setWidth( winWidth );
			window.center();
			isMinimized = false;
			this.hide();
			this.nextSibling().show();
		}
	}, {
		type : 'minimize',
		handler : function(evt, toolEl, owner, tool) {
			var window = owner.up( 'window' );
			window.collapse();
			winWidth = window.getWidth();
			window.setWidth( 150 );
			window.alignTo( Ext.getBody(), 'b-b' );
			this.hide();
			this.previousSibling().show();
			isMinimized = true;
		}
	} ],

	displayProperty : null,
	colors : [ "red", "blue", "black", "purple", "brown", "olive", "maroon", "orange" ],
	defaultColour : "rgba(0,0,0,0.5)",
	nextColourIndex : 0,
	valueToColourMap : [],
	characteristicList : {},


	assignColor : function(variantType, defaultColor, htmlLabel) {
		var colour;
		if ( this.characteristicList.length == 0 || ( ( colour = this.characteristicList[variantType] ) == undefined ) ) {
			colour = defaultColor == undefined ? this.colors[this.nextColourIndex] : defaultColor;
			this.valueToColourMap.push( htmlLabel == undefined ? [ "<font color='" + colour + "'>" + variantType + "</font><br>\n" ] : [htmlLabel] );
			this.characteristicList[variantType] = colour;
			this.nextColourIndex++;
		}
		return colour;
	},

//	sets the display property and creates a baseColourPicker that will be used on each variant
	setColourCode : function(property) {
		this.displayProperty = property;

		this.nextColourIndex = 0;
		this.valueToColourMap = [];
		this.characteristicList = {};
		
		var me = this;

		// if variant type property : CNV, SNV, indel, translocation, inversion
		if ( property instanceof VariantTypeProperty ) {
			this.baseColourPicker = function(variant) {
				return me.assignColor(variant.variantType);
			}
		}

		// if CNV type : LOSS, GAIN
		else if ( property instanceof CNVTypeProperty ) {
			this.baseColourPicker = function(variant) {
				return me.assignColor(variant.type);
			}
		}

		// if variant labels
		else if ( property instanceof VariantLabelProperty ) {

			this.baseColourPicker = function(variant) {
				if ( variant.labels.length > 0 ) {
					// Adding a ~ character to prevent users creating labels such as 'No Label' and screwing with the color picking
					return me.assignColor("~" + variant.labels[0].name, '#' + variant.labels[0].colour, ASPIREdb.view.LabelControlWindow.getHtmlLabel( variant.labels[0] ) + "<br>\n");
				} else {
					return me.assignColor('No Label', '#303030');
				}
			}
		}

		// if subject labels
		else if ( property instanceof SubjectLabelProperty ) {

			this.baseColourPicker = function(variant) {
				var subject = variant.subject;
				if ( subject != null && subject.labels.length > 0 ) {
					// Adding a ~ character to prevent users creating labels such as 'No Label' and screwing with the color picking
					return me.assignColor("~" + subject.labels[0].name, '#' + subject.labels[0].colour, ASPIREdb.view.LabelControlWindow.getHtmlLabel( subject.labels[0] ) + "<br>\n");
				} else {
					return me.assignColor('No Label', '#303030');
				}
			}
		}

		// if Characteristic type : benign, pathogenic, unknown
		else if ( property instanceof CharacteristicProperty ) {

			var name = property.name;

			this.baseColourPicker = function(variant) {
				var characteristicValueObject = variant.characteristics[name];
				if ( characteristicValueObject != undefined && characteristicValueObject !== null) {
					// Adding a ~ character to prevent users creating labels such as 'No Label' and screwing with the color picking
					return me.assignColor(characteristicValueObject.value);
				} else {
					return me.assignColor('No data', '#303030');
				}
			}
		}
	},

	baseColourPicker : null,

	getColour : function(variant) {

		if ( this.displayProperty == null || this.baseColourPicker == null )
			return this.defaultColour;

		return this.baseColourPicker(variant);
	},

	refresh : function() {
		var valuetoColourArray = [];
		var displayText = '';

		for (var i = 0; i < this.valueToColourMap.length; i++) {
			valuetoColourArray.push( this.valueToColourMap[i] );
			displayText = displayText + this.valueToColourMap[i];
		}

		this.setTitle( 'Ideogram legend : ' + (this.displayProperty && this.displayProperty.displayName ? this.displayProperty.displayName : '') );
		this.update( displayText );
	}
} );