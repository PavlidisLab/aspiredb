
Ext.define('ASPIREdb.view.ideogram.ColourLegend', {
    extend: 'Ext.window.Window',
    alias: 'widget.colourlegend',
    width: 200,
    height: 200,
    autoScroll: true,
    title: 'Ideogram',
    closable: false,
    resizable : true, 
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
    ],
tools: [
        {  
            type: 'restore',
            hidden : true,
            handler: function( evt,toolEl,owner,tool ) {
                var window = owner.up( 'window' );
                window.expand('',false);
             window.setWidth(winWidth);
                window.center();
                isMinimized = false;
                this.hide();
                this.nextSibling().show();
            }                                
        },{  
            type: 'minimize',
            handler: function( evt,toolEl,owner,tool ){
                var window = owner.up( 'window' );
                window.collapse();
                winWidth = window.getWidth();
                window.setWidth( 150 );
                window.alignTo( Ext.getBody(), 'bl-bl');
                this.hide();
                this.previousSibling().show();
                isMinimized = true;
            }                                
        }                            
    ]
});