
Ext.define('ASPIREdb.view.ideogram.ColourLegend', {
    extend: 'Ext.window.Window',
    alias: 'widget.colourlegend',
    width: 200,
    height: 200,
    autoScroll: true,
    title: 'Ideogram',
    closable: false,
    resizable : true,
    minimizable : true,
    layout: 'absolute',
    items: [
        {
            xtype: 'component',
            autoEl: 'canvas',
            itemId: 'canvasBox',
            x: 0,
            y: 0,
            width: 200,
            height: 200
        }
    ]
});