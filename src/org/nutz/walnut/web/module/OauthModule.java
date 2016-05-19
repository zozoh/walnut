package org.nutz.walnut.web.module;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.lang.stream.NullInputStream;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.web.filter.WnAsUsr;

@IocBean(create = "init")
@At("/oauth")
public class OauthModule extends AbstractWnModule {
	
	private static final Log log = Logs.get();
	
	String websiteUrlBase;

	@Ok("void")
	@At("/?")
	@Filters
	public void auth(String provider, HttpSession session,
			HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String returnTo = req.getRequestURL() + "/callback";
		if (req.getParameterMap().size() > 0) {
			StringBuilder sb = new StringBuilder().append(returnTo).append("?");
			for (Object name : req.getParameterMap().keySet()) {
				sb.append(name)
						.append('=')
						.append(URLEncoder.encode(
								req.getParameter(name.toString()),
								Encoding.UTF8)).append("&");
			}
			returnTo = sb.toString();
		}
		SocialAuthManager manager = new SocialAuthManager(); // 每次都要新建哦
		manager.setSocialAuthConfig(config);
		String url = manager.getAuthenticationUrl(provider, returnTo);
		log.debug("URL=" + url);
		Mvcs.getResp().setHeader("Location", url);
		Mvcs.getResp().setStatus(302);
		
		// TODO zozoh: 考虑到分布式， manager 应该被预先序列化到 /sys/auth/xxxx 文件内
		// 在 callback 的时候，得到 ID，再反序列化回来
		session.setAttribute("openid_manager", manager);
	}

	/**
	 * 统一的OAuth回调入口
	 */
	@At("/?/callback")
	@Ok("++cookie>>:/")
	@Fail(">>:/")
	@Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
	public Object callback(String _providerId, HttpSession session, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		SocialAuthManager manager = (SocialAuthManager) session.getAttribute("openid_manager");
		if (manager == null)
			throw new SocialAuthException("Not manager found!");
		session.removeAttribute("openid_manager"); //防止重复登录的可能性
		Map<String, String> paramsMap = SocialAuthUtil.getRequestParametersMap(req); 
		AuthProvider provider = manager.connect(paramsMap);
		Profile p = provider.getUserProfile();
		
		String key = String.format("oauth_%s:%s", p.getProviderId(), p.getValidatedId());
		WnUsr user = usrs.fetch(key);
		// 特殊优待github用户
		boolean isGithub = "github".equals(p.getProviderId());
		if (user == null) {
		    if (isGithub)
		        user = usrs.fetch(p.getDisplayName());
	        if (user == null) {
	            String username = isGithub ? p.getDisplayName() : R.UU32();
	            user = usrs.create(username, R.UU32());
	        }
            usrs.set(user.name(), "oauth_"+p.getProviderId(), p.getValidatedId());
		}
		return sess.create(user);
	}

	private SocialAuthConfig config;

	public void init() throws Exception {
		SocialAuthConfig config = new SocialAuthConfig();
		InputStream devConfig = getClass().getClassLoader().getResourceAsStream("oauth_consumer.properties_dev"); // 开发期所使用的配置文件
		if (devConfig == null)
			devConfig = getClass().getClassLoader().getResourceAsStream("oauth_consumer.properties"); // 真实环境所使用的配置文件
		if (devConfig == null)
			config.load(new NullInputStream());
		else {
			log.debug("Using " + devConfig);
			config.load(devConfig);
			Streams.safeClose(devConfig);
		}
		this.config = config;
	}
}
