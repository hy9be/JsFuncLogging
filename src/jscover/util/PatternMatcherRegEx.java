package jscover.util;

/**
 * Created by hyou on 8/12/15.
 */
import java.util.regex.Pattern;

public class PatternMatcherRegEx extends PatternMatcher {
    private Pattern regPattern;

    private PatternMatcherRegEx(boolean exclude, String pattern) {
        super(exclude);
        regPattern = Pattern.compile(pattern);
    }

    public static PatternMatcher getIncludePatternMatcher(String pattern) {
        return new PatternMatcherRegEx(false, pattern);
    }

    public static PatternMatcher getExcludePatternMatcher(String pattern) {
        return new PatternMatcherRegEx(true, pattern);
    }

    @Override
    public Boolean matches(String uri) {
        if (regPattern.matcher(uri).matches())
            return exclude;
        return null;
    }

    @Override
    public String toString() {
        return "PatternMatcherRegEx{regPattern=" + regPattern + ", exclude=" + exclude + '}';
    }
}
