package client;

import jscover.ConfigurationCommon;
import jscover.instrument.SourceProcessor;
import jscover.util.IoUtils;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        String testFilePath = "/Users/hyou/hyou_1tier_view/BIWebApp/code/html/MSTRWeb/javascript/mojo/js/source/DocModel.js";

        SourceProcessor sourceProcessor = new SourceProcessor(new ConfigurationCommon(), "test.js");
        //ParseTreeInstrumenter instrumenter = (ParseTreeInstrumenter) ReflectionUtils.getField(sourceProcessor, "instrumenter");

        //String source = "this.someFn()\n    ._renderItem = function() {};";

        String source = IoUtils.getInstance().loadFromFileSystem(new File(testFilePath));
        String instrumentedSource = sourceProcessor.instrumentSource(source);

        //String expectedSource = "_$jscoverage['test.js'].lineData[1]++;\nthis.someFn()._renderItem = function() {\n  _$jscoverage['test.js'].functionData[0]++;\n};\n";

        System.out.print(instrumentedSource);
    }
}
