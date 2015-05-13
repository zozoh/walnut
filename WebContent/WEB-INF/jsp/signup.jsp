<%@include file="/WEB-INF/jsp/_include/page_head.jsp" %>
<style rel="stylesheet" type="text/css">
form input {
	font-size:16px; padding:6px 10px;
}
</style>
<%@include file="/WEB-INF/jsp/_include/page_body.jsp" %>
<%/*------------------------------------------------*/%>
<form method="POST" action="${base}/u/do/signup">
<table>
	<tr>
		<td>UserName:</td>
		<td><input name="nm"  type="text"></td>
	</tr>
	<tr>
		<td>Password:</td>
		<td><input name="passwd" type="password"></td>
	</tr>
	<tr>
		<td>Email:</td>
		<td><input name="email" type="text"></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td><input type="submit" value="DO SINGUP"></td>
	</tr>
</table>
</form>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_tail.jsp" %>