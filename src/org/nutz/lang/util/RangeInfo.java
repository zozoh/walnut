package org.nutz.lang.util;

import java.util.Date;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;

public class RangeInfo {
    boolean hasMinValue;
    Object minValue;
    boolean minValueIncluded;
    boolean hasMaxValue;
    Object maxValue;
    boolean maxValueIncluded;

    private static Castors _C = Castors.me();

    public IntRange toIntRange() {
        IntRange r = new IntRange();
        if (null != minValue && hasMinValue) {
            r.left = _C.castTo(minValue, int.class);
            r.leftOpen = !minValueIncluded;
        }
        if (null != maxValue && hasMaxValue) {
            r.right = _C.castTo(maxValue, int.class);
            r.rightOpen = !maxValueIncluded;
        }
        return r;
    }

    public LongRange toLongRange() {
        LongRange r = new LongRange();
        if (null != minValue && hasMinValue) {
            r.left = _C.castTo(minValue, long.class);
            r.leftOpen = !minValueIncluded;
        }
        if (null != maxValue && hasMaxValue) {
            r.right = _C.castTo(maxValue, long.class);
            r.rightOpen = !maxValueIncluded;
        }
        return r;
    }

    public FloatRange toFloatRange() {
        FloatRange r = new FloatRange();
        if (null != minValue && hasMinValue) {
            r.left = _C.castTo(minValue, float.class);
            r.leftOpen = !minValueIncluded;
        }
        if (null != maxValue && hasMaxValue) {
            r.right = _C.castTo(maxValue, float.class);
            r.rightOpen = !maxValueIncluded;
        }
        return r;
    }

    public DoubleRange toDoubleRange() {
        DoubleRange r = new DoubleRange();
        if (null != minValue && hasMinValue) {
            r.left = _C.castTo(minValue, double.class);
            r.leftOpen = !minValueIncluded;
        }
        if (null != maxValue && hasMaxValue) {
            r.right = _C.castTo(maxValue, double.class);
            r.rightOpen = !maxValueIncluded;
        }
        return r;
    }

    public StrRange toStrRange() {
        StrRange r = new StrRange();
        if (null != minValue && hasMinValue) {
            r.left = minValue.toString();
            r.leftOpen = !minValueIncluded;
        }
        if (null != maxValue && hasMaxValue) {
            r.right = maxValue.toString();
            r.rightOpen = !maxValueIncluded;
        }
        return r;
    }

    public DateRange toDateRange() {
        DateRange r = new DateRange();
        if (null != minValue && hasMinValue) {
            r.left = _C.castTo(minValue, Date.class);
            r.leftOpen = !minValueIncluded;
        }
        if (null != maxValue && hasMaxValue) {
            r.right = _C.castTo(maxValue, Date.class);
            r.rightOpen = !maxValueIncluded;
        }
        return r;
    }

    public Mirror<?> getValueMirror() {
        if (null != minValue) {
            return Mirror.me(minValue.getClass());
        }
        if (null != maxValue) {
            return Mirror.me(maxValue.getClass());
        }
        return null;
    }

    /**
     * @return 这是否是一个有效的范围信息
     */
    public boolean isValid() {
        if (null == minValue && null == maxValue) {
            return false;
        }
        if (null == minValue || null == maxValue) {
            return true;
        }
        if (!minValue.getClass().equals(maxValue.getClass())) {
            return false;
        }
        return true;
    }

    public boolean isHasMinValue() {
        return hasMinValue;
    }

    public void setHasMinValue(boolean hasMinValue) {
        this.hasMinValue = hasMinValue;
    }

    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    public boolean isMinValueIncluded() {
        return minValueIncluded;
    }

    public void setMinValueIncluded(boolean minValueIncluded) {
        this.minValueIncluded = minValueIncluded;
    }

    public boolean isHasMaxValue() {
        return hasMaxValue;
    }

    public void setHasMaxValue(boolean hasMaxValue) {
        this.hasMaxValue = hasMaxValue;
    }

    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public boolean isMaxValueIncluded() {
        return maxValueIncluded;
    }

    public void setMaxValueIncluded(boolean maxValueIncluded) {
        this.maxValueIncluded = maxValueIncluded;
    }

}
