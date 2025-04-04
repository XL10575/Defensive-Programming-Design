import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputValidator {
    /**
     * Result container for parsing input.
     * Contains either a list of events and optional warnings, or errors if parsing failed.
     */
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

    /**
     * Parses the raw input string representing events.
     * @param inputText Multi-line input: first line is number of events N, next N lines are events.
     * @return ParseResult containing events list if successful, or errors if input is invalid.
     */
    public static ParseResult parseInput(String inputText) {
        ParseResult result = new ParseResult();
        if (inputText == null) {
            result.errors.add("No input provided");
            return result;
        }
        // Use a Scanner to read lines from the input text
        Scanner scanner = new Scanner(inputText);
        if (!scanner.hasNextLine()) {
            result.errors.add("No input provided");
            scanner.close();
            return result;
        }

        String firstLine = scanner.nextLine().trim();
        int expectedCount;
        try {
            expectedCount = Integer.parseInt(firstLine);
        } catch (NumberFormatException e) {
            result.errors.add("First line is not a valid integer for number of events: \"" + firstLine + "\"");
            scanner.close();
            return result;
        }
        if (expectedCount < 0) {
            result.errors.add("Number of events cannot be negative: " + expectedCount);
            // We will still try to parse events lines, but this is considered an error
        }

        int lineNumber = 1;  // for error messages (1-based indexing of lines, 1 = first line)
        int parsedCount = 0;
        // Read event lines
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            lineNumber++;
            if (line.isEmpty()) {
                // skip or treat as error? We'll treat empty line as a malformed event
                result.errors.add("Line " + lineNumber + ": Empty or whitespace line where an event was expected.");
                continue;
            }
            // Split by whitespace
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                result.errors.add("Line " + lineNumber + ": Malformed event, expected 2 numbers but found " + tokens.length);
                continue;
            }
            if (tokens.length > 2) {
                result.warnings.add("Line " + lineNumber + ": Extra data ignored after two numbers (\"" + line + "\")");
                // We will ignore any tokens after the first two
            }
            // Parse first two tokens as integers
            int startDay = 0, endDay = 0;
            boolean parseError = false;
            try {
                startDay = Integer.parseInt(tokens[0]);
            } catch (NumberFormatException e) {
                result.errors.add("Line " + lineNumber + ": \"" + tokens[0] + "\" is not a valid integer for start day");
                parseError = true;
            }
            try {
                endDay = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                result.errors.add("Line " + lineNumber + ": \"" + tokens[1] + "\" is not a valid integer for end day");
                parseError = true;
            }
            if (parseError) {
                continue;  // skip further validation for this line
            }
            // Now we have two parsed integers for days
            // Check range 1-366
            if (startDay < 1 || startDay > 366 || endDay < 1 || endDay > 366) {
                // If out of range, we'll clamp the values into range and warn
                int origStart = startDay;
                int origEnd = endDay;
                if (startDay < 1) startDay = 1;
                if (startDay > 366) startDay = 366;
                if (endDay < 1) endDay = 1;
                if (endDay > 366) endDay = 366;
                result.warnings.add("Line " + lineNumber + ": Event days out of range, adjusted (" 
                                     + origStart + "->" + startDay + ", " + origEnd + "->" + endDay + ")");
            }
            // Check and fix reversed range
            if (startDay > endDay) {
                // Swap them
                result.warnings.add("Line " + lineNumber + ": Start day greater than end day; swapping the values");
                int temp = startDay;
                startDay = endDay;
                endDay = temp;
            }
            // Now data is clean for this event, create Event object
            Event event = new Event(startDay, endDay);
            result.events.add(event);
            parsedCount++;
        }
        scanner.close();

        // Check if the number of events parsed matches the expected count (from first line).
        if (!result.hasErrors()) {  // only do consistency check if we didn't already encounter fatal errors
            if (parsedCount != expectedCount) {
                String msg = "Number of events mismatch: expected " + expectedCount + " but found " + parsedCount + " event line(s)";
                if (parsedCount < expectedCount) {
                    // Missing events: this is an error (can't recover missing data)
                    result.errors.add(msg);
                } else { 
                    // More lines than expected: we'll treat it as a warning and proceed with all parsed events
                    result.warnings.add(msg + ". Extra events will be processed.");
                }
            }
        }

        return result;
    }
}
