(function (sys, logx, traceID) {
    return {
      __log_tag: "-NoTag-",
      __debug: false,
      __trace_id: traceID,
      __log_args: function __log_args(_arguments) {
        var sys_msg = _arguments[0];
        var msg = "%s [%s] : " + sys_msg;
        var args = [this.__log_tag, this.__trace_id];
        var sys_args = [];
        for (var i = 1; i < _arguments.length; i++) {
          args.push(_arguments[i]);
          sys_args.push(_arguments[i]);
        }
        return [msg, args, sys_msg, sys_args];
      },
      setTag: function (tag) {
        this.__log_tag = tag;
      },
      setDebug: function (debug) {
        this.__debug = debug;
      },
      setup: function (tag, debug) {
        if (tag) this.__log_tag = tag;
        if ("boolean" === typeof debug) this.__debug = debug;
      },
      setTraceID: function (traceID) {
        this.__trace_id = traceID;
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
      ajax_error: function (errCode, reason) {
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
  