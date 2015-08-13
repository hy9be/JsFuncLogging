package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;


class BranchHelper {
    private static BranchHelper branchHelper = new BranchHelper();

    static BranchHelper getInstance() {
        return branchHelper;
    }

    boolean isBoolean(AstNode node) {
        if (node instanceof EmptyExpression)
            return false;
        switch (node.getType()) {
            case Token.EQ:
            case Token.NE:
            case Token.LT:
            case Token.LE:
            case Token.GT:
            case Token.GE:
            case Token.SHEQ:
            case Token.SHNE:
            case Token.OR:
            case Token.AND:
                return true;
        }
        if (node.getParent() instanceof IfStatement) {
            return ((IfStatement)node.getParent()).getCondition() == node;
        }
        if (node.getParent() instanceof ConditionalExpression) {
            return ((ConditionalExpression)node.getParent()).getTestExpression() == node;
        }
        if (node.getParent() instanceof WhileLoop) {
            return ((WhileLoop)node.getParent()).getCondition() == node;
        }
        if (node.getParent() instanceof DoLoop) {
            return ((DoLoop)node.getParent()).getCondition() == node;
        }
        if (node.getParent() instanceof ForLoop) {
            return ((ForLoop)node.getParent()).getCondition() == node;
        }
        return false;
    }


    public boolean isCoalesce(AstNode node) {
        return node.getType() == Token.OR && (node.getParent().getType() == Token.ASSIGN || node.getParent().getType() == Token.VAR);
    }
}
