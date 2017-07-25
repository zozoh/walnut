define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //=========================================================
    cronUI : function(){
        var reUI = this;
        //console.log(reUI.uiName, reUI.__hmaker__);
        while(!reUI.__zcron_ui__) {
            reUI = reUI.parent;
            if(!reUI)
                return;
        }
        return reUI;
    },
    //...............................................................
    getStrFromArrayUI : function(aryUI, anyPattern, anyValue) {
        var v  = aryUI.getData();
        var v2 = this.tidyArrayAsValueString(v) || anyValue;
        if(anyPattern == v2)
            v2 = anyValue;
        return v2;
    },
    //...............................................................
    setCronToArrayUI : function(aryUI, ozc, methodName) {
        var list  = [];
        if(ozc){
            var items = aryUI.getItems();
            var func  = ozc[methodName];
            for(var i=0; i<items.length; i++){
                var it = items[i];
                if(func.call(ozc, it))
                    list.push(it);
            }
        }
        aryUI.setData(list);
    },
    //...............................................................
    tidyArrayAsValueString : function(ary, dft, any){
        if(ary.length == 0)
            return dft;
        // 两个以内没必要压缩
        if(ary.length<=2)
            return ary.join(",");
        // 超过三个，可能压缩成范围
        var re    = [];
        var scope = [ary[0]];
        for(var i=1;i<ary.length;i++){
            var v = ary[i];
            // 判断是否为连续连续: scope.length == 1
            if(scope.length == 1 && (scope[0]+1) == v){
                scope[1] = v;
            }
            // 判断是否为连续连续: scope.length == 2
            else if(scope.length == 2 && (scope[1]+1) == v){
                scope[1] = v;
            }
            // 那么不连续
            else{
                re.push(scope.join((scope[1]-scope[0])>1?"-":","));
                scope = [v];
            }
        }
        re.push(scope.join((scope[1]-scope[0])>1?"-":","));
        var res = re.join(",");
        return res == any ? dft : res;
    }
    //...............................................................
}; // ~End methods

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
