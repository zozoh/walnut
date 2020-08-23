package org.nutz.walnut.tool;

import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.walnut.util.Wn;

public class GenAutoLoginUrl {

    public static void main(String[] args) {
        // .............................................
        // 计算签名前，你需要获得的信息

        // 签名的秘钥
        String ackey = "DEV-walnut-2016/demo";

        // 用户名
        String user = "demo";

        // 请求者时间
        long time = Wn.now();

        // 随机字符串
        String once = R.UU64();

        // .............................................
        // 拼合成要签名字符串
        String[] ss = Lang.array(ackey, user, time + "", once);
        String strForSign = Lang.concat(",", ss).toString();

        // .............................................
        // 生成签名
        String sign = Lang.sha1(strForSign);

        // .............................................
        // 输出签名的链接地址
        String url = "http://182.92.3.49:8080/u/do/login/auto";
        url += "?sign=" + sign;
        url += "&user=" + user;
        url += "&time=" + time;
        url += "&once=" + once;

        // .. 并打印
        System.out.println(url);

    }

}
