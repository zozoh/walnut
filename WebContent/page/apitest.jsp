<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/jsp/_include/page_head.jsp" %>
<title>测试API</title>
<link rel="stylesheet" type="text/css" href="${rs}/core/css/normalize.css">
<link rel="stylesheet" type="text/css" href="${rs}/core/css/animate.css">
<link rel="stylesheet" type="text/css" href="${rs}/core/css/font-awesome/css/font-awesome.css">
<link rel="stylesheet" type="text/css" href="${rs}/core/css/font-md/css/material-design-iconic-font.css">
<link rel="stylesheet" type="text/css" href="${rs}/core/css/mdcss/md.css">
<link rel="stylesheet" type="text/css" href="${rs}/core/css/mdcss/page/md-buildin.css">
<script src="${rs}/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js"></script>
<script src="${rs}/core/js/nutz/zhttp.js"></script>
<script src="${rs}/core/js/nutz/zutil.js"></script>
<script src="${rs}/core/css/mdcss/md.js"></script>
<script src="${rs}/core/css/mdcss/page/md-buildin.js"></script>
<%@include file="/WEB-INF/jsp/_include/page_body.jsp" %>
<%/*------------------------------------------------*/%>
<style>
    .submit-area {
        float: left;
        width: 50%;
        height: 500px;
        padding: 16px;
    }

    .submit-area textarea {
        width: 100%;
        height: 100%;
        padding: 16px;
    }

    .result-area {
        float: left;
        width: 50%;
        height: 500px;
        padding: 16px;
    }

    .result-area textarea {
        width: 100%;
        height: 100%;
        padding: 16px;
    }

    .submit-btn {
        float: left;
        width: 100%;
        height: 50px;
        line-height: 50px;
        background: #888;
        text-align: center;
        font-size: 1.5em;
    }

    .submit-btn:hover {
        background: #AAA;
        cursor: pointer;
    }
</style>
<div class="submit-area">
    <textarea></textarea>
</div>
<div class="result-area">
    <textarea></textarea>
</div>
<div class="submit-btn">提交请求</div>
<script>
    $(document).ready(function () {
        var $ta1 = $('.submit-area textarea');
        var $ta2 = $('.result-area textarea');
        var $sbtn = $('.submit-btn');

        $sbtn.on('click', function () {
            var sform = $z.fromJson($ta1.val());
            sform = sform || {}

            if (!sform.url || !sform.params) {
                alert('需要url与params两个参数');
                return;
            }

            $ta2.val('');
            $ta1.val($z.formatJson(sform, true));

            $http.get(sform.url, sform.params, function (re) {
                $ta2.val($z.formatJson(re, true));
            });
        });
    });
</script>
<%/*------------------------------------------------*/%>
<%@include file="/WEB-INF/jsp/_include/page_tail.jsp" %>