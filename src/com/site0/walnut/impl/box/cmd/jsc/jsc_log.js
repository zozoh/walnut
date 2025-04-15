(function (sys, logx, traceID) {
  return {
    __log_tag: "-NoTag-",
    __debug: false,
    __trace_id: traceID,
    __prefix: null,
    __log_args: function __log_args(_arguments, offset) {
      var rt_msg, lg_msg;
      offset = offset || 0;
      if (this.__prefix) {
        rt_msg = this.__prefix + _arguments[offset];
        lg_msg = "%s [%s] : " + this.__prefix + _arguments[offset];
      } else {
        rt_msg = _arguments[offset];
        lg_msg = "%s [%s] : " + _arguments[offset];
      }
      var rt_args = [];
      var lg_args = [this.__log_tag, this.__trace_id];
      for (var i = offset + 1; i < _arguments.length; i++) {
        rt_args.push(_arguments[i]);
        lg_args.push(_arguments[i]);
      }
      return [lg_msg, lg_args, rt_msg, rt_args];
    },
    setTag: function (tag) {
      this.__log_tag = tag;
    },
    setDebug: function (debug) {
      this.__debug = debug;
    },
    setTraceID: function (traceID) {
      this.__trace_id = traceID;
    },
    setPrefix: function (prefix) {
      this.__prefix = prefix;
    },
    setup: function (tag, debug, prefix) {
      if (tag) this.__log_tag = tag;
      if ("boolean" === typeof debug) this.__debug = debug;
      if ("string" == typeof prefix) this.__prefix = prefix;
    },
    info: function () {
      var aa = this.__log_args(arguments);
      logx.print("info", aa[0], aa[1]);
      if (this.__debug) sys.out.printlnf(aa[2], aa[3]);
    },
    warn: function () {
      var aa = this.__log_args(arguments);
      logx.print("warn", aa[0], aa[1]);
      if (this.__debug) sys.out.printlnf(aa[2], aa[3]);
    },
    error: function () {
      var aa = this.__log_args(arguments);
      logx.print("error", aa[0], aa[1]);
      if (this.__debug) sys.err.printlnf(aa[2], aa[3]);
    },
    printJsError: function () {
      var err = arguments[0];
      var aa;
      if (arguments.length > 1) {
        aa = this.__log_args(arguments, 1);
      } else {
        aa = this.__log_args([err, ""], 1);
      }
      var errStack = logx.getJsErrString(err);
      aa[0] = aa[0] + "\n" + errStack;
      logx.print("error", aa[0], aa[1]);
      if (this.__debug) {
        aa[2] = aa[2] + "\n" + errStack;
        sys.err.printlnf(aa[2], aa[3]);
      }
    },
    ajax_error: function (errCode, reason) {
      if ((errCode instanceof Error)|| errCode.reason) {
	    reason = errCode.reason || reason || null;
	    errCode = errCode.code || errCode.toString() || "Unknown Error";
	  } else {
		errCode = errCode.toString();
	  }
	  var m = /^[Ee]rror: *(.+)$/.exec(errCode);
      if (m) {
         errCode = m[1].trim();
      }
	  var json = JSON.stringify({
	    ok: false,
	    errCode: errCode,
	    data: reason,
	  });
	  logx.print("error", "AJAX ERROR: %s", [json]);
	  sys.out.println(json);
    },
    ajax_re: function (obj) {
      var json;
      if (typeof obj == "string") {
        json = obj;
      } else {
        json = JSON.stringify(obj);
      }
      logx.print("info", "AJAX RETURN: %s", [json]);
      sys.out.println(json);
    },
    ajax_ok: function (data) {
      var json = JSON.stringify({
        ok: true,
        data: data,
      });
      logx.print("info", "AJAX RETURN: %s", [json]);
      sys.out.println(json);
    },
  };
});
