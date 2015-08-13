package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.ast.*;

import java.util.ArrayList;
import java.util.List;

public class BranchStatementBuilder {

    public ExpressionStatement buildLineAndConditionInitialisation(String uri, int lineNo, int conditionNo, int position, int length, String source) {
        ElementGet indexLineNumber = buildLineDeclaration(uri, lineNo);

        NumberLiteral conditionNumberLiteral = new NumberLiteral();
        conditionNumberLiteral.setValue("" + conditionNo);
        ElementGet indexConditionNumber = new ElementGet(indexLineNumber, conditionNumberLiteral);

        FunctionCall fnCall = new FunctionCall();
        Name propertyName = new Name();
        propertyName.setIdentifier("init");
        PropertyGet propertyGet = new PropertyGet(indexConditionNumber, propertyName);
        fnCall.setTarget(propertyGet);
        NumberLiteral positionLiteral = new NumberLiteral();
        positionLiteral.setValue(""+position);
        fnCall.addArgument(positionLiteral);
        NumberLiteral lengthLiteral = new NumberLiteral();
        lengthLiteral.setValue(""+length);
        fnCall.addArgument(lengthLiteral);
        StringLiteral stringLiteral = new StringLiteral();
        stringLiteral.setValue(removeInstrumentation(source));
        stringLiteral.setQuoteCharacter('\'');
        fnCall.addArgument(stringLiteral);

        return new ExpressionStatement(fnCall);
    }

    protected String removeInstrumentation(String source) {
        return source.replaceAll(" *_\\$jscoverage\\['[^']+'\\]\\.[^\\[]+\\[\\d+\\]\\+\\+;\n", "");
    }

    public ExpressionStatement buildLineAndConditionCall(String uri, int lineNo, int conditionNo) {
        ElementGet indexLineNumber = buildLineDeclaration(uri, lineNo);

        NumberLiteral conditionNumberLiteral = new NumberLiteral();
        conditionNumberLiteral.setValue("" + conditionNo);
        ElementGet indexConditionNumber = new ElementGet(indexLineNumber, conditionNumberLiteral);

        Name resultName = new Name();
        resultName.setIdentifier("result");

        FunctionCall fnCall = new FunctionCall();
        Name propertyName = new Name();
        propertyName.setIdentifier("ranCondition");
        PropertyGet propertyGet = new PropertyGet(indexConditionNumber, propertyName);
        fnCall.setTarget(propertyGet);
        List<AstNode> arguments = new ArrayList<AstNode>();
        arguments.add(resultName);
        fnCall.setArguments(arguments);

        return new ExpressionStatement(fnCall);
    }

    private ElementGet buildLineDeclaration(String uri, int lineNo) {
        Name jscoverageVar = new Name();
        jscoverageVar.setIdentifier("_$jscoverage");

        StringLiteral fileNameLiteral = new StringLiteral();
        fileNameLiteral.setValue(uri);
        fileNameLiteral.setQuoteCharacter('\'');
        ElementGet indexJSFile = new ElementGet(jscoverageVar, fileNameLiteral);

        Name branchPropertyName = new Name();
        branchPropertyName.setIdentifier("branchData");
        PropertyGet branchProperty = new PropertyGet(indexJSFile, branchPropertyName);

        NumberLiteral lineNumberLiteral = new NumberLiteral();
        lineNumberLiteral.setValue("'" + lineNo + "'");
        return new ElementGet(branchProperty, lineNumberLiteral);
    }
}
