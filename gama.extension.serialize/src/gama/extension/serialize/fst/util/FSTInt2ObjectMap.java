package gama.extension.serialize.fst.util;

public interface FSTInt2ObjectMap<V> {
    int size();
    void put(int key, V value);
    V get(int key);
    void clear();
}