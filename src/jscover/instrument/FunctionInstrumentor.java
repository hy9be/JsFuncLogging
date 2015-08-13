package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */

import com.clematis.PointOfInterest;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.*;

import java.util.*;

public class FunctionInstrumentor implements NodeVisitor {

    private Set<PostProcess> postProcesses = new HashSet<PostProcess>();
    private String uri;
    private AstRoot astRoot;

    public FunctionInstrumentor(String uri) {
        this.uri = uri;
    }

    public void setAstRoot(AstRoot astRoot) {
        this.astRoot = astRoot;
    }

    public void postProcess() {
        for (PostProcess postProcess : postProcesses)
            postProcess.process();
    }

    /**
     * From https://github.com/saltlab/clematis
     * @param node
     * @return
     */
    public boolean visit(AstNode node) {
        /* ---------------* From original jscover *----------------
        if (branchHelper.isBoolean(node) && !(detectCoalesce && branchHelper.isCoalesce(node))) {
            //replaceWithFunction(node);
        }
        return true;
        */

        int tt = node.getType();

        if (tt == org.mozilla.javascript.Token.FUNCTION) {
            handleFunction((FunctionNode) node);
        } else if (tt == org.mozilla.javascript.Token.CALL
                && node.toSource().indexOf("FUNCTION_") == -1
                && node.toSource().indexOf("RSW(") == -1
                && node.toSource().indexOf("FCW(") == -1) {
            // Do nothing for function call
            //handleFunctionCall((FunctionCall) node);
        } else if (tt == org.mozilla.javascript.Token.RETURN) {
            // Do nothing for return
            // handleReturn((ReturnStatement) node);
        }

        if (tt == org.mozilla.javascript.Token.CALL
                && (node.toSource().indexOf("FUNCTION_") > -1)) {
            return false; // Don't process kids if the function call is part of our instrumentation
        } else {
            return true;  // process kids
        }
    }

    public int getLinePosition(AstNode node) {
        int pos = node.getPosition();
        AstNode parent = node.getParent();
        while (parent != null && parent.getLineno() == node.getLineno()) {
            pos += parent.getPosition();
            parent = parent.getParent();
        }
        return pos-1;
    }

    private void handleFunctionCall(FunctionCall node) {

        // Store information on function calls
        AstNode target = node.getTarget();
        String targetBody = target.toSource();
        int[] range = {0,0};
        int lineNo = -1;
        if (node.getParent().toSource().indexOf("FCW(") > -1) {
            lineNo = node.getParent().getParent().getParent().getLineno() +1;
        } else {
            lineNo = node.getLineno()+1;
        }
        AstNode newTarget = null;

        range[0] = node.getAbsolutePosition();
        range[1] = node.getAbsolutePosition()+node.getLength();

        if (target.toSource().indexOf("FCW") == 0) {
            // We don't want to instrument out code (dirty way)
            return;
        }


        int tt = target.getType();
        if (tt == org.mozilla.javascript.Token.NAME) {
            // Regular function call, 39
            // E.g. parseInt, print, startClock
            targetBody = target.toSource();
            String newBody = target.toSource().replaceFirst(targetBody, "FCW("+targetBody+",'"+targetBody+"',"+lineNo+")");
            System.out.println("--- NAME: " + newBody);
            newTarget = parse(newBody);

        } else if (tt == org.mozilla.javascript.Token.GETPROP) {
            // Class specific function call, 33
            // E.g. document.getElementById, e.stopPropagation
            String[] methods = targetBody.split("\\.");
            range[0] += targetBody.lastIndexOf(methods[methods.length-1])-1;
            targetBody = methods[methods.length-1];

            String newBody = target.toSource().replaceFirst("."+targetBody, "[FCW(\""+targetBody+"\", "+lineNo+")]");
            System.out.println("--- PROP: " + newBody);
            newTarget = parse(newBody);
        } else {
            if (tt == org.mozilla.javascript.Token.GETELEM) {
                System.out.println("====== " + org.mozilla.javascript.Token.GETELEM + " - " + targetBody);
            }
            else if (tt == org.mozilla.javascript.Token.LP) {
                System.out.println("====== " + org.mozilla.javascript.Token.LP + " - " + targetBody);
            }
            else if (tt == org.mozilla.javascript.Token.THIS) {
                System.out.println("====== " + org.mozilla.javascript.Token.THIS + " - " + targetBody);
            }
            else
                System.out.println("======");
        }
        if (newTarget != null) {
            newTarget.setLineno(node.getTarget().getLineno());
            node.setTarget(newTarget);
        }
        else {
            System.out.println("NEW TARGET NULL +++ " + node.getTarget());
        }
    }

