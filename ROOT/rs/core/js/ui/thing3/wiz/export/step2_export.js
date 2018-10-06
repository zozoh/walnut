(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/cmd_log'
], function(ZUI, Wn, CmdLogUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-export-2-export"
    ui-fitparent="yes"
    ui-gasket="log"></div>
*/};
//==============================================
return ZUI.def("app.wn.th3_e_2_export", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        UI.__log_list = [];

        new CmdLogUI({
            parent : UI,
            gasketName : "log",
            welcome : 'i18n:th3.export.welcome',
            // 偷偷记录一下日志给 done 用
            formatMessage : function(str) {
                UI.__log_list.push(str);
                return str;
            },
            done : function(){
                UI.parent.saveData();
                UI.parent.gotoStep(1);
            }
        }).render(function(){
            UI.defer_report("log");
        });

        return ["log"];
    },
    //...............................................................
    getData : function() {
        //console.log("step3.getData")
        return {
            oTmpFile  : this.__OUT_FILE,
            exportLog : this.__log_list
        };
    },
    //...............................................................
    setData : function(data) {
        var UI  = this;
        var opt = UI.options;
        
        // 数据
        var qc = opt.queryContext;
        var setup = data.setup;

        //console.log(qc)

        // console.log(data);
        // console.log(opt.cmdText)
        var cmdText = "thing " + opt.thingSetId + " query ";

        // 准备查询条件
        if(qc.match) {
            cmdText += '\'' + qc.match + '\'';
        }

        //,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
        // 准备分页信息
        if(qc.pgsz > 0 && qc.pn > 0) {
            // 自定义分页信息
            if(setup.pageRange) {
                var pn = setup.pageBegin > 0 ? setup.pageBegin : 1;
                // 计算跳过数据
                cmdText += ' -skip ' + (pn-1) * qc.pgsz;
                // 根据结束页码（包含）计算最多获取条目
                if(setup.pageEnd > 0 ) {
                    var pgnb = setup.pageEnd - setup.pageBegin + 1;
                    if(pgnb <=0 )
                        pgnb = 1;
                    cmdText += ' -limit ' + qc.pgsz * pgnb;
                }
            }
            // 否则采用当前页
            else {
                if(qc.skip > 0) {
                    cmdText += ' -skip ' + qc.skip;
                }
                if(qc.limit > 0) {
                    cmdText += ' -limit ' + qc.limit;
                }
            }
        }

        // 准备排序
        if(qc.sort) {
            cmdText += ' -sort \'' + qc.sort + '\'';
        }

        // 连接输入
        cmdText += ' | sheet -tpi json ';

        // 准备映射文件
        if(opt.mapping)
            cmdText += ' -mapping \'' + opt.mapping + '\'';

        // 准备日志模板
        if(opt.processTmpl)
            cmdText += ' -process \'' + opt.processTmpl + '\'';

        // 准备临时文件名称，为数据集名称+日期+时间
        var fnm = opt.thingSetNm 
                    + "_" + new Date().format("yyyy-mm-dd'T'HHMMss")
                    + "." + setup.exportType;

        // 创建临时文件
        Wn.execf('thing {{tsId}} tmpfile {{fnm}} -expi 1d', {
            tsId : opt.thingSetId,
            fnm  : fnm 
        }, function(re) {
            // 错误
            if(!re || /^e./.test(re)){
                UI.alert(re, "warn");
                return;
            }
            // 得到临时文件，准备导出到这个文件里
            var reo = $z.fromJson(re);
            cmdText += ' -out id:' + reo.id;

            //console.log(cmdText);

            // 记录一下
            UI.__OUT_FILE = reo;

            // 来吧，执行吧
            UI.gasket.log.runCommand(cmdText);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);