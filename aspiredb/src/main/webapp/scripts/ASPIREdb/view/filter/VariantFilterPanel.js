Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.AndFilterContainer',
              'ASPIREdb.view.filter.OrVariantFilterContainer', 'ASPIREdb.view.filter.FilterPanel' ] );

Ext.define( 'ASPIREdb.view.filter.VariantFilterPanel', {
   extend : 'ASPIREdb.view.filter.FilterPanel',
   alias : 'widget.filter_variant',
   title : 'Variant Filter',
   bodyStyle : 'background: #FFFFC2;',
   items : [ {
      xtype : 'filter_and',
      title : 'Variant Location:',
      itemId : 'variantFilterContainer',
      filterItemType : 'ASPIREdb.view.filter.OrVariantFilterContainer',
      suggestValuesRemoteFunction : VariantService.suggestValues,
      propertyStore : {
         // autoLoad: true,
         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestVariantLocationProperties,
            // dwrParam : ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0],
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }

         },
      }
   }, /**
       * { xtype : 'label', text : 'Variant characteristics:', padding : '5 5 5 5', }, { xtype : 'panel', itemId :
       * 'cnvFilterPanel', bodyStyle : 'background: #FFFFC2;', title : 'CNV:', collapsible : true, collapsed : true,
       * animCollapse : false, getRestrictionExpression : function() { var filterContainer = this.getComponent(
       * 'cnvCharacteristicFilterContainer' );
       * 
       * var cnvRestrictionExpression = filterContainer.getRestrictionExpression();
       * 
       * var variantRestriction = new VariantTypeRestriction();
       * 
       * variantRestriction.type = "CNV";
       * 
       * cnvRestrictionExpression.restrictions.push( variantRestriction );
       * 
       * return cnvRestrictionExpression; }, setRestrictionExpression : function(restriction) { // and filter container
       * var filterContainer = this.getComponent( 'cnvCharacteristicFilterContainer' );
       * filterContainer.setRestrictionExpression( restriction ); }, items : { xtype : 'filter_and', itemId :
       * 'cnvCharacteristicFilterContainer', suggestValuesRemoteFunction : VariantService.suggestValues, propertyStore : { //
       * autoLoad : true, proxy : { type : 'dwr', dwrFunction : VariantService.suggestPropertiesForVariantType,
       * dwrParams : [ 'CNV' ], model : 'ASPIREdb.model.Property', reader : { type : 'json', root : 'data',
       * totalProperty : 'count' } } }, filterItemType : 'ASPIREdb.view.filter.PropertyFilter' } }, { xtype : 'panel',
       * itemId : 'indelFilterPanel', bodyStyle : 'background: #FFFFC2;', title : 'Indel:', collapsible : true,
       * collapsed : true, animCollapse : false, getRestrictionExpression : function() {
       * 
       * var filterContainer = this.getComponent( 'indelCharacteristicFilterContainer' );
       * 
       * var indelRestrictionExpression = filterContainer.getRestrictionExpression();
       * 
       * var variantRestriction = new VariantTypeRestriction();
       * 
       * variantRestriction.type = "INDEL";
       * 
       * indelRestrictionExpression.restrictions.push( variantRestriction );
       * 
       * return indelRestrictionExpression; }, setRestrictionExpression : function(restriction) { // and filter
       * container var filterContainer = this.getComponent( 'indelCharacteristicFilterContainer' );
       * filterContainer.setRestrictionExpression( restriction ); }, items : { xtype : 'filter_and', itemId :
       * 'indelCharacteristicFilterContainer', suggestValuesRemoteFunction : VariantService.suggestValues, propertyStore : { //
       * autoLoad : true, proxy : { type : 'dwr', dwrFunction : VariantService.suggestPropertiesForVariantType,
       * dwrParams : [ 'INDEL' ], model : 'ASPIREdb.model.Property', reader : { type : 'json', root : 'data',
       * totalProperty : 'count' } } }, filterItemType : 'ASPIREdb.view.filter.PropertyFilter' } }, { xtype : 'panel',
       * itemId : 'snvFilterPanel', bodyStyle : 'background: #FFFFC2;', title : 'SNV:', collapsible : true, collapsed :
       * true, animCollapse : false, getRestrictionExpression : function() { var filterContainer = this.getComponent(
       * 'snvCharacteristicFilterContainer' );
       * 
       * var snvRestrictionExpression = filterContainer.getRestrictionExpression();
       * 
       * var variantRestriction = new VariantTypeRestriction();
       * 
       * variantRestriction.type = "SNV";
       * 
       * snvRestrictionExpression.restrictions.push( variantRestriction );
       * 
       * return snvRestrictionExpression; }, setRestrictionExpression : function(restriction) { // and filter container
       * var filterContainer = this.getComponent( 'snvCharacteristicFilterContainer' );
       * filterContainer.setRestrictionExpression( restriction ); }, items : { xtype : 'filter_and', itemId :
       * 'snvCharacteristicFilterContainer', suggestValuesRemoteFunction : VariantService.suggestValues, propertyStore : { //
       * autoLoad : true, proxy : { type : 'dwr', dwrFunction : VariantService.suggestPropertiesForVariantType,
       * dwrParams : [ 'SNV' ], model : 'ASPIREdb.model.Property', reader : { type : 'json', root : 'data',
       * totalProperty : 'count' } } }, filterItemType : 'ASPIREdb.view.filter.PropertyFilter' } }
       */

   ],

   getFilterConfig : function() {
      // var cnvFilterPanel = this.getComponent( 'cnvFilterPanel' );
      // var indelFilterPanel = this.getComponent( 'indelFilterPanel' );
      // var snvFilterPanel = this.getComponent( 'snvFilterPanel' );
      var config = new VariantFilterConfig();
      var conjunction = new Conjunction();
      conjunction.restrictions = [];

      var locationConjunction = new Conjunction();
      locationConjunction.restrictions = [];

      var variantFilterContainer = this.getComponent( 'variantFilterContainer' );

      conjunction.restrictions.push( variantFilterContainer.getRestrictionExpression() );

      var disjunction = new Disjunction();
      disjunction.restrictions = [];
      disjunction.restrictions.push( variantFilterContainer.getRestrictionExpression() );

      // disjunction.restrictions.push( cnvFilterPanel.getRestrictionExpression() );

      // disjunction.restrictions.push( indelFilterPanel.getRestrictionExpression() );

      // disjunction.restrictions.push( snvFilterPanel.getRestrictionExpression() );

      conjunction.restrictions.push( disjunction );

      config.restriction = conjunction;
      return config;
   },

   setFilterConfig : function(config) {

      var variantFilterContainer = this.getComponent( 'variantFilterContainer' );

      if ( config.restriction.restrictions ) {

         restrictions = config.restriction.restrictions;

         for (var i = 0; i < restrictions.length; i++) {

            variantFilterContainer.setRestrictionExpression(restrictions[i]);

         }

      }

   },

   separateVariantDisjunctions : function(disjunctions, variantType) {

      var separatedDisjunctions = [];

      var addVariantRestrictionToDisjunctions = function(innerRestriction, outerRestriction, somethingElseToDo) {

         if ( innerRestriction.type && innerRestriction.type == variantType ) {

            separatedDisjunctions.push( outerRestriction );

         }

      };

      var somethingElseToDoFunction = function() {
         // Questioner: There's nothing else to do?!?!? Are you sure???? Answer: Yes, I am sure. Questioner: Do the
         // dishes.
      };

      FilterUtil.traverseRidiculousObjectQueryGraphAndDoSomething( disjunctions, null,
         addVariantRestrictionToDisjunctions, somethingElseToDoFunction );

      return separatedDisjunctions;

   },

   shouldExpandVariantTypeBox : function(restrictions) {
      if ( restrictions.restrictions.length > 0 && restrictions.restrictions[0].restrictions.length > 1 ) {
         return true;
      }

      return false;
   },

   initComponent : function() {
      this.callParent();
   }

} );
