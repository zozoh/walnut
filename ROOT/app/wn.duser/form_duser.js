([ {
    key : "id",
    title : "ID",
    hide  : true,
    type : "string",
    editAs : "label"
}, {
    key : "nm",
    title : "名称",
    tip  : "用来登录的名称，请输入不少于 6 位的数字或者字母，也支持下划线",
    type : "string",
    editAs : "input",
    display : function(o){
        var html = '<i class="fa fa-male"></i>';
        html += '<b>' + (o.realname||o.nickname||"匿名") + '</b>';
        html += '<em>' + o.nm + '</em>';
        return html;
    }
}, {
    key : "realname",
    title : "真实姓名",
    hide  : true,
    type : "string",
    editAs : "input"
}, {
    key : "mobile",
    title : "移动电话",
    type : "string",
    editAs : "input"
}, {
    key : "email",
    title : "邮箱",
    type : "string",
    editAs : "input"
}, {
    key : "openid",
    title : "微信OpenId",
    type : "string",
    editAs : "label"
}, {
    key : "nickname",
    title : "用户昵称",
    hide  : true,
    type : "string",
    editAs : "label"
} ])