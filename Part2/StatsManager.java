import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsManager {

    // One instance shared across the whole app
    private static StatsManager instance;

    // All race records per typist name
    private final Map<String, List<RaceRecord>> history = new HashMap<>();

    // Best WPM per typist name
    private final Map<String, Double> personalBests = new HashMap<>();

    private StatsManager() {}

    public static StatsManager getInstance() {
        if (instance == null) {
            instance = new StatsManager();
        }
        return instance;
    }

    public void addRecord(RaceRecord record) {
        // Add to history
        history.putIfAbsent(record.name, new ArrayList<>());
        history.get(record.name).add(record);

        // Update personal best WPM
        double current = personalBests.getOrDefault(record.name, 0.0);
        if (record.wpm > current) {
            personalBests.put(record.name, record.wpm);
        }
    }

    public List<RaceRecord> getHistory(String name) {
        return history.getOrDefault(name, new ArrayList<>());
    }

    public double getPersonalBest(String name) {
        return personalBests.getOrDefault(name, 0.0);
    }

    public List<String> getAllNames() {
        return new ArrayList<>(history.keySet());
    }
}