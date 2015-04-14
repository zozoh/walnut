<%@include file="/WEB-INF/jsp/_include/page_head.jsp" %>
<%/*------------------------------------------------*/%>
<title>${obj.name}<c:if test="${not empty obj.obj}">: ${obj.obj.nm}</c:if></title>
<meta charset="utf-8"> 
<link rel="stylesheet" type="text/css" href="${rs}/css/font-awesome-4.3.0/css/font-awesome.css">
<%/*------------------------------------------------*/%>
<script src="${rs}/js/seajs/seajs-2.3.0/sea-debug.js" id="seajsnode"></script>
<script src="${rs}/js/seajs/seajs-2.3.0/seajs-text-debug.js"></script>
<script src="${rs}/js/seajs/seajs-2.3.0/seajs-css-debug.js"></script>
<script src="${rs}/js/jquery/jquery-2.1.3/jquery-2.1.3.js"></script>
<script src="${rs}/js/backbone/underscore-1.8.2/underscore.js"></script>
<script src="${rs}/js/backbone/backbone-1.1.2/backbone.js"></script>
<script src="${rs}/js/nutz/zutil.js"></script>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_body.jsp" %>
<%/*------------------------------------------------*/%>
<div id="app" appnm="${appnm}" ui-fitwindow="true"></div>
<%/*------------------------------------------------*/%>
<script>
window._app = <%=((org.nutz.walnut.web.module.WnApplication)
		request.getAttribute("obj"))
			.toJsonObjString()%>

window.$zui_i18n="zh-cn";

seajs.config({
	base  : "${rs}/js",
	debug : true,
	alias : {
	    /* "jquery"     : "jquery/jquery-2.1.3/jquery-2.1.3.js",
         "jquery1"    : "jquery/jquery-1.11.2/jquery-1.11.2.js",
         "backbone"   : "backbone/backbone-1.1.2/backbone.js",
         "underscore" : "backbone/underscore-1.8.2/underscore.js",
         "zutil"      : "nutz/zutil.js",*/
		"walnut"     : "walnut/walnut.js", 
		"zui"        : "nutz/zui.js",
		"zui_css"    : "nutz/zui.css"
	},
	paths : {
		"app" : "/a/load/"+_app.name,
		"ui"  : "${rs}/js/ui",
		"ext" : "${extrs}/js"
	},
	charset: 'utf-8'
});

//define.amd = define.cmd;
seajs.on("error", function(data){
	alert("load '" + data.uri + "' failed!!!'");
});


seajs.use("zui", function(ZUI){
	ZUI.loadi18n("ui/i18n/{{lang}}.js", function(){
		seajs.use("app/main.js", function(main){
            main.init();
        });
	})	
});



</script>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_tail.jsp" %>