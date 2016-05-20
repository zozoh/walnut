<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/_include/page_head.jsp" %>
<script src="${rs}/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js"></script>
<script>
	$(function($) {
		setDailog();
		$(window).resize(function() {
			setDailog();
		});
	});
	function setDailog() {
		var mt = ($(window).height() - $('.loginWrapper').height()) / 2;
		$('.loginWrapper').css('margin-top', mt-30);
	}

</script>
<style rel="stylesheet" type="text/css">
html{height:100%;}
body{
    background-color:#000;
	background-image:url("${rs}/core/img/bg02.jpg");
	background-position:center center;
	background-repeat:no-repeat;
	background-size:cover;
	margin: 0px;
	font-family:Microsoft YaHei;
	}
.loginWrapper{
	width:365px;
	height:400px;
	margin-left: auto;
	margin-right: auto;
	-webkit-box-shadow:0 0 30px rgba(0, 0, 0, .5);
	-moz-box-shadow:0 0 30px rgba(0, 0, 0, .5);
	box-shadow:0 0 30px rgba(0, 0, 0, .7);
}
.bgcolor01{
	background:#4e517c  ;
	}
.l01{
	height:3px;
	}
.l02{
	height:125px;
	background-image:url("${rs}/core/img/logownt.png");
	background-position: center center;
	background-repeat: no-repeat;
	background-color: rgba(0, 0, 0, .2);
	border: 1px solid rgba(255, 255, 255, .15);
	}
.l03{
	height:30px;
	font-size: 16px;
	font-weight: bold;
	color: #ffffff;
	line-height: 30px;
	text-align: center;
	}
.l03 .ver{
	font-size: 12px;
	font-weight: normal;
}
.l04{
	height:273px;
	background:rgba(255, 255, 255, .60);
	}
.input01{
	border:1px rgba(255,255,255,.36) solid;
	width:265px;
	height:40px;
	background-color: rgba(0, 0, 0, .05);
	font-size: 16px;
	margin-left: 50px;
	-webkit-box-shadow:inset 2px 2px 2px rgba(0, 0, 0, .20);
	-moz-box-shadow:inset 2px 2px 2px rgba(0, 0, 0, .20);
	box-shadow:inset 2px 2px 2px rgba(0, 0, 0, .20);
	padding-left: 40px;
}

.loginIcon01{
	background-image: url(${rs}/core/img/usericon.png);
	background-repeat: no-repeat;
}
.loginIcon02{
	background-image: url(${rs}/core/img/pwicon.png);
	background-repeat: no-repeat;
}
.inputWrapper{

}
.button01 {
	width:265px;
	height:40px;
	margin-left: 50px;
	font-size: 16px;
	color: #ffffff;
	font-weight: bold;
	border: 0px;
}


.loginWrapper{
	animation: animationFrames ease-out 0.3s;
	animation-iteration-count: 1;
	transform-origin: 50% 50%;
	animation-fill-mode:forwards; /*when the spec is finished*/
	-webkit-animation: animationFrames ease-out 0.3s;
	-webkit-animation-iteration-count: 1;
	-webkit-transform-origin: 50% 50%;
	-webkit-animation-fill-mode:forwards; /*Chrome 16+, Safari 4+*/
	-moz-animation: animationFrames ease-out 0.3s;
	-moz-animation-iteration-count: 1;
	-moz-transform-origin: 50% 50%;
	-moz-animation-fill-mode:forwards; /*FF 5+*/
	-o-animation: animationFrames ease-out 0.3s;
	-o-animation-iteration-count: 1;
	-o-transform-origin: 50% 50%;
	-o-animation-fill-mode:forwards; /*Not implemented yet*/
	-ms-animation: animationFrames ease-out 0.3s;
	-ms-animation-iteration-count: 1;
	-ms-transform-origin: 50% 50%;
	-ms-animation-fill-mode:forwards; /*IE 10+*/
}

