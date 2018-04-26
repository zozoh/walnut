/*
提供了 Thing 对象的 list 功能

本函数假设有下面格式参数输入

params: {
    // 下面这些字段为必须字段
    pid  : "14eulqbbkgh6vq9t5ldpipjvs0",  // ThingSet ID
    k    : "XXX"                          // 关键字，会用这个关键字查询提示信息

    // 下面的字段为可选字段
    f    : "th_nm",      // 提示字段, 记录的哪个字段是用来显示提示的，默认 `th_nm`
    n    : 10,           // 提示数量, 最多给出多少个提示信息，默认 10
}

用法
> httpparam -in id:${id} -map params | jsc /jsbin/thing_search_tip.js -vars 
*/
//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if(!params.pid){
        sys.exec("ajaxre e.api.thing.list.noThingSetId");
        return;
    }

    // 要显示的字段
    var key = params.f || "th_nm";
    var nb  = parseInt(params.n);
    if(isNaN(nb) || nb<=0 || nb>100){
        nb = 10;
    }

    // 准备生成命令
    var cmdText = 'obj id:' + params.pid + '/index';

    // 关键字
    if(params.k) {
        cmdText += ' -match \'nm:"^.*'+params.k.toLowerCase()+'.*"\'';
    }
    // 嗯，随便来一个条件就好
    else {
        cmdText += ' -match \''+key+':{"%exists":true}\'';
    }

    // 增加限制和排序条件
    cmdText += ' -VN -sort \'nm:1\' -limit ' + nb + ' -e \'^'+key+'$\'';


    // 运行命令，并输出返回值
    sys.exec(cmdText);
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
