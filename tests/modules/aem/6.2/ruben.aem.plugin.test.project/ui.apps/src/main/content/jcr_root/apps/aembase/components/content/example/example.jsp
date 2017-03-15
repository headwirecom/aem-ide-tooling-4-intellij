<%@page import="com.adobe.xmp.schema.rng.model.Context"%>
<%@page import="org.apache.sling.commons.json.*, com.cedarsoftware.util.io.*, java.util.*"%>
<%@include file="/libs/foundation/global.jsp" %>

<%
	Map graph = new HashMap();

	 JsonReader.jsonToMaps("{ data: 'json test'}");

	JSONObject obj = new JSONObject("{ data: 'json test'}");

	pageContext.setAttribute("json", obj);
%>

<%= obj.getString("data") %>

<script>
var data = <cq:include script="render_1.jsp"/>;
</script>

text property: ${properties.text}
<script>document.write(data.text);</script>
hello world
