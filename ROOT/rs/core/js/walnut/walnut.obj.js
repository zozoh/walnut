define(function (require, exports, module) {
//======================================================================
    var Walnut = require("walnut");
    module.exports = Walnut.def("walnut.obj", {
        // 初始化 ...
        init: function (app) {
            this.on("change:content", this.on_change_content);
            this.set("objId", app.obj.id);
            this.set("obj", $z.extend({}, app.obj));
            this.on("update:obj", this.on_update_obj);
        },
        //..............................................................
        on_update_obj: function () {
            var Mod = this;
            var objId = Mod.get("objId");
            var obj = Mod.get("obj");
            console.log("/o/set/id:" + objId)
            Mod.trigger("save:start");
            $.ajax({
                type: "POST",
                url: "/o/set/id:"+objId,
                contentType: "application/jsonrequest",
                data: $z.toJson(obj)
            }).done(function(re) {
                Mod.trigger("save:done");
            }).fail(function (re) {
                Mod.trigger("save:fail");
                console.log(re);
                throw "fail to save!";
            });
        },
        //..............................................................
        on_change_content: function () {
            var Mod = this;
            var objId = Mod.get("objId");
            var obj = Mod.get("obj");
            var content = this.get("content");
            Mod.trigger("save:start");
            $.ajax({
                type: "POST",
                url: "/p/write/id:" + objId,
                data: content
            }).done(function (re) {
                Mod.trigger("save:done");
            }).fail(function (re) {
                Mod.trigger("save:fail");
                console.log(re);
                throw "fail to save!";
            });
        }
    });
//===================================================================
});