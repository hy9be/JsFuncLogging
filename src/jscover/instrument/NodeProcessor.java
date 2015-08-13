package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

//Function Coverage added by Howard Abrams, CA Technologies (HA-CA) - May 20 2013, tntim96
class NodeProcessor {
    private StatementBuilder statementBuilder = new StatementBuilder();
    private SortedSet<Integer> validLines = new TreeSet<Integer>();
    private int functionNumber;// Function Coverage (HA-CA)
    private String fileName;
    private boolean includeFunctionCoverage;

    public NodeProcessor(String uri, boolean includeFunctionCoverage) {
        this.fileName = uri;
        this.includeFunctionCoverage = includeFunctionCoverage;
    }

    public ExpressionStatement buildInstrumentationStatement(int lineNumber) {
        return statementBuilder.buildInstrumentationStatement(lineNumber, fileName, validLines);
    }

    // Function Coverage (HA-CA)
    public ExpressionStatement buildFunctionInstrumentationStatement(int functionNumber) {
        return statementBuilder.buildFunctionInstrumentationStatement(functionNumber, fileName);
    }

    boolean processNode(AstNode node) {
        // Function Coverage (HA-CA), tntim96
        if (includeFunctionCoverage && node instanceof FunctionNode) {
            AstNode block = ((FunctionNode) node).getBody();
            if (block instanceof Block) {
                block.addChildToFront(buildFunctionInstrumentationStatement(functionNumber++));
            }
        }

        if (validLines.contains(node.getLineno())) {
            // Don't add instrumentation if already there
            return true;
        }

        if (node.getParent() != null && node.getLineno() == node.getParent().getLineno()) {
            // Don't add instrumentation if it will be added by parent for the
            // same line
            // TODO Need logic to determine if instrumentation will be added to
            // parent.
            // return true;
        }

        AstNode parent = node.getParent();
        if (parent instanceof ObjectProperty || parent instanceof FunctionCall) {
            return true;
        }
        if (node instanceof ExpressionStatement || node instanceof EmptyExpression || node instanceof Loop
                || node instanceof ContinueStatement || node instanceof VariableDeclaration || node instanceof LetNode
                || node instanceof SwitchStatement || node instanceof BreakStatement
                || node instanceof EmptyStatement || node instanceof ThrowStatement) {

            if (node.getLineno() < 1) {
                //Must be a case expression
                return true;
            }
            if (parent instanceof SwitchCase) {
                //Don't do anything here. Direct modification of statements will result in concurrent modification exception.
            } else if (parent instanceof LabeledStatement) {
                //Don't do anything here.
            } else if (isLoopInitializer(node)) {
                //Don't do anything here.
            } else if (parent != null) {
                addInstrumentationBefore(node);
            }
        } else if (node instanceof WithStatement) {
            addInstrumentationBefore(node);
        } else if (node instanceof SwitchCase) {
            List<AstNode> statements = ((SwitchCase) node).getStatements();
            if (statements == null) {
                return true;
            }
            for (int i = statements.size() - 1; i >= 0; i--) {
                AstNode statement = statements.get(i);
                statements.add(i, buildInstrumentationStatement(statement.getLineno()));
            }
        } else if (node instanceof FunctionNode || node instanceof TryStatement || isDebugStatement(node)) {
            if (!(parent instanceof InfixExpression) && !(parent instanceof VariableInitializer)
                    && !(parent instanceof ConditionalExpression) && !(parent instanceof ArrayLiteral)
                    && !(parent instanceof ParenthesizedExpression)) {
                addInstrumentationBefore(node);
            }
        } else if (node instanceof ReturnStatement) {
            addInstrumentationBefore(node);
        } else if (node instanceof LabeledStatement) {
            LabeledStatement labeledStatement = (LabeledStatement)node;
            ExpressionStatement newChild = buildInstrumentationStatement(labeledStatement.getLineno());
            parent.addChildBefore(newChild, node);
        } else if (node instanceof IfStatement) {
            addInstrumentationBefore(node);
        }
        return true;
    }

    private boolean isLoopInitializer(AstNode node) {
        if (node.getParent() instanceof ForLoop) {
            ForLoop forLoop = (ForLoop)node.getParent();
            if (forLoop.getInitializer() == node)
                return true;
        }
        return false;
    }

    private void addInstrumentationBefore(AstNode node) {
        AstNode parent = node.getParent();
        if (parent instanceof IfStatement) {
            addIfScope(node, (IfStatement) parent);
        } else if (parent instanceof Loop) {
            addLoopScope(node, (Loop) parent);
        } else if (parent instanceof WithStatement) {
            addWithScope(node, (WithStatement) parent);
        } else {
            parent.addChildBefore(buildInstrumentationStatement(node.getLineno()), node);
        }
    }

    private Scope makeReplacementScope(AstNode node) {
        Scope scope = new Scope();
        scope.addChild(buildInstrumentationStatement(node.getLineno()));
        scope.addChild(node);
        return scope;
    }

    private void addWithScope(AstNode node, WithStatement with) {
        Scope scope = makeReplacementScope(node);
        with.setStatement(scope);
    }

    private void addLoopScope(AstNode node, Loop parentLoop) {
        Scope scope = makeReplacementScope(node);
        parentLoop.setBody(scope);
    }

    private void addIfScope(AstNode node, IfStatement parentIf) {
        Scope scope = makeReplacementScope(node);
        if (parentIf.getThenPart() == node) {
            parentIf.setThenPart(scope);
        } else if (parentIf.getElsePart() == node) {
            parentIf.setElsePart(scope);
        }
    }

    private boolean isDebugStatement(AstNode node) {
        if (!(node instanceof KeywordLiteral))
            return false;
        KeywordLiteral keywordLiteral = (KeywordLiteral) node;
        return keywordLiteral.getType() == Token.DEBUGGER;
    }

    public SortedSet<Integer> getValidLines() {
        return validLines;
    }

    public int getNumFunctions() {
        return functionNumber;
    }
}
