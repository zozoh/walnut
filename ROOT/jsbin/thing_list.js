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

    // 标签
    if(params.lb){
        cmdText += " \"lbls:'" + params.lb + "'\"";
    }

    // 分类
    if(params.c){
        cmdText += " \"th_cate:'" + params.c + "'\"";
    }
    
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

    //log.warn(cmdText);

    // 运行命令，并输出返回值
    var reJson = sys.exec2(cmdText);
    sys.out.println(reJson);
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
