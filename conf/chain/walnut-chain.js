{
	"default" : {
		"ps" : [
		      "org.nutz.mvc.impl.processor.UpdateRequestAttributesProcessor",
		      "org.nutz.mvc.impl.processor.EncodingProcessor",
		      "org.nutz.walnut.web.processor.CreateWnContext",
		      "org.nutz.mvc.impl.processor.ModuleProcessor",
		      "org.nutz.mvc.impl.processor.ActionFiltersProcessor",
		      "org.nutz.mvc.impl.processor.AdaptorProcessor",
		      "org.nutz.mvc.impl.processor.MethodInvokeProcessor",
		      "org.nutz.mvc.impl.processor.ViewProcessor",
		      "org.nutz.walnut.web.processor.DeposeWnContext"
		      ],
		"error" : 'org.nutz.walnut.web.processor.WnFailProcessor'
	}
}