package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;

public abstract class PostProcess {
    private AstNode parent;
    private AstNode node;
    private FunctionCall functionCall;

    public PostProcess(AstNode parent, AstNode node, FunctionCall functionCall) {
        this.parent = parent;
        this.node = node;
        this.functionCall = functionCall;
    }

    public void process() {
        run(parent, node, functionCall);
    }

    abstract void run(AstNode parent, AstNode node, AstNode functionCall);
}
