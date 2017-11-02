

<html>



<head>

<script type="text/javascript" src="${pageContext.request.contextPath}/dwr/engine.js"></script>

<script src="${pageContext.request.contextPath}/dwr/interface/ChromosomeService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/HomeController.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/LabelService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/LoginStatusService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/PhenotypeService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/ProjectService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/QueryService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/SubjectService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/VariantService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/GeneService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/BurdenAnalysisService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/UserGeneSetService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/UserManagerService.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/UCSCConnector.js"></script>
<script src="${pageContext.request.contextPath}/dwr/dtoall.js"></script>
<script src="${pageContext.request.contextPath}/dwr/interface/ConfigUtils.js"></script>

<!-- DWR utils -->
<script src="${pageContext.request.contextPath}/scripts/lib/util.js"></script>

<script src="${pageContext.request.contextPath}/scripts/lib/ext-all-debug-w-comments.js"></script>
<!-- <script type="text/javascript" src="scripts/lib/ext-theme-neptune.js"></script>-->
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/lib/json.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/lib/ext-theme-steelblue.js"></script>

<script src="${pageContext.request.contextPath}/scripts/lib/DwrProxy.js"></script>

<!-- <link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />-->
<link rel="stylesheet" type="text/css"
    href="${pageContext.request.contextPath}/scripts/ASPIREdb/resources/css/multivaluecombo.css" />
<!-- <link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-neptune.css">-->
<link rel="stylesheet" type="text/css"
    href="${pageContext.request.contextPath}/scripts/lib/resources/css/ext-all-steelblue.css">
<link rel="icon" type="image/x-icon"
    href="${pageContext.request.contextPath}/scripts/ASPIREdb/resources/images/favicon.ico" />
<link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/scripts/ASPIREdb/resources/css/ideogram.css" />

<script src="${pageContext.request.contextPath}/scripts/ASPIREdb/view/ideogram/IdeogramCursorLayer.js"></script>
<script src="${pageContext.request.contextPath}/scripts/ASPIREdb/view/ideogram/ChromosomeLayer.js"></script>
<script src="${pageContext.request.contextPath}/scripts/ASPIREdb/view/ideogram/ChromosomeIdeogram.js"></script>
<script src="${pageContext.request.contextPath}/scripts/ASPIREdb/view/ideogram/ColourLegend.js"></script>
<script src="${pageContext.request.contextPath}/scripts/ASPIREdb/view/ideogram/DragPanning.js"></script>
<script src="${pageContext.request.contextPath}/scripts/ASPIREdb/view/filter/FilterUtil.js"></script>


<script>
    Ext.onReady(function() {

    // Fix tooltip size in Chrome and IE10
    // Ref: https://www.sencha.com/forum/archive/index.php/t-260106.html?s=7a9af4c0dd95c3c1c42c8c2c35acfddd
       
    delete Ext.tip.Tip.prototype.minWidth;
    
    if(Ext.isIE10 || Ext.isChrome) { 
       
       Ext.override(Ext.tip.Tip, {
          componentLayout: {
             type: 'fieldset',
             getDockedItems: function() { return []; }
          }
       });
    }
    
    // Fix for bug EXTJS-13103, BufferedRender with shared store with hidden grid.
    Ext.override(Ext.grid.plugin.BufferedRenderer, {


        //listen to panel show to refresh
        init: function(grid) {
            var me = this;
            me.callParent(arguments);
            me.grid.on('show', me.onViewRefresh, me);
        },


        //if it is hidden do nothing
        onViewRefresh: function() {
            if(this.grid.isHidden()) return;
            this.callParent(arguments);
        },


        //release the listener made on the override
        destroy: function() {
            var me = this,
                grid = me.grid;


            if (grid) {
                grid.un('show', me.onViewRefresh, me);
            }
            this.callParent(arguments);
        }
    });
    
    Ext.QuickTips.init();

    //Basic mask:
    var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."});
    myMask.show();

   Ext.application( {
      name : 'ASPIREdb',
      appFolder : 'scripts/ASPIREdb',
      enableQuickTips : true,
      
      launch : function() {

         var viewport = Ext.create( 'ASPIREdb.AspireDbPanel' );
         var ideogram = Ext.getCmp('ideogram');
         
         // Drag panning javascript functionality for ideogram
         dragPan(ideogram);
         myMask.hide();
      }
   } );

    });
   
</script>

<!--  matrix2viz -->
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/third-party/clusterfck.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/third-party/transpose.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/examples/sample_data.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/Cell.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/Label.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/LabelPanel.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/Dendrogram.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/Matrix.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/DefaultControlPanel.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/VerticalLabelNames.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/HorizontalLabelNames.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/ClusterHelper.js"></script>
<script src="${pageContext.request.contextPath}/scripts/lib/matrix2viz/js/Matrix2Viz.js"></script>
<!--script src="scripts/lib/matrix2viz/examples/matrixviz.js"></script-->

<!--  Autologout script -->
<script src="${pageContext.request.contextPath}/scripts/lib/autologout.js"></script>

</head>
<body>
</body>
</html>