/*
所有函数的 this 均为： 
{
	UI    : ZUI,       // this.thMain(); -> thM
    jBtn  : jQuery,    // 菜单项的 jQuery 对象
    menuItem : {..},   // 菜单项的配置对象
	bus : ZUI,         // thM.gasket.main (layout)
	man : man,         // thM.__main_data;
	objs : [..],       // thM.subUI("main/list").getChecked();
    POP : POP,         // POP 帮助对象
    Wn  : Wn           // Wn 帮助对象
}
*/
({
	// 打开一个短信群发的向导
	openSendSmsWizard : function(objs) {
		//console.log(this.uiName, objs)
		this.POP.openUIPanel({
			title : this.menuItem.text,
			ready : function(uiSW){
				uiSW.update(objs);
			},
			setup : {
				uiType : 'uix/smswizard/smswizard',
				uiConf : this.menuItem.setup
			},
			btnOk : null,
			btnCancel : null
		}, this.UI);
	},
	/*
	打开一个标准的命令批量执行向导：
	*/
	openBatchcmdsWizard : function(objs) {
		var context = this;
		//console.log(this.uiName, objs)
		var setup = this.menuItem.setup || {};
		this.POP.openUIPanel({
			title  : this.menuItem.text,
			width  : setup.width  || "80%",
			height : setup.height || "80%",
			ready  : function(uiBcW){
				uiBcW.update(objs);
			},
			setup : {
				uiType : 'uix/batchcmds/batchcmds',
				uiConf : _.extend({}, this.menuItem.setup, {
					thing : context,
					targetHome : context.man.home
				})
			},
			btnOk : null,
			btnCancel : null
		}, this.UI);
	}
})