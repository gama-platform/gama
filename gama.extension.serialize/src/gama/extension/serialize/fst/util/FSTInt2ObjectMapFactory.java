package gama.extension.serialize.fst.util;

public interface FSTInt2ObjectMapFactory {
    <V> FSTInt2ObjectMap<V> createMap(int size);
}