    private void handleReturn(ReturnStatement node) {
        // return statements

        int lineNo = node.getLineno()+1;
        AstNode newRV;

        if (node.getReturnValue() != null) {
            // Wrap return value
            newRV = parse("RSW(" + node.getReturnValue().toSource() + ", '" + node.getReturnValue().toSource().replace("'", "\\'") + "' ," + lineNo + ");");
//			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", '" + 'a' + "' ," + lineNo +");");
//			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", \"val\" ," + lineNo +");");
            newRV.setLineno(node.getReturnValue().getLineno());

        } else {
            // Return value is void
            newRV = parse("RSW(" + lineNo +")");
            newRV.setLineno(node.getLineno());
        }

        updateAllLineNo(newRV);
        node.setReturnValue(newRV);
    }

    private void handleFunction(FunctionNode node) {

        // Store information on function declarations
        AstNode parent = node.getParent();
        String name = node.getName();
        String body = node.toSource();
        int[] range = {node.getBody().getAbsolutePosition()+1,node.getEncodedSourceEnd()-1};
        int hash = node.hashCode();
        int type = node.getType();
        int lineNo = node.getLineno()+1;
        String arguments = new String();

        if(node.getParamCount() > 0){
            List<AstNode> params = node.getParams();
            for (AstNode pp: params) {
                arguments +=  "," + pp.toSource();
            }
            arguments = arguments.replaceFirst(",", "");
        } else {
            arguments = "";
        }

        if (node.getFunctionType() == FunctionNode.FUNCTION_EXPRESSION) {
            // Complicated Case
            if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.COLON) {
                // Assignment Expression
                name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
                name = name.substring(0,name.indexOf(":"));
            } else if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
                name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
                name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
            }
        } else {
            if (node.getFunctionType() == FunctionNode.FUNCTION_STATEMENT) {
                System.out.println("* " + node.getName());
            }
            // unrecognized;
            System.out.println("Unrecognized function name at " + lineNo);
        }

        // Add code at beginning of function declaration
        PointOfInterest beginningPOI = new PointOfInterest(new Object[]{name,
                type,
                range[0],
                -1,
                lineNo,
                body,
                hash,
                getScopeName(),
                arguments});

        // Add code before end of function declaration
        PointOfInterest endingPOI = new PointOfInterest(new Object[]{name,
                type,
                range[1],
                -2,
                lineNo,
                body,
                hash,
                getScopeName(),
                arguments});

        AstNode beginningNode = parse(beginningPOI.toString());

        AstNode endingNode = parse(endingPOI.toString());
        node.getBody().addChildToFront(beginningNode);
        node.getBody().addChildToBack(endingNode);

    }

    private void updateAllLineNo(AstNode body) {

        AstNode lastChild = (AstNode) body.getLastChild();

        if (lastChild == null) {
            // No children
            return;
        }

        while (true) {
            // Update line number of immediate children
            lastChild.setLineno(lastChild.getLineno()+body.getLineno());

            // Call recursively for grandchildren, greatgrandchildren, etc.
            updateAllLineNo(lastChild);

            if (body.getChildBefore(lastChild) != null) {
                lastChild = (AstNode) body.getChildBefore(lastChild);
            } else {
                break;
            }

        }

    }

    private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
    private ErrorReporter errorReporter = compilerEnvirons.getErrorReporter();

    public AstRoot parse(String code) {
        Parser p = new Parser(compilerEnvirons, errorReporter);

        System.out.println(code);
        return p.parse(code, null, 0);
    }

    protected String getFunctionName(FunctionNode f) {
        Name functionName = f.getFunctionName();

        if (functionName == null) {
            return "anonymous" + f.getLineno();
        } else {
            return functionName.toSource();
        }
    }

    private String scopeName = null;
    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getScopeName() {
        return scopeName;
    }
}
