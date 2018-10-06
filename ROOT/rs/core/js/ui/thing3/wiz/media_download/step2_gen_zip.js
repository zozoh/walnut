(function($z){
    $z.declare([
        'zui',
        'wn/util',
        'ui/support/cmd_log'
    ], function(ZUI, Wn, CmdLogUI){
    //==============================================
    var html = function(){/*
    <div class="ui-arena th3-media-down-2-gen-zip"    
        ui-fitparent="yes"
        ui-gasket="log"></div>
    */};
    //==============================================
    return ZUI.def("app.wn.th3_md_2_gen_zip", {
        dom  : $z.getFuncBodyAsStr(html.toString()),
        //...............................................................
        redraw : function(){
            var UI = this;
    
            UI.__log_list = [];
    
            new CmdLogUI({
                parent : UI,
                gasketName : "log",
                welcome : 'i18n:th3.data.down_begin',
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
            var setup = data.setup;

            // 确保有文件名
            if(!setup.fileName)
                throw "setup.fileName without defined!";

            // 确保文件名是 zip
            if(!/[.zip]$/.test(setup.fileName))
                setup.fileName += ".zip";
    
            //console.log(data)
           
            // 创建临时文件
            Wn.execf('thing {{tsId}} tmpfile {{fnm}} -expi 1d', {
                tsId : opt.oT.th_set,
                fnm  : setup.fileName 
            }, function(re) {
                // 错误
                if(!re || /^e./.test(re)){
                    UI.alert(re, "warn");
                    return;
                }   
                //  得到临时文件，记录一下
                var reo = $z.fromJson(re);
                UI.__OUT_FILE = reo;

                // 准备生成命令
                var cmdText;

                // 压缩指定文件
                if(_.isArray(opt.oMediaList) && opt.oMediaList.length > 0) {
                    cmdText = 'zip -hide -frd id:'+reo.id;
                    for(var i=0; i<opt.oMediaList.length; i++) {
                        var oM = opt.oMediaList[i];
                        cmdText += ' id:' + oM.id; 
                    }
                }
                // 压缩目录
                else {
                    cmdText = $z.tmpl('zip -hide -frd id:{{fid}} id:{{tsId}}/data/{{tid}}/{{dir}}')({
                        fid  : reo.id,
                        tsId : opt.oT.th_set,
                        tid  : opt.oT.id,
                        dir  : opt.dirName
                    });
                }
                
                // 来吧，执行吧
                UI.gasket.log.runCommand(cmdText);
            });
        }
        //...............................................................
    });
    //===================================================================
    });
    })(window.NutzUtil);