public class TypingTest
{
    public static void main(String[] args)
    {
        Typist t = new Typist('①', "TURBOFINGERS", 0.85);
        System.out.println("Starting progress: " + t.getProgress()); // expected: 0
        t.typeCharacter();
        System.out.println("After 1 character: " + t.getProgress()); // expected: 1
        t.typeCharacter();
        System.out.println("After 2 characters: " + t.getProgress()); // expected: 2
        t.typeCharacter();
        System.out.println("After 3 characters: " + t.getProgress()); // expected: 3
    }
}

