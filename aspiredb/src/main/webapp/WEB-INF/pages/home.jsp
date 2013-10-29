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
<script src="dwr/interface/UCSCConnector.js"></script>
<script src="dwr/dtoall.js"></script>

<script src="scripts/lib/ext-all-debug-w-comments.js"></script>
<!-- <script type="text/javascript" src="scripts/lib/ext-theme-neptune.js"></script>-->

<script type="text/javascript" src="scripts/lib/ext-theme-steelblue.js"></script>

<script src="scripts/lib/DwrProxy.js"></script>

<!-- <link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />-->
<link rel="stylesheet" type="text/css" href="scripts/ASPIREdb/resources/css/multivaluecombo.css" />
<!-- <link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-neptune.css">-->
<link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-steelblue.css">
<link rel="icon" type="image/x-icon" href="scripts/ASPIREdb/resources/images/favicon.ico" />

<script src="scripts/ASPIREdb/view/ideogram/IdeogramCursorLayer.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/ChromosomeLayer.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/ChromosomeIdeogram.js"></script>
<script src="scripts/ASPIREdb/view/filter/FilterUtil.js"></script>

<script>


    Ext.application({
        name: 'ASPIREdb',
        appFolder: 'scripts/ASPIREdb',

        launch: function () {
            //var win = Ext.create('ASPIREdb.view.LoginForm');
            var viewport = Ext.create('ASPIREdb.AspireDbPanel');
            //win.show();
        }
    });

   

</script>

<html>
<body>
</body>
</html>