Ext.require([ 'Ext.window.*', 'Ext.layout.container.Border','ASPIREdb.view.phenotypeSubjectLabelGrid']);


Ext.define('ASPIREdb.view.PhenotypeSubjectLabelWindow', {
	extend : 'Ext.Window',
	alias : 'widget.phenotypesubjectlabelwindow',
	singleton : true,
	title : 'Subject Labels',
	closable : true,
	closeAction : 'hide',
	width : 900,
	height : 450,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',
	
	
	items : [ {
		itemId : 'phenotypeSubjectLabelGrid',
		xtype : 'phenotypeSubjectLabelGrid',		
	}],

	
	initComponent : function() {
		
		this.callParent();

	},
	
	initGridAndShow : function(psvos, selPhenotypes){
			
		var ref = this;
		
		var grid = ref.getComponent('phenotypeSubjectLabelGrid');
		grid.populateGrid(psvos, selPhenotypes);
		ref.show();
		
	},
	
populateGrid : function(psvos) {	
	
	var data = [];
	
	
	for (var i = 0; i < ref.phenotypeSummaryValueObjects.length; i++){
		var phenSummary = ref.phenotypeSummaryValueObjects[i];
		var phenoSummaryValueObject = voMap[phenSummary.name];
		//we are attaching a new property to phenSummary here, calling it phenoSummaryMapSelectedSubjects
		
		if (phenoSummaryValueObject){
			phenSummary.phenoSummaryMapSelectedSubjects= phenoSummaryValueObject.phenoSummaryMap;
			phenSummary.displaySummarySelectedSubjects= phenoSummaryValueObject.displaySummary;
		} else{//if phenoSummaryValueObject is null or undefined
			console.log("null or undefined phenoSummaryValueObject: "+ phenSummary.name);
		}
							
	}
	ref.getView().refresh(true);
		
		var grid = ASPIREdb.view.PhenotypeSubjectLabelWindow.getComponent('phenotypeSubjectLabelGrid');
		
		var data = [];
		for ( var i = 0; i < psvos.length; i++) {
			var phenSummary = psvos[i];
			
			var row = [ phenSummary, phenSummary, phenSummary,phenSummary ];
			data.push(row);

		}

		grid.store.loadData(data);
		grid.setLoading(false);
		
		grid.enableToolbar(gvos,uri);

	},
		
	/**
	
	 // Show the gene manager window
	 	
	initGridAndShow : function(){
		
		var ref = this;
		ref.show();
		var panel = ref.down('#phenotypeSubjectLabelGrid');
				
		
		/**grid.setLoading(true);
		
		UserGeneSetService.getSavedUserGeneSetNames( {
			callback : function(geneSetNames) {	
				ASPIREdb.view.GeneManagerWindow.populateGeneSetGrid(geneSetNames);			
			}
		});
		
	
	},
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
	}*/

});
	