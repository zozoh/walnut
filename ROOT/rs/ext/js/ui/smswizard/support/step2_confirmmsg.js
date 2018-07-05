(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena smsw-step2-confirmmsg" ui-fitparent="yes" ui-gasket="main">
    <h4><i class="zmdi zmdi-mail-send"></i></h4>
    <header></header>
    <section>
        <span>{{smswizard.cf_tip}}</span>
    </section>
    <footer></footer>
</div>
*/};
//==============================================
return ZUI.def("ui.ext.smsw_step2_confirmmsg", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    isDataReady : function(){
    	return true;
    },
    //...............................................................
    getData : function(data) {

    },
    //...............................................................
    setData : function(data) {
        var UI = this;
        var opt = UI.options;

        if(!_.isArray(data.targets)){
            return;
        }

        // console.log(data)

        var jH = UI.arena.find('>header');
        var jS = UI.arena.find('>section');
        var jF = UI.arena.find('>footer');
        //---------------------------------------
        // 描述目标
        var names = [];
        for(var i=0; i<data.targets.length; i++) {
            if(i>2) {
                names.push('...');
                break;
            }
            var ta = data.targets[i];
            names.push(ta[opt.nameKey]);
        }
        jH.text(UI.msg('smswizard.cf_target', {
            names : names.join(', '),
            nb : data.targets.length
        }));
        //---------------------------------------
        // 描述例子
        if(data.exampleTarget) {
            $('<span>').text(UI.msg('smswizard.cf_example', {
                name : data.exampleTarget[opt.nameKey]
            })).prependTo(jS);
        }
        //---------------------------------------
        // 短信内容预览
        jF.text(data.exampleContent);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);