//-----------------------------------------
// 先来几个帮助方法
// load("nashorn:mozilla_compat.js");
// importPackage(java.util);
// importPackage(org.nutz.lang.util);
// importPackage(org.nutz.lang);
// importPackage(org.nutz.walnut.api.io);

(function (sys) {
  return {
    _toObj: function (obj) {
      if (typeof obj == "string") {
        obj = obj.trim();
        if (/^\{.+\}$/.test(obj)) {
          obj = eval("(" + obj + ")");
        }
      }
      return obj;
    },

    /**
     * js转换为json字符串
     */
    toJsonStr: function (jsobj) {
      if (typeof jsobj == "string") {
        return jsobj;
      }
      return JSON.stringify(jsobj);
    },

    getObjById: function (id) {
      var objStr = sys.exec2("o 'id:" + id + "' @json -cqn");
      return JSON.parse("(" + objStr + ")");
    },
  };
});
