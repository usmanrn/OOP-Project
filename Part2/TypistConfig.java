import java.awt.Color;

public class TypistConfig {

    public final char    symbol;
    public final String  name;
    public final double  baseAccuracy;
    public final Color   colour;
    public final double  burnoutMultiplier;
    public final double  speedMultiplier;
    public final double  mistypeMultiplier;
    public final boolean hasWristSupport;
    public final boolean hasEnergyDrink;
    public final boolean hasHeadphones;
    public final int     sponsorIndex;   // 0 = none

    public TypistConfig(char symbol, String name, double baseAccuracy, Color colour,
                        double burnoutMultiplier, double speedMultiplier, double mistypeMultiplier,
                        boolean hasWristSupport, boolean hasEnergyDrink, boolean hasHeadphones,
                        int sponsorIndex)
    {
        this.symbol            = symbol;
        this.name              = name;
        this.baseAccuracy      = baseAccuracy;
        this.colour            = colour;
        this.burnoutMultiplier = burnoutMultiplier;
        this.speedMultiplier   = speedMultiplier;
        this.mistypeMultiplier = mistypeMultiplier;
        this.hasWristSupport   = hasWristSupport;
        this.hasEnergyDrink    = hasEnergyDrink;
        this.hasHeadphones     = hasHeadphones;
        this.sponsorIndex      = sponsorIndex;
    }

    public double effectiveMistypeMultiplier() {
        return mistypeMultiplier * (hasHeadphones ? 0.75 : 1.0);
    }

    public int effectiveBurnoutDuration(int baseDuration) {
        return hasWristSupport ? Math.max(1, baseDuration - 1) : baseDuration;
    }
}