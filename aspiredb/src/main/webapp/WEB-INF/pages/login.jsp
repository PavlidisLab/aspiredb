

<script src="scripts/lib/ext-all-debug-w-comments.js"></script>
<!-- <script type="text/javascript" src="scripts/lib/ext-theme-neptune.js"></script>-->
<script type="text/javascript" src="scripts/lib/ext-theme-steelblue.js"></script>



<!-- <link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />-->

<!-- <link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-neptune.css">-->
<link rel="stylesheet" type="text/css"
	href="scripts/lib/resources/css/ext-all-steelblue.css">




<script>
   Ext.application( {
      name : 'ASPIREdb',
      appFolder : 'scripts/ASPIREdb',

      launch : function() {

         // http to https
         var DEFAULT_SSL_PORT = "8443";
         if ( window.location.protocol != "https:" ) {
            window.location.href = 'https://' + window.location.hostname + ':' + DEFAULT_SSL_PORT
               + window.location.pathname;
         }

         //var win = Ext.create();
         var viewport = Ext.create( 'ASPIREdb.view.LoginForm' );
         //win.show();
      }
   } );
</script>

<html>
<body>
</body>
</html>