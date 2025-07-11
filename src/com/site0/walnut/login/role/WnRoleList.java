package com.site0.walnut.login.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.nutz.lang.util.NutBean;

public class WnRoleList implements List<WnRole> {

    private List<WnRole> roles;

    public WnRoleList(List<WnRole> roles) {
        this.roles = roles;
    }

    public boolean isMemberOfRole(String... roleNames) {
        if (null != this.roles) {
            for (String roleName : roleNames) {
                for (WnRole r : this.roles) {
                    if (r.isMatchName(roleName) && r.getType().isOf(WnRoleType.MEMBER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isAdminOfRole(String... roleNames) {
        if (null != this.roles) {
            for (String roleName : roleNames) {
                for (WnRole r : this.roles) {
                    if (r.isMatchName(roleName) && r.getType().isOf(WnRoleType.ADMIN)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public WnRoleType getRoleTypeOfGroup(String group) {
        if (null != this.roles) {
            for (WnRole r : this.roles) {
                if (r.isMatchName(group)) {
                    return r.getType();
                }
            }
        }
        return WnRoleType.GUEST;
    }

    public WnRoleList getSubList(String... groups) {
        HashSet<String> set = new HashSet<>();
        for (String grp : groups) {
            set.add(grp);
        }
        List<WnRole> list = new ArrayList<>(roles.size());
        for (WnRole r : this.roles) {
            if (set.contains(r.getName())) {
                list.add(r);
            }
        }
        return new WnRoleList(list);
    }

    public Map<String, Boolean> getAllPrivileges() {
        Map<String, Boolean> re = new HashMap<>();
        for (WnRole r : this.roles) {
            r.mergePrivilegesTo(re);
        }
        return re;
    }

    public List<NutBean> toBeans() {
        List<NutBean> outs = new ArrayList<>(roles.size());
        for (WnRole bean : roles) {
            outs.add(bean.toBean());
        }
        return outs;
    }

    public void forEach(Consumer<? super WnRole> action) {
        roles.forEach(action);
    }

    public int size() {
        return roles.size();
    }

    public boolean isEmpty() {
        return roles.isEmpty();
    }

    public boolean contains(Object o) {
        return roles.contains(o);
    }

    public Iterator<WnRole> iterator() {
        return roles.iterator();
    }

    public Object[] toArray() {
        return roles.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return roles.toArray(a);
    }

    public boolean add(WnRole e) {
        return roles.add(e);
    }

    public boolean remove(Object o) {
        return roles.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return roles.containsAll(c);
    }

    public boolean addAll(Collection<? extends WnRole> c) {
        return roles.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends WnRole> c) {
        return roles.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return roles.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return roles.retainAll(c);
    }

    public void replaceAll(UnaryOperator<WnRole> operator) {
        roles.replaceAll(operator);
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return roles.toArray(generator);
    }

    public void sort(Comparator<? super WnRole> c) {
        roles.sort(c);
    }

    public void clear() {
        roles.clear();
    }

    public boolean equals(Object o) {
        return roles.equals(o);
    }

    public int hashCode() {
        return roles.hashCode();
    }

    public WnRole get(int index) {
        return roles.get(index);
    }

    public WnRole set(int index, WnRole element) {
        return roles.set(index, element);
    }

    public void add(int index, WnRole element) {
        roles.add(index, element);
    }

    public boolean removeIf(Predicate<? super WnRole> filter) {
        return roles.removeIf(filter);
    }

    public WnRole remove(int index) {
        return roles.remove(index);
    }

    public int indexOf(Object o) {
        return roles.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return roles.lastIndexOf(o);
    }

    public ListIterator<WnRole> listIterator() {
        return roles.listIterator();
    }

    public ListIterator<WnRole> listIterator(int index) {
        return roles.listIterator(index);
    }

    public List<WnRole> subList(int fromIndex, int toIndex) {
        return roles.subList(fromIndex, toIndex);
    }

    public Spliterator<WnRole> spliterator() {
        return roles.spliterator();
    }

    public Stream<WnRole> stream() {
        return roles.stream();
    }

    public Stream<WnRole> parallelStream() {
        return roles.parallelStream();
    }

}
