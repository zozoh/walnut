package com.site0.walnut.ext.media.subtitle;

import com.site0.walnut.ext.media.subtitle.bean.SubtitleObj;

public interface SubtitleService {

    SubtitleObj parse(CharSequence cs);

    String render(SubtitleObj sto);

}
