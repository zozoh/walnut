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