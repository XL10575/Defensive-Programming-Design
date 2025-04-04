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

        if (inputText == null) {
            result.errors.add("No input provided");
            return result;
        }

        Scanner scanner = new Scanner(inputText);
        if (!scanner.hasNextLine()) {
            result.errors.add("No input provided");
            scanner.close();
            return result;
        }

        String firstLine = scanner.nextLine().trim();
        int expectedCount = 0;

        try {
            expectedCount = Integer.parseInt(firstLine);
        } catch (NumberFormatException e) {
            result.errors.add("First line is not a valid integer for number of events: \"" + firstLine + "\"");
            scanner.close();
            return result;
        }

        if (expectedCount < 0) {
            result.errors.add("Number of events cannot be negative: " + expectedCount);
        }

        int lineNumber = 1;
        int parsedCount = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            lineNumber++;

            if (line.isEmpty()) {
                result.errors.add("Line " + lineNumber + ": Empty or whitespace line where an event was expected.");
                continue;
            }

            String[] tokens = line.split("\\s+");

            if (tokens.length < 2) {
                result.errors.add("Line " + lineNumber + ": Malformed event, expected 2 numbers but found " + tokens.length);
                continue;
            }

            if (tokens.length > 2) {
                result.warnings.add("Line " + lineNumber + ": Extra data ignored after two numbers (\"" + line + "\")");
            }

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

            if (parseError) continue;

            if (startDay < 1 || startDay > 366 || endDay < 1 || endDay > 366) {
                int origStart = startDay;
                int origEnd = endDay;
                if (startDay < 1) startDay = 1;
                if (startDay > 366) startDay = 366;
                if (endDay < 1) endDay = 1;
                if (endDay > 366) endDay = 366;

                result.warnings.add("Line " + lineNumber + ": Event days out of range, adjusted (" +
                        origStart + "->" + startDay + ", " + origEnd + "->" + endDay + ")");
            }

            if (startDay > endDay) {
                result.warnings.add("Line " + lineNumber + ": Start day greater than end day; swapping the values");
                int temp = startDay;
                startDay = endDay;
                endDay = temp;
            }

            result.events.add(new Event(startDay, endDay));
            parsedCount++;
        }

        scanner.close();

        if (!result.hasErrors()) {
            if (parsedCount < expectedCount) {
                result.errors.add("Number of events mismatch: expected " + expectedCount + " but found " + parsedCount + " event line(s)");
            } else if (parsedCount > expectedCount) {
                result.warnings.add("Extra events will be processed.");
            }
        }

        return result;
    }
}
