package org.nutz.walnut.ext.websocket.hdl;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 列出所有 WebSocket 对象，并支持自动清除无效连接
 * 
 * <pre>
 * websocket list "{条件}" -skip 0 -limit 1 -sort "nm:1" -clean -quiet
 * 
 *  - skip : 跳过多少条记录
 *  - limit : 限制，默认 100，最大不能超过 1000
 *  - sort : 对象搜索的排序方式
 *  - clean : 清除无效的 websocket 会话
 *  - quiet : 不显示输出（只有 -clean 模式下才有效）
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(clean)$")
public class websocket_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // ......................................................
        // 判断是否是清除模式
        boolean isClean = hc.params.is("clean");
        // ......................................................
        // [0 总的对象数, 1有效对象数, 2 无效会话数, 3 有效会话数, 4 空键对象数]
        int[] _cc = new int[5];
        // ......................................................
        // 输出日志
        Log log = isClean && hc.params.is("quiet") ? null : sys.getLog("I", "@{m}");
        // ......................................................
        // 得到查询条件
        WnQuery q = new WnQuery();

        // 附加条件
        NutMap map = Lang.map(hc.params.val(0));
        if (null != map && map.size() > 0) {
            q.setAll(map);
        }
        // 固定条件
        q.setv("d1", sys.getMyGroup());
        q.setv(WnWebSocket.KEY, new NutMap("$exists", true));
        // 跳过
        q.skip(hc.params.getInt("skip", 0));
        // 限制
        q.limit(Math.min(1000, hc.params.getInt("limit", 100)));
        // 排序
        if (hc.params.has("sort")) {
            NutMap sort = Lang.map(hc.params.check("sort"));
            q.sort(sort);
        }
        // ......................................................
        // 打印日志
        if (null != log) {
            log.info(Strings.dup('-', 80));
            log.info("List WebSockets:\n");
        }
        // ......................................................
        // 开始计时
        Stopwatch sw = Stopwatch.begin();
        // ......................................................
        // 循环处理每个 websocket 的宿主对象
        sys.io.each(q, new Each<WnObj>() {
            @Override
            public void invoke(int objIndex, WnObj o, int length) {
                // 计数
                _cc[0]++;

                // 得到 WebSocket 的 ID 列表
                Object wsIds = o.get(WnWebSocket.KEY);
                int wsnb = Lang.eleSize(wsIds);
                // ..................................................
                // 如果有 WebSocket
                if (wsnb > 0) {
                    // 计数
                    _cc[1]++;
                    // 打印日志
                    if (null != log) {
                        log.infof("%3d) %2d wsIds : %s", objIndex, wsnb, o.path());
                    }
                    // 循环处理每个 WebSocket ID
                    Lang.each(wsIds, new Each<String>() {
                        public void invoke(int index, String wsId, int length) {
                            boolean isAlive = null != WnWebSocket.get(wsId);
                            // 计数
                            if (isAlive) {
                                _cc[2]++;
                            } else {
                                _cc[3]++;
                            }
                            // 打印日志
                            if (null != log) {
                                log.infof("     - %d.%d %5s : %s%s",
                                          objIndex,
                                          index,
                                          isAlive ? "alive" : "--",
                                          wsId,
                                          isAlive ? "" : " --> should be removed");
                            }
                            // 移除
                            if (!isAlive && isClean) {
                                sys.io.pull(o.id(), WnWebSocket.KEY, wsId, false);
                            }
                        }
                    });
                }
                // ..................................................
                // 没有 WebSocket 的话，应该清除这个字段
                else if (o.containsKey(WnWebSocket.KEY)) {
                    // 计数
                    _cc[4]++;
                    // 打印日志
                    if (null != log) {
                        log.infof("%3d) !! clean KEY(%s) of %s",
                                  objIndex,
                                  WnWebSocket.KEY,
                                  o.path());
                    }
                    // 执行
                    if (isClean) {
                        sys.io.appendMeta(o, String.format("'!%s':true", WnWebSocket.KEY));
                    }
                }
            }

        });
        // ......................................................
        // 结束
        if (null != log) {
            sw.stop();
            // 打印计时
            log.infof("\nAll done in %dms (%s)", sw.du(), sw.toString());
            log.info(Strings.dup('-', 80));
            // 统计信息
            log.infof("%10s: %d\n%10s: %d\n%10s: %d\n%10s: %d\n%10s: %d\n",
                      "Total",
                      _cc[0],
                      "AvaObjs",
                      _cc[1],
                      "Alives",
                      _cc[2],
                      "Deads",
                      _cc[3],
                      "EmptyKey",
                      _cc[4]);
        }
    }

}
