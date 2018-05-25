/*
提供了 Thing 对象的 create 功能

本函数假设有下面格式参数输入

params: {
    th_set : th_set,   // 「必」数据集
    th_nm  : th_nm,    // 「选」数据名称
    data   : data      // 「选」要更新的字段数据
}

用法
cat id:${id} | json -put params | jsc /jsbin/thing_create.js -vars 
*/
//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if(!params.th_set){
        sys.exec("ajaxre e.api.thing.create.noThingSetId");
        return;
    }
    if(!params.data){
        sys.exec("ajaxre e.api.thing.create.noData");
        return;
    }

    // 准备生成命令
    cmdText = 'thing %s create %s -fields \'%s\' -cqn';

    // 运行命令，并输出返回值
    var fields = JSON.stringify(params.data);
    var re     = sys.exec2f(cmdText, 
                            params.th_set,
                            params.th_nm || "",
                            fields);

    // 错误
    if(/^e./.test(re)) {
        sys.exec("ajaxre '" + re + "'");
        return;
    }

    // 最后输出结果
    sys.out.println(re);
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
