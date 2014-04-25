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
    resizable: false,
	width : 925,
	height : 760,
	//layout : 'fit',
	bodyStyle : 'padding: 5px;',
	id : 'subjectPhenotypeHeatmapWindow',
	
	initComponent : function() {
		this.callParent();
	},
    
	draw : function( matrix ) {
   
        var matrixColumnMetadata = [];
        var matrixRowMetadata = [];
        
        for ( var i = 0 ; i < matrix.columnNames.length ; i++ ) {
            var type;
            if ( matrix.columnNames[i] == "Gender" || matrix.columnNames[i] == "Family history" ) {
                type = 'gender';
            } else {
                type = 'binary';
            }
            matrixColumnMetadata.push( { label: matrix.columnNames[i], type: type } );
        }
        
        for ( var i = 0 ; i < matrix.rowNames.length ; i++ ) {
            matrixRowMetadata.push( { label: matrix.rowNames[i]  } );
        }
        
        var convertedData = this.convertData(matrix.matrix, matrixColumnMetadata);
        
        var order = h2m.ClusterHelper.produceClusteredOrder(convertedData);
   
        var dataMatrix = {
                dimensions: {numberOfRows: matrix.rowNames.length, numberOfColumns: matrix.columnNames.length},
                getDataAt: function (rowIndex, columnIndex) {
                    return matrix.matrix[rowIndex][columnIndex];
                }
            };
        
		M2V = {};
		M2V.Util = {};
		M2V.Util.dataType = {};

		M2V.Util.dataType.renderNA = function (ctx, size) {
		};

		M2V.Util.dataType.renderGenderCell = function (ctx, gender, row, column, size) {
		    var color;
		    if (gender.toUpperCase() === "M") {
		        color = "rgb(72,209,204)";
		    } else if (gender.toUpperCase() === "F") {
		        color = "rgb(255,105,180)";

		    } else M2V.Util.dataType.renderNA(ctx, size);
		    ctx.fillStyle = color;
		    ctx.fillRect(1, 1, size.width - 2, size.height - 2);
		};

		M2V.Util.dataType.renderAbsentPresentCell = function (ctx, value, row, column, size) {
		    // TODO write a legend
            var color;
            if (value === 0 || value === "0" || value === "N" || value === "M" ) {
                color = "rgb(200,200,200)"; // grey
		    } else if (value === 1 || value === "1" || value === "Y" || value === "F" ) {
		        color = "rgb(0,0,0)"; // black
		    }  else M2V.Util.dataType.renderNA(ctx, size);
            
            ctx.fillStyle = color;
            ctx.fillRect(1, 1, size.width - 2, size.height - 2);
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
		
		var heatmap = Ext.create('Matrix2Viz', {
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
		            //{name: 'group', size: 50}
		        ],
		        column: [
		            {name: 'label', size: 100},
		            //{name: 'metaNumber', size: 10},
		            //{name: 'type', size: 50}
		        ]
		    },
		    renderers: {
		        cell: {
		        	'gender': {
		                //render: M2V.Util.dataType.renderGenderCell
                        render: M2V.Util.dataType.renderAbsentPresentCell
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
		            /*'group': {
		                render: renderDefaults.text
		            }*/
		        },
		        columnMetadata: {
		            'label': {
		                render: renderDefaults.text
		            },
		            /*'metaNumber': {
		                render: renderDefaults.metaNumber
		            },
		            'type': {
		                render: renderDefaults.text
		            }*/
		        }
		    },

		    // rows: rows, //sample_data.js
            rows: matrixRowMetadata, 
            
		    rowOrder: order.rowOrder,

		    // columns: columns, //sample_data.js
            columns: matrixColumnMetadata, 
            
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

        var t = new Ext.ToolTip({
            floating: {
                shadow: false
            },
            html: 'Events',
            hideDelay: 1,
            closable: false
        });
        t.show();
        
        heatmap.on('cell-mouse-in', function(index) {
            t.setTarget(this.matrix.el);
            //t.update('Cell Enter: '+index.row+','+index.col);
            t.update("Value is " + this.matrix.data.getDataAt(this.matrix.rowOrder[index.row], this.matrix.colOrder[index.col]));
            t.show();
        });
        
        heatmap.on('cell-mouse-out', function() {
            t.setTarget(this.matrix.el);
            //t.update('Cell Leave');
            t.show();
        });
        
        heatmap.on('cell-mouse-click', function() {
            t.setTarget(this.matrix.el);
            //t.update('Cell Click');
            t.show();
        });
        
        heatmap.on('label-mouse-in', function(index) {
            t.setTarget(this.matrix.el);
            //t.update('Label Enter: '+index.index+','+index.subIndex);
            if (index.orientation == Orientation.HORIZONTAL) {
                t.update(this.matrix.rows[this.matrix.rowOrder[index.index]].label);
            } else if (index.orientation == Orientation.VERTICAL) {
                t.update(this.matrix.columns[this.matrix.colOrder[index.index]].label);
            }
            t.show();
        });
        
        heatmap.on('label-mouse-out', function() {
            t.setTarget(this.matrix.el);
            //t.update('Label leave: ');
            t.show();
        });
        
        heatmap.on('label-mouse-click', function(index) {
            t.setTarget(this.matrix.el);
            //t.update('Label click: '+index.index+','+index.subIndex);
            t.show();
        });

        this.removeAll();
		this.add(heatmap);
		
		heatmap.draw();
		
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
                if ( value == null ) {
                    continue;
                }
	            if (columns[j].type === 'gender') {
	                if (value.toUpperCase() === 'M') {
	                    convertedValue = 1;
	                } else {
	                    convertedValue = 0;
	                }
	            } else if (columns[j].type === 'numeric') {
	                convertedValue = this.bin(3, value, columns[j].range);
	            } else if (columns[j].type === 'binary') {
                    if (value.toUpperCase() === 'Y' || value === '1') {
                        convertedValue = 1;
                    } else if (value.toUpperCase() === 'N' || value === '0') {
                        convertedValue = 0;
                    }
                }
	            convertedRow.push(convertedValue);
	        }
	        converted.push(convertedRow);
	    }
	    return converted;
	}
	
});