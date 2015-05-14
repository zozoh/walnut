<html>
<head>
    <meta charset="UTF-8">
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <title>${doc.title} - ${siteTitle}</title>
    <link rel="stylesheet" type="text/css" href="${doc.bpath}css/doc.css">
    <script language="Javascript" src="${doc.bpath}js/jquery-2.1.1.js"></script>
    <script language="Javascript" src="${doc.bpath}js/underscore.js"></script>
    <script language="Javascript" src="${doc.bpath}js/page.js"></script>
</head>
<body>
    <#include "lib:sky.ftl">
    <div class="doc">
        <div class="doc-title">${doc.title}</div>
        <#include "lib:docinfo.ftl">
        <div class="doc-main">
            <div class="doc-outline"><div class="doi-tt">-==outline==-</div></div>
            <div class="doc-content">${doc.content}</div>
        </div>
    </div>
    <#include "lib:footer.ftl">
</body>
</html>