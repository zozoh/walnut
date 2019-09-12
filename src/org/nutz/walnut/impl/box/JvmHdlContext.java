package org.nutz.walnut.impl.box;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nutz.ioc.Ioc;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class JvmHdlContext extends NutMap implements Cloneable {

    public Ioc ioc;

    public WnSystem sys;

    private Map<String, JvmHdl> hdls;

    public String hdlName;

    public JvmHdl hdl;

    public String[] args;

    public ZParams params;

    public WnPager pager;

    /**
     * 通常为 cmd xxx hdl 形式的 xxx 指代的命令参考对象
     */
    public WnObj oRefer;

    public JsonFormat jfmt;

    public Object output;

    /**
     * 一些补充的信息可以放在这里
     */
    private NutMap _attrs;

    public NutMap attrs() {
        if (null == _attrs) {
            _attrs = new NutMap();
        }
        return _attrs;
    }

    public JvmHdlContext clone() {
        JvmHdlContext hc = new JvmHdlContext();
        hc.ioc = this.ioc;
        hc.sys = this.sys;
        hc.hdls = this.hdls;
        hc.hdlName = this.hdlName;
        hc.hdl = this.hdl;
        hc.args = Arrays.copyOf(this.args, this.args.length);
        hc.params = this.params.clone();
        hc.pager = null == this.pager ? null : this.pager.clone();
        hc.oRefer = this.oRefer;
        hc.jfmt = null == this.jfmt ? null : this.jfmt.clone();
        hc.ioc = this.ioc;

        return hc;
    }

    public void setHandlers(Map<String, JvmHdl> hdls) {
        if (null == this.hdls) {
            this.hdls = new HashMap<String, JvmHdl>();
        } else {
            this.hdls.clear();
        }
        this.hdls.putAll(hdls);
    }

    public JvmHdl getHandler(String hdlName) {
        return this.hdls.get(hdlName);
    }

    /**
     * 调用本命令族的另外的执行器
     * 
     * @param otherHdlName
     *            执行器名称
     * @param beforeInvoke
     *            调用前如果想修改一下上下文，在这个回调里修改,传入 null 就是什么也不做咯
     * @return 执行器的执行结果
     * 
     * @throws WebException
     *             检查异常
     *             <ul>
     *             <li><code>e.cmd.jvmhdl.infinity_call</code> : 自己调自己会导致无穷递归
     *             <li><code>e.cmd.jvmhdl.invalidHdlName</code> : 错误的执行器名称
     *             <li>执行器抛出的异常包裹
     *             </ul>
     * @throws Exception
     * 
     */
    public Object doOtherHandler(String otherHdlName, Callback<JvmHdlContext> beforeInvoke) {
        // 自己不能调自己，否则无穷递归了
        if (this.hdlName.equals(otherHdlName)) {
            throw Er.createf("e.cmd.jvmhdl.infinity_call", "%s->%s", this.hdlName, otherHdlName);
        }

        // 克隆一份上下文
        JvmHdlContext hc2 = this.clone();

        // 找到对应的执行器对象
        hc2.hdl = this.hdls.get(otherHdlName);
        if (null == hc2.hdl) {
            throw Er.create("e.cmd.jvmhdl.invalidHdlName", otherHdlName);
        }
        hc2.hdlName = otherHdlName;

        // 调用执行器
        try {
            if (null != beforeInvoke) {
                beforeInvoke.invoke(hc2);
            }
            hc2.hdl.invoke(sys, hc2);
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }

        // 返回结果
        return hc2.output;
    }

    public void parseParams(String[] args) {
        // 得到注解
        JvmHdlParamArgs jhpa = null;

        if (null != this.hdl)
            jhpa = this.hdl.getClass().getAnnotation(JvmHdlParamArgs.class);

        // 解析
        if (null == jhpa) {
            this.params = ZParams.parse(args, null);
        }
        // 自动模式
        else if (Strings.isBlank(jhpa.regex())) {
            this.params = ZParams.parse(args, jhpa.value());
        }
        // 单独指定了正则表达式
        else {
            this.params = ZParams.parse(args, jhpa.value(), jhpa.regex());
        }
    }

}
