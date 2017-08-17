/*
提供了 Thing 对象的 list 功能

本函数假设有下面格式参数输入

params: {
    // 下面这些字段为必须字段
    pid  : "14eulqbbkgh6vq9t5ldpipjvs0",  // ThingSet ID
    s    : "ct:1,th_nm:-1",               // 排序字段
    lb   : "AA BB",                       // 指定标签
}

用法
> httpparam -in id:${id} -map params | jsc /jsbin/thing_list.js -vars 
*/
//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if(!params.pid){
        return ajax_error("api.thing.noThingSetId");
    }
    // 准备生成命令
    var cmdText = 'thing ' + paramObj.pid + ' query ';

    // 最多输出 100 条记录
    cmdText += "-limit 100";
    
    // 排序
    if(params.s)
        cmdText += " -sort '" + params.s + "' ";
    else
    	cmdText += " -sort 'ct:1' ";

    // 标签
    if(params.lb){
        cmdText += " \"lbls:'" + params.lb + "'\"";
    }
    //log.warn(cmdText);

    // 运行命令，并输出返回值
    var reJson = sys.exec2(cmdText);
    sys.out.println(reJson);
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
