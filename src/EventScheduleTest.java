import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class EventScheduleTest {

    @Test
    public void testValidInputNoOverlap() {
        String input = "2\n1 5\n6 10";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertTrue(result.errors.isEmpty());
        assertTrue(result.warnings.isEmpty());
        assertEquals(2, result.events.size());

        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    public void testValidInputWithOverlap() {
        String input = "3\n1 10\n5 15\n20 25";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertTrue(result.errors.isEmpty());
        assertTrue(result.warnings.isEmpty());
        assertEquals(3, result.events.size());

        List<String> overlaps = OverlapChecker.findOverlaps(result.events);
        assertEquals(1, overlaps.size());
        assertEquals("Event 1 (1, 10) overlaps with Event 2 (5, 15)", overlaps.get(0));
    }

    @Test
    public void testRecoverableErrorReverseDates() {
        String input = "1\n10 5";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertTrue(result.errors.isEmpty());
        assertEquals(1, result.warnings.size());
        assertEquals("Warning: Reversed dates corrected (5 10)", result.warnings.get(0));
        assertEquals(1, result.events.size());

        Event e = result.events.get(0);
        assertEquals(5, e.getStartDay());
        assertEquals(10, e.getEndDay());
    }

    @Test
    public void testFatalErrorNonInteger() {
        String input = "1\nfive ten";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertEquals(1, result.errors.size());
        assertTrue(result.events.isEmpty());
        assertEquals("Error: Non-integer input detected (five ten)", result.errors.get(0));
    }

    @Test
    public void testOutOfRangeValues() {
        String input = "1\n-5 400";
        InputValidator.ParseResult result = InputValidator.parseInput(input);
        assertTrue(result.errors.isEmpty());
        assertEquals(2, result.warnings.size());
        assertTrue(result.warnings.contains("Warning: Start day adjusted from -5 to 1"));
        assertTrue(result.warnings.contains("Warning: End day adjusted from 400 to 365"));

        Event e = result.events.get(0);
        assertEquals(1, e.getStartDay());
        assertEquals(365, e.getEndDay());
    }

    @Test
    public void testEventToString() {
        Event event = new Event(3, 7);
        assertEquals("(3, 7)", event.toString());
    }

    @Test
    public void testOverlapCheckerNoEvents() {
        List<String> overlaps = OverlapChecker.findOverlaps(null);
        assertTrue(overlaps.isEmpty());

        overlaps = OverlapChecker.findOverlaps(List.of());
        assertTrue(overlaps.isEmpty());
    }
    @Test
public void testExtraEventsBeyondExpected() {
    String input = "1\n10 20\n25 30";
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.warnings.contains("Extra events will be processed."));
}
@Test
public void testZeroEventsAllowed() {
    String input = "0";
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.errors.isEmpty());
    assertEquals(0, result.events.size());
}
@Test
public void testTooFewEvents() {
    String input = "2\n10 20";
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertEquals(1, result.errors.size());
    assertTrue(result.errors.get(0).contains("Number of events mismatch"));
}
@Test
public void testEmptyLineBetweenEvents() {
    String input = "2\n10 20\n   \n25 30";
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.errors.stream().anyMatch(e -> e.contains("Empty or whitespace")));
}
@Test
public void testNullInput() {
    InputValidator.ParseResult result = InputValidator.parseInput(null);
    assertTrue(result.hasErrors());
    assertEquals("No input provided", result.errors.get(0));
}
@Test
public void testEmptyInput() {
    InputValidator.ParseResult result = InputValidator.parseInput("");
    assertTrue(result.hasErrors());
    assertEquals("No input provided", result.errors.get(0));
}
@Test
public void testNonIntegerFirstLine() {
    InputValidator.ParseResult result = InputValidator.parseInput("abc\n1 2");
    assertTrue(result.hasErrors());
    assertTrue(result.errors.get(0).contains("not a valid integer for number of events"));
}
@Test
public void testNegativeExpectedCount() {
    InputValidator.ParseResult result = InputValidator.parseInput("-1\n10 20");
    assertTrue(result.hasErrors());
    assertTrue(result.errors.get(0).contains("Number of events cannot be negative"));
}
@Test
public void testMalformedEventOneToken() {
    String input = "1\n10";
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.errors.stream().anyMatch(e -> e.contains("expected 2 numbers")));
}
@Test
public void testExtraTokensInEventLine() {
    String input = "1\n10 20 30";
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.warnings.stream().anyMatch(w -> w.contains("Extra data ignored")));
}
@Test
public void testHasWarningsMethod() {
    String input = "1\n400 1"; // out of range and reversed
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    
    assertTrue(result.hasWarnings()); // This triggers the red line!
    assertFalse(result.hasErrors());
}
@Test
public void testOnlyStartDayOutOfRange() {
    String input = "1\n0 100";  // startDay out of range
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.hasWarnings());
}

@Test
public void testOnlyEndDayOutOfRange() {
    String input = "1\n10 400";  // endDay out of range
    InputValidator.ParseResult result = InputValidator.parseInput(input);
    assertTrue(result.hasWarnings());
}
private static boolean eventsOverlap(Event a, Event b) {
    return a.getStartDay() <= b.getEndDay() && b.getStartDay() <= a.getEndDay();
}
@Test
public void testEventsDoOverlap() {
    List<Event> events = new ArrayList<>();
    events.add(new Event(10, 20));
    events.add(new Event(15, 25)); // Overlaps with above

    List<String> overlaps = OverlapChecker.findOverlaps(events);
    assertEquals(1, overlaps.size());
}

@Test
public void testEventsDoNotOverlap() {
    List<Event> events = new ArrayList<>();
    events.add(new Event(10, 15));
    events.add(new Event(16, 20)); // No overlap

    List<String> overlaps = OverlapChecker.findOverlaps(events);
    assertTrue(overlaps.isEmpty());
}

}
