package com.theost.volli.utils;

import java.util.List;

public class ArrayUtils {

    public static<T> boolean isEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }

}
