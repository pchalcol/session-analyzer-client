package com.chaltec.web.session;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class JsonExclusionStrategy implements ExclusionStrategy {

    private final Map<Class<?>, String[]> excludedFields;

    public JsonExclusionStrategy() {
        excludedFields = new HashMap<>();
        for(Entry<Class<?>,String[]> entry : excludedFields.entrySet()) {
            Arrays.sort(entry.getValue());
        }
    }

    @Override
    public boolean shouldSkipField(final FieldAttributes f) {
        return excludedFields.containsKey(f.getDeclaredClass()) &&
            Arrays.binarySearch(excludedFields.get(f.getDeclaredClass()), f.getName()) >= 0;
    }

    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return false;
    }
}
