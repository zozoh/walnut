package org.nutz.mvc.impl.processor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.mvc.*;
import org.nutz.mvc.adaptor.PairAdaptor;

/**
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author MingzFan(Mingz.Fan@gmail.com)
 */
public class AdaptorProcessor extends AbstractProcessor {

    private HttpAdaptor adaptor;

    @Override
    public void init(NutConfig config, ActionInfo ai) throws Throwable {
        adaptor = evalHttpAdaptor(config, ai);
    }

    public void process(ActionContext ac) throws Throwable {
        List<String> phArgs = ac.getPathArgs();
        HttpServletRequest req = ac.getRequest();
        if (ac.getReferObject() != null)
            req.setAttribute(ActionContext.REFER_OBJECT, ac.getReferObject());
        Object[] args = adaptor.adapt(ac.getServletContext(),
                                      req,
                                      ac.getResponse(),
                                      phArgs.toArray(new String[phArgs.size()]));
        Object referObject = req.getAttribute(ActionContext.REFER_OBJECT);
        ac.setReferObject(referObject);
        req.removeAttribute(ActionContext.REFER_OBJECT);
        ac.setMethodArgs(args);
        doNext(ac);
    }

    protected static HttpAdaptor evalHttpAdaptor(NutConfig config, ActionInfo ai) {
        HttpAdaptor re = evalObj(config, ai.getAdaptorInfo());
        if (null == re)
            re = new PairAdaptor();
        if (re instanceof HttpAdaptor2)
            ((HttpAdaptor2) re).init(ai);
        else
            re.init(ai.getMethod());
        return re;
    }
}
