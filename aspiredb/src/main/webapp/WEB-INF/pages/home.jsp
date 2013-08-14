<script type="text/javascript" src="dwr/engine.js"></script>

<script src="dwr/interface/HomeController.js"></script>

<script src="dwr/interface/ProjectService.js"></script>

<script src="scripts/lib/ext-all-debug-w-comments.js"></script>

<link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />

<script>

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

        ProjectService.getProjects( {
            callback : function(projCollection) {
                alert(projCollection[0].name);
            }
        });
    */

</script>

<html>
<body>
</body>
</html>