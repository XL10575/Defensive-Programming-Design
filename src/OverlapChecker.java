import java.util.ArrayList;
import java.util.List;

public class OverlapChecker {

    public static List<String> findOverlaps(List<Event> events) {
        List<String> overlapMessages = new ArrayList<>();
        if (events == null) {
            return overlapMessages;
        }

        int n = events.size();
        for (int i = 0; i < n; i++) {
            Event e1 = events.get(i);
            for (int j = i + 1; j < n; j++) {
                Event e2 = events.get(j);
                if (eventsOverlap(e1, e2)) {
                    String message = "Event " + (i+1) + " " + e1 + " overlaps with Event " + (j+1) + " " + e2;
                    overlapMessages.add(message);
                }
            }
        }
        return overlapMessages;
    }

    private static boolean eventsOverlap(Event a, Event b) {
        return a.getStartDay() <= b.getEndDay() && b.getStartDay() <= a.getEndDay();
    }
}
