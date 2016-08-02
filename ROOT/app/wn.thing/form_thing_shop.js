([{
    key :"id",
    title :"ID",
    hide :true,
    type :"string",
    editAs :"label"
}, {
    key :"nm",
    title :"店铺名称",
    type :"string",
    editAs :"input"
}, {
    key :"cate",
    title :"经营范围",
    type :"string",
    editAs :"droplist",
    uiWidth : 120,
    uiConf : {
        items : ["餐馆", "咖啡厅", "休闲", "桌游", "酒吧", "日料", "韩餐", "意餐", "法餐"]
    }
}, {
    key :"brief",
    title :"店铺介绍",
    type :"string",
    editAs :"text"
}, {
    key :"city",
    title :"所在城市",
    type :"string",
    editAs :"droplist",
    uiWidth : 120,
    uiConf : {
        items : ["北京", "上海", "广州", "深圳", "厦门", "西安", "珠海", "天津", "沈阳"]
    }
}, {
    key :"lm",
    title :"最后修改时间",
    type :"datetime",
    editAs :"label"
}])