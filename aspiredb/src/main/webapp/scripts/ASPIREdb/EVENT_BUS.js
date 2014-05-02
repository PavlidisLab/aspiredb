Ext.require( [ 'Ext.util.Observable' ] );

// Events: login, logout
Ext.define( 'ASPIREdb.EVENT_BUS', {
   singleton : true,
   extend : 'Ext.util.Observable'
} );
