Ext.require( [ 'Ext.layout.container.*' ] );

Ext.define( 'ASPIREdb.view.filter.FilterPanel', {
   extend : 'Ext.Panel',
   closable : true,
   collapsible : true,
   header : false,
   width : 950,
   padding : '10 5 10 5',
   layout : {
      type : 'vbox',
      align : 'stretch'
   },

   initComponent : function() {
      this.callParent();

      var ref = this;

      this.insert( 0, {
         xtype : 'container',
         layout : {
            type : 'hbox',
            align : 'right'
         },
         items : [ {
            xtype : 'label',
            width : 900,
            text : this.title,
            padding : '5 5 5 5'
         }, {
            xtype : 'image',
            itemId : 'closeImage',
            margin : '5 5 5 5',
            cls : 'margin-right:auto',

            src : 'scripts/ASPIREdb/resources/images/icons/cross.png',
            listeners : {
               render : function(c) {
                  c.getEl().on( 'click', function(e) {
                     ref.close();
                     ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
                  }, c );
               }
            }

         } ]
      } );

   },

   closeEmptyFilter : function() {
      var filterContainer = this.getComponent( 'filterContainer' );

      if ( filterContainer.items.length == 0 ) {
         this.close();
      }

      ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
   }
} );
