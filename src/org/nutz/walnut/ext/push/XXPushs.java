package org.nutz.walnut.ext.push;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

public class XXPushs {
    
    protected static Map<String, JPushClient> jpushs = new HashMap<>();
    
    public static String send(WnSystem sys, String provider, String alias, String message, String alert, Map<String, String> extras, String platform) {
        WnObj tmp = sys.io.fetch(null, Wn.normalizeFullPath("~/.xxpush/config_"+provider, sys));
        if (tmp == null)
            return "e.cmd.push.provider_not_config";
        NutMap conf = sys.io.readJson(tmp, NutMap.class);
        switch (provider) {
        case "jpush":
            return toJpush(conf, alias, message, alert, extras, platform);

        default:
            break;
        }
        return "e.cmd.push.provider_not_support";
    }
    
    public static String toJpush(NutMap conf, String alias, String message, String alert, Map<String, String> extras, String platform) {
        // 先构建Android的
        AndroidNotification.Builder _and_noti_builder = AndroidNotification.newBuilder();
        if (Strings.isBlank(alert)) {
            _and_noti_builder.setAlert("").setTitle(message).addExtras(extras);
        } else {
            _and_noti_builder.setAlert(alert).addExtras(extras);
        }
        AndroidNotification android = _and_noti_builder.build();
        
        // 再构建ios的
        IosNotification.Builder _ios_noti_builder = IosNotification.newBuilder();
        if (Strings.isBlank(alert)) {
            extras.put("_message", message);
            _ios_noti_builder.setAlert("").addExtras(extras);
        } else {
            _ios_noti_builder.setAlert(alert).addExtras(extras);
        }
        IosNotification ios = _ios_noti_builder.build();
        Notification.Builder _builder = Notification.newBuilder();
        if (Strings.isBlank(platform)) {
            _builder.addPlatformNotification(android).addPlatformNotification(ios);
        } else if ("ios".equals(platform)) {
            _builder.addPlatformNotification(ios);
        } else {
            _builder.addPlatformNotification(android);
        }
        Notification notif = _builder.build();
        cn.jpush.api.push.model.PushPayload.Builder builder = PushPayload.newBuilder().setPlatform(Platform.all());
        builder.setAudience(Audience.alias(alias));
        builder.setNotification(notif);
        Options options = Options.newBuilder().setApnsProduction(true).build();
        builder.setOptions(options);
        
        String masterSecret = conf.getString("masterSecret");
        String appKey = conf.getString("appKey");
        String key = Lang.md5(masterSecret + "_" + appKey);
        JPushClient jpush = jpushs.get(key);
        if (jpush == null) {
            synchronized (jpushs) {
                jpush = jpushs.get(key);
                if (jpush == null) {
                    jpush = new JPushClient(masterSecret, appKey);
                    jpushs.put(key, jpush);
                }
            }
        }
        
        try {
            PushResult re = jpush.sendPush(builder.build());
            if (re.isResultOK())
                return "msgid="+re.msg_id;
            throw Err.create("e.cmd.push.jpush_fail", re);
        }
        catch (APIConnectionException | APIRequestException e) {
            throw Err.create(e, "e.cmd.push.jpush_fail", e.getMessage());
        }
    }
}
