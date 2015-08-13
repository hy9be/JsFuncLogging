package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;

import java.util.SortedSet;

class StatementBuilder {

    public ExpressionStatement buildInstrumentationStatement(int lineNumber, String fileName, SortedSet<Integer> validLines) {
        if (lineNumber < 1)
            throw new IllegalStateException("Illegal line number: " + lineNumber);
        validLines.add(lineNumber);

        Name var = new Name(0, "_$jscoverage");
        StringLiteral fileNameLiteral = new StringLiteral();
        fileNameLiteral.setValue(fileName);
        fileNameLiteral.setQuoteCharacter('\'');

        ElementGet indexJSFile = new ElementGet(var, fileNameLiteral);

        Name branchPropertyName = new Name();
        branchPropertyName.setIdentifier("lineData");
        PropertyGet lineProperty = new PropertyGet(indexJSFile, branchPropertyName);


        NumberLiteral lineNumberLiteral = new NumberLiteral();
        lineNumberLiteral.setValue("" + lineNumber);
        ElementGet indexLineNumber = new ElementGet(lineProperty, lineNumberLiteral);

        UnaryExpression unaryExpression = new UnaryExpression(Token.INC, 0, indexLineNumber, true);
        return new ExpressionStatement(unaryExpression);
    }

    // Function Coverage (HA-CA), tntim96
    public ExpressionStatement buildFunctionInstrumentationStatement(int functionNumber, String fileName) {
        Name var = new Name(0, "_$jscoverage");
        StringLiteral fileNameLiteral = new StringLiteral();
        fileNameLiteral.setValue(fileName);
        fileNameLiteral.setQuoteCharacter('\'');

        ElementGet indexJSFile = new ElementGet(var, fileNameLiteral);

        Name branchPropertyName = new Name();
        branchPropertyName.setIdentifier("functionData");
        PropertyGet lineProperty = new PropertyGet(indexJSFile, branchPropertyName);

        NumberLiteral lineNumberLiteral = new NumberLiteral();
        lineNumberLiteral.setValue("" + functionNumber);
        ElementGet indexLineNumber = new ElementGet(lineProperty, lineNumberLiteral);

        UnaryExpression unaryExpression = new UnaryExpression(Token.INC, 0, indexLineNumber, true);
        return new ExpressionStatement(unaryExpression);
    }
}
