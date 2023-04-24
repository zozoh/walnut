/*
当将对象指定的键记录到关键字中

执行方法【基本测试】:
jsc /jsbin/tools/auto_keywords.js -vars 'id:"${ID}",ks:"K1,K2..",to:"keywords"'

参数：
 - id : 对象ID
 - ks : 半角分隔的对象键
*/
//-------------------------------------------------------------
function __main(id, ks, to) {
  // 防守
  if (!id) {
    sys.err.println("未指定对象ID");
    return;
  }
  if (!ks) {
    sys.err.println("未指定对象字段 id");
    return;
  }
  if (!to) {
    sys.err.println("未指定对象目标字段");
    return;
  }
  // 开始计时
  var msBegin = Date.now();
  var cmdText, re;
  //.............................................................
  cmdText = "o 'id:" + id + "' @json -cqn";
  sys.out.printlnf("1. 读取目标对象: %s", cmdText);
  re = sys.exec2(cmdText);
  //sys.out.printlnf("re: %s", re)
  if (/^e\./.test(re)) {
    sys.err.println(re);
    return;
  }
  var obj = JSON.parse(re);

  //.............................................................
  sys.out.printlnf("2. 循环键: %s", ks);
  var vals = [];
  var keys = ks.split(",");
  for (var i = 0; i < keys.length; i++) {
    var k = keys[i];
    var v = obj[k];
    sys.out.printlnf("  - %s) %s : %s", i, k, v);
    if (v) {
      if ("string" == typeof v) {
        v = v.toUpperCase().split(/[\\/\t :\r\n_-]+/g);
      }
      if (v && v.length > 0) {
        for (var x = 0; x < v.length; x++) {
          var vx = v[x];
          if (vx && vals.indexOf(vx) < 0) {
            vals.push(vx);
          }
        }
      }
    }
  }

  //.............................................................
  var meta = {};
  meta[to] = vals.length > 0 ? vals : null;
  var input = JSON.stringify(meta);
  sys.out.printlnf("3. 更新字段: %s", input);
  sys.exec("o 'id:" + obj.id + "' @update @quiet", input);
  //
  // 结束
  //
  sys.out.println("======================================");
  var ms_done = parseInt(Date.now() - msBegin);
  sys.out.printlnf("Done in %sms", ms_done);
}
// 主函数入口
var id = id || null;
var ks = ks || "nm,title,abbr,nickname";
var to = to || "keywords";
sys.out.println("######################################");
sys.out.println("# 钩子：关键字");
sys.out.println("######################################");
sys.out.printlnf("$id  = %s", id);
sys.out.printlnf("$ks  = %s", ks);
sys.out.printlnf("$to  = %s", to);
sys.out.println("======================================");
__main(id, ks, to);
