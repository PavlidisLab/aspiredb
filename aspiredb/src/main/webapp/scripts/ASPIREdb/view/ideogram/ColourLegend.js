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
var ColourLegend = function(ctx) {

	this.displayProperty = null;
	this.colors = [ "red", "blue", "black", "purple", "brown", "olive", "maroon", "orange" ];
	this.defaultColour = "rgba(0,0,0,0.5)";
	this.nextColourIndex = 0;
//	this.valueToColourMap = [];
	this.characteristicList = {};
	this.ctx = ctx;
	this.baseColourPicker = null;
}


ColourLegend.prototype.assignColor = function(variantType, defaultColor, displayName){//, htmlLabel) {
	var colourTag;
	if ( this.characteristicList.length == 0 || ( ( colourTag = this.characteristicList[variantType] ) == undefined ) ) {
		var colour = defaultColor == undefined ? this.colors[this.nextColourIndex] : defaultColor;
		displayName = displayName == undefined ? variantType : displayName;
//		this.valueToColourMap.push( htmlLabel == undefined ? [ "<font color='" + colour + "'>" + variantType + "</font><br>\n" ] : [htmlLabel] );
		colourTag = {colour:colour, displayName: displayName};
		this.characteristicList[variantType] = colourTag;
		this.nextColourIndex++;
	}
	return colourTag;
};

//sets the display property and creates a baseColourPicker that will be used on each variant
ColourLegend.prototype.setColourCode = function(property) {
	this.displayProperty = property;

	this.nextColourIndex = 0;
//	this.valueToColourMap = [];
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
				return me.assignColor("~" + variant.labels[0].name, '#' + variant.labels[0].colour, variant.labels[0].name);//, ASPIREdb.view.LabelControlWindow.getHtmlLabel( variant.labels[0] ) + "<br>\n");
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
				return me.assignColor("~" + subject.labels[0].name, '#' + subject.labels[0].colour, subject.labels[0].name);//, ASPIREdb.view.LabelControlWindow.getHtmlLabel( subject.labels[0] ) + "<br>\n");
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
};



ColourLegend.prototype.getColour = function(variant) {

	if ( this.displayProperty == null || this.baseColourPicker == null ) {
		console.log("Null DisplayProperty or baseColourPicker");
		return this.defaultColour;
	}

	return this.baseColourPicker(variant).colour;
};

ColourLegend.prototype.measureWidth = function() {
	var maxWidth = 0;
	for (var legendEntry in this.characteristicList) {
		var displayName = this.characteristicList[legendEntry].displayName;
		var w = this.ctx.measureText(displayName).width + 45;

		maxWidth = w > maxWidth ? w : maxWidth;

	}
	return Math.round( maxWidth ) + 1;
};

ColourLegend.prototype.refresh = function() {

	var ctx = this.ctx;

	var legendX = 0;
	var legendY = 0;
	ctx.save();
	ctx.setTransform(1, 0, 0, 1, 0, 0);

	ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
	ctx.beginPath();
	ctx.lineWidth=1;
	ctx.textBaseline = "middle";

	// entries
	ctx.font = "14px Arial";
	var maxwidth = 0;
	var entries = 5;
	var entryHeight = 20;
	i = 0;
	for (var legendEntryKey in this.characteristicList) {
		var legendEntry = this.characteristicList[legendEntryKey];
		ctx.fillStyle = legendEntry.colour;
		ctx.fillRect(legendX + 5,legendY + 5 + entryHeight*i, 10, 10);

		ctx.fillStyle = "black";
		ctx.fillText(legendEntry.displayName, legendX + 35, legendY + 10 + entryHeight*i);
		var w = ctx.measureText(legendEntry.displayName).width + 35;

		maxwidth = w > maxwidth ? w : maxwidth;
		i++;
	}

	ctx.restore();


};