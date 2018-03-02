/*
提供了 Thing 对象商品形态的复合查询

本函数假设有下面格式参数输入

params: {
    pid  : "xxxx",  // 「必」商品库ID
    cid  : "xxxx",  // 「选」商品详情库ID
    id   : "xxxx",  // 「必」商品ID
}

本接口返回的数据是

{
    meta    : {..}    // 商品的数据
    detail  : {..}    // 商品详情的元数据
    models  : [{      // 同类别下还有哪些型号（去除重复后）
        id    : "xxx",      // 对应产品 ID
        text  : "xxx",      // 型号的 名称
        thumb : "id:xxx"    // 「选」型号的缩略图
    }],
    colors  : [{      // 「可选」当前型号还有哪些可选颜色或者图案
        id   : "xxx",       // 对应产品的 ID
        text : "Red",       // 颜色或者图案名称
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
    var jfmt = ' -json \'{locked:"^(_.+|c|m|g|md|race|ct|lm|d0|d1|ph)\\$", compact:false}\'';
    //---------------------------------------
    // 获取一下商品数据
    var re = sys.exec2f('thing %s get %s -full -q %s', 
                        params.pid, params.id, jfmt);
    if(/^e./.test(re)) {
        sys.exec("ajaxre", re);
        return;
    }
    var go = JSON.parse(re);
        
    //---------------------------------------
    // 获取商品的详情
    var gD = null;
    if(params.cid) {
        re = sys.exec2f('thing %s query \'th_nm:"%s"\' -q  -content', 
            params.cid, go.th_model);
        var reList = JSON.parse(re);
        if(reList.length > 0) {
            gD = reList[0];
        }
    }

    //---------------------------------------
    // 获取一下商品类型数据
    var cate = null;
    var models = [];
    var colors = [];
    if(go.th_cate) {

        // 查询一下符合与当前产品相同类型的其他产品
        var cnd = {th_cate:go.th_cate};
        if(go.lbls && go.lbls.length > 0) {
            cnd.lbls = go.lbls;
        }
        
        re = sys.exec2f('thing %s query -q \'%s\'', 
                params.pid, JSON.stringify(cnd));
        if(/^e./.test(re)) {
            sys.exec("ajaxre", re);
            return;
        }
        var list = JSON.parse(re);

        // 去除重复的型号，如果型号与当前型号相同，则归纳不同的颜色
        var mmap = {};
        var go_model = go.th_model || go.th_nm;
        for(var i=0 ; i<list.length; i++) {
            var mo = list[i];
            var model = mo.th_model || mo.th_nm;
            // 归纳型号
            if(!mmap[model])
                mmap[model] = mo;

            // 归纳颜色
            if(model == go_model && mo.th_color) {
                colors.push({
                    id   : mo.id,
                    text : mo.th_color
                });
            }
        }
        // 输出型号
        for(var key in mmap) {
            var mo = mmap[key];
            models.push({
                id    : mo.id,
                text  : key,
                thumb : mo.thumb || null
            });
        }
        // 对两个输出做排序(不需要考虑相等的问题)
        models.sort(function(a, b){
            return a.text > b.text ? 1 : -1;
        });
        colors.sort(function(a, b){
            return a.text > b.text ? 1 : -1;
        });
    }
    
    // 合并输出
    var reo = {
        meta    : go,
        detail  : gD,
        models  : models,
        colors  : colors,
    };
    sys.out.println(JSON.stringify(reo, null, '    '));
}
//........................................
// 执行: 需要 params 变量
_main(paramObj || {});
//........................................
