([{
    title : 'i18n:weixin.mp.setting',
    uiWidth : "all",
    fields : [{
        key :"appID",
        title : "i18n:weixin.mp.appID",
        tip :  "i18n:weixin.mp.appID_tip",
        type :"string"
    }, {
        key :"appsecret",
        title :"i18n:weixin.mp.appsecret",
        tip :  "i18n:weixin.mp.appsecret_tip",
        type :"string"
    }, {
        key :"token",
        title :"i18n:weixin.mp.token",
        tip :  "i18n:weixin.mp.token_tip",
        type :"string"
    }, {
        key :"pay_mch_id",
        title :"i18n:weixin.mp.pay_mch_id",
        type :"string"
    }, {
        key :"pay_key",
        title :"i18n:weixin.mp.pay_key",
        type :"string"
    }, {
        key :"pay_time_expire",
        title :"i18n:weixin.mp.pay_time_expire",
        tip :  "i18n:weixin.mp.pay_time_expire_tip",
        type :"string",
        dft : 10,
        uiWidth : 50
    }, {
        key :"pay_notify_url",
        title :"i18n:weixin.mp.pay_notify_url",
        type :"string"
    }, {
        key :"jsSdkUrl",
        title :"i18n:weixin.mp.jsSdkUrl",
        tip :  "i18n:weixin.mp.jsSdkUrl_tip",
        type :"string"
    }, {
        key :"jsApiList",
        title :"i18n:weixin.mp.jsApiList",
        tip :  "i18n:weixin.mp.jsApiList_tip",
        type :"object",
        editAs : "checklist",
        uiConf : {
            icon  : function(){return '<i class="fa fa-plug"></i>';},
            text  : function(str) {return str;},
            value : function(str) {return str;},
            items : ['onMenuShareTimeline'
                ,'onMenuShareAppMessage'
                ,'onMenuShareQQ'
                ,'onMenuShareWeibo'
                ,'onMenuShareQZone'
                ,'startRecord'
                ,'stopRecord'
                ,'onVoiceRecordEnd'
                ,'playVoice'
                ,'pauseVoice'
                ,'stopVoice'
                ,'onVoicePlayEnd'
                ,'uploadVoice'
                ,'downloadVoice'
                ,'chooseImage'
                ,'previewImage'
                ,'uploadImage'
                ,'downloadImage'
                ,'translateVoice'
                ,'getNetworkType'
                ,'openLocation'
                ,'getLocation'
                ,'hideOptionMenu'
                ,'showOptionMenu'
                ,'hideMenuItems'
                ,'showMenuItems'
                ,'hideAllNonBaseMenuItem'
                ,'showAllNonBaseMenuItem'
                ,'closeWindow'
                ,'scanQRCode'
                ,'chooseWXPay'
                ,'openProductSpecificView'
                ,'addCard'
                ,'chooseCard'
                ,'openCard']
        }
    }]
}, {
    title : 'i18n:weixin.mp.handlers',
    className : "wx-mp-handlers",
    uiWidth : "all",
    fields : [{
        key :"handlers",
        type :"object",
        uiType : 'app/wn.weixin.mp/c_weixin_mp_handler',
        uiConf : {}
    }]
}])