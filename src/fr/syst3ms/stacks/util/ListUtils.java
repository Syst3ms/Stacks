package fr.syst3ms.stacks.util;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by ARTHUR on 29/08/2017.
 */
public class ListUtils {

    public static <T> LinkedHashMap<Integer, T> mapFromList(List<T> list) {
        LinkedHashMap<Integer, T> map = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            T element = list.get(i);
            map.put(i, element);
        }
        return map;
    }
}
