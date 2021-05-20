package de.yoyosource;

import yapion.exceptions.parser.YAPIONParserException;
import yapion.hierarchy.output.FileGZIPOutput;
import yapion.hierarchy.output.Indentator;
import yapion.hierarchy.output.LengthOutput;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;

public class Skandieren {

    public static void main(String[] args) throws Exception {
        File toRead = new File("./src/main/resources/standard.scanrule");
        long modified = 0;
        while (true) {
            if (toRead.lastModified() <= modified + 100) {
                Thread.sleep(100);
                continue;
            }
            modified = toRead.lastModified();
            try {
                YAPIONObject yapionObject = YAPIONParser.parse(new File("./src/main/resources/standard.scanrule"));
                new ScanRule(yapionObject);
                System.out.println(yapionObject);
                File file = new File("./src/main/resources/standard.scanrule.gz");
                yapionObject.toYAPION(new FileGZIPOutput(file)).close();
                LengthOutput lengthOutput = new LengthOutput();
                lengthOutput.setIndentator(Indentator.QUAD_SPACE);
                System.out.println(yapionObject.toYAPION(lengthOutput).getPrettifiedLength() + " " + lengthOutput.getLength() + " " + file.length());
            } catch (YAPIONParserException e) {
                System.out.println("INVALID INPUT");
            }
        }
    }

}
