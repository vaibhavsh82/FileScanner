package android.macys.com.filescanner;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScanUtil {

    public static String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

    public static List<Pair<String, Integer>> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        int endIdx = list.size() > 5 ? 5 : list.size();
        list = list.subList(0, endIdx);

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        List<Pair<String, Integer>> sortedMap = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.add(Pair.create(entry.getKey(), entry.getValue()));
        }
        return sortedMap;
    }
}
