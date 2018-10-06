(function($z){
    $z.declare([
        'zui',
        'wn/util',
        'ui/form/form'
    ], function(ZUI, Wn, FormUI){
    //==============================================
    var html = function(){/*
    <div class="ui-arena th3-media-down-1-pickname" ui-fitparent="yes" ui-gasket="form">
    </div>
    */};
    //==============================================
    return ZUI.def("app.wn.th3_md_1_pickname", {
        dom  : $z.getFuncBodyAsStr(html.toString()),
        //...............................................................
        redraw : function() {
            var UI  = this;
            var opt = UI.options;

            // 计算一下默认下载名称
            var dftDownNm = [opt.oT.th_nm, opt.dirName].join("_") + ".zip";
            
            // 初始化表单
            new FormUI({
                parent : UI,
                gasketName : "form",
                uiWidth : "all",
                mergeData : false,
                fields : [{
                        key : "fileName",
                        title : "i18n:th3.data.down_nm",
                        type   : "string",
                        dft    : dftDownNm,
                        uiType : "@input"
                    }, {
                        key : "audoDownload",
                        title : "i18n:th3.export.audoDownload",
                        tip   : "i18n:th3.conf.export.audoDownload_tip",
                        type  : "boolean",
                        dft   : true,
                        uiWidth : "auto",
                        uiType : "@toggle",
                    }],
            }).render(function(){
                UI.defer_report("form");
            });
            
            return ["form"];
        },
        //...............................................................
        isDataReady : function(){
            return true;
        },
        //...............................................................
        getData : function(){
            return {
                setup : this.gasket.form.getData()
            };
        },
        //...............................................................
        setData : function(data) {
            this.gasket.form.setData(data.setup || {});
        },
        //...............................................................
    });
    //===================================================================
    });
    })(window.NutzUtil);