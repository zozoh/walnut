package org.nutz.walnut.ext.httpapi;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.pvg.BizPvgService;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.util.WnContext;

/**
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnHttpApiContext {

    /**
     * 当前线程上下文
     */
    WnContext wc;

    /**
     * 请求的域名
     */
    String usr;

    /**
     * 请求的 API 路径
     */
    String api;

    /**
     * 原始请求
     */
    HttpServletRequest req;
    
    /**
     * 请求的 URI
     */
    String uri;

    /**
     * 原始响应对象
     */
    HttpServletResponse resp;

    /**
     * 客户端指定的响应的 ContentType
     */
    String mimeType;

    /**
     * 从请求对象里分析出的有用元数据
     */
    NutMap reqMeta;

    /**
     * 执行账户：当前执行 API 的账户
     */
    WnAccount u;

    /**
     * 当前执行账户域的主目录
     */
    WnObj oHome;

    /**
     * API 对象的主目录
     */
    WnObj oApiHome;

    /**
     * API 对象
     */
    WnObj oApi;

    /**
     * 执行的命令（模板编译后的结果）
     */
    String cmdText;

    /**
     * 路径参数
     */
    List<String> args;

    /**
     * 路径参数的名称映射表
     */
    NutMap params;

    /**
     * 当前线程的旧会话：执行时会切换到【执行账户】
     */
    WnAuthSession oldSe;

    /**
     * API的执行会话：读取文件等用这个会话来验证权限
     */
    WnAuthSession se;

    /**
     * API 对应的网站工作目录
     */
    WnObj oWWW;
    
    /**
     * WWW 对于接口，声明了 oWWW 才会生成
     */
    WnWebService webs;

    /**
     * API 对应网站当前的用户登录会话
     */
    WnAuthSession wwwSe;
    
    /**
     * 是否要进行站点登录会话校验
     */
    boolean isNeedWWWAuth;

    /**
     * 业务权限设置文件
     */
    WnObj oPvgSetup;

    /**
     * 业务权限服务接口（只有声明了
     */
    BizPvgService bizPvgs;

    /**
     * 临时目录：请求对象的存储目录
     */
    WnObj oTmp;

    /**
     * 请求对象：临时文件
     */
    WnObj oReq;

    /**
     * 响应码
     */
    int respCode;
}
