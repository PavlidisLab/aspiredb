<!doctype html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>ASPIREdb</title>
<link type="text/css" rel="stylesheet" href="aspiredb.css">
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<!-- $Id: home.jsp,v 1.1 2013/07/23 23:19:32 ptan Exp $ -->
</head>
<body>

	<script language="javascript" src="aspiredb.nocache.js"></script>

	<!-- OPTIONAL: include this if you want history support -->
	<iframe id="__gwt_historyFrame" style="width: 0; height: 0; border: 0"></iframe>

	<div></div>

	<!-- loading screen -->
	<div id="loading">
		Loading <br /> <img src="ajax-loader.gif" />
	</div>

	<!-- Google Analytics -->
	<%@include file="/common/analytics.jsp"%>
	<script type="text/javascript">	
		googleAnalyticsTrackPageviewIfConfigured( "${pageContext.request.servletPath}" );
	</script>

</body>
</html>
