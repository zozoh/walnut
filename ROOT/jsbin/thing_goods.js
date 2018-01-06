/*
提供了 Thing 对象商品形态的复合查询

本函数假设有下面格式参数输入

params: {
    pid  : "xxxx",  // 「必」商品库ID
    cid  : "xxxx",  // 「选」商品类型库ID
    id   : "xxxx",  // 「必」商品ID
}

本接口返回的数据是

{
    current : {..}    // 商品当前型号的元数据
    cate    : {..}    // 商品所属类别的元数据
    models  : [{      // 同类别下还有哪些型号
        ..
    }, {
        ..
    }]  
}

用法
> httpparam -in id:${id} -map params | jsc /jsbin/thing_goods.js -vars 
*/
//........................................
// 处理请求参数
var paramStr = sys.json(params) || "{}";
var paramObj = JSON.parse(paramStr);
//........................................
function _main(params){
    // 检查关键值
    if(!params.pid){
        sys.exec("ajaxre e.api.thing.goods.noThingSetId");
        return;
    }
    if(!params.id){
        sys.exec("ajaxre e.api.thing.goods.noGoodsId");
        return;
    }
    // 统一过滤一下不输出的字段 
    var jfmt = ' -json \'{locked:"^(_.+|nm|c|m|g|md|race|ct|lm|tp|d0|d1|mime|ph)\\$", compact:false}\'';
    //---------------------------------------
    // 获取一下商品数据
    var re = sys.exec2f('thing %s get %s -full -q %s', params.pid, params.id, jfmt);
    if(/^e./.test(re)) {
        sys.exec("ajaxre", re);
        return;
    }
    var go = JSON.parse(re);

    //---------------------------------------
    // 获取一下商品类型数据
    var cate = {};
    var models = [];
    if(go.th_cate && params.cid) {

        // 获取当前商品类型
        re = sys.exec2f('thing %s get %s -full -q %s', params.cid, go.th_cate, jfmt);
        if(/^e./.test(re)) {
            sys.exec("ajaxre", re);
            return;
        }
        cate = JSON.parse(re);

        // 获取同类型下其他商品
        re = sys.exec2f('thing %s query \'th_cate:"%s", th_enabled:true\' -q %s',
                            params.pid, go.th_cate, jfmt);
        if(/^e./.test(re)) {
            sys.exec("ajaxre", re);
            return;
        }
        models = JSON.parse(re);
    }
    
    // 合并输出
    var reo = {
        current : go,
        cate    : cate,
        models  : models,
    };
    sys.out.println(JSON.stringify(reo, null, '    '));
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
