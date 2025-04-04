import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public class EventScheduleTest {

    @Test
    public void testNoOverlapScenario() {
        String input = 
            "3\n" +           // 3 events
            "1 5\n" +         // Event 1: day 1-5
            "10 15\n" +       // Event 2: day 10-15
            "20 30\n";        // Event 3: day 20-30 (no overlaps with others)
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse("Should have no errors", result.hasErrors());
        assertFalse("Should have no warnings", result.hasWarnings());
        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        assertTrue("Expected no overlaps", overlaps.isEmpty());
    }

    @Test
    public void testOverlapDetected() {
        String input = 
            "3\n" +
            "1 10\n" +    // Event 1: 1-10
            "5 8\n" +     // Event 2: 5-8 (overlaps with Event 1)
            "11 20\n";    // Event 3: 11-20 (does not overlap with 1 or 2)
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse(result.hasErrors());
        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        // We expect one overlap: Event 1 with Event 2
        assertEquals("There should be exactly one overlapping pair", 1, overlaps.size());
        String overlapMsg = overlaps.get(0);
        // It should mention event 1 and 2
        assertTrue("Overlap message should mention Event 1 and Event 2", 
                   overlapMsg.contains("Event 1") && overlapMsg.contains("Event 2"));
        // Specifically, it should be "Event 1 (1, 10) overlaps with Event 2 (5, 8)"
        assertEquals("Event 1 (1, 10) overlaps with Event 2 (5, 8)", overlapMsg);
    }

    @Test
    public void testOverlapAtBoundaryInclusive() {
        // Events that touch at the boundary day (one ends same day another begins) -> considered overlapping
        String input =
            "2\n" +
            "10 20\n" +   // Event 1: 10-20
            "20 30\n";    // Event 2: 20-30 (day 20 overlaps with Event 1)
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse(result.hasErrors());
        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        assertEquals("Events sharing a boundary day should overlap", 1, overlaps.size());
        assertEquals("Event 1 (10, 20) overlaps with Event 2 (20, 30)", overlaps.get(0));
    }

    @Test
    public void testInvalidNumberOfEventsFormat() {
        // First line not an integer
        String input = 
            "five\n" +   // invalid number
            "1 10\n" +
            "15 20\n";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertTrue("Should have errors due to non-integer first line", result.hasErrors());
        // Check that the error message is as expected
        String error = result.errors.get(0);
        assertTrue(error.contains("First line is not a valid integer"));
    }

    @Test
    public void testNegativeEventCount() {
        // Negative number of events
        String input = 
            "-2\n" +
            "1 5\n" +
            "2 6\n";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        // Negative count should be flagged as error, but parser will still attempt to parse lines
        assertTrue(result.hasErrors());
        // It should parse the two events nevertheless (robustness)
        assertEquals("Parsed events even with negative count", 2, result.events.size());
        // And it should mark the negative count error
        boolean foundNegError = result.errors.stream()
                                .anyMatch(msg -> msg.contains("cannot be negative"));
        assertTrue("Error for negative count is reported", foundNegError);
    }

    @Test
    public void testMalformedEventLineNotEnoughNumbers() {
        String input = 
            "2\n" +
            "100\n" +    // only one number on this line
            "50 60\n";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertTrue(result.hasErrors());
        // The first event line is malformed, so we expect an error about "expected 2 numbers but found 1"
        String error = result.errors.stream()
                        .filter(msg -> msg.contains("expected 2 numbers")).findFirst().orElse("");
        assertFalse("Error for malformed line present", error.isEmpty());
        // Only the second line is valid, so we expect one event parsed
        assertEquals(1, result.events.size());
        // Because of the error, we should not proceed to overlap check (main would stop).
    }

    @Test
    public void testExtraDataIgnored() {
        String input = 
            "1\n" +
            "10 12 99 100\n";  // extra numbers after the first two
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse("No fatal errors, just warning for extra data", result.hasErrors());
        assertTrue("Warning for extra data should be present", result.hasWarnings());
        String warning = result.warnings.get(0);
        assertTrue(warning.contains("Extra data ignored"));
        // The event should still be parsed correctly as 10-12
        assertEquals(1, result.events.size());
        Event ev = result.events.get(0);
        assertEquals(10, ev.getStartDay());
        assertEquals(12, ev.getEndDay());
    }

    @Test
    public void testOutOfRangeDaysCorrected() {
        String input =
            "2\n" +
            "0 367\n" +    // both start and end out of range
            "5 10\n";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse(result.hasErrors());
        // We expect a warning about adjustment
        assertTrue(result.hasWarnings());
        String warning = result.warnings.get(0);
        assertTrue("Warning should mention adjustment", warning.contains("adjusted"));
        // The out-of-range event should be clamped to 1 and 366
        Event ev = result.events.get(0);
        assertEquals(1, ev.getStartDay());
        assertEquals(366, ev.getEndDay());
        // Second event is normal
        assertEquals(5, result.events.get(1).getStartDay());
        assertEquals(10, result.events.get(1).getEndDay());
    }

    @Test
    public void testReversedDaysSwapped() {
        String input =
            "1\n" +
            "100 50\n";  // start > end
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());
        String warning = result.warnings.get(0);
        assertTrue("Warning should mention swapping", warning.toLowerCase().contains("swap"));
        // The event should be stored as 50-100 after swapping
        assertEquals(50, result.events.get(0).getStartDay());
        assertEquals(100, result.events.get(0).getEndDay());
    }

    @Test
    public void testOverlapMultiplePairs() {
        // A scenario with multiple overlaps:
        // Event1 overlaps Event2 and Event3, Event2 and Event3 do not overlap each other.
        String input =
            "3\n" +
            "1 10\n" +   // Event 1
            "5 6\n" +    // Event 2 (overlaps with Event 1, because 5-6 is inside 1-10)
            "10 20\n";   // Event 3 (overlaps with Event 1 at day 10, does not overlap Event 2)
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse(result.hasErrors());
        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        // Expect two overlaps: 1 with 2, and 1 with 3.
        // (Event 2 and 3 do not overlap because Event 2 ends at 6, before Event 3 starts at 10)
        assertEquals("Should find two overlapping pairs", 2, overlaps.size());
        // Collect the pairs for easier checking
        boolean found1and2 = false;
        boolean found1and3 = false;
        for (String msg : overlaps) {
            if (msg.contains("Event 1") && msg.contains("Event 2")) found1and2 = true;
            if (msg.contains("Event 1") && msg.contains("Event 3")) found1and3 = true;
        }
        assertTrue("Overlap between Event 1 and 2 reported", found1and2);
        assertTrue("Overlap between Event 1 and 3 reported", found1and3);
    }

    @Test
    public void testZeroEventsInput() {
        String input = "0\n";  // indicates zero events, and no event lines follow
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        // No events should be parsed
        assertEquals(0, result.events.size());
        // Overlap check on an empty list should just yield no overlaps
        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        assertTrue(overlaps.isEmpty());
    }
}
