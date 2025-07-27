//-----------------------------------------
// 先来几个帮助方法
// load("nashorn:mozilla_compat.js");
// importPackage(java.util);
// importPackage(org.nutz.lang.util);
// importPackage(org.nutz.lang);
// importPackage(org.nutz.walnut.api.io);

(function (sys, $log) {
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

    getMyName: function () {
      var re = sys.exec2("me -json -cqn | jsonx @tmpl '${nm}'");
      return re.trim();
    },

    checkExecReturn: function (re) {
      if (/^e\./.test(re)) {
        var pos = re.indexOf(":");
        var err = new Error(re);
        err.__is_error = true;
        err.ok = false;
        if (pos > 0) {
          err.code = re.substring(0, pos).trim();
          err.reason = re.substring(pos + 1).trim();
        } else {
          err.code = re;
          err.reason = null;
        }
        return err;
      }
    },

    checkExecReturnAndParseJson: function (re, returnError) {
      var reo = this.checkExecReturn(re);
      if (!reo) {
        return JSON.parse(re);
      }
      if (returnError) return reo;
    },

    exec: function (cmdText, input) {
      var re;
      if (input) {
        re = sys.exec2(cmdText, input).trim();
      } else {
        re = sys.exec2(cmdText).trim();
      }
      var err = this.checkExecReturn(re);
      if (err) throw err;
      return re;
    },

    execAsJson: function (cmdText, input) {
      var re = this.exec(cmdText, input);
      return JSON.parse(re);
    },

    tryLock: function(lockName, duration) {
      if (!duration || duration < 0) {
        duration = 3600;
      }
      var lockName = "job_booking";
      var cmdText = "lock -cqn @try -du " + duration + " '" + lockName + "'";
      $log.info("Try lock: %s", cmdText);
      var re = sys.exec2(cmdText);
      return JSON.parse(re);
    },

    freeLock: function(lock) {
      if (!lock) return;
      cmdText = "lock -cqn @free '" + lock.name + "' '" + lock.privateKey + "'";
      $log.info("Release lock: %s", cmdText);
      re = sys.exec2(cmdText);
      $log.info("Release lock result: %s", re);
    },
  };
});
