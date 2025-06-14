package com.site0.walnut.ext.media.edi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;

public class EdiSegmentFinder {

    private List<EdiSegment> list;

    private ListIterator<EdiSegment> it;

    public EdiSegmentFinder(List<EdiSegment> list) {
        this.list = list;
        reset();
    }

    public void reset() {
        it = this.list.listIterator();
    }

    public boolean isEnd() {
        return !it.hasNext();
    }

    public EdiSegment prev(String... tags) {
        // 防守
        if (!it.hasPrevious())
            return null;

        // 普通迭代
        if (tags.length == 0) {
            return it.previous();
        }

        // 循环查找
        while (it.hasPrevious()) {
            EdiSegment seg = it.previous();
            if (seg.is(tags)) {
                return seg;
            }
        }
        return null;
    }

    public EdiSegment next(String... tags) {
        // 防守
        if (!it.hasNext())
            return null;

        // 普通迭代
        if (tags.length == 0) {
            return it.next();
        }

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (seg.is(tags)) {
                return seg;
            }
        }
        return null;
    }

    public List<EdiSegment> prevAll(String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasPrevious())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;

        // 循环查找
        while (it.hasPrevious()) {
            EdiSegment seg = it.previous();
            if (always || seg.is(tags)) {
                re.add(seg);
            }
        }
        return re;
    }

    public List<EdiSegment> nextAll(String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (always || seg.is(tags)) {
                re.add(seg);
            }
        }
        return re;
    }

    public List<EdiSegment> nextAll(boolean noMatchBreak, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (always || seg.is(tags)) {
                re.add(seg);
            } else {
                if (noMatchBreak) {
                    // 不符合条件，回退一下指针，后续读取将从这个未匹配的开始
                    it.previous();
                    break;
                }
            }

        }
        return re;
    }

    public List<EdiSegment> nextAllUtilNoMatch(boolean noBreakUtilFound, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;
        boolean foundOne = false;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (always || seg.is(tags)) {
                re.add(seg);
                foundOne = true;
            } else if (!noBreakUtilFound || foundOne) {
                // 不符合条件，回退一下指针，后续读取将从这个未匹配的开始
                it.previous();
                break;
            }
        }
        return re;
    }

    public List<EdiSegment> prevUntil(boolean inclusive, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasPrevious())
            return re;

        // 循环查找
        while (it.hasPrevious()) {
            EdiSegment seg = it.previous();
            if (seg.is(tags)) {
                if (inclusive) {
                    re.add(seg);
                }
                break;
            } else {
                re.add(seg);
            }
        }
        return re;
    }

    public List<EdiSegment> nextUntil(boolean inclusive, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (seg.is(tags)) {
                if (inclusive) {
                    re.add(seg);
                }
                break;
            } else {
                re.add(seg);
            }
        }
        return re;
    }

    public EdiSegment tryNext(String tag) {
        // 防守
        if (!it.hasNext())
            return null;

        // 找下一行, 判断
        EdiSegment seg = it.next();
        if (seg.isTag(tag)) {
            return seg;
        }

        //若找不到, 则回退一行
        it.previous();
        return null;
    }

    /**
     * 1. 从当前位置开始，移动到 能匹配上 tag 的位置;
     * - a. 找到 tag 后，若 backOneSegment=true 那么则后退一步；
     * - b. 找到 tag 后，若 backOneSegment=false 则维持指向位置；
     * 2. 若找不到 tag，则回退到当前(匹配开始)的位置；
     */
    public boolean moveTo(boolean backOneSegment, String tag) {
        return moveTo(backOneSegment, tag, null);
    }

    /**
     * 1. 从当前位置开始，移动到 能匹配上 tag 的位置;
     * - a. 找到 tag 后，若 backOneSegment=true 那么则后退一步；
     * - b. 找到 tag 后，若 backOneSegment=false 则维持指向位置；
     * 2. 若找不到 tag， 或 中途遇到了 stopTags 中的 tag, 则回退到当前(匹配开始)的位置；
     */
    public boolean moveTo(boolean backOneSegment, String goalTag, String... stopTags) {
        // 防守
        if (!it.hasNext())
            return false;

        int stepNum = 0;
        boolean findOne = false;
        EdiSegment seg;

        // 查找判断
        while (it.hasNext()) {
            seg = it.next();
            stepNum++;

            if (seg.isTag(goalTag)) {
                findOne = true;
                if (backOneSegment) {
                    it.previous();
                }
                break;
            }
            if (stopTags != null && stopTags.length > 0) {
                if (seg.is(stopTags)) {
                    break;
                }
            }
        }

        // 若找不到，则返回原位置
        if (!findOne && stepNum > 0) {
            while (stepNum > 0) {
                it.previous();
                stepNum--;
            }
        }

        return findOne;
    }


    /**
     * 1. 此方法常常配合 moveTo 方法使用，先使用 moveTo 方法定位到区域前方;
     * 2.
     */
    public List<EdiSegment> findContinueSegments(String begin, String match, String boundary) {
        List<EdiSegment> re = new ArrayList<>(list.size());

        // 防守
        if (!it.hasNext())
            return re;

        // 检查能否匹配 begin Tag的报文
        EdiSegment seg = it.next();
        if (!seg.is(begin)) {
            it.previous();
            return re;
        }

        // 读取 match 相关 tag，并返回
        while (it.hasNext()) {
            seg = it.next();
            if (seg.isTag(match)) {
                re.add(seg);
            } else if (seg.isTag(boundary)) {
                it.previous();
                break;
            }
        }

        return re;
    }

}
