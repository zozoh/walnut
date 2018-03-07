/**
Navmenu 控件的运行时行为:

opt : {
    // 来自编辑器
    ...

    // 下面是服务器端转换增加的属性
    ...
}
*/
(function($, $z){
//...........................................................
$.fn.extend({ "hmc_video" : function(opt){
    // 得到自己所在控件
    var jq = this.closest(".hmc-video");
    var jCon = jq.find(".hmc-video-con");
    var jVid = jq.find('video');
    
    //console.log("I am hmc_video!!!", opt)
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 捕获 video 的事件
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 事件:准备好了可以播放了
    jVid.on("canplay", function(){
        jCon.attr("st", "pause");
    });

    // 事件:暂停时
    jVid.on("pause", function(){
        jCon.attr("st", "pause");
    });

    // 事件:播放时
    jVid.on("playing", function(){
        jCon.attr("st", "playing");
    });

    // 事件:完成时
    jVid.on("ended", function(){
        jCon.attr("st", "ended");
    });

    // 事件:点击播放
    jCon.on("click", ".hmcv-s-play", function(){
        jVid[0].play();
    });

    // 事件:点击重头播放
    jCon.on("click", ".hmcv-s-replay", function(){
        jVid[0].currentTime = 0;
        jVid[0].play();
    });

    // 事件:点击暂停
    jCon.on("click", ".hmcv-s-pause", function(){
        jVid[0].pause();
    });

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 返回自身以便链式赋值
    return jq;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

