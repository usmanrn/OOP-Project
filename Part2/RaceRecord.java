public class RaceRecord {
    public final String name;
    public final double wpm;
    public final double accuracyPercent;
    public final int    burnouts;
    public final double accuracyBefore;
    public final double accuracyAfter;
    public final int    position;

    public RaceRecord(String name, double wpm, double accuracyPercent,
                      int burnouts, double accuracyBefore, double accuracyAfter, int position)
    {
        this.name            = name;
        this.wpm             = wpm;
        this.accuracyPercent = accuracyPercent;
        this.burnouts        = burnouts;
        this.accuracyBefore  = accuracyBefore;
        this.accuracyAfter   = accuracyAfter;
        this.position        = position;
    }
}