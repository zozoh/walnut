package org.nutz.walnut.ext.aliyun.vod;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.aliyun.sdk.WnAliyunMessageCallback;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;

/**
 * 视频点播服务的封装类（每次使用需要新建）
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAliyunVodService {

    private DefaultAcsClient client;

    /**
     * 初始化一个云服务客户端
     * 
     * @param conf
     *            访问配置
     * @return 客户端
     */
    public WnAliyunVodService(WnAliyunVodConf conf) {
        DefaultProfile profile = DefaultProfile.getProfile(conf.getRegion(),
                                                           conf.getId(),
                                                           conf.getSecret());
        this.client = new DefaultAcsClient(profile);
    }

    /**
     * @param videoId
     *            视频ID
     * @return 视频播放信息
     */
    public GetPlayInfoResponse getPlayInfo(String videoId) {
        GetPlayInfoRequest req = new GetPlayInfoRequest();
        req.setVideoId(videoId);
        try {
            return client.getAcsResponse(req);
        }
        catch (ClientException e) {
            throw Er.create(e, "e.aliyun.vod.getPlayInfo");
        }
    }

    /**
     * @param videoId
     *            视频ID
     * @return 视频播放凭证
     */
    public GetVideoPlayAuthResponse getVideoPlayAuth(String videoId) {
        GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
        request.setVideoId(videoId);
        try {
            return client.getAcsResponse(request);
        }
        catch (ServerException e) {
            throw Lang.wrapThrow(e);
        }
        catch (ClientException e) {
            throw Er.create(e, "e.aliyun.vod.getVideoPlayAuth");
        }
    }

    /**
     * @param title
     *            新视频标题
     * @param fileName
     *            新视频文件名
     * @param callback
     *            【选】回调设置
     * @param extendData
     *            【选】数据透传，用来设置更多的自定义业务数据
     * @return 上传凭证
     */
    public CreateUploadVideoResponse createUploadVideo(String title,
                                                       String fileName,
                                                       WnAliyunMessageCallback callback,
                                                       NutBean extendData) {
        CreateUploadVideoRequest req = new CreateUploadVideoRequest();
        req.setTitle(title);
        req.setFileName(fileName);

        // 自定义参数
        NutMap userData = new NutMap();

        // 自定义回调
        if (null != callback) {
            NutMap cm = new NutMap();
            cm.put("CallbackURL", callback.getUrl());
            cm.put("CallbackType", callback.getType());
            String json = Json.toJson(cm, JsonFormat.compact().setQuoteName(true));
            userData.put("MessageCallback", json);
        }

        // 数据透传
        if (null != extendData) {
            String json = Json.toJson(extendData, JsonFormat.compact().setQuoteName(true));
            userData.put("Extend", json);
        }

        // 设置
        if (!userData.isEmpty()) {
            String json = Json.toJson(userData, JsonFormat.compact().setQuoteName(true));
            req.setUserData(json);
        }

        try {
            return this.client.getAcsResponse(req);
        }
        catch (ClientException e) {
            throw Er.create(e, "e.aliyun.vod.createUploadVideo");
        }

    }
    
//    public SearchMediaResponse searchMedia(WnQuery) {
//        
//    }

}
