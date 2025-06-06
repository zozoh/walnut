/*
提供了 Thing 对象的 文件上传 功能

本函数假设有下面格式参数输入

params: {
    th_set : th_set,   // 数据集
    th_id  : th_id,    // 数据ID
    name     : name        // 文件字段
}

用法
httpparam -in id:${id} -map params | jsc /jsbin/thing_media_delete.js -vars
*/
//........................................
//http://127.0.0.1:8080/api/leshaonian/thing/media_delete?th_set=~/thing/营期项目&th_id=vumurnhe5shuco32vfqvknqlui&name=wendal.txt
//........................................
// 执行: 需要 params 变量
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if(!params.th_set){
        sys.exec("ajaxre e.api.thing.update.noThingSetId");
        return;
    }
    if(!params.th_id){
        sys.exec("ajaxre e.api.thing.update.noThingId");
        return;
    }
    if(!params.name){
        sys.exec("ajaxre e.api.thing.update.noName");
        return;
    }

    // 准备生成命令
    cmdText = "thing '%s' media '%s' -del '%s' -cqn";

    // 运行命令，并输出返回值
    var re  = sys.exec2f(cmdText, 
                            params.th_set,
                            params.th_id,
                            params.name);

    // 错误
    if(/^e./.test(re)) {
        sys.exec("ajaxre '" + re + "'");
        return;
    }

    // 最后输出结果
    sys.out.println(re);
}
_main(paramObj);
//........................................
