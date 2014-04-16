/**
 * TODO: Rename to reflect new functionality
 * TODO: Namespace everything.
 */
/*
 bubbleEvents: [
 'label_in',
 'label_out',
 'label_click',
 'sub_label_in',
 'sub_label_out',
 'sub_label_click'
 ],
 */

Orientation = {HORIZONTAL: 1, VERTICAL: 2};

function Label(data, renderers, ctx, ctxOverlay) {
    this.data = data;
    this.ctx = ctx;
    this.ctxOverlay = ctxOverlay;
    this.renderers = renderers;
}

Label.prototype.highlight = function (width, height) {
    this.ctxOverlay.save();
    this.ctxOverlay.fillStyle = "rgba(200,0,0,0.1)";
    this.ctxOverlay.fillRect(0, 0, width, height);
    this.ctxOverlay.restore();
    this.width = width;
    this.height = height;
};

Label.prototype.clearHighlight = function () {
    this.ctxOverlay.save();
    this.ctxOverlay.clearRect(0, 0, this.width, this.height);
    this.ctxOverlay.restore();
};

// Label container should call this, it should position(translate) things.
Label.prototype.render = function (orientation, cellSize, subLabels) {
    var me = this;

    function centerHorizontally(blockWidth) {
        me.ctx.translate(cellSize.width / 2 + blockWidth / 2, 0);
    }

    function centerVertically(blockHeight) {
        me.ctx.translate(0, cellSize.height / 2 + blockHeight / 2);
    }

    function rotate() {
        me.ctx.rotate(-0.5 * Math.PI);
    }

    function finish() {
        me.ctx.restore();
    }

    this.ctx.save();
    if (orientation === Orientation.HORIZONTAL) {
        //centerVertically(8);
    } else {
        //centerHorizontally(8);
        rotate();
    }

    for (var i = 0; i < subLabels.length; i++) {
        var property = subLabels[i];
        var box;

        if (orientation === Orientation.HORIZONTAL) {
            box = {width: property.size, height: cellSize.height};
        } else {
            box = {width: property.size, height: cellSize.width};
        }

        this.ctx.save();
        this.renderers[property.name].render(this.ctx, box, this.data[property.name]);
        this.ctx.restore();
        this.ctx.translate(property.size, 0);
    }

    finish();
};

