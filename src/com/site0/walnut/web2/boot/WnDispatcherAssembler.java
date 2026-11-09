package com.site0.walnut.web2.boot;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Files;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.filter.global.CleanupFilter;
import com.site0.walnut.web2.filter.global.CreateMvcContextFilter;
import com.site0.walnut.web2.filter.global.EncodingFilter;
import com.site0.walnut.web2.filter.router.DmnRule;
import com.site0.walnut.web2.filter.router.DomainRouteFilter;
import com.site0.walnut.web2.filter.router.StaticRouter;
import com.site0.walnut.web2.jetty.Jetty9Request;
import com.site0.walnut.web2.jetty.Jetty9Response;
import com.site0.walnut.web2.jetty.Jetty9StaticService;
import com.site0.walnut.web2.jetty.WnMvcJettyHandler;
import com.site0.walnut.web2.router.WnRoutes;
import com.site0.walnut.web2.service.WnServiceFactory;
import com.site0.walnut.web2.support.WnAuthLookup;
import com.site0.walnut.web2.support.WnIoDomainResolver;
import com.site0.walnut.web2.support.WnIoStaticResolver;
import com.site0.walnut.web2.support.WnLoginAuthLookup;

/**
 * JETTY-05 装配器：把 {@code WnServiceFactory} 的服务装配成
 * {@link WnMvcDispatcher} + {@link WnMvcJettyHandler}。
 * <p>
 * 装配内容：services/io/mimes、Jetty9 request/response 类型、WnRoutes 统一路由、
 * globalHead = [CreateMvcContext, Encoding, DomainRoute]、globalTail = [Cleanup]、
 * staticService = Jetty9StaticService。
 * <p>
 * 无全局静态入口：由 {@code WnLauncher2} 每进程创建一个实例。
 */
public class WnDispatcherAssembler {

    private final WnServiceFactory factory;

    public WnDispatcherAssembler(WnServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * 装配并返回 Jetty Handler（供 {@code WnJetty9Server} 使用）。
     */
    public WnMvcJettyHandler buildHandler() {
        WnServiceHolder holder = factory.getServiceHolder();
        WnConfig conf = holder.getConfig();

        WnMvcFactory mvc = new WnMvcFactory().services(holder)
                                             .io(factory.getIo())
                                             .mimes(factory.getMimes())
                                             .requestType(Jetty9Request.class)
                                             .responseType(Jetty9Response.class);

        WnMvcMapping mapping = mvc.mapping();
        WnRoutes.register(mapping);

        // Host→域（需在匹配前执行；同时放 globalHead 保持顺序，链内幂等）
        Pattern mainHost = __main_host(conf);
        List<DmnRule> rules = __load_hostmap();
        DomainRouteFilter domain = new DomainRouteFilter(mainHost,
                                                         new WnIoDomainResolver(factory.getIo()),
                                                         rules);

        WnAuthLookup auth = new WnLoginAuthLookup(holder.getLoginApi());
        WnMvcFilter[] head = new WnMvcFilter[]{
                new CreateMvcContextFilter(auth, factory.getIo()),
                new EncodingFilter(conf.get("encoding", "UTF-8")),
                domain
        };
        WnMvcFilter[] tail = new WnMvcFilter[]{new CleanupFilter()};

        Jetty9StaticService staticService = new Jetty9StaticService(new StaticRouter(new WnIoStaticResolver(factory.getIo()),
                                                                                    factory.getMimes()),
                                                                    __web_root(conf));

        WnMvcDispatcher dispatcher = new WnMvcDispatcher(mapping,
                                                         mvc,
                                                         head,
                                                         tail,
                                                         staticService);
        return new WnMvcJettyHandler(mvc, dispatcher, domain);
    }

    // ----------------------------------------------------------------------

    private static Pattern __main_host(WnConfig conf) {
        String regex = conf.get("main-host");
        if (null == regex || regex.trim().isEmpty())
            return null;
        try {
            return Pattern.compile(regex.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    private static File __web_root(WnConfig conf) {
        try {
            String ar = conf.getAppRoot();
            return (null == ar || ar.isEmpty()) ? null : new File(ar);
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static List<DmnRule> __load_hostmap() {
        List<DmnRule> list = new ArrayList<>();
        File f = Files.findFile("hostmap");
        if (null == f || !f.isFile())
            return list;
        try {
            for (String line : java.nio.file.Files.readAllLines(f.toPath(),
                                                                StandardCharsets.UTF_8)) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#"))
                    continue;
                int i = s.indexOf("=>");
                if (i <= 0)
                    continue;
                String regex = s.substring(0, i).trim();
                String tmpl = s.substring(i + 2).trim();
                try {
                    list.add(DmnRule.of(regex, tmpl));
                }
                catch (Exception e) {
                    // 跳过非法规则
                }
            }
        }
        catch (Exception e) {
            // 读取失败按空规则处理
        }
        return list;
    }

}
