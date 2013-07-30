Ext.require([
    'ASPIREdb.view.FilterWindow',
    'ASPIREdb.controller.FilterWindow'
]);

Ext.application({
    name: 'ASPIREdb',
    appFolder: 'ASPIREdb',
    controllers: ['FilterWindow'],

    launch: function () {
        var win = Ext.create('widget.filterwindow');
        win.show();
    }
});

