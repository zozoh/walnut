(function(){
//==================================================
function _eval_enum_text (fld, v){
    if(fld.uiConf && _.isArray(fld.uiConf.items)){
        // 默认值
        if(_.isUndefined(v))
            v = fld.dft;
        // 解析值
        var _val = _.isFunction(fld.uiConf.value)
                      ? fld.uiConf.value
                      : function(o, index){
                            if(_.isString(o))
                                return index;
                            return _.isUndefined(o.val) ? index : o.val;
                        };

        // 解析文字
        var _txt = function(o, index){
            if(_.isString(o))
                return o;
            if(_.isObject(o))
                return o.text || o.val+"" || (_.isUndefined(o)?index:o) + "";
            return o+"";
        };

        // 寻找值 
        for(var i=0; i<fld.uiConf.items.length; i++){
            var it = fld.uiConf.items[i];
            var iv = _val(it, i);
            if(iv == v)
                return _txt(it, i);
        }
    }
}
//==================================================
// 基础类型
var JsObj = function(fld){
    if(fld) this.__init__(fld);
};
JsObj.prototype = {
    __init__ : function(fld){
        fld.type = fld.type || "string";
        this.__fld = fld;
    },
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    type : function(){
        return this.__fld;
    },
    value : function(){
        return this.__val;
    },
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    parseByObj : function(obj){
        var v = this.getValueFrom(obj);
        return this.parse(v);
    },
    getValueFrom : function(obj) {
        return $z.getValue(obj, this.__fld.key);
    },
    // 检查一个值是否合法，如果失败，返回错误对象
    // 错误对象由 invalid 函数定义
    // 如果成功，设置到对象的对应的键里
    setToObj : function(obj) {
        try{
            var val = this.toNative();
            $z.setValue(obj, this.__fld.key, val);
        }
        // 错误对象的定义参见 invalid 函数
        catch(E){
            return E;
        }
    },
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 子类将覆盖实现方法 _parse_and_return
    _parse_and_return : function(v){
        return v || this.__fld.dft;
    },
    parse : function(v) {
        // 准备要解析的值
        var val = _.isUndefined(v)||_.isNull(v)?this.__fld.dft:v;
        // 调用子类的实现
        var v0 = this._parse_and_return(val);
        // 采用默认值
        this.__val = v0;
        
        // 返回自身以便链式复制
        return this;
    },
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    toString : function(){
        return this.toStr();
    },
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    toText : function() {
        return $z.toJson(this.__val);
    },
    toStr : function(){
        return $z.toJson(this.__val);
    },
    toNumber : function(){
        return -1;
    },
    toInt : function(){
        return parseInt(this.toNumber());
    },
    toBoolean : function(){
        return this.__val ? true : false;
    },
    // 返回可以便于传输的值表达形式
    toNative : function(){
        switch(this.__fld.nativeAs){
            case "string" : 
                return this.toStr();
            case "int" :
                return this.toInt();
            case "float" :
                return this.toNumber();
            case "boolean" : 
                return this.toBoolean();
        }
        // 默认直接返回
        return this.__val;
    },
    clone  : function(){
        return new JsObj(this.__fld).parse(this.__val);
    },
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    invalid : function(v){
        return  {
            code : "e.fld.invalid." + this.__fld.type,
            date : v || this.__val
        };
    }
};
//..................................................
// 支持正则表达式验证: fld.validate
var JsString = function(fld){this.__init__(fld);};
JsString.prototype = new JsObj();
JsString.prototype._parse_and_return = function(v){
    // 采用默认
    if(_.isUndefined(v) || _.isNull(v)){
        return v;
    }
    // 分析
    v = "" + v;
    if(_.isRegExp(this.__fld.validate)){
        var regex = new RegExp(fld.validate);
        if(!regex.test(this.__v)){
            throw this.invalid(v);
        }
    }
    return v;
};
JsString.prototype.toText = function(){
    return _eval_enum_text(this.__fld, this.__val) || this.__val;
};
JsString.prototype.toStr = function(){
    return this.__val;
};
JsString.prototype.toNumber = function(){
    return this.__val * 1;
};
JsString.prototype.toBoolean = function(){
    return /^(yes|on|true)$/.test(this.__val);
};
JsString.prototype.clone = function(){
    return new JsString(this.__fld).parse(this.__val);
};
/*..................................................
标准格式为:  [Date(), Date()]
fld {
    validate : @see $z.parseData 支持的正则表达式 
    format   : "yyyy-mm-dd"
}
*/
var JsDateRange = function(fld){
    this.__init__(fld);
    this.__fld.nativeAs = fld.nativeAs || "int";
};
JsDateRange.prototype = new JsObj();
JsDateRange.prototype._parse_and_return = function(v){
    if(!v || v.length == 0){
        return undefined;
    }

    if(_.isString(v)){
        v = $.trim(v).split(",");
    }

    // 必须是数组
    if(!_.isArray(v))
        v = [v];
    
    // 确保数组内每个元素都是日期对象
    var re = [];
    for(var i=0;i<Math.max(2,v.length);i++){
        re[i] = $z.parseDate(v[i], this.__fld.validate);
    }

    // 确保这是一个二元数组，空的话，选择今天
    if(re.length == 0){
        var today = new Date().format("yyyy-mm-dd");
        re = [new Date(today+" 00:00:00"), new Date(today+" 23:59:59")];
    }
    // 否则，和今天进行相比，组成一个数组
    else if(re.length == 1){
        var ms = re[0].getTime();
        var now = new Date();
        // 在今天之前...
        if(now.getTime() > ms){
            re = [re[0], now];
        }
        // 在今天之后
        else{
            re = [now, re[0]];
        }
    }

    // 返回
    return re;
};
JsDateRange.prototype.toText = function(){
     return this.toStr();
};
JsDateRange.prototype.toStr = function(){
    var ss = [];
    for(var i=0; i<this.__val.length; i++)
        ss.push(this.__val[i].format(this.__fld.format || "yyyy-mm-dd"))
    return ss.join(",");
};
JsDateRange.prototype.toNumber = function(){
    if(this.__val.length>1){
        return this.__v[1].getTime() - this.__v[0].getTime();
    }
    return this.__v[0].getTime();

};
JsDateRange.prototype.toNative = function(){
    var re = [];
    for(var i=0; i<this.__val.length;i++){
        var d = this.__val[i];
        // 变字符串
        if("string" == this.__fld.nativeAs){
            re.push(d.format(this.__fld.format || "yyyy-mm-dd HH:MM:ss"));
        }
        // 否则就变整数
        else{
            re.push(d.getTime());
        }
    }
    return re;
};
JsDateRange.prototype.toBoolean = function(){
    return this.toNumber() > 0;
};
JsDateRange.prototype.clone = function(){
    return new JsDateRange(this.__fld).parse(this.__val);
};
/*..................................................
标准格式为:  Date()
fld {
    validate : @see $z.parseData 支持的正则表达式 
    format   : "yyyy-mm-dd"
    nativeAs : "string | int"   // 原生的字段值是字符串还是毫秒数，默认 int
}
*/
var JsDateTime = function(fld){
    this.__init__(fld);
    this.__fld.nativeAs = fld.nativeAs || "int";
};
JsDateTime.prototype = new JsObj();
JsDateTime.prototype._parse_and_return = function(v){
    return $z.parseDate(v, this.__fld.validate);
};
JsDateTime.prototype.toText = function(){
     return this.toStr();
};
JsDateTime.prototype.toStr = function(){
    if(this.__val)
        return this.__val.format(this.__fld.format || "yyyy-mm-dd HH:MM:ss");
    return "";
};
JsDateTime.prototype.toNumber = function(){
    return this.__val ? this.__val.getTime() : 0;
};
JsDateTime.prototype.toBoolean = function(){
    return this.toNumber() > 0;
};
JsDateTime.prototype.clone = function(){
    return new JsDateTime(this.__fld).parse(this.__val);
};
/*..................................................
标准格式为:  $z.parseTime() 输出的格式
fld {
    validate : @see $z.parseTime 支持的正则表达式
               支持 "@min" 快捷表示 /^(\d{1,2}):(\d{1,2})$/
    nativeAs : "string | int"   // 原生的字段值是字符串还是毫秒数，默认 int
}
*/
var JsTime = function(fld){
    this.__init__(fld);
    this.__fld.nativeAs = fld.nativeAs || "int";
};
JsTime.prototype = new JsObj();
JsTime.prototype._parse_and_return = function(v){
    return $z.parseTime(v, this.__fld.validate);
};
JsTime.prototype.toText = function(){
     return this.__val.key;
};
JsTime.prototype.toStr = function(){
    return this.__val.key;
};
JsTime.prototype.toInt = function(){
    return this.__val.sec;
};
JsTime.prototype.toNumber = function(){
    return this.__val.sec;
};
JsTime.prototype.toBoolean = function(){
    return true;
};
JsTime.prototype.clone = function(){
    return new JsTime(this.__fld).parse(this.__val);
};
/*..................................................
标准格式为 Number
*/
var JsInt = function(fld){
    this.__init__(fld);
    if(!_.isNumber(this.__fld.dft))
        this.__fld.dft = -1;
};
JsInt.prototype = new JsObj();
JsInt.prototype._parse_and_return = function(v){
    return parseInt(v);
};
JsInt.prototype.toText = function(){
     return _eval_enum_text(this.__fld, this.__val) || this.toStr();
};
JsInt.prototype.toStr = function(){
    return this.__val + "";
};
JsInt.prototype.toInt = function(){
    return this.__val;
};
JsInt.prototype.toNumber = function(){
    return this.__val;
};
JsInt.prototype.toBoolean = function(){
    return this.__val == 0 ? false : true;
};
JsInt.prototype.clone = function(){
    return new JsInt(this.__fld).parse(this.__val);
};
/*..................................................
标准格式为 Number
*/
var JsFloat = function(fld){
    this.__init__(fld);
    if(!_.isNumber(this.__fld.dft))
        this.__fld.dft = 0;
};
JsFloat.prototype = new JsObj();
JsFloat.prototype._parse_and_return = function(v){
    return v * 1;
};
JsFloat.prototype.toText = function(){
     return _eval_enum_text(this.__fld, this.__val) || this.toStr();
};
JsFloat.prototype.toStr = function(){
    return this.__val + "";
};
JsFloat.prototype.toInt = function(){
    return this.__val;
};
JsFloat.prototype.toNumber = function(){
    return this.__val;
};
JsFloat.prototype.toBoolean = function(){
    return this.__val == 0 ? false : true;
};
JsFloat.prototype.clone = function(){
    return new JsFloat(this.__fld).parse(this.__val);
};
/*..................................................
标准格式为 Boolean
*/
var JsBoolean = function(fld){
    this.__init__(fld);
    if(!_.isBoolean(this.__fld.dft))
        this.__fld.dft = false;
};
JsBoolean.prototype = new JsObj();
JsBoolean.prototype._parse_and_return = function(v){
    if(_.isString(v)){
        return /^yes|on|true$/i.test(v);
    }
    return v ? true : false;;
};
JsBoolean.prototype.toText = function(){
     return _eval_enum_text(this.__fld, this.__val) || this.toStr();
};
JsBoolean.prototype.toStr = function(){
    return this.__val ? "true" : "false";
};
JsBoolean.prototype.toInt = function(){
    return this.__val ? 1 : 0;
};
JsBoolean.prototype.toNumber = function(){
    return this.__val ? 1 : 0;
};
JsBoolean.prototype.toBoolean = function(){
    return this.__val;
};
JsBoolean.prototype.clone = function(){
    return new JsBoolean(this.__fld).parse(this.__val);
};
//..................................................
var jType = function(fld) {
    switch(fld.type){
        case "object" : 
            return new JsObj(fld);

        case "daterange" : 
            return new JsDateRange(fld);

        case "datetime" : 
            return new JsDateTime(fld);

        case "time" : 
            return new JsTime(fld);

        case "int" : 
            return new JsInt(fld);

        case "float" : 
            return new JsFloat(fld);

        case "boolean" : 
            return new JsBoolean(fld);
            
    }
    // 默认当做字符串
    return new JsString(fld);
};
//..................................................
// 挂载到 window 对象
window.jType = jType;

// TODO 支持 AMD | CMD 
if (typeof define === "function") {
    // CMD
    if(define.cmd) {
        define(function (require, exports, module) {
            module.exports = jType;
        });
    }
    // AMD
    else {
        define("jtype", [], function () {
            return jType;
        });
    }
}
//==================================================
})();