/**
提供运行时的帮助函数集合，IDE 也会用的到
*/
(function($, $z){
window.HmRT = {
    //...............................................................
    // 自动寻找一个合适的详情页面
    // - obj 表示要渲染的数据
    // - sitemap 表示站点索引数据，默认取 window.__SITEMAP
    explainAutoHref : function(obj, sitemap) {
        sitemap = sitemap || window.__SITEMAP;
        if(sitemap && obj.th_set){
            for(var rph in sitemap) {
                var oPage = sitemap[rph];
                // 如果符合目标对象所属的 th_set
                // API 返回的数据类型为 obj | goods 
                if(oPage.hm_pg_tsid == obj.th_set
                    && /^(obj|goods)$/.test(oPage.hm_api_return)) {
                    // 那么就是它了
                    return window.__ROOT_PATH + rph + ".html";
                }
            }
        }
    },
    //...............................................................
    // 通用的解析一个链接的方法，支持 @auto
    // - href 链接，@auto 的话则自动调用 explainAutoHref
    // - obj 表示要渲染的数据
    // - isIDE 表示在 IDE 里，那么就应该总是返回 href
    explainHref : function(href, obj, isIDE) {
        // 试图自动寻找链接
        if("@auto" == href && !isIDE) {
            return HmRT.explainAutoHref(obj);
        }
        return href;
    },
    //...............................................................
    // 判断一个模板的数据类型能否与 api 的返回值匹配
    isMatchDataType : function(apiReTypes, tmplDataType){
        if(!apiReTypes || !tmplDataType)
            return false;
        if(!_.isArray(apiReTypes)){
            apiReTypes = [apiReTypes];
        }

        for(var i=0;i<apiReTypes.length;i++) {
            var apiReType = apiReTypes[i];
            if(_.isArray(tmplDataType)){
                if(tmplDataType.indexOf(apiReType) < 0)
                    return false
            }
            else if(apiReType != tmplDataType){
                return false;
            }
        }

        return true;
    },
    //...............................................................
    // 将一个数据尽量转换成模板能支持的数据类型
    // 如果转换失败将抛错, null 将被原样返回
    convertDataForTmpl : function(data, tmplDataType){
        if(HmRT.isMatchDataType(["page", "list"], tmplDataType)){
            if(data.list && data.pager)
                return data.list;
            if(_.isArray(data))
                return data;
            throw "api_data_nomatch : API(" + data + ") != Template("+tmplDataType+")";
        }
        return data;
    },
    //...............................................................
    // 根据模板的数据类型，判断给定数据是否为空
    isDataEmptyForTmpl : function(data, tmplDataType) {
        if(!data)
            return true;
        if(HmRT.isMatchDataType(["page", "list"], tmplDataType)){
            if(!_.isArray(data) || data.length == 0)
                return true;
        }
        return false;
    },
    /*...............................................................
    解析动态设置的 setting 对象，
    函数接受 :
    - setting : {..}     // 符合动态设置规范的 JSON 对象
    - asMap : false      // 默认返回数组，本选项可以让返回值变成对象，键为 key

    函数返回（基本符合 form 控件的field定义）:
    [{
        type     : "thingset",  // 项目类型
        arg      : "xxx",       // 项目参数
        dft      : "xxx",       // 项目默认值
        mapping  : {..}         // 映射表（基本只有@com类型才会有用）
        required : true,        // 字段是否必须
        key      : "xxx",       // 字段名
        title    : "xxx",       // 字段显示名
        tip      : "xxx",       // 提示信息
    }, {
        // 下一个选项
    }]
    */
    parseSetting : function(setting, asMap) {
        var re = asMap ? {} : [];
        // 循环
        for(var key in setting) {
            var val = setting[key];
            // 默认的字段
            var fld = {
                type     : "input",
                arg      : undefined,
                dft      : undefined,
                key      : key,
                title    : undefined,
                required : false,
                mapping  : undefined,
                tip      : undefined,
            };

            // 字符串形式
            if(_.isString(val)) {
                // 分析一下
                var m = /^([*])?(\(([^\)]+)\))?@(input|TSS|thingset|site|com|link|toggle|switch|droplist|fields)(=([^:#{]*))?(:([^#{]*))?(\{[^}]*\})?(#(.*))?$/.exec(val);
                // 指定了类型
                if(m) {
                    fld.required = m[1] ? true : false;
                    fld.title = m[3];
                    fld.type  = m[4];
                    fld.dft   = m[6];
                    fld.arg   = m[8];
                    fld.mapping = m[9] ? $z.fromJson(m[9]) : null;
                    fld.tip   = m[11];
                }
            }
            // 对象形式
            else if(_.isObject(val)){
                _.extend(fld, val, {
                    key : key
                });
            }
            // 靠什么鬼!
            else {
                throw "unsupport setting value type<" + (typeof val) + "> : " + val;
            }

            // 计入结果
            if(asMap) {
                re[key] = fld;
            }else{
                re.push(fld);
            }
        }

        //console.log(re);

        // 返回
        return re;
    },
    /*...............................................................
    解析参数表的值，无论是接口参数表还是模板选项都，通用
    函数接受 : 
     - result : {..}        // 动态设置的 result 对象
     - opt : {              // 配置信息
        // 替换参数值的上下文
        context : {..},

        // 动态设置的配置信息，必须为 Map 形式！！！不能是数组
        setting : {..},

        // 页面请求的参数表，动态值会从里面取
        // 如果取不到，则用默认值，默认值也木有，则记入 lackKeys
        // 当然，在 IDE 调用的时候，是木有这个参数的
        request : {..},

        // 获取控件值的函数
        getComValue: F(comId):Object,

        // 是否截取 result 值的空白，默认 true
        trimValue : true,
     }
    函数返回 : {
        dynamicKeys : ["c", "site"],  // 动态参数名
        lackKeys    : ["c"],          // 标识了 required 的字段，有哪些没值
        data : {..},                  // 重新填充完毕的数据，可以直接被提交
        
    }
    */
    evalResult : function(result, opt){
        // 准备返回值
        var re = {
            dynamicKeys : [],
            lackKeys    : [],
            data : {},
        };
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 处理默认的选项
        opt.trimValue = opt.trimValue === false ? false : true;
        opt.context = opt.context || {};
        opt.request = opt.request || {};
        opt.setting = opt.setting || {};
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 循环处理 ...
        for(var key in result) {
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 得到值
            var val = result[key];
            if(opt.trimValue)
                val = $.trim(val);
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 进行标准占位符替换
            var v2 = $z.tmpl(val, {
                escape: /\$\{([\s\S]+?)\}/g
            })(opt.context);
            //console.log(key, val, v2);

            // 特殊类型的值
            // TODO Session 变量
            // TODO Cookie 的值
            var m = /^([@#])(<(.+)>)?(.*)$/.exec(v2);
            if(m && m[2]) {
                var p_tp  = m[1];
                var p_val = m[3];
                var p_arg = $.trim(m[4]);
                //console.log(m, p_tp, p_val, p_arg);
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 动态参数: 这里就直接取默认值了
                if("@" == p_tp) {
                    re.dynamicKeys.push(key);
                    p_val = opt.request[p_val] || p_arg;
                    if(p_val) {
                        re.data[key] = p_val;
                    }
                    // 继续吧
                    continue;
                }
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 来自控件
                if("#" == p_tp) {
                    // 得到控件的值，如果有值就填充到参数表
                    var comVal = $z.invoke(opt, "getComValue", [p_val]);
                    if(comVal) {
                        // 得到对应动态设置项
                        var conf = opt.setting[key] || {};
                        // 得到映射表
                        if(conf.mapping) {
                            // 融合到参数表同时进行映射
                            try {
                                for(var key in conf.mapping) {
                                    re.data[key] = comVal[conf.mapping[key]];
                                }
                            }
                            // 出错了
                            catch(E){
                                throw "e_p_mapping : " + p_arg; 
                            }
                        }
                        // 直接将控件返回值融合到参数表
                        else {
                            // 对象
                            if(_.isObject(comVal)) {
                                _.extend(re.data, comVal);
                            }
                            // 普通值，直接填充
                            else {
                                re.data[key] = comVal;
                            }
                        }
                    }
                    // 继续下一个参数
                    continue;
                }
            }
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // 普通值
            re.data[key] = v2;
        } // ~ for(var key in result)
        
        // 随后搜索一轮缺失的值
        for(var key in opt.setting) {
            if(opt.setting[key] && opt.setting[key].required){
                var v = re.data[key];
                if(_.isUndefined(v) || _.isNull(v) || (_.isString(v) && v.length==0))
                    re.lackKeys.push(key);
            }
        }

        // 搞定，收工
        return re;
    },
    //...............................................................
    // 触发本 jQuery 插件对应的 dynamic 控件的重新加载行为
    invokeDynamicReload : function(jq, jumpToHead) {
        var jCom = jq.closest("[hm-dynamic-id]");
        var dyId = jCom.attr("hm-dynamic-id");
        if(dyId) {
            $("#"+dyId+" > .hmc-dynamic")
                .hmc_dynamic("reload", jumpToHead, function(){
                    // 重新调整皮肤尺寸
                    if(window.__wn_skin_context) {
                        var SC = window.__wn_skin_context;
                        $z.invoke(SC.skin, "ready", [], SC);
                        $z.invoke(SC.skin, "resize", [], SC);
                    }        
                });
        }
    },
    //...............................................................
    // 将一个 Thing 格式的对象的 markdown 内容转换成 html
    // - API : 一个 regapi 的前缀
    // - th  : Thing 对象，里面需要有 content, th_set, id 这几个字段
    thContentToHtml : function(API, th){
        return $z.markdownToHtml(th.content||"", {
            media : function(src){
                // 看看是否是媒体
                var m = /^media\/(.+)$/.exec(src);
                if(m){
                    return API + "/thing/media"
                            + "?pid=" + th.th_set
                            + "&id="  + th.id
                            + "&fnm=" + m[1];
                }
                // 原样返回
                return src;
            }
        });
    },
    //...............................................................
};  // ~ window.HmRT =
})(window.jQuery, window.NutzUtil);