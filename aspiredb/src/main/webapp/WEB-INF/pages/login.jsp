<script src="scripts/lib/ext-all-debug-w-comments.js"></script>
<!-- <script type="text/javascript" src="scripts/lib/ext-theme-neptune.js"></script>-->
<script type="text/javascript" src="scripts/lib/ext-theme-steelblue.js"></script>
<script type="text/javascript" src="scripts/lib/json.js"></script>
<script src="https://www.google.com/recaptcha/api.js?render=explicit" async defer></script>
<script type="text/javascript" src="scripts/lib/json.js"></script>

<!-- <link href="http://cdn.sencha.com/ext/gpl/4.2.0/resources/css/ext-all.css" rel="stylesheet" />-->

<!-- <link rel="stylesheet" type="text/css" href="scripts/lib/resources/css/ext-all-neptune.css">-->
<link rel="stylesheet" type="text/css"
    href="scripts/lib/resources/css/ext-all-steelblue.css">



<%@page
    import="org.apache.commons.configuration.PropertiesConfiguration"%>
<%@page import="org.apache.commons.configuration.CompositeConfiguration"%>
<%@page import="org.apache.commons.configuration.io.FileHandler"%>

<%
    String USER_CONFIGURATION = "aspiredb.properties";
    String DEFAULT_CONFIGURATION = "default.properties";

    CompositeConfiguration localConfig = new CompositeConfiguration();
    
    PropertiesConfiguration pc = new PropertiesConfiguration();
    FileHandler handler = new FileHandler( pc );
    handler.setFileName( USER_CONFIGURATION );
    handler.load();
    localConfig.addConfiguration( pc );
    
    pc = new PropertiesConfiguration();
    handler = new FileHandler( pc );
    handler.setFileName( DEFAULT_CONFIGURATION );
    handler.load();
    localConfig.addConfiguration( pc );
    
    System.setProperty( "aspiredb.sslPort", localConfig.getString( "aspiredb.sslPort" ) );
    System.setProperty( "aspiredb.recaptcha.publicKey", localConfig.getString( "aspiredb.recaptcha.publicKey" ) );
%>

<script>

   Ext.application( {
      name : 'ASPIREdb',
      appFolder : 'scripts/ASPIREdb',

      launch : function() {
        
         var port = "<%=System.getProperty("aspiredb.sslPort")%>";

         if ( port == null ) {
            port = "";
         }

         // http to https
         // TODO SSL Disabled for now until it works on sandbox
         //var DEFAULT_SSL_PORT = "8443";
         //if ( window.location.protocol != "https:" ) {
         //   window.location.href = 'https://' + window.location.hostname + ':' + port + window.location.pathname;
         //}

         //var win = Ext.create();
         var viewport = Ext.create( 'ASPIREdb.view.LoginForm' );
         //win.show();
      }
   } );

   Ext.define('ASPIREdb.globals', {
       singleton: true,
       recaptchaPublicKey: "<%=System.getProperty("aspiredb.recaptcha.publicKey")%>"
   });
</script>




<html>
<body>
</body>
</html>