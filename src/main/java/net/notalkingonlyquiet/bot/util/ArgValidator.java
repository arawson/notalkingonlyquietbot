package net.notalkingonlyquiet.bot.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides simple validation for parameters in the order they are expected.
 */
public class ArgValidator {
    public static final String TOO_MANY_ERROR = "TOO MANY";
    public static final String EXPECTED_MORE_ERROR = "EXPECTED MORE";

    private final List<Triple<Predicate<String>, Function<String, ?>, String>> parameters = new ArrayList<>();
    private int remainderIndex = -1;
    private boolean setUppercase = false;

    public ArgValidator() {}

    public ArgValidator toUpperCase() {
        setUppercase = true;
        return this;
    }

    /**
     * Expect a literal string value in the next position.
     * @param literal
     * @return this! so you can chain to your heart's content!
     */
    public ArgValidator expectLiteral(String literal, String error) {
        Preconditions.checkState(remainderIndex == -1);
        Preconditions.checkNotNull(literal);
        Preconditions.checkNotNull(error);
        parameters.add(new ImmutableTriple<>(
                (s -> s.equals(literal)),
                (s -> s),
                error
        ));
        return this;
    }

    public ArgValidator expectChoice(String error, String ... values) {
        Preconditions.checkState(remainderIndex == -1);
        Preconditions.checkState(values.length > 0);
        Preconditions.checkNotNull(error);
        parameters.add(new ImmutableTriple<>(
                (s -> Arrays.asList(values).contains(s)),
                (s -> s),
                error
        ));
        return this;
    }

    public ArgValidator expectString(String error) {
        Preconditions.checkState(remainderIndex == -1);
        Preconditions.checkNotNull(error);
        parameters.add(new ImmutableTriple<>(
                (s -> true),
                (s -> s),
                error
        ));
        return this;
    }

    public ArgValidator expectInt(String error) {
        Preconditions.checkState(remainderIndex == -1);
        Preconditions.checkNotNull(error);
        parameters.add(new ImmutableTriple<>(
                (s -> {
                    try {
                        Integer.parseInt(s);
                        return true;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                }),
                (s -> Integer.parseInt(s)),
                error
        ));
        return this;
    }

    public ArgValidator expectRemainder() {
        Preconditions.checkState(remainderIndex == -1);
        remainderIndex = parameters.size();
        return this;
    }

    public Result parse(String ... args) {
        return parse(Arrays.asList(args));
    }

    public Result parse(List<String> args) {
        Result r = new Result();

        StringBuilder remainder = new StringBuilder();
        int i = 0;
        for(; i < args.size(); i++) {
            String a = setUppercase ? args.get(i).toUpperCase() : args.get(i);

            if (i >= parameters.size()) {
                if (this.remainderIndex == -1) {
                    r.errors.add(TOO_MANY_ERROR);
                    break;
                }
                if (i >= this.remainderIndex) {
                    if (remainder.length() != 0) {
                        remainder.append(" ");
                    }
                    remainder.append(a);
                }
            } else {
                if (parameters.get(i).getLeft().test(a)) {
                    r.results.add(parameters.get(i).getMiddle().apply(a));
                } else {
                    r.errors.add(parameters.get(i).getRight());
                }
            }
        }

        if (this.remainderIndex != -1) {
            r.results.add(remainder.toString());
        }

        if (i < parameters.size()) {
            r.errors.add(EXPECTED_MORE_ERROR);
        }

        r.ok = r.errors.size() == 0;

        return r;
    }

    public static class Result {
        public boolean ok = false;
        public List<Object> results = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
    }
}
