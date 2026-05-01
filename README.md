# TypingRaceSimulator

Object Oriented Programming Project — ECS414U

## Project Structure

```
TypingRaceSimulator/
├── Part1/    # Textual simulation (Java, command-line) - all done
└── Part2/    # GUI simulation - all achieved, implemented both Leaderboard & Ranking System and Sponsor & Prize System
```

## Part 1 — Textual Simulation

### How to compile

```bash
cd Part1
javac Typist.java TypingRace.java
```

### How to run

The race is started by calling `startRace()` on a `TypingRace` object.
A simple way to test this is to add a `main` method to `TypingRace`, for example:

```java
public static void main(String[] args) {
    TypingRace race = new TypingRace(40);
    race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
    race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
    race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
    race.startRace();
}
```

Then run:

```bash
java TypingRace
```

## Part 2 — GUI Simulation

Placed all GUI-related source files in this folder. The graphical version is started by calling `startRaceGUI()`.

### Features implemented
- Passage selection (short/medium/long/custom)
- 2–6 configurable typists
- Difficulty modifiers: Autocorrect, Caffeine Mode, Night Shift
- Typist customisation: typing style, keyboard type, symbol, colour, accessories
- Sponsor selection per typist
- Live race with highlighted passage progress per typist
- Statistics screen: WPM, accuracy %, burnout count, accuracy changes
- Personal bests and full race history
- Side-by-side typist comparison
- Leaderboard & Ranking System (Option A)
- Sponsor & Prize System with upgrade shop (Option B)

## Dependencies

- Java Development Kit (JDK) 11 or higher
- No external libraries required for Part 1
- Part 2 uses Java Swing (included in standard JDK)

## Notes

- All code should compile and run using standard command-line tools without any IDE-specific configuration.
- The starter code in Part1 was originally written by Ty Posaurus. It contains known issues — finding and fixing them is part of the coursework.


### How to compile

```bash
cd Part2
javac *.java
```

### How to run

```bash
java TypingRaceGUI
```