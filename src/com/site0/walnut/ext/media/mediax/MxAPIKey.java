package com.site0.walnut.ext.media.mediax;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE})
@Documented
public @interface MxAPIKey {
    String value();
}
