Ext.require([
    'Ext.util.Observable',
    'ASPIREdb.model.Project'
]);

// Events: login, logout
Ext.define('ASPIREdb.ActiveProjectSettings', {
    extend: 'Ext.util.Observable',
    singleton: true,

    constructor: function () {
        this.store = Ext.create('Ext.data.Store', {
            model: 'ASPIREdb.model.Project',
            proxy: {
                type: 'localstorage'
            }
        });

        this.setActiveProject([{id:1, name:'', description:''}]);

        this.callParent(arguments);
    },

    getActiveProjectIds: function () {
        var ids = [];
        this.store.each(function (record) {
            ids.push(record.data.id);
            return false;
        });
        return ids;
    },

    setActiveProject: function (project) {
        this.store.removeAll();
        this.store.add(project);
    }
});
