package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;

import java.util.SortedSet;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.logging.Level.SEVERE;

//Function Coverage added by Howard Abrams, CA Technologies (HA-CA) - May 20 2013
public class ParseTreeInstrumenter implements NodeVisitor {
    private static Logger logger = Logger.getLogger(ParseTreeInstrumenter.class.getName());
    private String fileName;
    private NodeProcessor nodeProcessor;

    public ParseTreeInstrumenter(String uri, boolean includeFunctionCoverage) {
        this.fileName = uri;
        this.nodeProcessor = new NodeProcessor(uri, includeFunctionCoverage);
    }

    public SortedSet<Integer> getValidLines() {
        return nodeProcessor.getValidLines();
    }

    // Function Coverage (HA-CA)
    public int getNumFunctions() {
        return nodeProcessor.getNumFunctions();
    }

    public boolean visit(AstNode node) {
        try {
            return nodeProcessor.processNode(node);
        } catch (RuntimeException t) {
            logger.log(SEVERE, format("Error on line %s of %s", node.getLineno(), fileName), t);
            return true;
        }
    }

}
