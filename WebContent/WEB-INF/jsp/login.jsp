<%@include file="/WEB-INF/jsp/_include/page_head.jsp" %>
<style rel="stylesheet" type="text/css">
form {
	width:300px; margin:100px auto;
}
input {
	font-size:16px; padding:6px 10px;
}
button {
	font-size:20px; padding:6px 20px;;
}
</style>
<%@include file="/WEB-INF/jsp/_include/page_body.jsp" %>
<%/*------------------------------------------------*/%>
<form method="POST" action="${base}/u/do/login">
<div class="margin:20px auto;">
<img src="${rs}/core/img/walnut_logo.png"/>
</div>
<table>
	<tr>
		<td>UserName:</td>
		<td><input name="nm"  type="text" value="demo"></td>
	</tr>
	<tr>
		<td>Password:</td>
		<td><input name="passwd" type="password" value="123456"></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td><button>Login</button></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td><a href="${base}/u/signup">Get new one</a></td>
	</tr>
</table>
</form>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_tail.jsp" %>