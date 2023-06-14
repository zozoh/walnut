package org.nutz.walnut.alg.stack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class LinkedStack<T> implements WnStack<T> {

    private LinkedList<T> stack = new LinkedList<>();

    @Override
    public void push(T item) {
        stack.addFirst(item);
    }

    @Override
    public T pop() {
        return stack.removeFirst();
    }

    @Override
    public List<T> popUtil(Predicate<T> filter, boolean includesive) {
        List<T> popped = new LinkedList<>();
        T top;
        while (!isEmpty()) {
            top = pop();
            if (filter.test(top)) {
                // 将符合条件的元素重新压入栈中
                if (!includesive) {
                    push(top);
                }
                // 返回的结果也包括边界元素
                else {
                    popped.add(top);
                }
                break;
            }
            popped.add(top);
        }
        return popped;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] popUtilAsArray(Predicate<T> filter, boolean includesive, Class<T> classOfT) {
        List<T> popped = this.popUtil(filter, includesive);
        T[] array = (T[]) Array.newInstance(classOfT, popped.size());
        popped.toArray(array);
        return array;
    }

    @Override
    public List<T> popAll() {
        List<T> popped = new ArrayList<>(stack.size());
        popped.addAll(stack);
        stack.clear();
        return popped;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] popAllAsArray(Class<T> classOfT) {
        List<T> popped = this.popAll();
        T[] array = (T[]) Array.newInstance(classOfT, popped.size());
        popped.toArray(array);
        return array;
    }

    @Override
    public T peek() {
        return stack.peekFirst();
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = stack.size() - 1;
        for (T obj : stack) {
            sb.append(String.format("%02d) [ %10s ]\n", i, obj.toString()));
        }
        return sb.toString();
    }

    @Override
    public int search(T item) {
        int index = 1;
        for (T element : stack) {
            if (element.equals(item)) {
                return index;
            }
            index++;
        }
        return -1;
    }

}
