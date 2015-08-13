package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import jscover.ConfigurationCommon;
import jscover.util.IoUtils;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;

import java.util.SortedSet;

import static java.lang.String.format;

//Function Coverage added by Howard Abrams, CA Technologies (HA-CA) - May 20 2013
public class SourceProcessor {

    private static final String initLine = "  _$jscoverage['%s'].lineData[%d] = 0;\n";

    // Function Coverage (HA-CA)
    private static final String initFunction = "  _$jscoverage['%s'].functionData[%d] = 0;\n";

    private String uri;
    private ParseTreeInstrumenter instrumenter;
    private BranchInstrumentor branchInstrumentor;
    private Parser parser;
    private IoUtils ioUtils = IoUtils.getInstance();
    private boolean includeBranchCoverage;
    private boolean includeFunctionCoverage;
    private boolean localStorage;
    private boolean isolateBrowser;

    public SourceProcessor(ConfigurationCommon config, String uri) {
        this.uri = uri;
        this.instrumenter = new ParseTreeInstrumenter(uri, config.isIncludeFunction());
        this.branchInstrumentor = new BranchInstrumentor(uri, config.isDetectCoalesce());
        parser = new Parser(config.getCompilerEnvirons());
        this.includeBranchCoverage = config.isIncludeBranch();
        this.includeFunctionCoverage = config.isIncludeFunction();
        this.localStorage = config.isLocalStorage();
        this.isolateBrowser = config.isolateBrowser();
    }

    ParseTreeInstrumenter getInstrumenter() {
        return instrumenter;
    }

    BranchInstrumentor getBranchInstrumentor() {
        return branchInstrumentor;
    }

    private String getIsolateBrowserJS() {
        return "var jsCover_isolateBrowser = " + (isolateBrowser ? "true" : "false") + ";\n";
    }

    public String instrumentSource(String source) {
        return instrumentSource(uri, source);
    }

    protected String instrumentSource(String sourceURI, String source) {
        AstRoot astRoot = parser.parse(source , sourceURI, 1);

        //Do not add line number
        //astRoot.visitAll(instrumenter);
        if (includeBranchCoverage) {
            branchInstrumentor.setAstRoot(astRoot);
            astRoot.visitAll(branchInstrumentor);
            //branchInstrumentor.postProcess();
        }
        return astRoot.toSource();
    }

    protected String getJsLineInitialization(String fileName, SortedSet<Integer> validLines) {
        fileName = fileName.replace("\\", "\\\\").replace("'", "\\'");
        StringBuilder sb = new StringBuilder(format("if (! _$jscoverage['%s']) {\n", fileName));
        sb.append(format("  _$jscoverage['%s'] = {};\n", fileName));
        sb.append(format("  _$jscoverage['%s'].lineData = [];\n", fileName));
        for (Integer line : validLines) {
            sb.append(format(initLine, fileName, line));
        }
        sb.append("}\n");
        return sb.toString();
    }

    // Function Coverage (HA-CA)
    protected String getJsFunctionInitialization(String fileName, int numFunction) {
        fileName = fileName.replace("\\", "\\\\").replace("'", "\\'");
        StringBuilder sb = new StringBuilder(format("if (! _$jscoverage['%s'].functionData) {\n", fileName));
        sb.append(format("  _$jscoverage['%s'].functionData = [];\n", fileName));
        for ( int i = 0; i < numFunction; ++i) {
            sb.append(format(initFunction, fileName, i));
        }
        sb.append("}\n");
        return sb.toString();
    }
}
