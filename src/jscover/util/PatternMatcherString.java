package jscover.util;

/**
 * Created by hyou on 8/12/15.
 */
public class PatternMatcherString extends PatternMatcher {
    private String pattern;

    public PatternMatcherString(String pattern) {
        super(true);
        this.pattern = pattern;
    }

    public Boolean matches(String uri) {
        if (uri.startsWith(pattern))
            return exclude;
        return null;
    }

    @Override
    public String toString() {
        return "PatternMatcherString{pattern='" + pattern + '\'' + '}';
    }
}