@keyframes animationFrames{
	0% {
		transform:  translate(-26px,-24px)  scaleX(0.10) scaleY(0.10) ;
	}
	85% {
		transform:  translate(-26px,-24px)  scaleX(1.10) scaleY(1.10) ;
	}
	100% {
		transform:  translate(-26px,-24px)  scaleX(1.00) scaleY(1.00) ;
	}
}

@-moz-keyframes animationFrames{
	0% {
		-moz-transform:  translate(-26px,-24px)  scaleX(0.10) scaleY(0.10) ;
	}
	85% {
		-moz-transform:  translate(-26px,-24px)  scaleX(1.10) scaleY(1.10) ;
	}
	100% {
		-moz-transform:  translate(-26px,-24px)  scaleX(1.00) scaleY(1.00) ;
	}
}

@-webkit-keyframes animationFrames {
	0% {
		-webkit-transform:  translate(-26px,-24px)  scaleX(0.10) scaleY(0.10) ;
	}
	85% {
		-webkit-transform:  translate(-26px,-24px)  scaleX(1.10) scaleY(1.10) ;
	}
	100% {
		-webkit-transform:  translate(-26px,-24px)  scaleX(1.00) scaleY(1.00) ;
	}
}

@-o-keyframes animationFrames {
	0% {
		-o-transform:  translate(-26px,-24px)  scaleX(0.10) scaleY(0.10) ;
	}
	85% {
		-o-transform:  translate(-26px,-24px)  scaleX(1.10) scaleY(1.10) ;
	}
	100% {
		-o-transform:  translate(-26px,-24px)  scaleX(1.00) scaleY(1.00) ;
	}
}

@-ms-keyframes animationFrames {
	0% {
		-ms-transform:  translate(-26px,-24px)  scaleX(0.10) scaleY(0.10) ;
	}
	85% {
		-ms-transform:  translate(-26px,-24px)  scaleX(1.10) scaleY(1.10) ;
	}
	100% {
		-ms-transform:  translate(-26px,-24px)  scaleX(1.00) scaleY(1.00) ;
	}
}
</style>
<%@include file="/WEB-INF/jsp/_include/page_body.jsp" %>
<%/*------------------------------------------------*/%>
<form method="POST" action="${base}/u/do/login">

<div class='loginWrapper'>
	<div class='l02'></div>
	<div class='l04'>
		<div class="inputWrapper" >
			<input class="input01 loginIcon01" name="nm"  type="text" autoComplete="off" placeholder="${msg['u.login.usernm']}" value="" style="margin-top: 62px"/>
		</div>
		<div class="inputWrapper">
			<input class="input01 loginIcon02" name="passwd" type="password" placeholder="${msg['u.login.passwd']}" value="" style="margin-top: 28px"/>
		</div>
		<div class="inputWrapper">
			<button class="button01 bgcolor01" style="margin-top: 28px">${msg['u.login.submit']}</button>
		</div>
	</div>

</div>
</form>
<div>
	<button onclick="oauth_github();">Github登陆</button>
	<button onclick="wx_mp_qrcode();">微信登陆二维码</button>
	<script type="text/javascript">
		var uu32 = "<%=org.nutz.lang.random.R.UU32()%>"
		function oauth_github() {
			window.location.href = "http://" + window.location.host + "${base}/oauth/github";
		}
		function wx_mp_qrcode() {
			$.ajax({
				url : "${base}/api/root/mplogin/qrcode?uu32="+uu32,
				dataType : "json",
				success : function(re){
					console.log(re);
					if (re) {
						$("#img_wx_mp_qrcode").attr("src", "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+re.ticket);
					}
				},
				error : function (e, e2) {
					console.log(e);
					console.log(e2);
				}
			});
			
		}
		function wx_mp_login_check() {
			$.ajax({
				url : "${base}/u/check/mplogin?uu32="+uu32,
				dataType : "json",
				error : function (e, e2) {
					if (e.status == 200 || e.status == 302) {
						window.location = "${base}/";
					}
				}
			});
		}
		setInterval(wx_mp_login_check, 3000);
	</script>
	<img alt="" src="" id="img_wx_mp_qrcode">
</div>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_tail.jsp" %>
