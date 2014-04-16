/**
 * 
 */
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

Ext.require([ 'Ext.Window', 'Matrix2Viz' ]);

Ext.define('ASPIREdb.view.SubjectPhenotypeHeatmapWindow', {
	extend : 'Ext.Window',
	alias : 'widget.subjectPhenotypeHeatmapWindow',
	singleton : true,
	title : 'Subject Phenotype Heatmap',
	closable : true,
	closeAction : 'hide',
	width : 600,
	height : 600,
	//layout : 'fit',
	bodyStyle : 'padding: 5px;',
	id : 'subjectPhenotypeHeatmapWindow',
	
	initComponent : function() {
		this.callParent();
	},

	draw : function() {
		
		// sample_data.js
		var dataMatrix = {
			    dimensions: {numberOfRows: data.data.length, numberOfColumns: data.data[0].length},
			    getDataAt: function (rowIndex, columnIndex) {
			        return data.data[rowIndex][columnIndex];
			    }
			};

		var convertedData = this.convertData(data.data, columns);

		var order = h2m.ClusterHelper.produceClusteredOrder(convertedData);

		M2V = {};
		M2V.Util = {};
		M2V.Util.dataType = {};

		M2V.Util.dataType.renderNA = function (ctx, size) {
		};

		M2V.Util.dataType.renderGenderCell = function (ctx, gender, row, column, size) {
		    var color;
		    if (gender === "m") {
		        color = "rgb(72,209,204)";
		    } else if (gender === "f") {
		        color = "rgb(255,105,180)";

		    } else M2V.Util.dataType.renderNA(ctx, size);
		    ctx.fillStyle = color;
		    ctx.fillRect(1, 1, size.width - 2, size.height - 2);
		};

		M2V.Util.dataType.renderAbsentPresentCell = function (ctx, value, row, column, size) {
		    if (value === 0) {
		        ctx.fillStyle = "black";
		        ctx.fillRect(size.width / 4, size.height / 4, size.width / 2, size.height / 2);
		    } else if (value === 1) {
		        ctx.strokeStyle = "black";
		        ctx.strokeRect(size.width / 4, size.height / 4, size.width / 2, size.height / 2);
		    } else M2V.Util.dataType.renderNA(ctx, size);
		};
		
		//TODO: provide centering helper function
		var renderDefaults = {
		    text: function (ctx, box, text) {
		        ctx.translate(0, box.height / 2 + 4);  //fontSize
		        ctx.fillText(text, 0, 0);
		    },
		    label: function (ctx, box, text) {
		        ctx.translate(0, box.height / 2 + 4);  //fontSize
		        ctx.fillText(text, 0, 0);
		    },
		    group: function (ctx, box, value) {
		        ctx.translate(0, box.height / 2 + 4);  //fontSize
		        ctx.fillText(value, 0, 0);
		    },
		    type: function (ctx, box, value) {
		        ctx.translate(0, box.height / 2 + 4);  //fontSize
		        ctx.fillText(value, 0, 0);
		    },
		    metaNumber: function (ctx, box, value) {
		        ctx.fillStyle = 'rgb(0,100,0)';
		        ctx.fillRect(0, 1, box.width * value, box.height - 2);
		    }
		};

		/*
		console.log('items size='+this.items.length);
		var testPanel = Ext.create('Ext.Panel', {
			renderTo : Ext.getCmp('matrix_div').getEl(),
			id : 'label_2',
			width : 50,
			height: 50,
			html : "success<br/><br/><br/><br/>success<br/><br/><br/><br/>success<br/><br/><br/><br/>!"
		});
		console.log('testPanel='+testPanel);
		this.add(testPanel);
		console.log('items size='+this.items.length);
		*/
		
		var matrix = Ext.create('Matrix2Viz', {
		    //renderTo: "matrix_div",
			//renderTo: document.body,
			//renderTo: this.down('#matrix_div').getEl(),
			renderTo: this.el,
		    width: 900,
		    height: 700,

		    data: dataMatrix,
		    labelFormat: {
		        row: [
		            {name: 'label', size: 90},
		            {name: 'group', size: 50}
		        ],
		        column: [
		            {name: 'label', size: 100},
		            {name: 'metaNumber', size: 10},
		            {name: 'type', size: 50}
		        ]
		    },
		    renderers: {
		        cell: {
		        	'gender': {
		                render: M2V.Util.dataType.renderGenderCell
		            },
		            'binary': {
		                render: M2V.Util.dataType.renderAbsentPresentCell
		            },
		            'numeric': {
		                render: function(ctx, data, row, column, size) {
		                    var red = Math.round(255 * (data / (column.range.high - column.range.low)));
		                    ctx.fillStyle = "rgb(" + red + ",0,0)";
		                    ctx.fillRect(1, 1, size.width - 2, size.height - 2);
		                }
		            }
		        },
		        rowMetadata: {
		            'label': {
		                render: renderDefaults.text
		            },
		            'group': {
		                render: renderDefaults.text
		            }
		        },
		        columnMetadata: {
		            'label': {
		                render: renderDefaults.text
		            },
		            'metaNumber': {
		                render: renderDefaults.metaNumber
		            },
		            'type': {
		                render: renderDefaults.text
		            }
		        }
		    },

		    rows: rows, //sample_data.js
		    rowOrder: order.rowOrder,

		    columns: columns, //sample_data.js
		    columnOrder: order.columnOrder,

		    clustering: order,

		    cellSize: {
		        width: 20,
		        height: 10
		    },

		    controlPanel: 'DefaultControlPanel',

		    // TODO: add row/column options for both
		    displayOptions: {
		        showRowDendrogram: true,
		        showColumnDendrogram: true,
		        showRowLabels: true,
		        showColumnLabels: true
		    }
		});

		this.add(matrix);
		
		matrix.draw();
		
	},
	
	 bin : function(numberOfBins, value, range) {
	    return Math.floor(value / ((range.high - range.low) / numberOfBins));
	},

	convertData : function (data, columns) {
	    var converted = [];
	    for (var i = 0; i < data.length; i++) {
	        var row = data[i];
	        var convertedRow = [];
	        for (var j = 0; j < row.length; j++) {
	            var value = row[j];
	            var convertedValue = value;
	            if (columns[j].type === 'gender') {
	                if (value === 'm') {
	                    convertedValue = 1;
	                } else {
	                    convertedValue = 0;
	                }
	            } else if (columns[j].type === 'numeric') {
	                convertedValue = this.bin(3, value, columns[j].range);
	            }
	            convertedRow.push(convertedValue);
	        }
	        converted.push(convertedRow);
	    }
	    return converted;
	}
	
});