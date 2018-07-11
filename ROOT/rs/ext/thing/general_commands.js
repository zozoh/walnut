/*
所有函数的 this 均为： 
{
	UI    : ZUI,       // 当前执行此命令的UI，可能为 th_manager 或者 th_obj_index
    jBtn  : jQuery,    // 菜单项的 jQuery 对象
    menuItem : {..},   // 菜单项的配置对象
    bus : ZUI,         // bus对象，即 th_manager
    POP : POP,         // POP 帮助对象
    Wn  : Wn           // Wn 帮助对象
}
*/
({
	
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
	}
})