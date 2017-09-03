(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
    <div style="position:absolute; padding:6px;">
    <button class="get_data">GET DATA</button>
    </div>
    <div class="myform" ui-gasket="myform" style="width:100%; height:100%; "></div>
</div>
*/};
//===================================================================
return ZUI.def("ui.test_form3", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .get_data" : function(){
            console.log(this.subUI("myform").getData());
        },
    },
    //...............................................................
    update : function(o){
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "myform",
            on_change : function(key, val){
                console.log("form change:", key, val);
            },
            title : "更多测试表单",
            uiWidth : "all",
            fields : [{
                key   : "poli",
                title : "政治面貌",
                tip   : "你就看着填吧",
                type  : "int",
                editAs : "droplist",
                emptyArrayAsUndefined : true,
                uiWidth : "auto",
                dft : 1,
                uiConf : {
                    items : ["党员","团员","群众","敌特"]
                }
            },{
                key   : "nationality",
                title : "国籍",
                tip   : "你就看着填吧",
                type  : "object",
                editAs : "droplist",
                emptyArrayAsUndefined : true,
                uiWidth : "auto",
                dft : 1,
                uiConf : {
                    multi : 1,
                    items : ["中国","美国","日本","天堂","地狱","!!!一个随便什么不知道特别名字的地方我很想去但是去不了的地方*","A","B","C","D","E"]
                }
            },{
                key   : "FTF",
                title : "饮食口味",
                tip   : "你就看着填吧",
                type  : "object",
                editAs : "checklist",
                dft   : 1,
                uiConf : {
                    items : [{
                        icon:'<i class="fa fa-fire"></i>',
                        text:"辣"
                    },{
                        text:"酸"
                    },{
                        text:"清淡再清淡"
                    },{
                        text:"卤味"
                    }]
                }
            },{
                key   : "Fleval",
                title : "攻击能力级别",
                tip   : "给你一拳看你是不是能受得了",
                type  : "int",
                editAs : "radiolist",
                dft   : 1,
                uiConf : {
                    items : ["蠢萌","很厉害","靠无敌了","弗利萨"]
                }
            },{
                key   : "yaoyao",
                title : "抓药",
                type  : "string",
                editAs : "radiolist",
                dft   : "全部抓药 全部代煎 等取",
                uiConf : {
                    items : [{val:"全部抓药 全部代煎 送",text:"全部抓药 全部代煎 送  "},{val:"全部抓药 全部代煎 等取",text:"全部抓药 全部代煎 等取 "},{val:"全部抓药 自煎",text:"全部抓药 自煎 "},{val:"全部抓药 自煎 （三七自备）",text:"全部抓药 自煎 （三七自备）"},{val:"全部抓药 自煎 顺丰",text:"全部抓药 自煎 顺丰"},{val:"7付代煎等取，7付自煎",text:"7付代煎等取，7付自煎 "},{val:"只拿7付代煎",text:"只拿7付代煎 "},{val:"不抓药",text:"不抓药"},{val:"待定",text:"待定"}]
                }
            },{
                key   : "myphoto",
                title : "我的图片",
                tip   : "随便选个图片",
                type  : "object",
                uiWidth : "auto",
                uiType : "ui/picker/opicker",
                uiConf : {
                    clearable : false,
                    parseData : function(obj){
                        if(obj)
                            return Wn.getById(obj.fid);
                    },
                    formatData : function(o){
                        return o ? {fid:o.id} : null;
                    }
                }
            },{
                key   : "myfiles",
                title : "我的多个文件",
                tip   : "随便选个一些文件和文件夹咯",
                type  : "object",
                //dft   : [{fid:'t6c2m2r5a0htep2rto785n588f'},{fid:'3d7gf4mdtghbqqrknp2snkpo3a'}],
                uiType : "ui/picker/opicker",
                uiConf : {
                    setup : {
                        checkable : true
                    },
                    parseData : function(objs){
                        var re = [];
                        if(objs){
                            objs.forEach(function(o, index){
                                re.push(Wn.getById(o.fid));
                            });
                        }
                        return re;
                    },
                    formatData : function(os){
                        var re = [];
                        os.forEach(function(o, index){
                            re.push({fid:o.id});
                        });
                        return re;
                    }
                }
            },{            
                key   : "comment",
                title : "补充说明",
                tip   : "随便写点什么咯",
                type  : "string",
                editAs : "text",
                uiConf : {
                    height: 120
                }
            }]
        }).render(function(){
            this.setData({
                id: o.id,
                x:100, y:80, 
                birthday : "1977-09-21",
                name:'I am zozoh', 
                favColor : null,
                sex:"m",
                live:true,
                //myphoto : {fid:'4thoboi83khmdqmqqvf5arogki'}
            });
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);