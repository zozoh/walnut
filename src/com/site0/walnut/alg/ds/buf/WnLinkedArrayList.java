package com.site0.walnut.alg.ds.buf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * 链接数组列表
 * <p>
 * 它试图在 LinkedList 与 ArrayList 中找到一个新的效率与空间的平衡。
 * <p>
 * 它的特点是增加了查找都比较快，但是删除会比较慢
 * 
 * <pre>
 * 实际上的存储 ...
 * |<--- width --->|
 * |               | -----
 * [T T T T T T ...]  ^
 * [T T T T T T ...]  height
 * [T T T T T T ...]  V 
 * [T T T T T T ...] -----
 * [T T T . . . ...] <- last
 *        ^          
 *        +-- index
 * 
 * </pre>
 * 
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @param <T>
 */
public class WnLinkedArrayList<T> implements List<T> {

    private int index;

    private int height;

    private int width;

    private ArrayList<T[]> data;

    private T[] last;

    private Class<T> eleType;

    public WnLinkedArrayList(Class<T> eleType) {
        this(eleType, 50);
    }

    public WnLinkedArrayList(Class<T> eleType, int width) {
        this.eleType = eleType;
        this.width = width;
        this.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public WnLinkedArrayList<T> clone() {
        WnLinkedArrayList<T> re = new WnLinkedArrayList<>(eleType, width);
        re.height = this.height;
        re.index = this.index;
        // 中间数据
        for (T[] arr : this.data) {
            T[] ar2 = (T[]) Array.newInstance(eleType, width);
            System.arraycopy(arr, 0, ar2, 0, width);
            re.data.add(ar2);
        }
        // 最后一行
        if (this.index > 0) {
            System.arraycopy(last, 0, re.last, 0, index);
        }
        return re;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean isFirst = true;
        for (T[] objs : data) {
            for (T obj : objs) {
                if (!isFirst) {
                    sb.append(", ");
                }
                if (null == obj) {
                    sb.append("null");
                } else {
                    sb.append(obj.toString());
                }
                isFirst = false;
            }
        }
        for (int i = 0; i < this.index; i++) {
            T obj = this.last[i];
            if (!isFirst) {
                sb.append(", ");
            }
            if (null == obj) {
                sb.append("null");
            } else {
                sb.append(obj.toString());
            }
            isFirst = false;
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int size() {
        return height * width + index;
    }

    @Override
    public boolean isEmpty() {
        return null == this.first();
    }

    @Override
    public boolean contains(Object o) {
        if (null == o)
            return false;
        for (T[] objs : data) {
            for (T ele : objs) {
                if (null != ele && ele.equals(o)) {
                    return true;
                }
            }
        }
        for (int i = 0; i < this.index; i++) {
            T ele = this.last[i];
            if (null != ele && ele.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new WnLinkedArrayListIterator<>(this);
    }

    private void __copy_to_array(Object a) {
        for (int i = 0; i < height; i++) {
            int pos = width * i;
            Object arr = data.get(i);
            System.arraycopy(arr, 0, a, pos, width);
        }
        if (this.index > 0) {
            int pos = width * height;
            System.arraycopy(last, 0, a, pos, index);
        }
    }

    @Override
    public Object[] toArray() {
        Object[] a = new Object[this.size()];
        __copy_to_array(a);
        return a;
    }

    @Override
    public <E> E[] toArray(E[] a) {
        __copy_to_array(a);
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean add(T e) {
        // 需要增加一个新行
        if (index > 0 && index >= width) {
            index = 0;
            height++;
            this.data.add(last);
            this.last = (T[]) Array.newInstance(eleType, width);
        }

        // 设置当前行
        this.last[this.index++] = e;

        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        if (index >= 0) {
            return null != this.remove(index);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (null == c || c.isEmpty()) {
            return true;
        }
        List<Object> cans = new ArrayList<>(c.size());
        cans.addAll(c);
        for (T[] objs : data) {
            for (T ele : objs) {
                cans.remove(ele);
                if (cans.isEmpty()) {
                    break;
                }
            }
            if (cans.isEmpty())
                break;
        }
        for (int i = 0; i < this.index; i++) {
            T ele = this.last[i];
            cans.remove(ele);
            if (cans.isEmpty()) {
                break;
            }
        }
        return cans.isEmpty();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        // Guard
        if (null == c || c.isEmpty()) {
            return false;
        }
        // 计入
        for (T e : c) {
            this.add(e);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(int atPos, Collection<? extends T> c) {
        // Guard
        if (null == c || c.isEmpty()) {
            return false;
        }
        // 加入最后一个
        if (atPos == this.size()) {
            return this.addAll(c);
        }

        // 首先将自己的数据全都 copy 出来
        T[] olds = (T[]) Array.newInstance(eleType, size());
        this.toArray(olds);

        // 重置自己
        this.clear();

        // 计算位置
        int y = atPos / width;
        int x = atPos - y * width;
        this.height = y;

        // Copy 前面的数据：整行
        T[] arr;
        int pos;
        for (int i = 0; i < y; i++) {
            arr = (T[]) Array.newInstance(eleType, width);
            pos = i * width;
            System.arraycopy(olds, pos, arr, 0, width);
            this.data.add(arr);
        }

        // 这一行开始，有了新数据了
        arr = (T[]) Array.newInstance(eleType, width);

        // Copy 前面的数据：最后一行
        if (x > 0) {
            pos = y * width;
            System.arraycopy(olds, pos, arr, 0, x);
        }

        // 插入新的数据
        Object[] newArr = c.toArray();
        int addLen = c.size();

        // 首先补足最后一行
        int remain = width - x;
        int offset = Math.min(addLen, remain);
        if (offset > 0) {
            System.arraycopy(newArr, 0, arr, x, offset);
        }

        // 计入，并准备最后一行
        this.data.add(arr);
        this.height++;
        this.last = (T[]) Array.newInstance(eleType, width);
        this.index = 0;

        // 剩下的，可以插入几行呢？
        int remainLen = addLen - offset;
        if (remainLen > 0) {
            int newY = remainLen / width;
            int newX = remainLen - remainLen / width;
            this.height += newY;
            for (int i = 0; i < newY; i++) {
                int srcPos = offset + i * width;
                System.arraycopy(newArr, srcPos, this.last, 0, width);
                this.data.add(this.last);
                this.last = (T[]) Array.newInstance(eleType, width);
            }

            // 还余下多少数据插入新行呢？
            int newPos = addLen - newX;
            System.arraycopy(newArr, newPos, this.last, 0, newX);
            this.index = newX;
        }

        // 插入后面的数据: 先补齐到整行
        remain = olds.length - atPos;
        if (remain > 0) {
            int space = width - this.index;
            int n = Math.min(remain, space);
            if (n > 0) {
                System.arraycopy(olds, atPos, this.last, this.index, n);
                this.data.add(this.last);
                this.height++;
                this.last = (T[]) Array.newInstance(eleType, width);
                remain -= n;
                atPos += n;
            }

            // 插入后面的数据: 整行 copy
            int oldY = remain / width;
            int oldX = remain - oldY * width;
            this.height += oldY;
            for (int i = 0; i < oldY; i++) {
                pos = atPos + i * width;
                arr = (T[]) Array.newInstance(eleType, width);
                System.arraycopy(olds, pos, arr, 0, width);
                this.data.add(arr);
            }

            // 还剩下的最后一行
            if (oldX > 0) {
                pos = atPos + oldY * width;
                this.index = oldX;
                System.arraycopy(olds, pos, this.last, 0, oldX);
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> c) {
        // Guard
        if (null == c || c.isEmpty()) {
            return false;
        }

        // 首先将自己的数据全都 copy 出来
        T[] olds = (T[]) Array.newInstance(eleType, size());
        this.toArray(olds);

        // 重置数据
        this.clear();

        // 逐个加入
        for (T obj : olds) {
            if (!c.contains(obj)) {
                this.add(obj);
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> c) {
        // Guard
        if (null == c || c.isEmpty()) {
            return false;
        }

        // 首先将自己的数据全都 copy 出来
        T[] olds = (T[]) Array.newInstance(eleType, size());
        this.toArray(olds);

        // 重置数据
        this.clear();

        // 逐个加入
        for (T obj : olds) {
            if (c.contains(obj)) {
                this.add(obj);
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        this.data = new ArrayList<>();
        this.height = 0;
        this.index = 0;
        this.last = (T[]) Array.newInstance(this.eleType, this.width);
    }

    @Override
    public T get(int index) {
        // Guard
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(String.format("Index(%d) Size(%d)", index, size()));
        }
        // Find the position
        int y = index / width;
        int x = index - y * width;
        if (y < this.height) {
            T[] arr = this.data.get(y);
            return arr[x];
        }
        // 指定在最后一行
        return this.last[x];
    }

    public T first() {
        if (this.height > 0) {
            return this.data.get(0)[0];
        }
        if (this.index > 0) {
            return this.last[0];
        }
        return null;
    }

    public T last() {
        if (this.index > 0) {
            return this.last[index - 1];
        }
        if (this.height > 0) {
            return this.data.get(height - 1)[width - 1];
        }
        return null;
    }

    public T popFirst() {
        if (this.isEmpty())
            return null;
        return this.remove(0);
    }

    public T popLast() {
        if (this.isEmpty())
            return null;
        return this.remove(this.size() - 1);
    }

    public T setFirst(T element) {
        if (this.isEmpty()) {
            this.add(element);
            return null;
        }
        return this.set(0, element);
    }

    public T setLast(T element) {
        if (this.isEmpty()) {
            this.add(element);
            return null;
        }
        return this.set(this.size() - 1, element);
    }

    @Override
    public T set(int index, T element) {
        // Guard
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(String.format("Index(%d) Size(%d)", index, size()));
        }
        // Prepare the return object
        T re;
        // Find the position
        int y = index / width;
        int x = index - y * width;
        if (y < this.height) {
            T[] arr = this.data.get(y);
            re = arr[x];
            arr[x] = element;
        }
        // 指定在最后一行
        else {
            re = this.last[x];
            this.last[x] = element;
        }

        return re;
    }

    @Override
    public void add(int index, T element) {
        List<T> cans = new ArrayList<>(1);
        cans.add(element);
        this.addAll(index, cans);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T remove(int index) {
        // Guard
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(String.format("Index(%d) Size(%d)", index, size()));
        }

        // 准备返回值
        T re = null;

        // 优化：移除的是最后一行
        int mal = width * height;
        if (index >= mal) {
            int ix = index - mal;
            re = last[ix];
            // 向前填补
            int lastI = this.index - 1;
            for (int i = ix; i < this.index; i++) {
                if (i == lastI) {
                    last[i] = null;
                } else {
                    last[i] = last[i + 1];
                }
            }
            this.index--;
            return re;
        }

        // 首先将自己的数据全都 copy 出来
        T[] olds = (T[]) Array.newInstance(eleType, size());
        this.toArray(olds);

        // 重置数据
        this.clear();

        // 逐个加入
        for (int i = 0; i < olds.length; i++) {
            if (i != index) {
                T obj = olds[i];
                this.add(obj);
            } else {
                re = olds[i];
            }
        }

        return re;
    }

    @Override
    public int indexOf(Object o) {
        if (null == o)
            return -1;

        for (int y = 0; y < this.height; y++) {
            T[] arr = this.data.get(y);
            for (int x = 0; x < arr.length; x++) {
                T ele = arr[x];
                if (null != ele && ele.equals(o)) {
                    return y * width + x;
                }
            }
        }
        // Search in last line
        for (int x = 0; x < this.index; x++) {
            T ele = this.last[x];
            if (null != ele && ele.equals(o)) {
                return this.height * width + x;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (null == o)
            return -1;

        // Search in last line
        for (int x = this.index - 1; x >= 0; x--) {
            T ele = this.last[x];
            if (null != ele && ele.equals(o)) {
                return this.height * width + x;
            }
        }

        for (int y = this.height - 1; y >= 0; y++) {
            T[] arr = this.data.get(y);
            for (int x = arr.length - 1; x >= 0; x++) {
                T ele = arr[x];
                if (null != ele && ele.equals(o)) {
                    return y * width + x;
                }
            }
        }

        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        WnLinkedArrayListIterator<T> it = new WnLinkedArrayListIterator<>(this);
        it.cursor = index;
        return it;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        List<T> list = new ArrayList<>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            T e = this.get(i);
            list.add(e);
        }
        return list;
    }

}
