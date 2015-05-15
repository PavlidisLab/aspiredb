Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.OrFilterContainer' ] );

Ext.define( 'ASPIREdb.view.filter.ProjectOverlapFilterContainer', {
   extend : 'Ext.Container',
   alias : 'widget.filter_projectoverlap',
   layout : {
      type : 'vbox'
   },
   config : {
      propertyStore : {

         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForProjectOverlap,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      },
      propertyStore2 : {

         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForNumberOfVariantsInProjectOverlap,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      },
      propertyStore3 : {

         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForSupportOfVariantsInProjectOverlap,
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      },
      suggestValuesRemoteFunction : null,
      projectOverlapFilterItemType : 'ASPIREdb.view.filter.ProjectOverlapPropertyFilter'
   },
   items : [ {
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
      }
   } ],

   getRestrictionExpression : function() {
      var filterContainer = this.getComponent( 'filterContainer' );

      var projectOverlapConfig = new ProjectOverlapFilterConfig();

      projectOverlapConfig.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();

      var overlapProjectComboBox = filterContainer.getComponent( 'overlapProjectComboBox' );

      var overlapProjectIds = [];

      if ( overlapProjectComboBox.getValue() != 'PROJECT_PLACEHOLDER' ) {
         overlapProjectIds.push( overlapProjectComboBox.getValue() );

      }
      projectOverlapConfig.overlapProjectIds = overlapProjectIds;

      var overlapRestriction = filterContainer.getComponent( 'overlapItem' ).getRestrictionExpression();

      projectOverlapConfig.restriction1 = this.validateOverlapRestriction( overlapRestriction );

      var numVariantsOverlapRestriction = filterContainer.getComponent( 'numVariantsOverlapItem' )
         .getRestrictionExpression();

      projectOverlapConfig.restriction2 = this.validateOverlapRestriction( numVariantsOverlapRestriction );

      // "supportOfVariantsOverlapItem" should be hidden and empty, however we need it there because the dwr object
      // requires it.
      var supportOfVariantsOverlapRestriction = filterContainer.getComponent( 'supportOfVariantsOverlapItem' )
         .getRestrictionExpression();

      projectOverlapConfig.restriction3 = this.validateOverlapRestriction( supportOfVariantsOverlapRestriction );

      projectOverlapConfig.phenotypeRestriction = filterContainer.getComponent( 'phenRestriction' )
         .getRestrictionExpression();

      projectOverlapConfig.invert = filterContainer.getComponent( 'invertCheckbox' ).getValue();

      return projectOverlapConfig;
   },

   setRestrictionExpression : function(config) {

      var filterContainer = this.getComponent( 'filterContainer' );

      // this doesn't set the overlapCombo box because the saved overlapped project might not be applicable to the
      // active project you are searching in,
      // i.e. you saved the query with 'evicas' as the active project, and searching for overlap with 'suzannes'.
      // but then you try to load the query again with 'suzannes' as the active project, so you would be searching for
      // 'suzannes' overlap with itself

      filterContainer.getComponent( 'overlapItem' ).setRestrictionExpression( config.restriction1 );

      filterContainer.getComponent( 'numVariantsOverlapItem' ).setRestrictionExpression( config.restriction2 );

      filterContainer.getComponent( 'supportOfVariantsOverlapItem' ).setRestrictionExpression( config.restriction3 );

      filterContainer.getComponent( 'phenRestriction' ).setRestrictionExpression( config.phenotypeRestriction );

      filterContainer.getComponent( 'invertCheckbox' ).setValue( config.invert );

   },

   validateOverlapRestriction : function(restriction) {

      // the widget defaults to a SetRestriction if nothing is set, so return SimpleRestriction(what dwr expects) if it
      // is not set
      if ( restriction.operator == null || restriction.property == null ) {

         var simpleRestriction = new SimpleRestriction();
         simpleRestriction.property = null;
         simpleRestriction.operator = null;
         var value = new NumericValue();
         value.value = null;
         simpleRestriction.value = value;
         return simpleRestriction;

      }

      return restriction;
   },

   getNewOverlapItemFunction : function(remoteFunction, storeRef, id, hidden) {

      if ( hidden == undefined || hidden == null ) {

         hidden = false;
      }

      var filterTypeItem = this.getProjectOverlapFilterItemType();

      var getNewOverlapItem = function() {

         return Ext.create( filterTypeItem, {
            propertyStore : storeRef,
            suggestValuesRemoteFunction : remoteFunction,
            itemId : id,
            hidden : hidden
         } );

      };

      return getNewOverlapItem;
   },

   initComponent : function() {
      this.callParent();

      var filterContainer = this.getComponent( "filterContainer" );

      var getNewOverlapItem = this.getNewOverlapItemFunction( VariantService.suggestPropertiesForProjectOverlap, this
         .getPropertyStore(), 'overlapItem' );

      var overlapItem = getNewOverlapItem();

      var getNewNumVariantsOverlapItem = this.getNewOverlapItemFunction(
         VariantService.suggestPropertiesForNumberOfVariantsInProjectOverlap, this.getPropertyStore2(),
         'numVariantsOverlapItem' );

      var numVariantsOverlapItem = getNewNumVariantsOverlapItem();

      var getNewVariantSupportOverlapItem = this.getNewOverlapItemFunction(
         VariantService.suggestPropertiesForSupportOfVariantsInProjectOverlap, this.getPropertyStore3(),
         'supportOfVariantsOverlapItem', true );

      var supportOfVariantsOverlapItem = getNewVariantSupportOverlapItem();

      var projectStore = Ext.create( 'Ext.data.Store', {
         id : 'projectStore',
         proxy : {
            type : 'dwr',
            dwrFunction : ProjectService.getOverlapProjects,
            dwrParams : [ ASPIREdb.ActiveProjectSettings.getActiveProjectIds() ],
            model : 'ASPIREdb.model.Project',
            reader : {
               type : 'json',
               root : 'name'
            }
         }
      } );

      // TODO Show Overlapped Variants
      filterContainer.insert( 0, {
         xtype : 'button',
         flex : 1,
         itemId : 'overlappedVariants',
         text : 'Overlapped Variants',
         disabled : true,
         handler : this.overlappedVariantsHandler,
         scope : this
      } );

      filterContainer.insert( 0, {
         xtype : "checkbox",
         itemId : "invertCheckbox"
      } );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Invert Filter: '
      } );

      filterContainer.insert( 0, {
         xtype : 'filter_phenotype_property',
         itemId : 'phenRestriction'
      } );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap Phenotype Association: '
      } );

      filterContainer.insert( 0, supportOfVariantsOverlapItem );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap study support: ',
         hidden : true
      } );

      filterContainer.insert( 0, numVariantsOverlapItem );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap variant support: '
      } );

      filterContainer.insert( 0, overlapItem );
      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Overlap size: '
      } );

      filterContainer.insert( 0, {

         xtype : 'combo',
         itemId : 'overlapProjectComboBox',
         // editable : false,
         // forceSelection : true,
         // value : 'PROJECT_PLACEHOLDER',
         // store : [ [ 'PROJECT_PLACEHOLDER', '<Project Name>' ] ],
         // listeners : {
         // scope : this,
         // 'change' : this.overlapProjectComboBoxHandler,
         // },
         store : projectStore,
         editable : false,
         displayField : 'name',
         allowBlank : false,
         valueField : 'id',
         forceSelection : true,
         emptyText : "Choose project...",
         msgTarget : 'qtip',
         listeners : {
            'select' : function() {
               this.up().down( "#overlappedVariants" ).setDisabled( false );
            }
         }
      } );

      filterContainer.insert( 0, {
         xtype : 'label',
         text : 'Project to search for overlap: '
      } );

   },

   overlapProjectComboBoxHandler : function(cmp, newValue, oldValue, eOpts) {
      var filterContainer = this.getComponent( "filterContainer" );
      filterContainer.getComponent( 'phenRestriction' ).updateProjectIds( [ newValue ] );
   },

   // TODO Show Overlapped Variants
   overlappedVariantsHandler : function() {
      var filterContainer = this.getComponent( "filterContainer" );
      var overlapProjectComboBox = filterContainer.getComponent( 'overlapProjectComboBox' );
      ASPIREdb.view.filter.FilterWindow.overlappedVariantsHandler( overlapProjectComboBox.getValue() );
      console.log( 'overlap project selected : ' + overlapProjectComboBox.getValue() );
   },

} );
