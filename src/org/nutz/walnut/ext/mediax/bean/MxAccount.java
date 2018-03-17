package org.nutz.walnut.ext.mediax.bean;

/**
 * 媒体平台的账号信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MxAccount {

    /**
     * 这个为目标媒体的类型，必须固定。下面我们会给出一个全媒体所有键的表
     */
    public MxAType mtype;

    /**
     * 某些媒体，譬如某个小 Discuz 论坛需要一个地址 <br>
     * 但是其他的媒体，譬如知乎，地址是固定的 <br>
     * 所以 Discuz 的实现类需要这个字段，而知乎的实现类就不需要
     */
    public String host;

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
