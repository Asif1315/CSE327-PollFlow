package com.pollflow.proxy;

import java.util.HashMap;
import java.util.Map;

public class Cache {
    private static final Map<String, Object> cache = new HashMap<>();
    
    public static Object get(String key) {
        return cache.get(key);
    }
    
    public static void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public static void remove(String key) {
        cache.remove(key);
    }
    
    public static void clear() {
        cache.clear();
    }
    
    public static boolean contains(String key) {
        return cache.containsKey(key);
    }
}
