(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena smsw-step1-picktmpl" ui-fitparent="yes">
	<section ui-gasket="form"></section>
	<footer><pre></pre></footer>
</div>
*/};
//==============================================
return ZUI.def("ui.ext.smsw_step1_picktmpl", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        new FormUI({
        	parent : UI,
        	gasketName : 'form',
        	mergeData : false,
        	uiWidth : "all",
        	on_change : function(key, val) {
        		// 改变模板列表
        		if('lang' == key) {
        			UI.showTemplateList(val);
        		}
        		// 无论怎样，都要渲染一下模板
      			UI.renderTemplate('tmplName' == key);
        	},
        	fields : [{
        		key : 'lang',
        		title : 'i18n:smswizard.lang',
        		uiType : "@switch",
        		uiConf : {
        			items : 'obj ~/.sms/i18n/* -l -json -e "^(id|nm)"',
        			idKey : 'nm',
		        	icon  : '<i class="zmdi zmdi-globe-alt"></i>',
		        	text  : function(o){
		        		return UI.msg('langs.' + o.nm)
		        	},
		        	value : function(o) {
		        		return o.nm;
		        	}
        		}
        	}, {
        		key : 'tmplName',
        		title : 'i18n:smswizard.tmplName',
        		uiType : "@droplist",
        		uiConf : {
        			emptyItem : {
        				text : 'i18n:smswizard.tmplNameNone',
        				value : null
        			},
        			items : [],
        			text : function(o){
		        		return o.nm
		        	},
		        	value : function(o) {
		        		return o.nm;
		        	}
        		}
        	}, {
        		key : 'exampleTarget',
        		title : 'i18n:smswizard.exampleTarget',
        		type : "object",
        		uiType : "@droplist",
        		uiConf : {
        			emptyItem : {
        				text : 'i18n:smswizard.exampleTargetNone',
        				value : null
        			},
        			items : [],
        			escapeHtml : false,
        			text : function(o){
		        		return o[opt.nameKey];
		        	},
		        	tip : function(o){
		        		return o[opt.phoneKey];
		        	},
		        	value : function(o) {
		        		return o;
		        	}
        		}
        	}, {
        		key : 'params',
        		title : 'i18n:smswizard.params',
        		tip   : 'i18n:smswizard.params_tip',
        		type : "object",
        		dft : {},
        		uiType : "@pair",
        		uiConf : {
        		}
        	}]
        }).render(function(){
        	UI.defer_report('form');
        });

        // 返回延迟加载
        return ['form'];
    },
    //...............................................................
    showTemplateList : function(lang, callback) {
    	var UI = this;

    	var cmdText = 'obj ~/.sms/i18n/'+lang+'/* -l -json -e "^(id|nm|ph)$"';
    	
		UI.gasket.form.getFormCtrl('tmplName')
			.setItems(cmdText, function(list){
				$z.doCallback(callback, [list]);		
			});
    },
    //...............................................................
    renderTemplate : function(isTmplChanged) {
    	var UI  = this;
    	var jPr = UI.arena.find('pre');

    	// 得到数据
    	var data = UI.getData();
    	console.log(data);

    	// 有内容的话，加载
    	if(data.tmplName) {
    		var o = Wn.fetch('~/.sms/i18n/' + data.lang + '/' + data.tmplName);
    		var txt = Wn.read(o);

    		// 解析占位符
    		var tm = $z.parseTmpl(txt);
    		console.log(tm)

    		// 得到参数
    		var params = _.extend({}, tm.obj, data.params);

    		// 更新表单参数字段
    		if(isTmplChanged) {
    			UI.gasket.form.getFormCtrl('params').setObjTemplate(tm.obj);
    		}

    		// 更新参数表，遇到 =xxx 形式的值用示例目标的值替换
    		if(data.exampleTarget) {
    			for(var key in params) {
    				var val = params[key];
    				var m = /^=(.+)$/.exec(val);
    				if(m) {
    					var k2 = m[1];
    					params[key] = data.exampleTarget[k2] || val;
    				}
    			}
    		}

    		// 更新模板的显示
    		var txt2 = tm.tmpl(params);
    		jPr.text(txt2);
    	}
    	// 没有的话清空
    	else {
    		UI.gasket.form.getFormCtrl('params').setObjTemplate({});
    		jPr.text(UI.msg("smswizard.notmpl"));
    	}

    	// 检查下一步按钮状态
    	UI.parent.checkNextBtnStatus();
    },
    //...............................................................
    isDataReady : function(){
    	var data = this.getData();
    	console.log('is ready', data);
    	return data.lang && data.tmplName && data.params;
    },
    //...............................................................
    getData : function(data) {
    	return this.gasket.form.getData();
    },
    //...............................................................
    setData : function(data) {
    	var UI = this;

    	// 默认语言
    	$z.setUndefined(data, 'lang', UI.lang);

    	// 更新表单里的目标列表
    	UI.gasket.form.getFormCtrl('exampleTarget')
			.setItems(data.targets);

    	// 设置
    	UI.showTemplateList(data.lang, function(){
    		UI.gasket.form.setData(data);
    		// 检查下一步按钮状态
    		UI.parent.checkNextBtnStatus();
    	});
    },
    //...............................................................
    resize : function(){
    	var UI = this;
    	var jS = UI.arena.find('>section');
    	var jF = UI.arena.find('>footer');
    	
    	jS.css('height', UI.arena.height() - jF.outerHeight());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);