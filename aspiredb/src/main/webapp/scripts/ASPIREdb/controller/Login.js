Ext.require([
    'ASPIREdb.view.LoginForm'
]);

Ext.define('ASPIREdb.controller.Login', {
    extend: 'Ext.app.Controller',

    views: [
        'LoginForm'
    ]
});