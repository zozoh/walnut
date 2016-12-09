define(function (require, exports, module) {

    var Wn = require("wn/util");
    var SearchUI = require("ui/search/search");

    function init() {
        new SearchUI({
            $pel: $(document.body),
            exec: Wn.exec,
            app: Wn.app(),
            menu: [{
                text: "登陆",
                handler: function () {
                    var sUI = this;
                    var objs = sUI.uiList.getChecked();
                    if (!objs || objs.length == 0) {
                        alert(sUI.msg("srh.e.nochecked"));
                        return;
                    }
                    var bobj = objs[0];
                    // 执行登陆命令，然后打开新的窗口
                    Wn.exec("login " + bobj.nm);
                    window.open(window.location.origin);
                }
            }, "edit", "refresh"],
            data: "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort 'nm:-1'",
            edtCmdTmpl: {
                "edit": "obj id:{{id}} -u '<%=json%>' -o"
            },
            maskConf: {
                width: 640,
                height: 480
            },
            formConf: {
                formatData: function (o) {
                    return o;
                }
            },
            list: {
                layout: {
                    sizeHint: [240, 240, "*"]
                },
                fields: [{
                    key: "id",
                    title: "ID",
                    type: "string",
                    editAs: "label"
                }, {
                    key: "nm",
                    title: "域名城",
                    type: "string",
                    editAs: 'label'
                }, {
                    key: "alias",
                    title: "别名",
                    type: "string"
                }],
                checkable: false
            },
            pager: {
                dft: {
                    pn: 1,
                    pgsz: 100
                }
            }
        }).render(function () {
            this.uiFilter.setData({
                match: {d0: "sys", d1: "usr"}
            });
            this.uiPager.setData();
            this.refresh();
        });
    }

    exports.init = init;
});