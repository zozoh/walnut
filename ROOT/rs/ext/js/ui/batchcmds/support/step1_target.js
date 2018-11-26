(function($z){
$z.declare([
	'zui',
	'wn/util',
	'ui/search2/search',
	'ui/list/list'
], function(ZUI, Wn, SearchUI, ListUI){
//==============================================
var html = function(){/*
<div class="ui-arena bc-step1-target" ui-fitparent="yes">
	<div class="cd-canlist"  ui-gasket="candidates"></div>
    <div class="cd-selected">
        <h4>
            <span>执行目标：</span>
            <a>移除选中</a>
        </h4>
        <div class="cd-selist" ui-gasket="selected"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.ext.bc_step1_target", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        // 移除选中
        "click .cd-selected h4 a" : function() {
            var UI = this;
            // 得到选中的对象们
            var list = UI.gasket.selected.getChecked();

            // 移除选中
            var jN2 = UI.gasket.selected.remove(list);

            // 高亮下一个节点
            UI.gasket.selected.setActived(jN2);

            // 同步下一步的按钮状态
            UI.parent.checkNextBtnStatus();
        }
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
		var opt = UI.options;

        // 显示设备搜索栏
        new SearchUI({
            parent : UI,
            gasketName : "candidates",
            menu : [{
                icon : '<i class="zmdi zmdi-refresh"></i>',
                tip  : "i18n:th3.refresh_tip",
                asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
                asyncHandler : function(jq, mi, callback) {
                    this.refresh(callback, true);
                }
            }, {
                icon : '<i class="zmdi zmdi-square-right"></i>',
                text : "加入候选",
                handler : function(){
                    // 获取选中
                    var list = this.uiList.getChecked();
                    
                    // 列表为空
                    if(!list || list.length ==0) {
                        UI.alert("请先选择要分配的设备，谢谢");
                        return;
                    }

                    // 加入列表
                    for(var i=0; i<list.length; i++) {
                        var dev = list[i];
                        var jIt = UI.gasket.selected.add(dev,dev.id,0);
                        $z.blinkIt(jIt);
                    }

                    // 同步下一步的按钮状态
                    UI.parent.checkNextBtnStatus();
                }
            }],
            data : function(params, callback){
				opt.thing.UI.invokeExtCommand({
					method : opt.targetBy,
					args : [opt.targetHome, params, callback]
				});
            },
            list : {
                fields : opt.thing.UI.invokeExtCommand({
					method : opt.listFieldsBy
				})
            } // ~ End of List
        }).render(function(){
            UI.defer_report("candidates");
        });

        // 显示已选定设备
        new ListUI({
            parent : UI,
            gasketName : "selected",
            escapeHtml : false,
            icon : opt.listIcon || '<i class="zmdi zmdi-star-circle"></i>',
            checkable : true,
            text : function(o) {
                return o[opt.nameKey || "nm"];
            }
        }).render(function(){
            UI.defer_report("selected")
        });

        // 返回延迟加载
        return ["candidates", "selected"];
    },
    //...............................................................
    isDataReady : function() {
        var data = this.getData();
        return data.targets && data.targets.length > 0;
    },
    //...............................................................
    getData : function(){
        return {
            targets : this.gasket.selected.getData()
        };
    },
    //...............................................................
    setData : function(data) {
        var UI = this;
        // 不需要做啥
        // console.log(data)
        // 首先刷新列表
        UI.gasket.candidates.refresh();
        // 设置已经选择的列表
        UI.gasket.selected.setData(data.targets || []);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);