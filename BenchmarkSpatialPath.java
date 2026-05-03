import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BenchmarkSpatialPath {
    public static void main(String[] args) {
        int segmentCount = 1000;
        int pointsPerSegment = 50;

        List<List<Integer>> segments = new ArrayList<>();
        int ptVal = 0;
        for (int i = 0; i < segmentCount; i++) {
            List<Integer> segment = new ArrayList<>();
            for (int j = 0; j < pointsPerSegment; j++) {
                segment.add(ptVal);
                if (j < pointsPerSegment - 1) {
                   ptVal++;
                }
            }
            segments.add(segment);
        }

        // Test List Contains
        long startList = System.nanoTime();
        List<Integer> ptsList = new ArrayList<>();
        for (List<Integer> ent : segments) {
            for (Integer p : ent) {
                if (!ptsList.contains(p)) {
                    ptsList.add(p);
                }
            }
        }
        long endList = System.nanoTime();

        // Test LinkedHashSet
        long startSet = System.nanoTime();
        Set<Integer> ptsSet = new LinkedHashSet<>();
        for (List<Integer> ent : segments) {
            for (Integer p : ent) {
                ptsSet.add(p);
            }
        }
        List<Integer> resultList = new ArrayList<>(ptsSet);
        long endSet = System.nanoTime();

        System.out.println("List Contains Time: " + (endList - startList) / 1000000.0 + " ms");
        System.out.println("Set Add Time: " + (endSet - startSet) / 1000000.0 + " ms");
        System.out.println("Result lists size matches: " + (ptsList.size() == resultList.size()));
    }
}
