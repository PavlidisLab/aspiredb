/**
 *
 * @param cellData
 * @param cellRow
 * @param cellColumn
 * @param ctx
 * @param ctxOverlay
 * @param position
 * @param size
 * @param renderFn
 * @constructor
 */
function Cell(cellData, cellRow, cellColumn, ctx, ctxOverlay, position, size, renderFn) {
    this.ctx = ctx;
    this.ctxOverlay = ctxOverlay;
    this.data = cellData;
    this.row = cellRow;
    this.column = cellColumn;
    this.position = position;
    this.size = size;
    this.renderCellContent = renderFn;
}

Cell.prototype.draw = function () {
    this.ctx.save();
    this.ctx.translate(this.position.x, this.position.y);
    // TODO: getCellData -> data?
    this.renderCellContent(this.ctx, this.data, this.row, this.column, this.size);
    this.ctx.restore()
};

Cell.prototype.highlight = function () {
    this.ctxOverlay.save();
    this.ctxOverlay.translate(this.position.x, this.position.y);
    this.renderCellContentHighlight(this.ctxOverlay, this.data, this.row, this.column, this.size);
    this.ctxOverlay.restore()
};

Cell.prototype.clearHighlight = function () {
    this.ctxOverlay.save();
    this.ctxOverlay.translate(this.position.x, this.position.y);
    this.ctxOverlay.clearRect(0, 0, this.size.width, this.size.height);
    this.ctxOverlay.restore()
};

/*
Cell.prototype.renderCellContent = function (ctx, data, row, column, size) {
    if (column.type === 'gender') {
        M2V.Util.dataType.renderGenderCell(ctx, data, size);
    } else if (column.type === 'binary') {
        M2V.Util.dataType.renderAbsentPresentCell(ctx, data, size);
    } else {
        var red = Math.round(255 * (data / (column.range.high - column.range.low)));
        ctx.fillStyle = "rgb(" + red + ",0,0)";
        ctx.fillRect(1, 1, size.width - 2, size.height - 2);
    }
};
*/

Cell.prototype.renderCellContentHighlight = function (ctx, data, row, column, size) {
    ctx.strokeStyle = "rgb(0,0,0)";
    ctx.lineWidth = 1;
    ctx.strokeRect(0.5, 0.5, size.width - 1, size.height - 1);
};
