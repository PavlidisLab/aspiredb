

<html>
<body>
</body>
</html>



<script type="text/javascript" src="dwr/engine.js"></script>

<script src="dwr/interface/ChromosomeService.js"></script>
<script src="dwr/interface/HomeController.js"></script>
<script src="dwr/interface/LabelService.js"></script>
<script src="dwr/interface/LoginStatusService.js"></script>
<script src="dwr/interface/PhenotypeService.js"></script>
<script src="dwr/interface/ProjectService.js"></script>
<script src="dwr/interface/QueryService.js"></script>
<script src="dwr/interface/SubjectService.js"></script>
<script src="dwr/interface/VariantService.js"></script>
<script src="dwr/interface/GeneService.js"></script>
<script src="dwr/interface/BurdenAnalysisService.js"></script>
<script src="dwr/interface/UserGeneSetService.js"></script>
<script src="dwr/interface/UserManagerService.js"></script>
<script src="dwr/interface/UCSCConnector.js"></script>
<script src="dwr/dtoall.js"></script>
<script src="dwr/interface/ConfigUtils.js"></script>

<!-- DWR utils -->
<script src="scripts/lib/util.js"></script>

<script src="scripts/lib/ext-all-debug-w-comments.js"></script>
<!-- <script type="text/javascript" src="scripts/lib/ext-theme-neptune.js"></script>-->
<script type="text/javascript" src="scripts/lib/json.js"></script>

<script type="text/javascript" src="scripts/lib/ext-theme-steelblue.js"></script>

<script src="scripts/lib/DwrProxy.js"></script>

<!-- <link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />-->
<link rel="stylesheet" type="text/css"
    href="scripts/ASPIREdb/resources/css/multivaluecombo.css" />
<!-- <link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-neptune.css">-->
<link rel="stylesheet" type="text/css"
    href="scripts/lib/resources/css/ext-all-steelblue.css">
<link rel="icon" type="image/x-icon"
    href="scripts/ASPIREdb/resources/images/favicon.ico" />
<link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="scripts/ASPIREdb/resources/css/ideogram.css" />

<script src="scripts/ASPIREdb/view/ideogram/IdeogramCursorLayer.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/ChromosomeLayer.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/ChromosomeIdeogram.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/DragPanning.js"></script>
<script src="scripts/ASPIREdb/view/filter/FilterUtil.js"></script>


<script>

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
         var ideogramBody = Ext.getCmp('ideogram').body.dom;
         
         // Drag panning javascript functionality for ideogram
         dragPan(ideogramBody);
         myMask.hide();
      }
   } );
   
</script>

<!--  matrix2viz -->
<script src="scripts/lib/matrix2viz/third-party/clusterfck.js"></script>
<script src="scripts/lib/matrix2viz/third-party/transpose.js"></script>
<script src="scripts/lib/matrix2viz/examples/sample_data.js"></script>
<script src="scripts/lib/matrix2viz/js/Cell.js"></script>
<script src="scripts/lib/matrix2viz/js/Label.js"></script>
<script src="scripts/lib/matrix2viz/js/LabelPanel.js"></script>
<script src="scripts/lib/matrix2viz/js/Dendrogram.js"></script>
<script src="scripts/lib/matrix2viz/js/Matrix.js"></script>
<script src="scripts/lib/matrix2viz/js/DefaultControlPanel.js"></script>
<script src="scripts/lib/matrix2viz/js/VerticalLabelNames.js"></script>
<script src="scripts/lib/matrix2viz/js/HorizontalLabelNames.js"></script>
<script src="scripts/lib/matrix2viz/js/ClusterHelper.js"></script>
<script src="scripts/lib/matrix2viz/js/Matrix2Viz.js"></script>
<!--script src="scripts/lib/matrix2viz/examples/matrixviz.js"></script-->
