package com.breskul.bibernate.collection;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Lazy List implementation
 *
 * @author Artem Yankovets
 */
public class LazyList<T> implements List<T> {

    private Supplier<List<?>> collectionSupplier;
    private List<T> internalList;

    public LazyList(Supplier<List<?>> collectionSupplier) {
        this.collectionSupplier = collectionSupplier;
    }

    private List<T> getInternalList() {
        if (internalList == null) {
            internalList = (List<T>) collectionSupplier.get();
        }
        return internalList;
    }

    @Override
    public int size() {
        return internalList.size();
    }

    @Override
    public boolean isEmpty() {
        return internalList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internalList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return internalList.iterator();
    }

    @Override
    public Object[] toArray() {
        return internalList.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return internalList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return internalList.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return internalList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return internalList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return internalList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return internalList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return internalList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return internalList.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        internalList.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        internalList.sort(c);
    }

    @Override
    public void clear() {
        internalList.clear();
    }

    @Override
    public boolean equals(Object o) {
        return internalList.equals(o);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public T get(int index) {
        return internalList.get(index);
    }

    @Override
    public T set(int index, T element) {
        return internalList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        internalList.add(index, element);
    }

    @Override
    public T remove(int index) {
        return internalList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return internalList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return internalList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return internalList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return internalList.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return internalList.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<T> spliterator() {
        return internalList.spliterator();
    }

    public static <E> List<E> of() {
        return List.of();
    }

    public static <E> List<E> of(E e1) {
        return List.of(e1);
    }

    public static <E> List<E> of(E e1, E e2) {
        return List.of(e1, e2);
    }

    public static <E> List<E> of(E e1, E e2, E e3) {
        return List.of(e1, e2, e3);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4) {
        return List.of(e1, e2, e3, e4);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5) {
        return List.of(e1, e2, e3, e4, e5);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
        return List.of(e1, e2, e3, e4, e5, e6);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return List.of(e1, e2, e3, e4, e5, e6, e7);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return List.of(e1, e2, e3, e4, e5, e6, e7, e8);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }

    @SafeVarargs
    public static <E> List<E> of(E... elements) {
        return List.of(elements);
    }

    public static <E> List<E> copyOf(Collection<? extends E> coll) {
        return List.copyOf(coll);
    }

    @Override
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        return internalList.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return internalList.removeIf(filter);
    }

    @Override
    public Stream<T> stream() {
        return internalList.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return internalList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        internalList.forEach(action);
    }
}
