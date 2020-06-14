package org.nutz.walnut.validate;

public class WnValidating {

    WnValidator validator;

    boolean not;

    Object[] args;

    public boolean isTrue(Object val) {
        if (not)
            return !validator.isTrue(val, args);
        return validator.isTrue(val, args);
    }

    public WnValidator getValidator() {
        return validator;
    }

    public void setValidator(WnValidator validator) {
        this.validator = validator;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}
