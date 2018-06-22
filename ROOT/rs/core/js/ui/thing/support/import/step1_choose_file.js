(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-1-choose-file" ui-fitparent="yes">
    <div class="thiw-main">
        <input type="file">
        <section>{{thing.import.cf_tip1}}</section>
        <b class="ui-btn">选择文件...</b>
        <section class="thiw-tip2">{{thing.import.cf_tip2}}：<code></code></section>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_1_choose_file", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(opt) {
        $z.setUndefined(opt, "accept", ".csv, .xls");
    },
    //...............................................................
    events : {
        // 模拟输入框点击
        'click b.ui-btn' : function() {
            console.log(this.arena.find('input[type="file"]').length)
            this.arena.find('input[type="file"]').click();
        },
        // 监控文件选择输入框
        'change input[type="file"]' : function() {
            var UI = this;
            var jIn = UI.arena.find('input[type="file"]');
            if(jIn[0].files.length > 0) {
                UI.setFile(jIn[0].files);
            }
            // 清空，以便每次都能截获改变事件
            jIn.val('');
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        if(opt.accept) {
            UI.arena.find('input[type="file"]').attr("accept", opt.accept);
            UI.arena.find('.thiw-tip2 code').text(opt.accept);
        }
        // 否则就是全部文件
        else {
            UI.arena.find('.thiw-tip2 code').text(UI.msg("thing.import.cf_all"));
        }
    },
    //...............................................................
    dragAndDrop : ".thiw-main",
    on_drop : function(fs){
        this.setFile(fs);
    },
    //...............................................................
    setFile : function(fs) {
        var UI  = this;
        var opt = UI.options;
        //console.log(fs)

        // 没有选择任何文件（嗯，掉到这个函数的话，这个应该是不太可能发生的）
        // 不过还是防止一下某个二货，直接调这个函数吧 ^_^!
        if(!fs || fs.length == 0) {
            UI.alert("thing.import.e_fnone", "warn");
            return;
        }
        // 只能选择一个文件
        if(fs.length > 1) {
            UI.alert("thing.import.e_fmulti", "warn");
            return;
        }
        // 取得文件
        var f = fs[0];
        // 文件格式不对
        if(opt.accept) {
            var acList = opt.accept.split(/ *, */g);
            var isAccepted = false;
            for(var i=0; i<acList.length; i++) {
                var ac = acList[i];
                if($z.isEndsWith(f.name, ac)) {
                    isAccepted = true;
                    break;
                }
            }
            if(!isAccepted) {
                UI.alert(UI.msg("thing.import.e_accept", {
                    fname  : f.name,
                    accept : opt.accept
                }), "warn");
                return;
            }
        }
        // 嗯，看来没啥问题，下一步咯
        UI.__file = f;
        UI.parent.saveData();
        UI.parent.gotoStep(1);
    },
    //...............................................................
    getData : function(){
        return {
            theFile : this.__file
        };
    },
    //...............................................................
    setData : function() {
        // 不需要做啥
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);