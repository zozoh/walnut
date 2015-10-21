define(function (require, exports, module) {
    module.exports = {
        //......................................
        "string" : {
            defaultEditAs : "input",
            normalize : function(fld){

            },
            display : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            get : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            set : function(fld, o, v){
                $z.setValue(o, fld.key, v+"");
            }
        },
        //......................................
        "boolean" : {
            defaultEditAs : "bool_switch",
            normalize : function(fld){
                var UI = this;
                // 不是数组认为无效
                if(!_.isArray(fld.setup)){
                    fld.setup = [{text:"i18n:no",val:false},{text:"i18n:yes",val:true}];
                }
                // 深层检查一下
                else{
                    for(var i=0; i<2; i++){
                        var se = fld.setup[i];
                        if(!_.isObject(se)) {
                            fld.setup[i] = {
                                text : UI.text(se),
                                val  : (i==1)
                            }
                        } else {
                            se.text = se.text || (i==0?"i18n:no":"i18n:yes");
                            se.val = se.val || (i==0?false:true);
                        }
                     } // end for   
                }
            },
            display : function(fld, o){
                var v = $z.getValue(o, fld.key, fld.dft);
                var vIndex = (v === fld.setup[1].val) ? 1 : 0;
                return fld.setup[vIndex].text;
            },
            get : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            set : function(fld, o, v){
                $z.setValue(o, fld.key, v);
            }
        },
        //......................................
        "date" : {
            defaultEditAs : "input",
            normalize : function(fld){

            },
            display : function(fld, o){
                var v = $z.getValue(o, fld.key, fld.dft)
                var date = new Date();
                date.setTime(v)
                return date.toLocaleString();
            },
            get : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            set : function(fld, o, v){
                $z.setValue(o, fld.key, v   );
            }
        },
        //......................................
        "datetime" : {
            defaultEditAs : "input",
            normalize : function(fld){

            },
            display : function(fld, o){
                var v = $z.getValue(o, fld.key, fld.dft)
                var date = new Date();
                date.setTime(v)
                return date.toLocaleString();
            },
            get : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            set : function(fld, o, v){
                $z.setValue(o, fld.key, v   );
            }
        },
        //......................................
        "int" : {
            defaultEditAs : "input",
            normalize : function(fld){

            },
            display : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            get : function(fld, o){
                return $z.getValue(o, fld.key, fld.dft);
            },
            set : function(fld, o, v){
                $z.setValue(o, fld.key, v);
            }
        }
    };

});