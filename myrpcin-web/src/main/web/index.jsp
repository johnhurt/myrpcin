<!DOCTYPE>
<%@ page import="org.orgama.server.JspInterop"%>
<html>
	<head>
		<title>myrpcin</title>

		<meta http-equiv="content-type" content="text/html; charset=UTF-8">

		<%
			/** Call into the jsp page interop method indicating that the user
			 *	Has loaded the jsp page.
			 */
			JspInterop.onLoad(pageContext);
		%>

		<%-- This script loads your compiled module. Do this last --%>
		<script type="text/javascript" language="javascript"
				src="in.myrpc.App/in.myrpc.App.nocache.js">
		</script>
	</head>

	<body>
		<%-- History support --%>
		<iframe src="javascript:''"
				id="__gwt_historyFrame"
				tabIndex='-1'
				style="position:absolute;width:0;height:0;border:0">
		</iframe>
	</body>
</html>
