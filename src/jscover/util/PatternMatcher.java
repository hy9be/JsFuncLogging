package jscover.util;

/**
 * Created by hyou on 8/12/15.
 */
public abstract class PatternMatcher {
    protected boolean exclude;

    public PatternMatcher(boolean exclude) {
        this.exclude = exclude;
    }

    public abstract Boolean matches(String uri);
}
