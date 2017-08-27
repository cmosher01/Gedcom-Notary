package nu.mine.mosher.gedcom;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestExtractor {
    private static final String PAYLOAD = "REFN b7cf4966-c91f-464c-a346-0b97b3aee92a";
    private static final String MASK = "__GEDCOM__";

    private static String[] extract(final String from, final String mask) {
        return new NotaryExtractor(mask).extract(from);
    }

    @Test
    void nominal() {
        String n = "This is a " + MASK + ": " + PAYLOAD + " " + MASK + " test.";
        assertArrayEquals(new String[]{PAYLOAD, "This is a  test."}, extract(n, MASK));
    }

    @Test
    void atBeginning() {
        String n = MASK + ": " + PAYLOAD + " " + MASK + " test.";
        assertArrayEquals(new String[]{PAYLOAD, " test."}, extract(n, MASK));
    }

    @Test
    void atEnd() {
        String n = "This is a " + MASK + ": " + PAYLOAD + " " + MASK;
        assertArrayEquals(new String[]{PAYLOAD, "This is a "}, extract(n, MASK));
    }

    @Test
    void solitary() {
        String n = MASK + ": " + PAYLOAD + " " + MASK;
        assertArrayEquals(new String[]{PAYLOAD, ""}, extract(n, MASK));
    }

    @Test
    void none() {
        String n = "This string __GEDCOM__ has no match";
        assertArrayEquals(new String[]{"", n}, extract(n, MASK));
    }

    @Test
    void maskWithOddCharacters() {
        final String mask = "_*G.E[D^C{1}O$M_*\\_";
        String n = "This is a " + mask + ": " + PAYLOAD + " " + mask + " test.";
        assertArrayEquals(new String[]{PAYLOAD, "This is a  test."}, extract(n, mask));
    }

    @Test
    void firstOfTwo() {
        String n = "This is a " + MASK + ": " + PAYLOAD + " " + MASK + " test. " + MASK + ": NOTE xxx " + MASK + "And more.";
        assertArrayEquals(new String[]{PAYLOAD, "This is a  test. " + MASK + ": NOTE xxx " + MASK + "And more."}, extract(n, MASK));
    }

    @Test
    void withNewlines() {
        String n = "This is a \n" + MASK + ": " + PAYLOAD + " " + MASK + " te\nst.\n";
        assertArrayEquals(new String[]{PAYLOAD, "This is a \n te\nst.\n"}, extract(n, MASK));
    }

    @Test
    void withNewlinesInPayload() {
        final String payload = "REFN b7cf4966\nc91f\n464c\na346-0b97b3aee92a";
        String n = "This is a \n" + MASK + ": " + payload + " " + MASK + " te\nst.\n";
        assertArrayEquals(new String[]{payload, "This is a \n te\nst.\n"}, extract(n, MASK));
    }
}
