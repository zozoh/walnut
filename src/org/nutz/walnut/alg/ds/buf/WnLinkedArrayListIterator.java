package org.nutz.walnut.alg.ds.buf;

import java.util.ListIterator;
import java.util.NoSuchElementException;

class WnLinkedArrayListIterator<T> implements ListIterator<T> {

    int cursor;

    WnLinkedArrayList<T> list;

    int len;

    WnLinkedArrayListIterator(WnLinkedArrayList<T> list) {
        this.list = list;
        this.cursor = 0;
        this.len = list.size();
    }

    @Override
    public boolean hasNext() {
        return cursor < list.size();
    }

    @Override
    public T next() {
        if (this.cursor >= this.len) {
            throw new NoSuchElementException();
        }
        return list.get(cursor++);
    }

    @Override
    public boolean hasPrevious() {
        return cursor > 0;
    }

    @Override
    public T previous() {
        if (cursor > 0) {
            return list.get(--cursor);
        }
        throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
        return cursor;
    }

    @Override
    public int previousIndex() {
        return cursor - 1;
    }

    @Override
    public void remove() {
        list.remove(cursor);
    }

    @Override
    public void set(T e) {
        list.set(cursor, e);
    }

    @Override
    public void add(T e) {
        list.add(cursor, e);
    }

}
