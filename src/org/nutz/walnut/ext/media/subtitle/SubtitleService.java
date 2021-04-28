package org.nutz.walnut.ext.media.subtitle;

import org.nutz.walnut.ext.media.subtitle.bean.SubtitleObj;

public interface SubtitleService {

    SubtitleObj parse(CharSequence cs);

    String render(SubtitleObj sto);

}
