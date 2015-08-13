package client;

import jscover.ConfigurationCommon;
import jscover.instrument.SourceProcessor;
import jscover.util.IoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {

    public static void main(String[] args) {

        String testFilePath = "/Users/hyou/Downloads/one-tier-multiline.js";

        SourceProcessor sourceProcessor = new SourceProcessor(new ConfigurationCommon(), "test.js");

        String source = IoUtils.getInstance().loadFromFileSystem(new File(testFilePath));
        String instrumentedSource = sourceProcessor.instrumentSource(source);

        String path = "/Users/hyou/Downloads/one-tier-inst.js";
        try {
            Files.write(Paths.get(path), instrumentedSource.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(instrumentedSource);
    }
}
