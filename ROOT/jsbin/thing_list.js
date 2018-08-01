/*
提供了 Thing 对象的 list 功能

本函数假设有下面格式参数输入

params: {
    // 下面这些字段为必须字段
    pid  : "14eulqbbkgh6vq9t5ldpipjvs0",  // ThingSet ID

    // 下面的字段为可选字段
    s    : "ct:1",       // 排序字段
    lb   : "AA BB",      // 指定标签
    c    : "xxx",        // 指定分类的值
    detail : "yes",      // 如果为 "yes" 表示读取数据详情
    pn   : 1             // 页数，如果 >=1 则表示分页，
    pgsz : 10            // 页大小，默认 10，只有 pn>=1 才生效
    skip : 10            // 跳过多少记录，不声明则用 (pn-1) * pgsz      
    // 其他字段，均作为过滤条件
    ...          
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
        sys.exec("ajaxre e.api.thing.list.noThingSetId");
        return;
    }
    // 准备生成命令
    var cmdText = 'thing ' + params.pid + ' query ';
    
    // 排序
    if(params.s)
        cmdText += " -sort '" + params.s + "' ";
    else
    	cmdText += " -sort 'ct:1' ";
    
    // 读取详情
    if("yes" == params.detail) {
    	cmdText += " -content";
    }

    // 设置页大小
    var pgsz = parseInt(params.pgsz);
    if(isNaN(pgsz)) {
        pgsz = 10;
    }
    // 数字的话检查一下
    else {
        // 最多一千条记录
        if(pgsz > 1000) {
            pgsz = 1000;
        }
        // 比1大才有意义
        else if(pgsz < 1) {
            pgsz = 10;
        }

    }
    cmdText += " -limit " + pgsz;

    // 分页
    if(params.pn >= 1) {
        // 设置 skip
        if((typeof params.skip) != "number") {
            params.skip = (params.pn - 1) * params.pgsz;
        }
        // 加入标识
        cmdText += " -pager";
    }
    
    // 设置跳过的数据
    if(params.skip > 0) {
        cmdText += " -skip " + params.skip;
    }

    // 准备查询条件
    var flt = {};

    // 标签
    if(params.lb)
        flt.lbls = params.lb;

    // 分类
    if(params.c)
        flt.th_cate = params.c;

    // 循环参数表，找到过滤函数
    for(var key in params) {
        // 固定参数，已经处理过了，忽略
        if(/^(pid|c|lb|s|pn|pgsz|skip|detail|jfmt)$/.test(key))
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

    // 循环一下，看看是否要改变值的类型
    for(var key in flt) {
        var val = flt[key];
        // 布尔
        if(/^(true|false)$/.test(val)) {
            flt[key] = val == "true";
            continue;
        }
        // 数字
        val = val *1;
        if(!isNaN(val)) {
            flt[key] = val;
        }
    }

    // 看看搜索匹配条件的字符串是啥
    var cnd = JSON.stringify(flt);
    if(cnd != "{}")
        cmdText += " '" + cnd.replace(/'/g, "\\'");

    log.info(cmdText);

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
