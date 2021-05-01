package org.nutz.walnut.ext.net.sms.provider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.nutz.http.Http;

/**
 * 短信http接口的java代码调用示例 基于Nutz.Http
 *
 * @author songchao
 * @since 2015-04-03
 */
public class YunpianSmsApi {

    // 查账户信息的http地址
    private static String URI_GET_USER_INFO = "http://yunpian.com/v1/user/get.json";

    // 通用发送接口的http地址
    private static String URI_SEND_SMS = "http://yunpian.com/v1/sms/send.json";

    // 模板发送接口的http地址
    private static String URI_TPL_SEND_SMS = "http://yunpian.com/v1/sms/tpl_send.json";

    /**
     * 取账户信息
     *
     * @return json格式字符串
     * @throws java.io.IOException
     */
    public static String getUserInfo(String apikey) throws IOException, URISyntaxException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("apikey", apikey);
        return post(URI_GET_USER_INFO, params);
    }

    /**
     * 通用接口发短信
     *
     * @param apikey
     *            apikey
     * @param text
     *            短信内容
     * @param mobile
     *            接受的手机号
     * @return json格式字符串
     * @throws IOException
     */
    public static String sendSms(String apikey, String text, String mobile) throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("apikey", apikey);
        params.put("text", text);
        params.put("mobile", mobile);
        return post(URI_SEND_SMS, params);
    }

    /**
     * 通过模板发送短信(不推荐)
     *
     * @param apikey
     *            apikey
     * @param tpl_id
     *            模板id
     * @param tpl_value
     *            模板变量值
     * @param mobile
     *            接受的手机号
     * @return json格式字符串
     * @throws IOException
     */
    public static String tplSendSms(String apikey, long tpl_id, String tpl_value, String mobile)
            throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("apikey", apikey);
        params.put("tpl_id", String.valueOf(tpl_id));
        params.put("tpl_value", tpl_value);
        params.put("mobile", mobile);
        return post(URI_TPL_SEND_SMS, params);
    }

    /**
     * 基于HttpClient 4.3的通用POST方法
     *
     * @param url
     *            提交的URL
     * @param paramsMap
     *            提交<参数，值>Map
     * @return 提交响应
     */
    public static String post(String url, Map<String, Object> params) {
        return Http.post(url, params, 5 * 1000);
    }
}