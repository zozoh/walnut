package org.nutz.walnut.api.auth;

public enum WnHumanSex {
    UNKNOWN(0), MALE(1), FEMALE(2);

    private int value;

    private WnHumanSex(int sex) {
        this.value = sex;
    }

    public int getValue() {
        return this.value;
    }

    public static WnHumanSex parseInt(int sex) {
        if (0 == sex)
            return UNKNOWN;
        if (1 == sex)
            return MALE;
        if (2 == sex)
            return FEMALE;
        return UNKNOWN;
    }
}
