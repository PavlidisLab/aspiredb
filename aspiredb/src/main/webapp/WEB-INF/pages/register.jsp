

<script src="scripts/lib/ext-all-debug-w-comments.js"></script>
<!-- <script type="text/javascript" src="scripts/lib/ext-theme-neptune.js"></script>-->
<script type="text/javascript" src="scripts/lib/ext-theme-steelblue.js"></script>
<script src="https://www.google.com/recaptcha/api.js?render=explicit" async defer></script>
<script type="text/javascript" src="scripts/lib/json.js"></script>


<!-- <link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />-->

<!-- <link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-neptune.css">-->
<link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-steelblue.css">

<script>


    Ext.application({
        name: 'ASPIREdb',
        appFolder: 'scripts/ASPIREdb',

        launch: function () {
            //var win = Ext.create();
            var viewport = Ext.create('ASPIREdb.view.RegistrationForm');
            //win.show();
        }
    });

    Ext.define('ASPIREdb.globals', {
        singleton: true,
        recaptchaPublicKey: "${ appConfig["aspiredb.recaptcha.publicKey"]}"
    });

</script>

<html>
<body>
<%@include file="/common/analytics.jsp" %>
</body>
</html>