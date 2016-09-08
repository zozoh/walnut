(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_panel',
    'ui/o_view_obj/o_view_preview',
    'ui/o_view_obj/o_view_meta',
    'ui/upload/upload',
], function(ZUI, Wn, HmMethods, ObjPreviewUI, ObjMetaUI, UploadUI){
//==============================================
var html = `
<div class="ui-arena hm-prop-folder" ui-fitparent="yes">
    <section class="pf-preview" ui-gasket="preview"></section>
    <section class="pf-meta"    ui-gasket="meta"></section>
    <section class="pf-upload"  ui-gasket="upload"></section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hm_prop_folder", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("active:folder",  function(o){
            UI.oFolder = o;
            UI.gasket.upload.setTarget(o);
            UI.do_active_file(UI.oFolder);
        });

        UI.listenBus("active:file",  function(o){
            // console.log("active:file", o.id)
            UI.do_active_file(o);
        });

        UI.listenBus("blur:file",  function(nextObj){
            if(!nextObj)
                UI.do_active_file(UI.oFolder);
        });
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        new ObjPreviewUI({
            parent : UI,
            gasketName : "preview"
        }).render(function(){
            UI.defer_report("preview");
        });

        new ObjMetaUI({
            parent : UI,
            gasketName : "meta",
            hideTitle  : true,
        }).render(function(){
            UI.defer_report("meta");
        });


        new UploadUI({
            parent : UI,
            gasketName : "upload",
            finish : function() {
                UI.fire("reload:folder");
            }
        }).render(function(){
            UI.defer_report("upload");
        });

        return ["preview", "meta", "upload"];
    },
    //...............................................................
    do_active_file : function(oFile) {
        var UI = this;

        // 显示对象预览
        UI.gasket.preview.update(oFile);

        // 元数据更新
        UI.gasket.meta.update(oFile);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);