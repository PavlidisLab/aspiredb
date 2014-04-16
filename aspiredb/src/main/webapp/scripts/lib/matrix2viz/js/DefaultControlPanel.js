/**
 *
 */
Ext.define('DefaultControlPanel', {
    alias: "widget.DefaultControlPanel",
    extend: 'Ext.panel.Panel',
    width: 100,
    height: 100,
    border: false,
    layout: 'vbox',
    config: {
        matrix2viz: null
    },

    initComponent: function () {
        this.items = [
            {
                xtype: 'panel',
                layout: 'hbox',
                width: 95,
                height: 35,
                items: [
                    {
                        xtype: 'button',
                        text: '5',
                        width: 20,
                        height: 20,
                        handler: function() {
                            this.updateCellSizes({width: 5, height: 5});
                        },
                        scope: this.getMatrix2viz()
                    },
                    {
                        xtype: 'button',
                        text: '10',
                        width: 20,
                        height: 20,
                        handler: function() {
                            this.updateCellSizes({width: 10, height: 10});
                        },
                        scope: this.getMatrix2viz()
                    },
                    {
                        xtype: 'button',
                        text: '15',
                        width: 20,
                        height: 20,
                        handler: function() {
                            this.updateCellSizes({width: 15, height: 15});
                        },
                        scope: this.getMatrix2viz()
                    },
                    {
                        xtype: 'button',
                        text: 'Fit',
                        width: 30,
                        height: 20,
                        handler: function() {
                            this.fitToScreen();
                        },
                        scope: this.getMatrix2viz()
                    }
                ]
            }

/*
 {
 xtype: 'button',
 itemId: 'zoomButton',
 enableToggle: true,
 text: 'Zoom',
 width: 70,
 height: 20,
 toggleHandler: function (button, state) {
 if (state) {
 this.updateCellSizes({width: 5, height: 5});
 } else {
 this.updateCellSizes({width: 15, height: 15})
 }
 },
 scope: this.getMatrix2viz()
 }
 */
        ];
        this.callParent(arguments);
    }
});