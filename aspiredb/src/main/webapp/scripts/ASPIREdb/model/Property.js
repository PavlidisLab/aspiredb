/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 19/08/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
Ext.define('ASPIREdb.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'displayName',  type: 'string'},
        {name: 'exampleValues',   type: 'int'},
        {name: 'operators', type: 'array'}
    ]
});