(function($z){
    $z.declare([
        'zui',
        'wn/util',
        'ui/form/form'
    ], function(ZUI, Wn, FormUI){
    //==============================================
    var html = function(){/*
    <div class="ui-arena th-media-down-1-pickname" ui-fitparent="yes" ui-gasket="form">
    </div>
    */};
    //==============================================
    return ZUI.def("app.wn.thmd_1_pickname", {
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
                        title : "i18n:thing.data.down_nm",
                        type   : "string",
                        dft    : dftDownNm,
                        uiType : "@input"
                    }, {
                        key : "audoDownload",
                        title : "i18n:thing.export.audoDownload",
                        tip   : "i18n:thing.conf.export.audoDownload_tip",
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