/**
 * 
 */
/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 */

Ext.require( [ 'Ext.Window', 'Matrix2Viz' ] );

/**
 * Show subject-phenotype clusters in a heatmap.
 */
Ext.define( 'ASPIREdb.view.SubjectPhenotypeHeatmapWindow', {
   /**
    * @memberOf ASPIREdb.view.SubjectPhenotypeHeatmapWindow
    */
   extend : 'Ext.Window',
   alias : 'widget.subjectPhenotypeHeatmapWindow',
   singleton : true,
   title : 'Subject Phenotype Heatmap',
   closable : true,
   closeAction : 'hide',
   width : 800,
   height : 600,
   layout : 'fit',
   bodyStyle : 'padding: 10px;',
   id : 'subjectPhenotypeHeatmapWindow',

   initComponent : function() {
      var ref = this;
      this.callParent();

   },

   exportButtonHandler : function() {
      if ( this.heatmap == null ) {
         return;
      }

      var canvas = this.heatmap.down( '#matrixCanvas' );
      if ( canvas == null ) {
         return;
      }

      var imgsrc = canvas.el.dom.toDataURL( 'image/png' );
      var strDownloadMime = "image/octet-stream";
      document.location.href = imgsrc.replace( "image/png", strDownloadMime );
   },

   draw : function(matrix) {

      var me = this;
      var matrixColumnMetadata = [];
      var matrixRowMetadata = [];

      for (var i = 0; i < matrix.columnNames.length; i++) {
         var type;
         var range = {};
         if ( matrix.columnNames[i].toLocaleLowerCase() == "gender"
            || matrix.columnNames[i].toLocaleLowerCase() == "family history" ) {
            type = 'gender';
         } else if ( new RegExp( "^age" ).test( matrix.columnNames[i].toLocaleLowerCase() )
            || matrix.columnNames[i].toLocaleLowerCase() == "hairwhorls"
            || matrix.columnNames[i].toLocaleLowerCase() == "phenotype" ) {
            type = 'count';
         } else {
            type = 'binary';
         }
         matrixColumnMetadata.push( {
            Phenotype : matrix.columnNames[i],
            type : type,
            range : range
         } );
      }

      for (var i = 0; i < matrix.rowNames.length; i++) {
         matrixRowMetadata.push( {
            Subject : matrix.rowNames[i]
         } );
      }

      var convertedData = me.convertData( matrix.matrix, matrixColumnMetadata );
      me.convertedData = convertedData;

      var order = h2m.ClusterHelper.produceClusteredOrder( convertedData );
      console.log( "number of columns " + matrix.columnNames.length + "; equals columnOrder.length? "
         + (matrix.columnNames.length == order.columnOrder.length) );

      var dataMatrix = {
         dimensions : {
            numberOfRows : matrix.rowNames.length,
            numberOfColumns : matrix.columnNames.length
         },
         getDataAt : function(rowIndex, columnIndex) {
            return matrix.matrix[rowIndex][columnIndex];
         }
      };

      M2V = {};
      M2V.Util = {};
      M2V.Util.dataType = {};

      // See PhenotypeGrid colors
      var colors = [ "#b35806", "#31a354", "#636363", "#d8b365", "#2c7fb8", "#addd8e", "#7570b3", "#a6bddb" ];
      colorNAIndex = 2;

      // a bit hard to match this with PhenotypeGrid because it depends on the
      // db value order
      M2V.Util.dataType.renderCountCell = function(ctx, value, row, column, size) {
         var color;
         var offset = 2;

         var idx = parseInt( value ) + offset;

         if ( !isNaN( idx ) ) {
            if ( idx >= colors.length ) {

               idx = colorNAIndex;
            }
         } else if ( value.length > 0 ) {
            // value is not an integer

            if ( me.valuesSeen == undefined ) {
               me.valuesSeen = [];
            }
            if ( me.valuesSeen.indexOf( value ) < 0 ) {
               me.valuesSeen.push( value );
            }

            idx = me.valuesSeen.indexOf( value );

         }

         ctx.fillStyle = colors[idx];
         ctx.fillRect( 1, 1, size.width - 2, size.height - 2 );
      };

      M2V.Util.dataType.renderGenderCell = function(ctx, value, row, column, size) {
         var color;
         value = value.toUpperCase();
         if ( value.toUpperCase() === "MALE" || value.toUpperCase() === "M" || value.toUpperCase() === "Y" ) {
            color = colors[4]; // blue
         } else if ( value.toUpperCase() === "FEMALE" || value.toUpperCase() === "F" || value.toUpperCase() === "N" ) {
            color = colors[3]; // yellow
         } else {
            color = colors[2]; // grey
         }

         ctx.fillStyle = color;
         ctx.fillRect( 1, 1, size.width - 2, size.height - 2 );
      };

      // TODO support data types with more than 2 values
      M2V.Util.dataType.renderAbsentPresentCell = function(ctx, value, row, column, size) {
         // TODO write a legend
         var color;
         value = value.toUpperCase();
         if ( value === 0 || value === "0" || value.toUpperCase() === "N" || value.toUpperCase() === "MALE"
            || value.toUpperCase() === "M" ) {
            color = colors[1]; // green
         } else if ( value === 1 || value === "1" || value.toUpperCase() === "Y" || value.toUpperCase() === "FEMALE"
            || value.toUpperCase() === "F" ) {
            color = colors[0]; // brown
         } else {
            color = colors[2]; // unknown
         }

         ctx.fillStyle = color;
         ctx.fillRect( 1, 1, size.width - 2, size.height - 2 );
      };

      // TODO: provide centering helper function
      var renderDefaults = {
         text : function(ctx, box, text) {
            ctx.translate( 0, box.height / 2 + 4 ); // fontSize
            ctx.fillText( text, 0, 0 );
         },
         label : function(ctx, box, text) {
            ctx.translate( 0, box.height / 2 + 4 ); // fontSize
            ctx.fillText( text, 0, 0 );
         },
         group : function(ctx, box, value) {
            ctx.translate( 0, box.height / 2 + 4 ); // fontSize
            ctx.fillText( value, 0, 0 );
         },
         type : function(ctx, box, value) {
            ctx.translate( 0, box.height / 2 + 4 ); // fontSize
            ctx.fillText( value, 0, 0 );
         },
         metaNumber : function(ctx, box, value) {
            ctx.fillStyle = 'rgb(0,100,0)';
            ctx.fillRect( 0, 1, box.width * value, box.height - 2 );
         }
      };

      var heatmap = Ext.create( 'Matrix2Viz', {
         renderTo : this.el,
         width : 900,
         height : 700,

         data : dataMatrix,
         labelFormat : {
            row : [ {
               name : 'Subject',
               size : 90
            },
            // {name: 'group', size: 50}
            ],
            column : [ {
               name : 'Phenotype',
               size : 100
            }, ]
         },
         renderers : {
            cell : {
               'count' : {
                  render : M2V.Util.dataType.renderCountCell
               },
               'gender' : {
                  render : M2V.Util.dataType.renderGenderCell
               },
               'binary' : {
                  render : M2V.Util.dataType.renderAbsentPresentCell
               },
               'numeric' : {
                  render : function(ctx, data, row, column, size) {
                     var red = Math.round( 255 * (data / (column.range.high - column.range.low)) );
                     ctx.fillStyle = "rgb(" + red + ",0,0)";
                     ctx.fillRect( 1, 1, size.width - 2, size.height - 2 );
                  }
               }
            },
            rowMetadata : {
               'Subject' : {
                  render : renderDefaults.text
               },
            },
            columnMetadata : {
               'Phenotype' : {
                  render : renderDefaults.text
               },
            }
         },

         // rows: rows, //sample_data.js
         rows : matrixRowMetadata,

         rowOrder : order.rowOrder,

         // columns: columns, //sample_data.js
         columns : matrixColumnMetadata,

         columnOrder : order.columnOrder,

         clustering : order,

         cellSize : {
            width : 20,
            height : 10
         },

         controlPanel : 'DefaultControlPanel',

         // TODO: add row/column options for both
         displayOptions : {
            showRowDendrogram : true,
            showColumnDendrogram : true,
            showRowLabels : true,
            showColumnLabels : true
         }
      } );

      this.heatmap = heatmap;

      var resizer = Ext.create( 'Ext.resizer.Resizer', {
         handles : 'all',
         target : heatmap
      } );

      var t = new Ext.ToolTip( {
         floating : {
            shadow : false
         },
         html : 'Events',
         hideDelay : 1,
         target : heatmap.el,
         closable : false,
         width : 400,
      } );
      t.show();

      heatmap
         .on( 'cell-mouse-in',
            function(index) {
               // t.update('Cell Enter: '+index.row+','+index.col);
               var colLabel = this.matrix.columns[this.matrix.colOrder[index.col]].Phenotype;
               var rowLabel = this.matrix.rows[this.matrix.rowOrder[index.row]].Subject;
               var cellValue = this.matrix.data.getDataAt( this.matrix.rowOrder[index.row],
                  this.matrix.colOrder[index.col] );
               t.update( "Subject : " + rowLabel + "<br/>" + "Phenotype : " + colLabel + "<br/>" + "Value : "
                  + cellValue );
               t.show();
            } );

      heatmap.on( 'cell-mouse-out', function() {
         // t.update('Cell Leave');
         t.show();
      } );

      heatmap.on( 'cell-mouse-click', function() {
         // t.update('Cell Click');
         t.show();
      } );

      heatmap.on( 'label-mouse-in', function(index) {
         // t.update('Label Enter: '+index.index+','+index.subIndex);
         if ( this.matrix.rows != null ) {
            if ( index.orientation == Orientation.HORIZONTAL ) {
               t.update( this.matrix.rows[this.matrix.rowOrder[index.index]].Subject );
            } else if ( index.orientation == Orientation.VERTICAL ) {
               t.update( this.matrix.columns[this.matrix.colOrder[index.index]].Phenotype );
            }
         }
         t.show();
      } );

      heatmap.on( 'label-mouse-out', function() {
         // t.update('Label leave: ');
         t.show();
      } );

      heatmap.on( 'label-mouse-click', function(index) {
         // t.update('Label click: '+index.index+','+index.subIndex);
         t.show();
      } );

      this.removeAll();
      this.add( heatmap );

      heatmap.draw();

   },

   bin : function(numberOfBins, value, range) {
      return Math.floor( value / ((range.high - range.low) / numberOfBins) );
   },

   encode : function(s) {
      var number = "";
      for (var i = 0; i < s.length; i++)
         number += s.charCodeAt( i ).toString();
      return number;
   },

   convertData : function(data, columns) {
      var converted = [];
      var valuesSeen = [];

      for (var i = 0; i < data.length; i++) {
         var row = data[i];
         var convertedRow = [];
         for (var j = 0; j < row.length; j++) {
            var value = row[j];
            var convertedValue = value;
            if ( value == null ) {
               continue;
            }
            if ( columns[j].type === 'gender' ) {
               if ( value.toUpperCase() === 'M' ) {
                  convertedValue = 1;
               } else if ( value.toUpperCase() === 'F' ) {
                  convertedValue = 0;
               } else {
                  convertedValue = 2;
               }
            } else if ( columns[j].type === 'numeric' ) {
               convertedValue = this.bin( 3, value, columns[j].range );
            } else if ( columns[j].type === 'binary' ) {
               if ( value.toUpperCase() === 'Y' || value === '1' ) {
                  convertedValue = 1;
               } else if ( value.toUpperCase() === 'N' || value === '0' ) {
                  convertedValue = 0;
               } else {
                  convertedValue = 2;
               }
            } else if ( columns[j].type === 'count' ) {

               // probably a factor
               if ( isNaN( parseInt( convertedValue ) ) ) {

                  if ( valuesSeen.indexOf( convertedValue ) < 0 ) {
                     valuesSeen.push( convertedValue );
                  }

                  convertedValue = valuesSeen.indexOf( convertedValue );
               }
            }
            convertedRow.push( convertedValue );
         }
         converted.push( convertedRow );
      }
      return converted;
   }

} );