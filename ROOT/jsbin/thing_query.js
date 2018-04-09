/*
提供了 Thing 对象的 query 功能

本函数假设有下面格式参数输入

params: {
    // 下面这些字段为必须字段
    pid  : "14eulqbbkgh6vq9t5ldpipjvs0",  // ThingSet ID
                                          // 半角逗号分隔表示多个
    k    : "搜索关键字",                    // 关键字
    s    : "price:-1,vvv:1",              // 排序字段
    detail : "yes",      // 如果为 "yes" 表示读取数据详情
    jfmt : true,       // 输出的结果是否需要 JSON 格式化  
    pn   : 8,          // 要跳转的页码
    pgsz : 12,         // 页大小，默认 100
    skip : 84,         // 要跳过的记录数
    
    // 其他字段，均作为过滤条件
    _flt : "*(搜索条件)@com:filter",
}

用法
> httpparam -in id:${id} -vint "pn,pgsz,skip" -map params | jsc /jsbin/thing_query.js -vars 
*/
//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if(!params.pid){
        sys.exec("ajaxre e.api.thing.query.noThingSetId");
        return;
    }
    // 准备生成命令
    var cmdText = 'thing query -tss "' + params.pid + '" -qn -pager ';

    // 输出结果格式化
    if(!params.jfmt)
        cmdText += ' -c ';

    // 分页信息
    cmdText += "-limit " + (params.pgsz || 100);
    if(params.skip > 0)
        cmdText += " -skip " + params.skip;

    // 排序
    if(params.s)
        cmdText += " -sort '" + params.s + "' ";

    // 读取详情
    if("yes" == params.detail) {
        cmdText += " -content";
    }

    // 搜索关键字
    var flt = {};
    if(params.k){
        flt.th_nm = "^.*" + params.k;
        // flt.th_brief = "^.*" + params.k;
    }

    // 循环参数表，找到过滤函数
    for(var key in params) {
        // 固定参数，已经处理过了，忽略
        if(/^(pid|k|s|pn|pgsz|skip|detail|cb|jfmt)$/.test(key))
            continue;
        // 处理值
        var val = params[key];
        if(!val)
            continue;
        // 试图将值拆分成数组
        var vv = val.split(/ *\|\| */g);

        // 单个值，就直接来吧
        if(vv.length == 1) {
            flt[key] = vv[0];
        }
        // 变成 or，并加入过滤表
        else {
            flt[key] = ["%or"].concat(vv);
        }
    }

    // 看看搜索匹配条件的字符串是啥
    var cnd = JSON.stringify(flt);
    if(cnd != "{}")
        cmdText += " '" + cnd.replace(/'/g, "\\'");

    //log.warn(cmdText);

    // 运行命令，并输出返回值
    var reJson = sys.exec2(cmdText);

    // 错误
    if(/^e./.test(reJson)) {
        sys.exec("ajaxre '" + reJson + "'");
        return;
    }

    // 最后输出结果
    sys.out.println(reJson);
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
