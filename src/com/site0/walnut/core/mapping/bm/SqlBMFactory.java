package com.site0.walnut.core.mapping.bm;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.sql.SqlBM;
import com.site0.walnut.core.mapping.WnBMFactory;
import com.site0.walnut.ext.data.sqlx.loader.SqlHolder;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 输入的配置为: <code>sql({?daoName}:{entityName}:{?filter})</code>
 * 
 * 譬如:
 * 
 * <pre>
 * // 采用默认数据源
 * sql(:pet:{color:"red"})
 * 
 * // 指定数据源
 * sql(hisory:pet:{color:"red"})
 * </pre>
 * 
 * 本类需要指定 entity，同时你需要为 entity 定义下面几个 sql:
 * 
 * <ul>
 * <li>1. {entity}.fetch
 * <li>2. {entity}.count
 * <li>3. {entity}.delete
 * <li>3. {entity}.insert
 * <li>4. {entity}.update
 * </ul>
 * 
 * 定义就放在 <code>~/.sqlx</code> 目录下，具体细节可以参看<code>man sqlx</code>
 * 当然，没有这个定义，系统会默认给出一个这个实体的 SQL 实现。
 * 
 * 同时本类期望的实体表有如下字段:
 * 
 * <pre>
 * id: String      # 就是对象的 ID，每个对象都会将内容存放再这里
 * nm: String      # 关联的对象名称
 * tp: String      # 对象类型，譬如 mp4,txt 等
 * mime: String    # 内容类型，譬如 video/mp4, text/plain 等
 * sha1: String    # 内容的 sha1 签名
 * len: int        # 内容的大小
 * ct: DateTime(3) # 本记录的创建世间
 * lm: DateTime(3) # 本记录的最后修改
 * content: Blob   # 字节内容
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class SqlBMFactory implements WnBMFactory {

    /**
     * 这个需要通过 IOC 注入得到实例
     */
    private WnIo io;
    private WnIoHandleManager handles;
    private String swapPath;

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setHandles(WnIoHandleManager handles) {
        this.handles = handles;
    }

    public void setSwapPath(String swapPath) {
        this.swapPath = swapPath;
    }

    @Override
    public WnIoBM load(WnObj oHome, String args) {
        SqlBMArgs bminfo = new SqlBMArgs(args);

        // 读取数据源的配置信息

        NutBean vars = Wn.getVarsByObj(oHome);
        WnDaoAuth auth = WnDaos.loadAuth(io, bminfo.daoName, vars);

        // 获取 SQL 模板管理器
        SqlHolder sqls = Sqlx.getSqlHolderByPath(io, vars, null);

        return new SqlBM(handles, swapPath, auth, sqls, bminfo.entityName);
    }

    static class SqlBMArgs {
        String daoName;
        String entityName;
        Object filter;

        SqlBMArgs(String str) {
            String[] ss = Ws.splitTrimedEmptyAsNull(str, ":", 3);
            this.daoName = Ws.sBlank(ss[0], "default");
            this.entityName = ss[1];

            String flt = ss.length >= 3 ? ss[2] : null;
            if (!Ws.isBlank(flt)) {
                // 数组的话，就是并联条件
                if (Ws.isQuoteBy(flt, '[', ']')) {
                    this.filter = Json.fromJson(flt);
                }
                // 否则尝试给一个条件
                else {
                    NutMap map = Wlang.map(flt);
                    if (map.isEmpty()) {
                        map.put("1", 1);
                    }
                }
            }
            // 总之不能让条件为空
            else {
                this.filter = Wlang.map("1", 1);
            }

        }
    }

}
