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
    /**
     * ToDo: Need to be changed once we implement more than one project
     * 
     */
    getActiveProjectName: function () {
        var name = '';
        this.store.each(function (record) {
            name=record.data.name;
            return false;
        });
        return name;
    },

    setActiveProject: function (project) {
        this.store.removeAll();
        this.store.add(project);
    }
});
