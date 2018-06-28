/*
提供了 Thing 对象的 文件上传 功能

本函数假设有下面格式参数输入

params: {
    th_set : th_set,   // 数据集
    th_id  : th_id,    // 数据ID
    id     : id        // 文件字段
}

用法
jsc /jsbin/thing_upload.js ${id} ${http-qs-th_set} ${http-qs-th_id} ${http-qs-name}
*/
//........................................
//http://127.0.0.1:8080/api/leshaonian/thing/upload?th_set=~/thing/营期项目&th_id=vumurnhe5shuco32vfqvknqlui&name=wendal.txt
//........................................
// 执行: 需要 params 变量
var params = {
	id : args[0],
	th_set : args[1],
	th_id  : args[2],
	name   : args[3]
}
//........................................
function _main(params){
    // 检查关键值
    if(!params.th_set){
        sys.exec("ajaxre e.api.thing.update.noThingSetId");
        return;
    }
    if(!params.th_id){
        sys.exec("ajaxre e.api.thing.update.noThingId");
        return;
    }
    if(!params.id){
        sys.exec("ajaxre e.api.thing.update.noId");
        return;
    }
    if(!params.name){
        params.name = new Date().getTime();
    }

    // 准备生成命令
    cmdText = 'thing %s media %s -overwrite -add %s -read id:%s -cqn';

    // 运行命令，并输出返回值
    var re  = sys.exec2f(cmdText, 
                            params.th_set,
                            params.th_id,
                            params.name,
							params.id);

    // 错误
    if(/^e./.test(re)) {
        sys.exec("ajaxre '" + re + "'");
        return;
    }

    // 最后输出结果
    sys.out.println(re);
}
_main(params);
//........................................
