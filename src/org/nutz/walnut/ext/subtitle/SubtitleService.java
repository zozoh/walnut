package org.nutz.walnut.ext.subtitle;

import org.nutz.walnut.ext.subtitle.bean.SubtitleObj;

public interface SubtitleService {

    SubtitleObj parse(CharSequence cs);

    String render(SubtitleObj sto);

}
