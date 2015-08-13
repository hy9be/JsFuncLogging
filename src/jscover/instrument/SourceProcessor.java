package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import jscover.ConfigurationCommon;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;

//Function Coverage added by Howard Abrams, CA Technologies (HA-CA) - May 20 2013
public class SourceProcessor {

    private static final String initLine = "  _$jscoverage['%s'].lineData[%d] = 0;\n";

    // Function Coverage (HA-CA)
    private static final String initFunction = "  _$jscoverage['%s'].functionData[%d] = 0;\n";

    private String uri;
    private FunctionInstrumentor functionInstrumentor;
    private Parser parser;
    private boolean includeBranchCoverage;
    private boolean isolateBrowser;

    public SourceProcessor(ConfigurationCommon config, String uri) {
        this.uri = uri;
        this.functionInstrumentor = new FunctionInstrumentor(uri);
        parser = new Parser(config.getCompilerEnvirons());
        this.includeBranchCoverage = config.isIncludeBranch();
        this.isolateBrowser = config.isolateBrowser();
    }

    FunctionInstrumentor getFunctionInstrumentor() {
        return functionInstrumentor;
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
            functionInstrumentor.setAstRoot(astRoot);
            astRoot.visitAll(functionInstrumentor);
            //functionInstrumentor.postProcess();
        }
        return astRoot.toSource();
    }
}
