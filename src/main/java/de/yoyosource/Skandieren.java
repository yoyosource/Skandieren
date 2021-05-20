package de.yoyosource;

import yapion.exceptions.parser.YAPIONParserException;
import yapion.hierarchy.output.FileGZIPOutput;
import yapion.hierarchy.output.LengthOutput;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;

public class Skandieren {

    public static void main(String[] args) throws Exception {
        File toRead = new File("/Users/jojo/IdeaProjects/Skandieren2.0/src/main/resources/standard.scanrule");
        long modified = 0;
        while (true) {
            if (toRead.lastModified() <= modified + 100) {
                Thread.sleep(100);
                continue;
            }
            modified = toRead.lastModified();
            try {
                YAPIONObject yapionObject = YAPIONParser.parse(new File("/Users/jojo/IdeaProjects/Skandieren2.0/src/main/resources/standard.scanrule"));
                System.out.println(yapionObject);
                File file = new File("/Users/jojo/IdeaProjects/Skandieren2.0/src/main/resources/standard.scanrule.gz");
                yapionObject.toYAPION(new FileGZIPOutput(file)).close();
                System.out.println(yapionObject.toYAPION(new LengthOutput()).getPrettifiedLength() + " " + yapionObject.toYAPION(new LengthOutput()).getLength() + " " + file.length());
            } catch (YAPIONParserException e) {
                System.out.println("INVALID INPUT");
            }
        }
    }

}
