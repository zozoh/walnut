package com.site0.walnut.ext.data.fake.out;

import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.data.fake.FakeOutput;

public class FakeListOutput implements FakeOutput {

    private List<NutBean> list;

    public FakeListOutput(List<NutBean> list) {
        this.list = list;
    }

    @Override
    public void write(Object obj) {
        if (null != obj && obj instanceof NutBean) {
            NutBean meta = (NutBean) obj;
            list.add(meta);
        }
    }

}
