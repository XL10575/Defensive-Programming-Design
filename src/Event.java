public class Event {
    private final int startDay, endDay;

    public Event(int startDay, int endDay) {
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public int getStartDay() { return startDay; }
    public int getEndDay() { return endDay; }

    @Override
    public String toString() {
        return "(" + startDay + ", " + endDay + ")";
    }
}
