/**
 * jsc /jsbin/tools/build_search_keywords.js -vars 'id:"67a..89b"'
 * !!! 输入的 ID 相当于是 archive 的 ID
 * !!! 如果不输入，则整站编制索引
 * "hook":true 表示在钩子里调用，那么必须要有 ID
 */
// 找到所有的当前目录下的文本对象
/*
// 批量更新数据
jsc /jsbin/tools/build_search_keywords.js -vars 'limit:10,q:\'{"tp":"FILE"}\''
 - id : 对象ID
 - ks : 半角分隔的对象键
 - to : "keywords" 目标字段
*/
var limit = limit;
if (!(limit >= 0)) {
  limit = 10;
}
var skip = skip;
if (!(skip >= 0)) {
  skip = 0;
}
var hook = hook;
if ("boolean" != typeof hook) {
  hook = false;
}
var id = id;
var pid = pid;
var q = q || '{"race":"FILE"}';
var ks = ks || "nm,title";
var to = to || "keywords";
// 在钩子里必须有 ID
if (!hook || id) {
  var beginMs = Date.now();
  var query = JSON.parse(q);
  if (id) {
    query.id = id;
  }
  if (pid) {
    query.pid = pid;
  }
  var qJson = JSON.stringify(query);
  var cmdText =
    "o @query -mine '" +
    qJson +
    "' -limit " +
    limit +
    " -skip " +
    skip +
    " -sort 'ct:1' @json #TP -cqnl";
  sys.out.println(cmdText);
  var re = sys.exec2(cmdText);
  var list = JSON.parse(re);

  // 准备调用参数
  var akVars = { ks: ks, to: to };

  if (list.length == 1) {
    var o = list[0];
    akVars.id = o.id;
    var vs = JSON.stringify(akVars);
    cmdText = "jsc /jsbin/tools/auto_keywords.js -vars '" + vs + "'";
    sys.out.println(cmdText);
    sys.exec(cmdText);
  }
  // 批量
  else {
    var N = list.length;
    for (var i = 0; i < N; i++) {
      var o = list[i];
      akVars.id = o.id;
      var vs = JSON.stringify(akVars);
      cmdText = "jsc /jsbin/tools/auto_keywords.js -vars '" + vs + "'";
      sys.out.printlnf("[%s/%s] : %s", i, N, cmdText);
      sys.exec2(cmdText);
    }
  }

  // 最后输出汇总
  var duInMs = Date.now() - beginMs;
  sys.out.println("=======================================");
  sys.out.printlnf(
    "%s items done in %ss (%sms) ~~",
    list.length,
    Math.round(duInMs / 1000),
    duInMs
  );
}
