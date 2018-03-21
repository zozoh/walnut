package org.nutz.walnut.ext.mediax.bean;

/**
 * 媒体平台的账号信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MxAccount {

    // 验证用的账号密码，当然对于今日头条，微信公众号，这个相当于是
    // AppID/SecretKey
    public String login;
    public String passwd;
    public String token;

    // 下面的信息对于查找比较有帮助
    public String nickname;
    public int alvl;
    public String thumb;

}
