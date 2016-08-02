([{
    key :"id",
    title :"ID",
    hide :true,
    type :"string",
    editAs :"label"
}, {
    key :"title",
    title :"标题",
    type :"string",
    editAs :"input"
}, {
    key :"brief",
    title :"摘要",
    type :"string",
    editAs :"text"
}, {
    key :"content",
    title :"正文",
    hide :true,
    type :"string",
    editAs :"text",
    uiConf : {
        height : 600
    }
}, {
    key :"lm",
    title :"最后修改时间",
    type :"datetime",
    editAs :"label"
}])