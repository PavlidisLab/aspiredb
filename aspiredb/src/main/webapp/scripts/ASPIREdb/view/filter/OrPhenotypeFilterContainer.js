Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.PropertyFilter' ] );

/*
 * 
 * This could be combined with OrFilterContainer with a little re-jiggering making a new file for now because it is a
 * little simpler and because of time constraints
 * 
 * 
 */
Ext.define( 'ASPIREdb.view.filter.OrPhenotypeFilterContainer', {
   extend : 'ASPIREdb.view.filter.FilterPanel',
   alias : 'widget.filter_or_pheno',
   closable : true,
   title : 'OR Filter',
   width : 910,
   layout : {
      type : 'vbox'
   },

   /*
    * border: 1, style: { border: "1px solid lightgray" },
    */
   getRestrictionExpression : function() {
      var filterContainer = this.getComponent( 'filterContainer' );
      return filterContainer.getRestrictionExpression();
   },

   setRestrictionExpression : function(restriction) {
      var filterContainer = this.getComponent( 'filterContainer' );

      filterContainer.removeAll();

      filterContainer.setRestrictionExpression( restriction );

   },

   initComponent : function() {
      var me = this;
      this.items = [ {
         xtype : 'container',
         itemId : 'filterContainer',
         layout : {
            type : 'vbox',
            defaultMargins : {
               top : 5,
               right : 5,
               left : 5,
               bottom : 5
            }
         },
         getRestrictionExpression : function() {
            var disjunction = new Disjunction();
            disjunction.restrictions = [];
            this.items.each( function(item, index, length) {
               disjunction.restrictions.push( item.getRestrictionExpression() );
            } );
            return disjunction;
         },

         setRestrictionExpression : function(restriction) {

            var filterContainer = me.getComponent( "filterContainer" );

            if ( restriction instanceof Disjunction ) {

               for (var i = 0; i < restriction.restrictions.length; i++) {

                  var filter = Ext.create( 'ASPIREdb.view.filter.PhenotypeFilter' );

                  filter.setRestrictionExpression( restriction.restrictions[i] );

                  filterContainer.add( filter );

               }

            }

         },

         items : [ {
            xtype : 'panel',
            border : 0,
            layout : {
               type : 'hbox',
               defaultMargins : {
                  // top : 5,
                  right : 5,
               // left : 5,
               // bottom : 5
               }
            },
            items : [ Ext.create( 'ASPIREdb.view.filter.PhenotypeFilter' ), {
               xtype : 'button',
               itemId : 'addButton',
               // padding : '5 5 5 5',
               // icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
               text : 'OR'
            } ]
         } ]

      } ];

      this.callParent();

      me.down( "#addButton" ).on( 'click', function(button, event) {
         var filterContainer = me.getComponent( "filterContainer" );
         filterContainer.add( Ext.create( 'ASPIREdb.view.filter.PhenotypeFilter' ) );
         filterContainer.doLayout();
      } );

   }
} );
