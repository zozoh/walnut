/*
解析数据类型，对于一个数据类型的定义 fld 如下:
{
    key      : "nm"          // 字段对应的对象键
    icon     : HTML          // 字段的图标
    text     : "i18n:xxx"    // 字段的标题
    tip      : "i18n:xxx",   // 字段的提示说明

    type     : "string"      // 【String】值的类型,下面有详细介绍
    editAs   : "input"       // 【input】 编辑控件,下面有详细介绍
    dft      : xxx           // 默认值
}

parse      方法将任何值根据 fld 的定义，转换成自己的标准对象
toStr      方法将标准对象变成字符串
toInt      方法将标准对象变成整数
toBool     方法将标准对象变成布尔
toNative   方法将标准对象变成对应的 JS 原生对象
clone      方法则将标准对象复制一份
*/
define(function (require, exports, module) {
//==================================================
module.exports = {
    //......................................
    "object" : {
        parse : function(fld, v){
            // DOM
            if(_.isElement(v) || $z.isjQuery(v)){
                return $(v)[0].outerHTML;
            }
            // 字符串要变 JSON
            else if(_.isString(v)){
                return $z.fromJson(v);
            }
            // 空数组的问题
            if(fld.emptyArrayAsUndefined && _.isArray(v) && v.length == 0)
                return undefined;
            // 其他的直接返回就好
            return v ? v : fld.dft;
        },
        toStr : function(fld, v){
            return $z.toJson(v);
        },
        toInt : function(fld, v){
            return -1;
        },
        toBool : function(fld, v){
            return v ? true : false;
        },
        toNative : function(fld, v){
            return v;
        },
        clone  : function(fld, v){
            return $z.clone(v);
        }
    },
    //......................................
    // 支持正则表达式验证: fld.validate
    "string" : {
        parse : function(fld, v){
            if(_.isUndefined(v) || _.isNull(v)){
                return fld.dft;
            }
            v = "" + v;
            if(_.isRegExp(fld.validate)){
                var regex = new RegExp(fld.validate);
                if(!regex.test(v)){
                    throw _E("i18n:e.fld.invalid.string",fld,v);
                }
            }
            return v || fld.dft;
        },
        toStr : function(fld, v){
            return v;
        },
        toInt : function(fld, v){
            return v * 1;
        },
        toBool : function(fld, v){
            return v ? true : false;
        },
        toNative : function(fld, v){
            return v;
        },
        clone  : function(fld, v){
            return v;
        }
    },
    /*......................................
    标准格式为:  [Date(), Date()]
    fld {
        validate : @see $z.parseData 支持的正则表达式 
        format   : "yyyy-mm-dd"
    }
    */
    "daterange" : {
        parse : function(fld, v){
            if(_.isString(v)){
                v = $.trim(v).split(",");
            }
            // 必须是数组
            if(!_.isArray(v))
                return fld.dft;
            
            //console.log("fld test : ", fld.key, ":["+v+"]", "test:", (v?true:false));
            for(var i=0;i<Math.max(2,v.length);i++){
                var item = v[i];
                if(_.isString(item)){
                    v[i] = $z.parseDate($.trim(item), fld.validate);
                } else {
                    v[i] = $z.parseDate(item);
                }
            }
            return v;
        },
        toStr : function(fld, v){
            var ss = [];
            for(var i=0; i<v.length; i++)
                ss.push(v[i].format(fld.format || "yyyy-mm-dd"))
            return ss.join(",");
        },
        toInt : function(fld, v){
            if(v.length>1){
                return v[1].getTime() - v[0].getTime();
            }
            return v[0].getTime();
        },
        toBool : function(fld, v){
            return v ? true : false;
        },
        toNative : function(fld, v){
            return this.toStr(fld, v);
        },
        clone  : function(fld, v){
            var re = [];
            for(var i=0; i<v.length; i++)
                ss.push(v[i].format(fld.format || "yyyy-mm-dd"));
            return re;
        }
    },
    /*......................................
    标准格式为:  Date()
    fld {
        validate : @see $z.parseData 支持的正则表达式 
        format   : "yyyy-mm-dd"
        nativeAs : "string | int"   // 原生的字段值是字符串还是毫秒数，默认 int
    }
    */
    "datetime" : {
        parse : function(fld, v){
            // 本身就是标准值
            if(_.isDate(v)){
                return v;
            }
            // 字符串需要去掉空白
            if(_.isString(v)){
                v = $.trim(v);
            }
            if(!v)
                v = fld.dft;
            if(!v)
                return null;
            //console.log("fld test : ", fld.key, ":["+v+"]", "test:", (v?true:false));
            return $z.parseDate(v, fld.validate);
        },
        toStr : function(fld, v){
            return v.format(fld.format || "yyyy-mm-dd");
        },
        toInt : function(fld, v){
            return v.getTime();
        },
        toBool : function(fld, v){
            return true;
        },
        toNative : function(fld, v){
            if("string" == fld.nativeAs)
                return this.toStr(fld, v);
            return v.getTime();
        },
        clone  : function(fld, v){
            return new Date(v);
        }
    },
    /*......................................
    标准格式为:  $z.parseTime() 输出的格式
    fld {
        validate : @see $z.parseTime 支持的正则表达式
                   支持 "@min" 快捷表示 /^(\d{1,2}):(\d{1,2})$/
        nativeAs : "string | int"   // 原生的字段值是字符串还是毫秒数，默认 int
    }
    */
    "time" : {
        parse : function(fld, v){
            // 本身就是标准值
            if(_.isObject(v) && v.HH && v.mm && v.ss)
                return v;

            var vali = fld.validate;
            if("@min" == vali)
                vali = /^(\d{1,2}):(\d{1,2})$/;
            return $z.parseTime(v, vali);
        },
        toStr : function(fld, v){
            return v.key;
        },
        toInt : function(fld, v){
            return v.sec;
        },
        toBool : function(fld, v){
            return v ? true : false;
        },
        toNative : function(fld, v){
            if("string" == fld.nativeAs)
                return v.key;
            return v.sec;
        },
        clone  : function(fld, v){
            return _.extend({}, v);
        }
    },
    /*......................................
    标准格式为 Number
    fld {
        
    }
    */
    "int" : {
        parse : function(fld, v){
            var re = parseInt(v);
            return isNaN(re) ? fld.dft : re;
        },
        toStr : function(fld, v){
            return "" + v;
        },
        toInt : function(fld, v){
            return v;
        },
        toBool : function(fld, v){
            return v ? true : false;
        },
        toNative : function(fld, v){
            return v;
        },
        clone  : function(fld, v){
            return v;
        }
    },
    /*......................................
    标准格式为 Number
    fld {
        
    }
    */
    "boolean" : {
        parse : function(fld, v){
            if(_.isString(v)){
                return /^yes|on|true$/i.test(v);
            }
            return v ? true : false;
        },
        toStr : function(fld, v){
            return v ? "true" : "false";
        },
        toInt : function(fld, v){
            return v ? 1 : 0;
        },
        toBool : function(fld, v){
            return v;
        },
        toNative : function(fld, v){
            return v;
        },
        clone  : function(fld, v){
            return v;
        }
    }
};
//==================================================
});