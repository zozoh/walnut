(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_droplist'
], function(ZUI, Wn, DroplistUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="handler.mat" class="hdl-mat">
        <div class="key"><select>
            <option value="MsgType">{{weixin.mp.hdl_mat_MsgType}}</option>
            <option value="Content">{{weixin.mp.hdl_mat_Content}}</option>
            <option value="Event">{{weixin.mp.hdl_mat_Event}}</option>
            <option value="EventKey">{{weixin.mp.hdl_mat_EventKey}}</option>
        </select></div>
        <div class="val"><input spellcheck="false"></div>
        <div class="regex">
            <i class="fa fa-square-o"></i>
            <i class="fa fa-check-square"></i>
            <b>{{weixin.mp.hdl_mat_regex}}</b>
        </div>
        <div class="mat-del">{{del}}</div>
    </div>
    <div code-id="handler.item" class="wxmh-hdl">
        <h4>
            <i class="fa fa-filter"></i><b></b>
            <span class="hdl-del" data-balloon="{{weixin.mp.hdl_del}}" data-balloon-pos="left"><i class="fa fa-close"></i></span>
        </h4>
        <div class="hdl-matchs">
            <div class="hdl-match-add"><%=weixin.mp.hdl_match_add%></div>
        </div>
        <div class="hdl-context">
            <span><i class="fa fa-square-o"></i><i class="fa fa-check-square"></i><b>{{weixin.mp.hdl_context}}</b></span>
        </div>
        <div class="hdl-command"><textarea placeholder="{{weixin.mp.hdl_command}}" spellcheck="false"></textarea></div>
    </div>
    <div code-id="handler.add" class="wxmh-hdl-add">
        <i class="fa fa-plus"></i> <b>{{weixin.mp.hdl_add}}</b>
    </div>
</div>
<div class="ui-arena weixin-mp-handler" ui-fitparent="yes">
    
</div>
*/};
//==============================================
return ZUI.def("app.wn.weixin.c_mp_handler", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .wxmh-hdl-add" : function() {
            var UI = this;
            UI._append_handler({
                id:"ddddd",
                match : ["haha",{Event:"^AAA"},{MsgType:"text"}]
            });
        },
        "click .hdl-context span, .hdl-mat .regex" : function(e){
            var jq = $(e.currentTarget);
            if(jq.attr("checked"))
                jq.removeAttr("checked");
            else
                jq.attr("checked", "yes");
        },
        "click .wxmh-hdl .hdl-del" : function(e){
            $z.removeIt($(e.currentTarget).closest(".wxmh-hdl"));
        },
        "click .hdl-match-add" : function(e){
            var UI = this;
            UI._append_match($(e.currentTarget).closest(".wxmh-hdl"), 
                "Content", UI.msg("weixin.mp.hdl_mat_Content_example"));
        },
        "click .mat-del" : function(e){
            $(e.currentTarget).closest(".hdl-mat").remove();
        }
    },
    //...............................................................
    _append_match : function(jHdl, key, val) {
        var UI = this;
        var jMadd = jHdl.find(".hdl-match-add");
        var jMat = UI.ccode("handler.mat");

        // 匹配类型
        jMat.find(".key select").val(key);

        // 匹配字符串值
        if(_.isString(val)){
            jMat.find(".val input").val(val);
            // ^ 开头表示正则表达式
            if(/^\^.+/.test(val))
                jMat.find(".regex").attr("checked", "yes");
        }
        // 匹配正则表达式
        else if(_.isObject(val) && val.regex){
            jMat.find(".val input").val(val.regex);
            jMat.find(".regex").attr("checked", "yes");
        }

        // 加入 dom
        jMat.insertBefore(jMadd);
    },
    //...............................................................
    _append_match_by_obj : function(jHdl, obj) {
        var UI = this;
        // 纯文字 
        if(_.isString(obj)){
            UI._append_match(jHdl, "Content", obj);
        }
        // 数组
        else if(_.isArray(obj)){
            for(var i=0;i<obj.length; i++){
                var mat = obj[i];
                UI._append_match_by_obj(jHdl, mat);
            }
        }
        // 那么就是对象咯
        else {
            for(var key in obj){
                UI._append_match(jHdl, key, obj[key]);
                break;
            }
        }
    },
    //...............................................................
    _append_handler : function(hdl) {
        var UI   = this;
        var jAdd = UI.arena.find(".wxmh-hdl-add");
        var jHdl = UI.ccode("handler.item");

        // 标题
        jHdl.find("h4 b").text(hdl.id || 'anonymous');

        // 匹配
        if(hdl.match){
            UI._append_match_by_obj(jHdl, hdl.match);
        }

        // 上下文
        if(hdl.context)
            jHdl.find(".hdl-context span").attr("checked", "yes");

        // 命令
        if(hdl.command)
            jHdl.find(".hdl-command textarea").val(hdl.command);

        // 加入 DOM
        jHdl.insertBefore(jAdd);

        // 模拟点击
        //jHdl.find(".hdl-match-add").click();
    },
    //...............................................................
    _get_hdl_from_dom : function(jHdl){

    },
    //...............................................................
    setData : function(hdls) {
        var UI = this;

        // 清空自身
        UI.releaseAllChildren();
        UI.arena.empty();

        // 依次加入 handler
        if(_.isArray(hdls) && hdls.length > 0) {
            for(var i=0; i<hdls.length; i++) {
                UI._append_handler(hdls[i]);
            }
        }

        // 最后添加新建按钮
        UI.ccode("handler.add").appendTo(UI.arena).click();
    },
    //...............................................................
    getData : function(){
        var UI   = this;
        var hdls = [];

        UI.arena.find(".wxmh-hdl").each(function(){
            hdls.push(UI._get_hdl_from_dom($(this)));
        });

        return hdls;
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);