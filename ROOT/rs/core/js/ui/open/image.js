define(function (require, exports, module) {
    exports.open = function ($sel, obj) {
        // TODO 图片查看
        var html = "<img class='full-screen' src='/o/read/id:" + obj.id + "' />";
        $sel.append(html);
    }
});
