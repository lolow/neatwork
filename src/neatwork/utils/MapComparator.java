package neatwork.utils;

import java.util.*;


/**
 * Compare deux objets par rapport a leur reference dans un map, un integer
 * @author L. DROUET
 * @version 1.0
 */
public class MapComparator implements Comparator {
    private Map map;

    public MapComparator(Map map) {
        this.map = map;
    }

    public int compare(Object o1, Object o2) {
        Integer ref1 = (Integer) map.get(o1);
        Integer ref2 = (Integer) map.get(o2);
        int r1 = 0;
        int r2 = 0;

        if (ref1 != null) {
            r1 = ref1.intValue();
        }

        if (ref2 != null) {
            r2 = ref2.intValue();
        }

        return r1 - r2;
    }
}
