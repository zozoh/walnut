package org.nutz.walnut.ext.vcode;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

@IocBean
public class WnVCodeService implements VCodeService {

    @Inject("refer:io")
    private WnIo io;

    @Override
    public WnObj save(String vcodePath, String code, int duInMin, int maxRetry) {
        __assert_vcode_path(vcodePath);

        WnObj oCode = io.createIfNoExists(null, vcodePath, WnRace.FILE);

        // 得到过期时间
        long expi = Wn.now() + (duInMin * 60000L);

        // 准备验证码的元数据
        oCode.expireTime(expi);
        oCode.setv("v_code", code);
        oCode.setv("v_retry", 0);
        oCode.setv("v_remax", maxRetry);
        io.set(oCode, "^(expi|v_code|v_retry|v_remax)$");

        // 返回验证码对象
        return oCode;
    }

    private void __assert_vcode_path(String vcodePath) {
        // 检查路径
        if (!vcodePath.startsWith("/var/vcode/")) {
            throw Er.create("e.vode.path.invalid", vcodePath);
        }
    }

    @Override
    public boolean check(String vcodePath, String code) {
        WnObj oCode = __check_vcode(vcodePath, code);
        return oCode != null;
    }

    @Override
    public boolean checkAndRemove(String vcodePath, String code) {
        WnObj oCode = __check_vcode(vcodePath, code);

        // 验证成功
        if (null != oCode) {
            io.delete(oCode);
            return true;
        }

        // 验证失败
        return false;
    }

    private WnObj __check_vcode(String vcodePath, String code) {
        __assert_vcode_path(vcodePath);

        WnObj oCode = io.fetch(null, vcodePath);

        if (null != oCode) {
            int retry = oCode.getInt("v_retry");
            int remax = oCode.getInt("v_remax");
            
            // zozoh: 这里做一下冗余防守: 超过最大重试次数，删除验证码，返回空
            if (retry >= remax) {
                io.delete(oCode);
                return null;
            }

            // 根据方法进行验证
            String expectCode = oCode.getString("v_code");

            // 验证成功
            if (Lang.equals(code, expectCode)) {
                return oCode;
            }

            // 验证失败，失败次数 +1
            retry++;

            // 超过最大重试次数，删除验证码，返回空
            if (retry >= remax) {
                io.delete(oCode);
                return null;
            }
            // 持久化重试次数
            else {
                io.inc(oCode.id(), "v_retry", 1, false);
            }
        }

        // 返回
        return null;
    }

}
