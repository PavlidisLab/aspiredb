Ext.define( 'ASPIREdb.view.filter.PhenotypeFilter', {
   extend : 'Ext.Container',
   alias : 'widget.filter_phenotype_property',

   layout : 'hbox',
   width : 850,
   /**
    * @private
    * @override
    */
   initComponent : function() {
      this.items = [ {
         xtype : 'combo',
         itemId : 'nameCombo',
         emptyText : 'phenotype',
         width : 400,
         matchFieldWidth : false,
         triggerAction : 'query',
         // autoSelect: true,
         hideTrigger : true,
         displayField : 'displayName',
         store : Ext.create( 'ASPIREdb.PhenotypeSuggestionStore', {
            remoteFunction : PhenotypeService.suggestPhenotypes
         } ),
         listConfig : {
            loadingText : 'Searching...',
            emptyText : 'No results.'
         },
         listeners : {
            select : {
               fn : function( obj, records ) {
                  var record = records[0];
                  var valueCombo = this.getComponent( 'valueCombo' );
                  valueCombo.clearValue();
                  valueCombo.lastQuery = null;
                  valueCombo.getStore().setProperty( record.raw );
                  ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
               },
               scope : this
            },
         }
      }, {
         xtype : 'combo',
         itemId : 'valueCombo',
         emptyText : 'value',
         displayField : 'displayValue',
         triggerAction : 'query',
         minChars : 0,
         matchFieldWidth : false,
         hideTrigger : true,
         autoSelect : true,
         enableKeyEvents : true,
         store : Ext.create( 'ASPIREdb.ValueSuggestionStore', {
            remoteFunction : PhenotypeService.suggestPhenotypeValues
         } ),
         listeners : {
            select : {
               fn : function( obj, records ) {
                  ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
               },
               scope : this,
            }
         },
         listConfig : {
            loadingText : 'Searching...',
            emptyText : 'No results.'
         }
      }, {
         xtype : 'button',
         itemId : 'removeButton',
         text : 'X'
      } ];

      this.callParent();

      this.getComponent( "removeButton" ).on( 'click', function( button, event ) {
         // TODO: fix with custom events
         var item = button.ownerCt;
         var filterContainer = item.ownerCt;
         filterContainer.remove( item );
         filterContainer.doLayout();

         if ( filterContainer.ownerCt.closeEmptyFilter )
            filterContainer.ownerCt.closeEmptyFilter();

         ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
      } );

   },

   getRestrictionExpression : function() {
      var nameCombo = this.getComponent( "nameCombo" );
      var valueCombo = this.getComponent( "valueCombo" );

      var phenotypeRestriction = new PhenotypeRestriction();
      phenotypeRestriction.name = nameCombo.getValue();
      phenotypeRestriction.value = valueCombo.getValue();
      return phenotypeRestriction;
   },

   setRestrictionExpression : function( phenotypeRestriction ) {
      var nameCombo = this.getComponent( "nameCombo" );
      var valueCombo = this.getComponent( "valueCombo" );
      nameCombo.setValue( phenotypeRestriction.name );
      valueCombo.setValue( phenotypeRestriction.value );
   }
} );