public class TypistTest
{
    public static void main(String[] args)
    {
        Typist t = new Typist('①', "TEST", 0.8);

        // Test slideBack()
        t.slideBack(5);
        System.out.println("Progress after sliding back: " + t.getProgress());
    }
}
