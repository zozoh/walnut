{
	"default" : {
		"ps" : [
		      "org.nutz.mvc.impl.processor.UpdateRequestAttributesProcessor",
		      "org.nutz.mvc.impl.processor.EncodingProcessor",
		      "com.site0.walnut.web.processor.CreateWnContext",
		      "org.nutz.mvc.impl.processor.ModuleProcessor",
		      "org.nutz.mvc.impl.processor.ActionFiltersProcessor",
		      "org.nutz.mvc.impl.processor.AdaptorProcessor",
		      "org.nutz.mvc.impl.processor.MethodInvokeProcessor",
		      "org.nutz.mvc.impl.processor.ViewProcessor",
		      "com.site0.walnut.web.processor.DeposeWnContext"
		      ],
		"error" : 'com.site0.walnut.web.processor.WnFailProcessor'
	}
}