/**
 * TODO: Rename. Refactor with VerticalLabelNames.
 */
Ext.define('HorizontalLabelNames', {
    alias: "widget.HorizontalLabelNames",
    extend: 'Ext.panel.Panel',
    layout: 'absolute',
    items: [
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'canvas',
            x: 0,
            y: 0,
            style: {
                'z-index': '0'
            }
        },
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'canvasOverlay',
            x: 0,
            y: 0,
            style: {
                'z-index': '1'
            }
        }
    ],

    config: {
        labelVisibleLength: null,
        propertiesToRender: []
    },

    initComponent: function () {
        this.callParent(arguments);
    },

    afterRender: function () {
        this.callParent(arguments);

        this.canvas = this.getComponent("canvas").getEl();
        this.ctx = this.canvas.dom.getContext("2d");
        this.canvasOverlay = this.getComponent("canvasOverlay").getEl();
        this.ctxOverlay = this.canvasOverlay.dom.getContext("2d");
    },

    refreshCanvasSize: function () {
        this.canvas.dom.width = this.getWidth();
        this.canvas.dom.height = this.getLabelVisibleLength();
        this.canvasOverlay.dom.width = this.getWidth();
        this.canvasOverlay.dom.height = this.getLabelVisibleLength();
    },

    /**
     *
     */
    draw: function () {
        this.refreshCanvasSize();

        this.ctx.save();
        this.ctx.translate(0, this.getLabelVisibleLength());
        this.ctx.fillStyle = "black";
        for (var i = 0; i < this.propertiesToRender.length; i++) {
            var property = this.propertiesToRender[i];
            this.ctx.translate(0, -(property.size / 2 + 4));  //fontSize
            this.ctx.fillText(property.name, 0, 0);
            this.ctx.translate(0, -(property.size / 2 - 4));  //fontSize
        }
        this.ctx.restore();
    }

});

