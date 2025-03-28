package com.site0.walnut.ext.sys.httpapi;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.pvg.BizPvgService;
import com.site0.walnut.ext.data.pvg.WnAuthOptions;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.WnContext;

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
     * 记录请求的 Query String 全文
     */
    String reqQuery;

    /**
     * 请求的 QueryString 签名，如果到了生成缓存对象这一步，<br>
     * 且这个对象有值，表示API会验证签名，所以生成缓存对象时要加上这个签名
     */
    String reqQuerySign;

    /**
     * 这个标志位表示请求完毕后，需要把响应结果缓存起来的目标路径
     */
    String cacheObjPath;

    /**
     * 这个标志位表示请求完毕后，需要把响应结果缓存起来的目标对象
     * <p>
     * 如果声明了这个标志位，上面那个<code>cacheObjPath</code> 将被无视
     */
    WnObj cacheObj;

    /**
     * 分析请求对象时，也会顺便收集的 QueryString 参数表 <br>
     * 启用缓存时会用到它
     */
    NutMap reqQueryMap;

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

    // /**
    // * WWW 对于接口，声明了 oWWW 才会生成
    // */
    // WnWebService webs;
    //
    // /**
    // * API 对应网站当前的用户登录会话
    // */
    // WnAuthSession wwwSe;
    WnAuthOptions authOptions;
    WnLoginApi loginApi;
    WnSession wwwSe;
    WnUser wwwMe;

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
