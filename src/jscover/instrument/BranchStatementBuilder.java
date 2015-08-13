package jscover.instrument;

/**
 * Created by hyou on 8/12/15.
 */
import org.mozilla.javascript.ast.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

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

    public FunctionNode buildBranchRecordingFunction(String uri, int id, int lineNo, int conditionNo) {
        Name functionName = new Name();
        functionName.setIdentifier(format("visit%d_%d_%d", id, lineNo, conditionNo));
        FunctionNode functionNode = new FunctionNode();
        functionNode.setFunctionName(functionName);

        Name resultName = new Name();
        resultName.setIdentifier("result");
        functionNode.addParam(resultName);

        Block block = new Block();
        block.addStatement(buildLineAndConditionCall(uri, lineNo, conditionNo));

        ReturnStatement returnStatement = new ReturnStatement();
        returnStatement.setReturnValue(resultName);
        block.addChild(returnStatement);
        functionNode.setBody(block);
        return functionNode;
    }

    public FunctionNode buildMstrLoggingFunction(String uri, int id, int lineNo) {

        Name functionName = new Name();
        functionName.setIdentifier(format("visit%d_%d_%d", id, lineNo));
        FunctionNode functionNode = new FunctionNode();
        functionNode.setFunctionName(functionName);

        Name resultName = new Name();
        resultName.setIdentifier("result");
        functionNode.addParam(resultName);

        Block block = new Block();
        //block.addStatement(buildFunctionLoggingCall());

        ReturnStatement returnStatement = new ReturnStatement();
        returnStatement.setReturnValue(resultName);
        block.addChild(returnStatement);
        functionNode.setBody(block);
        return functionNode;
    }

    private ExpressionStatement buildFunctionLoggingCall(String uri, int lineNo) {

        Name resultName = new Name();
        resultName.setIdentifier("result");

        FunctionCall fnCall = new FunctionCall();
        Name propertyName = new Name();
        propertyName.setIdentifier("lineNumber");
        //PropertyGet propertyGet = new PropertyGet(1, propertyName);
        //fnCall.setTarget(propertyGet);
        List<AstNode> arguments = new ArrayList<AstNode>();
        arguments.add(resultName);
        fnCall.setArguments(arguments);

        return new ExpressionStatement(fnCall);
    }
}
