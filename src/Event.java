public class Event {
    private final int startDay;
    private final int endDay;

    public Event(int startDay, int endDay) {
        // Assuming validation already done by barricade:
        this.startDay = startDay;
        this.endDay = endDay;
        // We could add an assertion to double-check the invariant:
        assert startDay <= endDay : "Event start day is after end day (should have been handled by parser)";
    }

    public int getStartDay() {
        return startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    @Override
    public String toString() {
        return "(" + startDay + ", " + endDay + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event other = (Event) o;
        return this.startDay == other.startDay && this.endDay == other.endDay;
    }

    @Override
    public int hashCode() {
        return startDay * 367 + endDay;  // simple hash (prime 367 > 366)
    }
}
