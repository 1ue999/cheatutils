package com.zergatul.cheatutils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ImmutableList<E> implements Iterable<E> {

    private final Object[] array;

    public ImmutableList() {
        array = new Object[0];
    }

    public ImmutableList(Collection<E> collection) {
        this(collection.toArray());
    }

    private ImmutableList(Object[] array) {
        this.array = array;
    }

    public ImmutableList<E> add(E element) {
        int size = array.length;
        Object[] newArray = new Object[size + 1];
        System.arraycopy(array, 0, newArray, 0, size);
        newArray[size] = element;
        return new ImmutableList<>(newArray);
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) array[index];
    }

    public ImmutableList<E> remove(E element) {
        int size = array.length;
        int i = 0;
        found: {
            if (element == null) {
                for (; i < size; i++) {
                    if (array[i] == null) {
                        break found;
                    }
                }
            } else {
                for (; i < size; i++) {
                    if (element.equals(array[i])) {
                        break found;
                    }
                }
            }
            return this;
        }

        Object[] newArray = new Object[size - 1];
        if (i > 0) {
            System.arraycopy(array, 0, newArray, 0, i);
        }
        if (i < size - 1) {
            System.arraycopy(array, i + 1, newArray, i, size - i - 1);
        }
        return new ImmutableList<>(newArray);
    }

    public ImmutableList<E> removeIf(Predicate<E> predicate) {
        List<E> list = new ArrayList<>();
        for (E e: this) {
            if (!predicate.test(e)) {
                list.add(e);
            }
        }
        return new ImmutableList<>(list.toArray());
    }

    public ImmutableList<E> set(int index, E element) {
        int size = array.length;
        Object[] newArray = new Object[size];
        System.arraycopy(array, 0, newArray, 0, size);
        newArray[index] = element;
        return new ImmutableList<>(newArray);
    }

    public int size() {
        return array.length;
    }

    @SuppressWarnings("unchecked")
    public Stream<E> stream() {
        return Arrays.stream(array).map(e -> (E) e);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                return (E) array[index++];
            }
        };
    }
}