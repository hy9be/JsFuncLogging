package jscover;

/**
 * Created by hyou on 8/12/15.
 */
import jscover.util.IoUtils;
import jscover.util.PatternMatcher;
import jscover.util.PatternMatcherRegEx;
import jscover.util.PatternMatcherString;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import static java.lang.String.format;
import static java.util.logging.Level.SEVERE;
//import static jscover.Main.HELP_PREFIX1;
//import static jscover.Main.HELP_PREFIX2;

public class ConfigurationCommon extends Configuration {
    private static final Logger logger = Logger.getLogger(ConfigurationCommon.class.getName());
    public static final String ONLY_INSTRUMENT_REG_PREFIX = "--only-instrument-reg=";
    public static final String NO_INSTRUMENT_PREFIX = "--no-instrument=";
    public static final String NO_INSTRUMENT_REG_PREFIX = "--no-instrument-reg=";
    public static final String JS_VERSION_PREFIX = "--js-version=";
    public static final String NO_BRANCH_PREFIX = "--no-branch";
    public static final String DETECT_COALESCE_PREFIX = "--detect-coalesce";
    public static final String NO_FUNCTION_PREFIX = "--no-function";
    public static final String LOCAL_STORAGE_PREFIX = "--local-storage";
    public static final String LOG_LEVEL = "--log=";

    protected boolean showHelp;
    protected boolean invalid;
    protected boolean includeBranch = true;
    protected boolean detectCoalesce;
    protected boolean includeFunction = true;
    protected boolean localStorage;
    protected boolean isolateBrowser;
    protected final List<PatternMatcher> patternMatchers = new ArrayList<PatternMatcher>();
    protected int JSVersion = Context.VERSION_1_5;
    protected CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
    protected boolean defaultSkip;
    protected IoUtils ioUtils = IoUtils.getInstance();
    protected Level logLevel = SEVERE;

    public void setIncludeBranch(boolean includeBranch) {
        this.includeBranch = includeBranch;
    }

    public void setDetectCoalesce(boolean detectCoalesce) {
        this.detectCoalesce = detectCoalesce;
    }

    public void setIncludeFunction(boolean includeFunction) {
        this.includeFunction = includeFunction;
    }

    public void setLocalStorage(boolean localStorage) {
        this.localStorage = localStorage;
    }

    public void setIsolateBrowser(boolean isolateBrowser) {
        this.isolateBrowser = isolateBrowser;
    }

    public void setJSVersion(int JSVersion) {
        this.JSVersion = JSVersion;
    }

    public Boolean showHelp() {
        return showHelp;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isIncludeBranch() {
        return includeBranch;
    }

    public boolean isDetectCoalesce() {
        return detectCoalesce;
    }

    public boolean isIncludeFunction() {
        return includeFunction;
    }

    public boolean isLocalStorage() {
        return localStorage;
    }

    public boolean isolateBrowser() {
        return isolateBrowser;
    }

    public int getJSVersion() {
        return JSVersion;
    }

    public CompilerEnvirons getCompilerEnvirons() {
        return compilerEnvirons;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public boolean skipInstrumentation(String uri) {
        for (PatternMatcher patternMatcher : patternMatchers) {
            Boolean instrumentIt = patternMatcher.matches(uri);
            if (instrumentIt != null) {
                logger.log(Level.FINEST, "Matched URI ''{0}'' Pattern ''{1}'' Skip {2}", new Object[]{uri, patternMatcher, instrumentIt});
                return instrumentIt;
            }
        }
        return defaultSkip;
    }

    protected void setInvalid(String message) {
        System.err.println(message);
        showHelp = true;
        invalid = true;
    }

    public void addNoInstrument(String arg) {
        String uri = arg.substring(NO_INSTRUMENT_PREFIX.length());
        if (uri.startsWith("/"))
            uri = uri.substring(1);
        patternMatchers.add(new PatternMatcherString(uri));
    }

    public void addOnlyInstrumentReg(String arg) {
        String patternString = arg.substring(ONLY_INSTRUMENT_REG_PREFIX.length());
        if (patternString.startsWith("/"))
            patternString = patternString.substring(1);
        defaultSkip = true;
        try {
            patternMatchers.add(PatternMatcherRegEx.getIncludePatternMatcher(patternString));
        } catch (PatternSyntaxException e) {
            setInvalid(format("Invalid pattern '%s'", patternString));
            e.printStackTrace(System.err);
        }
    }

    public void addNoInstrumentReg(String arg) {
        String patternString = arg.substring(NO_INSTRUMENT_REG_PREFIX.length());
        if (patternString.startsWith("/"))
            patternString = patternString.substring(1);
        try {
            patternMatchers.add(PatternMatcherRegEx.getExcludePatternMatcher(patternString));
        } catch(PatternSyntaxException e) {
            e.printStackTrace(System.err);
            setInvalid(format("Invalid pattern '%s'", patternString));
        }
    }

    protected boolean parseArg(String arg) {
        //if (arg.equals(HELP_PREFIX1) || arg.equals(HELP_PREFIX2)) {
        if (false) {
            showHelp = true;
        } else if (arg.equals(NO_BRANCH_PREFIX)) {
            includeBranch = false;
        } else if (arg.equals(NO_FUNCTION_PREFIX)) {
            includeFunction = false;
        } else if (arg.equals(DETECT_COALESCE_PREFIX)) {
            detectCoalesce = true;
        } else if (arg.equals(LOCAL_STORAGE_PREFIX)) {
            localStorage = true;
        } else if (arg.startsWith(NO_INSTRUMENT_PREFIX)) {
            addNoInstrument(arg);
        } else if (arg.startsWith(NO_INSTRUMENT_REG_PREFIX)) {
            addNoInstrumentReg(arg);
        } else if (arg.startsWith(ONLY_INSTRUMENT_REG_PREFIX)) {
            addOnlyInstrumentReg(arg);
        } else if (arg.startsWith(JS_VERSION_PREFIX)) {
            JSVersion = (int) (Float.valueOf(arg.substring(JS_VERSION_PREFIX.length())) * 100);
        } else if (arg.startsWith(LOG_LEVEL)) {
            logLevel = Level.parse(arg.substring(LOG_LEVEL.length()));
        } else {
            return false;
        }
        return true;
    }
}
