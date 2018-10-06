(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th3-import-2-uploading" ui-fitparent="yes">
    <h4>
        <i class="zmdi zmdi-cloud-upload"></i>
        {{th3.import.up_title}}<code></code>
    </h4>
    <div class="thiw-ing">
        <div class="thiw-ing-nb">0%</div>
        <div class="thiw-ing-bar"><span></span></div>
        <div class="thiw-ing-msg"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.th3_i_2_uploading", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    dredraw : function() {
        var UI = this;
        var f  = {
            name : "哈哈哈.xls"
        };

        // 准备上传文件
        UI.arena.find('h4 code').text(f.name);

        // 首先清空进度条
        var jUpNb  = UI.arena.find('.thiw-ing-nb');
        var jUpBar = UI.arena.find('.thiw-ing-bar span');

        jUpBar.css('width', "40%");
    },
    //...............................................................
    getData : function(){
        return {
            oTmpFile : this.__tmp_file
        };
    },
    //...............................................................
    setData : function(data) {
        var UI  = this;
        var opt = UI.options;

        var f  = data.theFile || {
            name : "哈哈哈.xls"
        };

        // 准备上传文件
        UI.arena.find('h4 code').text(f.name);

        // 首先清空进度条
        var jUpNb  = UI.arena.find('.thiw-ing-nb');
        var jUpBar = UI.arena.find('.thiw-ing-bar span');
        var jUpMsg = UI.arena.find('.thiw-ing-msg');

        // 进度条归零
        jUpBar.css('width', "0%");

        // 先创建一个临时文件
        var cmdText = 'thing {{tsId}} tmpfile import_${id}.{{suffix}} -expi 1d';
        Wn.execf(cmdText, {
            tsId   : opt.thingSetId,
            suffix : $z.getSuffixName(f.name, true)
        }, function(re) {
            // 错误
            if(!re || /^e./.test(re)){
                UI.alert(re, "warn");
                return;
            }
            // 得到临时文件，等上传完毕 (done) 才计入数据吧
            var reo = $z.fromJson(re);

            // 开始写入
            $z.uploadFile({
                file : f,
                url  : "/o/upload/id:" + reo.id,
                progress : function(e) {
                    var p = $z.toPercent(e.loaded / e.total);
                    jUpNb.text(p);
                    jUpBar.css("width", p);
                    if("100%" == p) {
                        jUpMsg.html(UI.msg('thing.import.up_finish'));
                    }
                    // 否则更新信息
                    else {
                        jUpMsg.html(UI.msg('thing.import.up_ing', {
                            loaded : $z.sizeText(e.loaded)
                        }));
                    }
                },
                evalReturn : "ajax",
                done : function(reo) {
                    UI.__tmp_file = reo;
                    UI.parent.saveData();
                    UI.parent.gotoStep(1);
                },
                fail : function(re) {
                    UI.alert(re, "warn");
                }
            });
        });
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);