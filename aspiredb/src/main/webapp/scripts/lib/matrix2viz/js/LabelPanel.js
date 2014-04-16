/**
 *
 * TODO: Think of supporting interface similar to Grid?
 */

Ext.define('LabelPanel', {
    alias: "widget.LabelPanel",
    extend: 'Ext.panel.Panel',
    layout: 'absolute',
    items: [
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'labelCanvas',
            x: 0,
            y: 0,
            style: {
                'z-index': '0'
            }
        },
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'labelCanvasOverlay',
            x: 0,
            y: 0,
            style: {
                'z-index': '1'
            }
        }
    ],

    bubbleEvents: [
        'label-mouse-in',
        'label-mouse-out',
        'label-mouse-click'
    ],

    config: {
        labelItems: null,
        order: null,
        orientation: null,
        leftX: 0,
        topY: 0,
        labelVisibleLength: null,
        cellSize: null,
        renderers: null,
        subLabels: []
    },

    initComponent: function () {
        this.highlightedLabel = null;
        this.lastLabel = null;
        this.callParent(arguments);
    },

    initListeners: function () {
        var me = this;
        this.canvasOverlay.on('mousemove', function (event) {
            var scrollTop = me.body.getScrollTop();
            var scrollLeft = me.body.getScrollLeft();
            var x = event.browserEvent.pageX + scrollLeft - me.body.getX();
            var y = event.browserEvent.pageY + scrollTop - me.body.getY();
            me.onMouseMove(x, y);
        });

        this.canvasOverlay.on('mouseout', function (event) {
            me.onMouseMove(-1, -1);
        });

        this.canvasOverlay.on('click', function (event) {
            var scrollTop = me.body.getScrollTop();
            var scrollLeft = me.body.getScrollLeft();
            var x = event.browserEvent.pageX + scrollLeft - me.body.getX();
            var y = event.browserEvent.pageY + scrollTop - me.body.getY();
            var index = me.getLabel(x,y);
            if (index != null) me.fireEvent('label-mouse-click', index);
        });
    },

    getLabel: function (x, y) {
        var index, subIndex;
        if (this.orientation === Orientation.HORIZONTAL) {
            index = Math.floor(y / this.cellSize.height);
            var xEnd = 0;
            for (var i = 0; i < this.subLabels.length; i++) {
                var property = this.subLabels[i];
                xEnd = xEnd + property.size;
                if (x < xEnd) {
                    subIndex = i;
                    break;
                }
            }
        } else {
            index = Math.floor(x / this.cellSize.width);
            var yEnd = this.labelVisibleLength;
            for (var i = 0; i < this.subLabels.length; i++) {
                var property = this.subLabels[i];
                yEnd = yEnd - property.size;
                if (y > yEnd) {
                    subIndex = i;
                    break;
                }
            }
        }

        if (index >= this.labelItems.length || index < 0) {
            return null;
        }

        return {
            index: index,
            subIndex: subIndex
        };
    },

    onMouseMove: function (x, y) {
        var label = this.getLabel(x, y);

        if ((this.lastLabel == null && label == null)
            || (this.lastLabel != null && label != null && this.lastLabel.index == label.index && this.lastLabel.subIndex == label.subIndex)) {
            return;
        }

        if (this.lastLabel != null) {
            this.clearHighlight(this.lastLabel.index, this.getCellSize());
            this.fireEvent('label-mouse-out', this.lastLabel);
        }

        this.lastLabel = label;
        if (label != null) {
            this.highlight(label.index, this.getCellSize());
            this.fireEvent('label-mouse-in', label);
        }
    },

    afterRender: function () {
        this.callParent(arguments);

        this.canvas = this.getComponent("labelCanvas").getEl();
        this.ctx = this.canvas.dom.getContext("2d");
        this.canvasOverlay = this.getComponent("labelCanvasOverlay").getEl();
        this.ctxOverlay = this.canvasOverlay.dom.getContext("2d");

        var labels = [];
        this.getLabelItems().forEach(function (item) {
            labels.push(new Label(item, this.getRenderers(), this.ctx, this.ctxOverlay));
        }, this);

        this.labels = labels;

        this.initListeners();
    },

    getScrollbarSize: function () {
        if (!this.scrollbarSize) {
            var db = document.body,
                div = document.createElement('div');

            div.style.width = div.style.height = '100px';
            div.style.overflow = 'scroll';
            div.style.position = 'absolute';

            db.appendChild(div); // now we can measure the div...

            // at least in iE9 the div is not 100px - the scrollbar size is removed!
            this.scrollbarSize = {
                width: div.offsetWidth - div.clientWidth,
                height: div.offsetHeight - div.clientHeight
            };

            db.removeChild(div);
        }
        return this.scrollbarSize;
    },

    refreshCanvasSize: function () {
        if (this.getOrientation() === Orientation.HORIZONTAL) {
            this.canvas.dom.width = this.getLabelVisibleLength();
            this.canvas.dom.height = this.labels.length * this.getCellSize().height + this.getScrollbarSize().height;
            this.canvasOverlay.dom.width = this.getLabelVisibleLength();
            this.canvasOverlay.dom.height = this.labels.length * this.getCellSize().height + this.getScrollbarSize().height;
        } else {
            this.canvas.dom.width = this.labels.length * this.getCellSize().width + this.getScrollbarSize().width;
            this.canvas.dom.height = this.getLabelVisibleLength();
            this.canvasOverlay.dom.width = this.labels.length * this.getCellSize().width + this.getScrollbarSize().width;
            this.canvasOverlay.dom.height = this.getLabelVisibleLength();
        }
    },

    clearHighlight: function (index, cellSize) {
        var width, height;
        this.ctxOverlay.save();
        this.ctxOverlay.translate(this.getLeftX(), this.getTopY());
        if (this.orientation === Orientation.HORIZONTAL) {
            this.ctxOverlay.translate(0, cellSize.height * index);
            width = this.getLabelVisibleLength();
            height = cellSize.height;
        } else {
            this.ctxOverlay.translate(cellSize.width * index, 0);
            width = cellSize.width;
            height = this.getLabelVisibleLength();
        }
        this.labels[index].clearHighlight(width, height);
        this.ctxOverlay.restore();
    },

    highlight: function (index, cellSize) {
        if (this.highlightedLabel != null) {
            this.highlightedLabel.clearHighlight();
        }
        var width, height;
        this.ctxOverlay.save();
        this.ctxOverlay.translate(this.getLeftX(), this.getTopY());
        if (this.orientation === Orientation.HORIZONTAL) {
            this.ctxOverlay.translate(0, cellSize.height * index);
            width = this.getLabelVisibleLength();
            height = cellSize.height;
        } else {
            this.ctxOverlay.translate(cellSize.width * index, 0);
            width = cellSize.width;
            height = this.getLabelVisibleLength();
        }
        this.labels[index].highlight(width, height);
        this.ctxOverlay.restore();

        this.highlightedLabel = this.labels[index];
    },

    updateCellSizes: function (cellSize) {
        this.cellSize = cellSize;
    },

    scrollTop: function (offset) {
        this.canvasOverlay.dom.marginTop = -1 * offset;
        this.canvas.dom.scrollTop.marginTop = -1 * offset;
    },

    /**
     *
     */
    draw: function () {
        this.refreshCanvasSize();

        var me = this;

        function moveToNextPosition() {
            if (me.getOrientation() === Orientation.HORIZONTAL) {
                me.ctx.translate(0, me.getCellSize().height);
            } else {
                me.ctx.translate(me.getCellSize().width, 0);
            }
        }

        this.ctx.save();

        if (me.getOrientation() == Orientation.HORIZONTAL) {
            this.ctx.translate(0, 0);
        } else {
            this.ctx.translate(0, me.getLabelVisibleLength() - 5);
        }

        this.getOrder().forEach(function drawLabel(index) {
            var label = me.labels[index];
            label.render(me.getOrientation(), me.getCellSize(), me.getSubLabels());
            moveToNextPosition();
        });
        this.ctx.restore();
    }

});

