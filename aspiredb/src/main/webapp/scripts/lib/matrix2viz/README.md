matrix2viz
==========

## Overview

__matrix2viz__ is a matrix visualization ExtJs component. It takes care of some boring parts allowing developer to concentrate on more interesting things. 

##### matrix2viz takes care of:
- exposing cell and label mouse events
- zooming in and out
- synchronized scrolling of matrix and label panels if the entire matrix doesn't fit on the screen
- resizing and layout of visualization parts
- cell and label positioning
- cell and label highlighting on hover
- dendrogram rendering

#### Dependencies
 - Modern web browser
 - ExtJs 4.2

## API

### Key concepts:
Two major parts of visualization are:
- matrix of cells
- labels for the rows and columns
 
...Diagram goes here...

### Configuration

```javascript
var visualization = Ext.create('Matrix2Viz', {
	... configuration ...
}
```

Configuration boils down to providing data and functions for rendering these data. There are two types of data:
- cell data
- row and column data

#### data

_data_ is an object that must provide _dimensions_ property and _getDataAt_ function:

```javascript
data: {
	getDataAt: function(rowIndex, columnIndex) {...}, // returns your cell data object
	dimensions: {numberOfRows: ..., numberOfColumns: ...} // dimensions of your data 
}
```

The framework is data agnostic. In other words, your _getDataAt(rowIndex,columnIndex)_ function can return anything. The returned object is just passed to the rendering function you provide.
	
Each column can have metadata associated with it. In the simplest case it could be just a text label. 

#### columns
```javascript
columns: [
	{
 		// If all your cell data are of the same type this is not needed.
 		type : 'numeric', // [optional] string representing datatype of this column.
 		
 		// Any number of properties of any type.
 		label : 'Calories',
 		group : 'Nutrition',
 		average : 123,
 		...
	},
	...
]
```

#### rows

Row metadata is similar but doesn't suppot _type_ property.

```javascript
rows: [
		{
 			// Any number of properties of any type
 			label : 'Pasta',
 			taste: 'Very tasty',
 			...
		},
		...
]
```

### renderers

Renderers draw your data using HTML5's _CanvasRenderingContext2d_. 

Config:
```javascript
renderers: [
  cell: [
  	'numeric' : {render: ...},
	'boolean' : {render: ...},
	'some other data type' : {render: ...}
  ], 
  columnMetadata: [
  	'label' : {render: ...},
  	'group' : {render: ...},
  	'average' : {render: ...},
  ]
  rowMetadata: [
  	'label' : {render: ...},
	'taste' : {render: ...}
  ]
]
```

If all columns are of the same type, _cell_ should be a single object and not an array:

```javascript
renderers: [
  cell: {
  	'default' : {render: ...},
  }
```

Each cell renderer is specified as:
```javascript
'data type name' : {
  render: function(canvasContext, drawingBoxSize, cellData, rowMetadata, columnMetadata) {...}
}
```

... insert diagram ...

 - canvasContext
 - drawingBoxSize -- this allows you to know how much space is available for your drawing, this is affected by zoom.
 - cellData
 - rowMetadata
 - columnMetadata

Each column or row renderer is specified as:
```javascript
'property X' : {
  renderer: function(canvasContext, drawingBoxSize, propertyValue)
}
```

__TODO__: describe each argument of renderer function.

### labelFormat

This configuration parameter allows you to describe the format of your labels. Label can be composed of multiple elements: text labels, small graphic elements such as mini bar and pie charts, etc.

```javascript
	labelFormat: {
    	row: [
            {name: 'label', size: 90},
            {name: 'average', size: 10},
            {name: 'group', size: 50}
        ],
        column: [
            {name: 'label', size: 100},
            {name: 'taste', size: 50}
        ]
    }
```
The order of elements in the array is the order label elements will be rendered in.
 
