/**
 *
 */
Ext.define('Matrix', {
    alias: "widget.Matrix",
    extend: 'Ext.panel.Panel',
    autoScroll: true,
    layout: 'absolute',
    items: [
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'matrixCanvas',
            x: 0,
            y: 0,
            style: {
                'z-index': '0'
            }
        },
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'matrixCanvasOverlay',
            x: 0,
            y: 0,
            style: {
                'z-index': '1'
            }
        }
    ],
    bubbleEvents: [
        'cell-mouse-in',
        'cell-mouse-out',
        'cell-mouse-click'
    ],
    config: {
        /**
        *  @type {{dimensions: {numberOfRows: Number, numberOfColumns: Number}, getDataAt: Function}}
        */
        data: null,
        renderers: null,
        rows: null,
        rowOrder: null,
        columns: null,
        colOrder: null,
        cellSize: null
    },

    /**
     * @override
     */
    initComponent: function () {
        this.lastCellIndex = null;
        this.cells = [];
        this.numberOfRows = this.getData().dimensions.numberOfRows;
        this.numberOfColumns = this.getData().dimensions.numberOfColumns;

        this.calculateMatrixSize();
/*
        this.setWidth(this.getScreenSize().width);
        this.setHeight(this.getScreenSize().height);
*/

        this.callParent(arguments);
    },

    /**
     *
     * @override
     */
    afterRender: function () {
        this.callParent(arguments);

        this.matrixCanvas = this.getComponent("matrixCanvas").getEl();
        this.matrixCanvasOverlay = this.getComponent("matrixCanvasOverlay").getEl();

        this.ctx = this.matrixCanvas.dom.getContext("2d");
        this.ctxOverlay = this.matrixCanvasOverlay.dom.getContext("2d");

        this.initListeners();
    },

    /**
     * @private
     */
    refreshCanvasSize: function () {
        this.matrixCanvas.dom.width = this.matrixSize.width;
        this.matrixCanvas.dom.height = this.matrixSize.height;

        this.matrixCanvasOverlay.dom.width = this.matrixSize.width;
        this.matrixCanvasOverlay.dom.height = this.matrixSize.height;
    },

    /**
     */
    initListeners: function () {
        var me = this;
        this.matrixCanvasOverlay.on('mousemove', function (event) {
            var scrollTop = me.body.getScrollTop();
            var scrollLeft = me.body.getScrollLeft();
            var x = event.browserEvent.pageX + scrollLeft - me.body.getX();
            var y = event.browserEvent.pageY + scrollTop - me.body.getY();
            me.onMouseMove(x, y);
        });

        this.matrixCanvasOverlay.on('mouseout', function (event) {
            me.onMouseMove(-1, -1);
        });

        this.matrixCanvasOverlay.on('click', function (event) {
            var scrollTop = me.body.getScrollTop();
            var scrollLeft = me.body.getScrollLeft();
            var x = event.browserEvent.pageX + scrollLeft - me.body.getX();
            var y = event.browserEvent.pageY + scrollTop - me.body.getY();

            var index = me.getCellIndex(x, y);
            if (index == null) {
                return;
            }
            me.fireEvent('cell-mouse-click', index);
        });


    },

    /**
     *
     * @param x
     * @param y
     */
    onMouseMove: function (x, y) {
        var index = this.getCellIndex(x, y);

        if ((this.lastCellIndex == null && index == null)
            || (this.lastCellIndex != null && index != null && this.lastCellIndex.row == index.row && this.lastCellIndex.col == index.col)) {
            return;
        }

        if (this.lastCellIndex != null) {
            var lastCell = this.cells[this.lastCellIndex.row][this.lastCellIndex.col];
            lastCell.clearHighlight();
            this.fireEvent('cell-mouse-out', this.lastCellIndex);
        }

        this.lastCellIndex = index;

        if (index == null) {
            return;
        }

        var cell = this.cells[index.row][index.col];
        cell.highlight();
        this.fireEvent('cell-mouse-in', index);
    },

    /**
     *
     * @param x
     * @param y
     * @returns {{row: number, col: number}}
     */
    getCellIndex: function (x, y) {
        var rowIndex = Math.floor(y / this.cellSize.height);
        var colIndex = Math.floor(x / this.cellSize.width);
        if (rowIndex < 0 || colIndex < 0) return null;
        if (rowIndex >= this.numberOfRows || colIndex >= this.numberOfColumns) return null;
        return {
            row: rowIndex,
            col: colIndex
        };
    },

    setCellSize: function (cellSize) {
        this.cellSize = cellSize;
        this.calculateMatrixSize();
    },

    calculateMatrixSize: function () {
        this.matrixSize = {
            width: this.numberOfColumns * this.getCellSize().width,
            height: this.numberOfRows * this.getCellSize().height
        };
    },

    getPixelSize: function() {
        return this.matrixSize;
    },

    /**
     *
     */
    draw: function () {
        this.refreshCanvasSize();
        this.lastCellIndex = null;
        this.cells = [];

        for (var displayRowIndex = 0; displayRowIndex < this.numberOfRows; displayRowIndex++) {
            var rowIndex = this.rowOrder[displayRowIndex];
            var y = displayRowIndex * this.cellSize.height;
            var rowOfCells = [];
            this.cells.push(rowOfCells);
            for (var displayColIndex = 0; displayColIndex < this.numberOfColumns; displayColIndex++) {
                var colIndex = this.colOrder[displayColIndex];
                var x = displayColIndex * this.cellSize.width;
                var cellData = this.getData().getDataAt(rowIndex,colIndex);
                var cellRow = this.rows[rowIndex];
                var cellColumn = this.columns[colIndex];
                var cellType = cellColumn.type || 'default';
                var renderFn = this.getRenderers()[cellType].render;
                var cell = new Cell(cellData, cellRow, cellColumn, this.ctx,
                    this.ctxOverlay, {x: x, y: y}, this.cellSize, renderFn);
                rowOfCells.push(cell);
                cell.draw();
            }
        }
    }
});



