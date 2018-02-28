(function ($z) {
    $z.declare([
        'zui',
        'wn/util',
        'ui/form/form'
    ], function (ZUI, Wn, FormUI) {
        return ZUI.def("ui.test_form2", {
            //...............................................................
            dom: `
                <div class="ui-arena" ui-fitparent="yes">
                    <div class="myform" ui-gasket="myform" style="width:100%; height:100%;"></div>
                </div>
            `,
            //...............................................................
            events: {},
            update: function (o) {
                var UI = this;
                new FormUI({
                    parent: UI,
                    gasketName: "myform",
                    on_change: function (key, val) {
                        console.log("form change:", key, val);
                    },
                    title: "上传图片测试",
                    uiWidth: "auto",
                    fields: [{
                        key: "_head_img",
                        title: "免冠照",
                        tip: "",
                        virtual: true,
                        type: "string",
                        editAs: "image",
                        uiWidth: "all",
                        uiConf: {
                            width: 200,
                            iosfix: true,
                            compress: 50,
                            target: function () {
                                return "~/.test/avatar.jpg";
                            },
                            dataType: "id",
                            parseData: function () {
                                var oHeadImage = Wn.get("~/.test/avatar.jpg", true);
                                if (oHeadImage) {
                                    return oHeadImage;
                                }
                                return null;
                            }, done: function (imgObj) {
                                console.log(imgObj);
                                var imgCmdText = "cp ~/.test/avatar.jpg ~/.test/avatar_org.jpg";
                                console.log(imgCmdText);
                                Wn.exec(imgCmdText, function (re) {
                                    if (re) {
                                        alert(re);
                                    }
                                });
                            }
                        }
                    }]
                }).render(function () {
                    this.setData({});
                });
            }
            //...............................................................
        });
//===================================================================
    });
})(window.NutzUtil);