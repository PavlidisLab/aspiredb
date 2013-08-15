<script type="text/javascript" src="dwr/engine.js"></script>

<script src="dwr/interface/HomeController.js"></script>
<script src="dwr/interface/ProjectService.js"></script>
<script src="dwr/interface/ChromosomeService.js"></script>
<script src="dwr/interface/QueryService.js"></script>
<script src="dwr/dtoall.js"></script>

<script src="scripts/lib/ext-all-debug-w-comments.js"></script>

<link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />

<script src="scripts/ASPIREdb/view/ideogram/IdeogramCursorLayer.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/ChromosomeLayer.js"></script>
<script src="scripts/ASPIREdb/view/ideogram/ChromosomeIdeogram.js"></script>

<script>
/*

    ProjectService.getProjects( {
        callback : function(projCollection) {
            alert(projCollection[0].name);
        }
    });
*/

    Ext.application({
        name: 'ASPIREdb',
        appFolder: 'scripts/ASPIREdb',

        launch: function () {
            var win = Ext.create('ASPIREdb.view.LoginForm');
            var viewport = Ext.create('ASPIREdb.AspireDbPanel', {
                loginForm: win
            });
            win.show();
        }
    });

    /*
        HomeController.getTestValueObject( {
            callback : function(testvo) {
                alert(testvo.name);
            }
        });

    */

</script>

<html>
<body>
</body>
</html>