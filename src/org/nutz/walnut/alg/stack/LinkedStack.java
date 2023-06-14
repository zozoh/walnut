package org.nutz.walnut.alg.stack;

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

    @Override
    public T peek() {
        return stack.peekFirst();
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
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
