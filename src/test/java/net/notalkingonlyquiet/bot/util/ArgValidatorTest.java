package net.notalkingonlyquiet.bot.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArgValidatorTest {

    @Test
    public void testNoArgs() {
        ArgValidator.Result r;

        ArgValidator a = new ArgValidator();
        r = a.parse();
        assertTrue(r.ok);
        assertTrue(r.results.isEmpty());
        assertTrue(r.errors.isEmpty());
    }

    @Test
    public void testNotEnoughArgs() {
        ArgValidator b = new ArgValidator().expectLiteral("A", "ERR");
        ArgValidator.Result r = b.parse();
        assertFalse(r.ok);
        assertTrue(r.results.size() == 0);
        assertTrue(r.errors.contains(ArgValidator.EXPECTED_MORE_ERROR));
    }

    @Test
    public void testTooManyArgs() {
        ArgValidator a = new ArgValidator();
        ArgValidator.Result r = a.parse("A");
        assertFalse(r.ok);
        assertTrue(r.results.isEmpty());
        assertTrue(r.errors.contains(ArgValidator.TOO_MANY_ERROR));
    }

    @Test
    public void testExpectLiteral() {
        ArgValidator a = new ArgValidator();

        a.expectLiteral("guy", "ERR");

        ArgValidator.Result r = a.parse("guy");

        assertEquals(r.ok, true);
        assertEquals(r.results.size(), 1);
        assertEquals(r.results.get(0), "guy");
        assertEquals(r.errors.size(), 0);

        r = a.parse("no");

        assertEquals(r.ok, false);
        assertEquals(r.results.size(), 0);
        assertTrue(r.errors.size() > 0);
        assertTrue(r.errors.contains("ERR"));
    }

    @Test
    public void testExpectChoice() {
        ArgValidator a = new ArgValidator().expectChoice("ERR", "A", "B");
        ArgValidator.Result r = a.parse("A");
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertTrue(r.results.contains("A"));

        r = a.parse("B");
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertTrue(r.results.contains("B"));

        r = a.parse("C");
        assertFalse(r.ok);
        assertTrue(r.results.isEmpty());
        assertTrue(r.errors.contains("ERR"));
    }

    @Test
    public void testExpectString() {
        ArgValidator a = new ArgValidator().expectString("ERR");

        ArgValidator.Result r = a.parse("B");
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertTrue(r.results.contains("B"));
    }

    @Test
    public void testExpectInt() {
        ArgValidator a = new ArgValidator().expectInt("ERR");

        ArgValidator.Result r = a.parse("1234");
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertTrue(r.results.contains(1234));

        r = a.parse("B");
        assertFalse(r.ok);
        assertTrue(r.errors.contains("ERR"));
        assertTrue(r.results.isEmpty());
    }

    @Test
    public void testExpectRemainder() {
        ArgValidator a = new ArgValidator().expectRemainder();

        ArgValidator.Result r = a.parse("1", "2", "3");
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertFalse(r.results.isEmpty());
        assertEquals(r.results.get(0), "1 2 3");

        r = a.parse();
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertFalse(r.results.isEmpty());
    }

    @Test
    public void testMulti1() {
        ArgValidator a = new ArgValidator();
        a.expectLiteral("A", "NO A");
        a.expectRemainder();

        ArgValidator.Result r = a.parse("A", "AND", "THEN");
        assertTrue(r.ok);
        assertTrue(r.errors.isEmpty());
        assertEquals(r.results.get(0), "A");
        assertEquals(r.results.get(1), "AND THEN");
    }

    @Test
    public void testNoToUpperCase() {
        ArgValidator a = new ArgValidator().expectLiteral("ABCD", "ERR");
        ArgValidator.Result r = a.parse("abcd");
        assertFalse(r.ok);
    }

    @Test
    public void testToUpperCase() {
        ArgValidator a = new ArgValidator().expectLiteral("ABCD", "ERR1")
                .expectString("ERR2").toUpperCase();
        ArgValidator.Result r = a.parse("abcd", "cdef");
        assertTrue(r.ok);
        assertEquals(r.results.get(0), "ABCD");
        assertEquals(r.results.get(1), "CDEF");
    }

    @Test
    public void testExpectRegexCapture() {
        ArgValidator a = new ArgValidator().expectRegexCapture("<(A+)><(A+)>", "ERR");

        ArgValidator.Result r = a.parse("A");
        assertFalse(r.ok);
        assertEquals(r.errors.get(0), "ERR");

        r = a.parse("<AAA><A>");
        assertTrue(r.ok);
        assertEquals("AAA A", r.results.get(0));

        r = a.parse("<A>");
        assertFalse(r.ok);
    }
}
