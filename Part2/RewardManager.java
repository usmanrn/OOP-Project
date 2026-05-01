import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {

    private static RewardManager instance;

    // Option A: points and badge tracking
    private final Map<String, Integer>      cumulativePoints    = new HashMap<>();
    private final Map<String, List<String>> badges              = new HashMap<>();
    private final Map<String, Integer>      consecutiveWins     = new HashMap<>();
    private final Map<String, Integer>      racesWithoutBurnout = new HashMap<>();
    private final Map<String, Integer>      totalRaces          = new HashMap<>();

    // Option B: earnings and upgrades
    private final Map<String, Integer>      earnings            = new HashMap<>();
    private final Map<String, List<String>> upgrades            = new HashMap<>();

    private RewardManager() {}

    public static RewardManager getInstance() {
        if (instance == null) instance = new RewardManager();
        return instance;
    }

    // ── Option A ──────────────────────────────────────────────────

    // Points: position bonus + WPM bonus - burnout penalty
    public int calculatePoints(int position, double wpm, int burnouts) {
        int pts = 0;
        if      (position == 1) pts += 10;
        else if (position == 2) pts += 6;
        else if (position == 3) pts += 3;
        else                    pts += 1;

        pts += (int)(wpm / 10);       // +1 per 10 WPM
        pts -= burnouts * 2;          // -2 per burnout
        return Math.max(0, pts);
    }

    public void addPoints(String name, int points) {
        cumulativePoints.put(name, cumulativePoints.getOrDefault(name, 0) + points);
    }

    public int getPoints(String name) {
        return cumulativePoints.getOrDefault(name, 0);
    }

    public void updateBadges(String name, int position, int burnouts) {
        badges.putIfAbsent(name, new ArrayList<>());
        totalRaces.put(name, totalRaces.getOrDefault(name, 0) + 1);

        // Consecutive wins
        if (position == 1) {
            consecutiveWins.put(name, consecutiveWins.getOrDefault(name, 0) + 1);
        } else {
            consecutiveWins.put(name, 0);
        }

        // Races without burnout
        if (burnouts == 0) {
            racesWithoutBurnout.put(name, racesWithoutBurnout.getOrDefault(name, 0) + 1);
        } else {
            racesWithoutBurnout.put(name, 0);
        }

        // Award badges
        List<String> b = badges.get(name);
        if (consecutiveWins.getOrDefault(name, 0) >= 3 && !b.contains("Speed Demon")) {
            b.add("Speed Demon");
        }
        if (racesWithoutBurnout.getOrDefault(name, 0) >= 5 && !b.contains("Iron Fingers")) {
            b.add("Iron Fingers");
        }
        if (totalRaces.getOrDefault(name, 0) >= 10 && !b.contains("Veteran")) {
            b.add("Veteran");
        }
        if (position == 1 && !b.contains("First Win")) {
            b.add("First Win");
        }
    }

    public List<String> getBadges(String name) {
        return badges.getOrDefault(name, new ArrayList<>());
    }

    public Map<String, Integer> getAllPoints() {
        return cumulativePoints;
    }

    // ── Option B ──────────────────────────────────────────────────

    // Sponsors: name → condition description
    public static final String[] SPONSOR_NAMES = {
        "None",
        "KeyCorp: +50 coins if no burnouts",
        "SpeedEx: +30 coins if WPM > 50",
        "AccuType: +40 coins if accuracy > 90%"
    };

    // Upgrades available in the shop
    public static final String[] UPGRADE_NAMES  = {
        "Better Keyboard (+0.05 acc) — 100 coins",
        "Wrist Brace (burnout -1 turn) — 75 coins",
        "Focus Pills (mistype x0.9) — 50 coins"
    };
    public static final int[] UPGRADE_COSTS = { 100, 75, 50 };

    public int calculateEarnings(int position, double wpm, int burnouts, int sponsorIndex, double accuracyPct) {
        int coins = 0;
        if      (position == 1) coins += 200;
        else if (position == 2) coins += 100;
        else if (position == 3) coins += 50;
        else                    coins += 25;

        if (wpm > 40) coins += (int)((wpm - 40) * 2); // speed bonus
        coins -= burnouts * 20;                         // burnout penalty
        coins = Math.max(0, coins);

        // Sponsor bonus
        if (sponsorIndex == 1 && burnouts == 0)     coins += 50;
        if (sponsorIndex == 2 && wpm > 50)          coins += 30;
        if (sponsorIndex == 3 && accuracyPct > 90)  coins += 40;

        return coins;
    }

    public void addEarnings(String name, int coins) {
        earnings.put(name, earnings.getOrDefault(name, 0) + coins);
    }

    public int getEarnings(String name) {
        return earnings.getOrDefault(name, 0);
    }

    public Map<String, Integer> getAllEarnings() {
        return earnings;
    }

    public boolean buyUpgrade(String name, int upgradeIndex) {
        int cost = UPGRADE_COSTS[upgradeIndex];
        if (getEarnings(name) < cost) return false;
        earnings.put(name, getEarnings(name) - cost);
        upgrades.putIfAbsent(name, new ArrayList<>());
        upgrades.get(name).add(UPGRADE_NAMES[upgradeIndex]);
        return true;
    }

    public List<String> getUpgrades(String name) {
        return upgrades.getOrDefault(name, new ArrayList<>());
    }
}