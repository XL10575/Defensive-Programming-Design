import java.util.*;

public class InputValidator {

    public static class ParseResult {
        public List<Event> events = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    public static ParseResult parseInput(String inputText) {
        ParseResult result = new ParseResult();

        // 1) Create a Scanner (or record error if input is null/empty)
        Scanner scanner = createScanner(inputText, result);
        if (scanner == null) {
            return result; // Early exit if no valid scanner
        }

        // 2) Parse first line => get expectedCount (could add error if invalid)
        int expectedCount = parseFirstLine(scanner, result);

        // 3) Parse all remaining lines => accumulate events, errors, warnings
        int parsedCount = parseAllEventLines(scanner, result);

        // 4) Final check: compare parsedCount vs expectedCount if no prior errors
        finalizeCountCheck(expectedCount, parsedCount, result);

        scanner.close();
        return result;
    }

    /**
     * Creates a Scanner if possible. 
     * Returns null if no valid input is found.
     */
    private static Scanner createScanner(String inputText, ParseResult result) {
        if (inputText == null) {
            result.errors.add("No input provided");
            return null;
        }

        Scanner sc = new Scanner(inputText);
        if (!sc.hasNextLine()) {
            result.errors.add("No input provided");
            sc.close();
            return null;
        }
        return sc;
    }

    /**
     * Reads the first line from scanner, parses it as an integer (expectedCount),
     * and handles any negative or invalid integer scenario.
     */
    private static int parseFirstLine(Scanner scanner, ParseResult result) {
        String firstLine = scanner.nextLine().trim();
        int expectedCount;
        try {
            expectedCount = Integer.parseInt(firstLine);
        } catch (NumberFormatException e) {
            result.errors.add(
                "First line is not a valid integer for number of events: \"" + firstLine + "\""
            );
            return 0; // Default to 0 if invalid
        }
        if (expectedCount < 0) {
            result.errors.add("Number of events cannot be negative: " + expectedCount);
        }
        return expectedCount;
    }

    /**
     * Reads all remaining lines from scanner and parses each as an Event.
     * Returns the total number of successfully parsed events.
     */
    private static int parseAllEventLines(Scanner scanner, ParseResult result) {
        int lineNumber = 1;
        int parsedCount = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            lineNumber++;
            Event event = parseSingleEventLine(line, lineNumber, result);
            if (event != null) {
                result.events.add(event);
                parsedCount++;
            }
        }
        return parsedCount;
    }

    /**
     * Parses a single line into an Event. If any error occurs, returns null.
     */
    // Helper: Splits the line into tokens.
private static String[] tokenizeLine(String line) {
    return line.split("\\s+");
}

// Helper: Checks if the tokenized line has at least 2 tokens.
private static boolean isValidTokenCount(String[] tokens, int lineNumber, ParseResult result) {
    if (tokens.length < 2) {
        result.errors.add("Line " + lineNumber + ": Malformed event, expected 2 numbers but found " + tokens.length);
        return false;
    }
    return true;
}

// Helper: If there are extra tokens, add a warning.
private static void warnExtraTokens(String[] tokens, String line, int lineNumber, ParseResult result) {
    if (tokens.length > 2) {
        result.warnings.add("Line " + lineNumber + ": Extra data ignored after two numbers (\"" + line + "\")");
    }
}

/**
 * Parses a single event line into an Event object.
 * Returns null if there is any error.
 */
private static Event parseSingleEventLine(String line, int lineNumber, ParseResult result) {
    if (line.isEmpty()) {
        result.errors.add("Line " + lineNumber + ": Empty or whitespace line where an event was expected.");
        return null;
    }
    String[] tokens = tokenizeLine(line);
    if (!isValidTokenCount(tokens, lineNumber, result)) {
        return null;
    }
    warnExtraTokens(tokens, line, lineNumber, result);

    Integer startDay = tryParseInt(tokens[0], lineNumber, "start day", result);
    Integer endDay   = tryParseInt(tokens[1], lineNumber, "end day", result);
    if (startDay == null || endDay == null) {
        return null;
    }

    int[] adjusted = adjustDaysIfNeeded(startDay, endDay, lineNumber, result);
    int finalStart = adjusted[0];
    int finalEnd = adjusted[1];

    if (finalStart > finalEnd) {
        result.warnings.add("Line " + lineNumber + ": Start day greater than end day; swapping the values");
        int temp = finalStart;
        finalStart = finalEnd;
        finalEnd = temp;
    }
    return new Event(finalStart, finalEnd);
}


    /**
     * Safely parses an integer from a token; returns null and records an error if invalid.
     */
    private static Integer tryParseInt(String token, int lineNumber, String label, ParseResult result) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            result.errors.add(
                "Line " + lineNumber + ": \"" + token + "\" is not a valid integer for " + label
            );
            return null;
        }
    }

    /**
     * Checks if days are out of range and adjusts them, adding a warning if needed.
     * Returns a 2-element array [start, end] with possibly updated values.
     */
    private static int[] adjustDaysIfNeeded(int startDay, int endDay, int lineNumber, ParseResult result) {
        boolean outOfRange = (startDay < 1 || startDay > 366 || endDay < 1 || endDay > 366);
        if (outOfRange) {
            int origStart = startDay;
            int origEnd = endDay;
            startDay = clamp(startDay, 1, 366);
            endDay = clamp(endDay, 1, 366);
            result.warnings.add("Line " + lineNumber + ": Event days out of range, adjusted (" +
                    origStart + "->" + startDay + ", " + origEnd + "->" + endDay + ")");
        }
        return new int[]{startDay, endDay};
    }
    
    private static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }
    

    /**
     * If no prior errors, compare parsedCount to expectedCount. Record errors/warnings if mismatch.
     */
    private static void finalizeCountCheck(int expectedCount, int parsedCount, ParseResult result) {
        if (result.hasErrors()) {
            return; // skip if we already have errors
        }
        if (parsedCount < expectedCount) {
            result.errors.add(
                "Number of events mismatch: expected " + expectedCount +
                " but found " + parsedCount + " event line(s)"
            );
        } else if (parsedCount > expectedCount) {
            result.warnings.add("Extra events will be processed.");
        }
    }
}
