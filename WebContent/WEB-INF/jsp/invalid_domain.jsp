<%@include file="/WEB-INF/jsp/_include/page_head.jsp" %>
<%@page import="org.nutz.lang.util.NutBean"%>
<title>${msg['e.dmn.title']}</title>
<%@include file="/WEB-INF/jsp/_include/page_body.jsp" %>
<%/*------------------------------------------------*/%>
<h1>${msg['e.dmn.title']}</h1>
<pre>
<%=msg.get("e.dmn.detail", (NutBean)obj) %>:

 - ${err_message}
</pre>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_tail.jsp" %>
