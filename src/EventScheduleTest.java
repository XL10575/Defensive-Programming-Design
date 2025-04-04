import org.junit.Test;
import static org.junit.Assert.*;
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
}
