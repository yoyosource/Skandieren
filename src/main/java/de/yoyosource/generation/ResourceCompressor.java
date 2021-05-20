package de.yoyosource.generation;

import yapion.hierarchy.output.FileGZIPOutput;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.io.IOException;

public class ResourceCompressor {

    public static void main(String[] args) throws IOException {
        for (File file : new File("./src/main/resources").listFiles((dir, name) -> name.endsWith(".scanrule"))) {
            System.out.println("COMPRESSING: " + file);
            YAPIONObject yapionObject = YAPIONParser.parse(new File("./src/main/resources/standard.scanrule"));
            System.out.println("OBJECT:      " + yapionObject);
            yapionObject.toYAPION(new FileGZIPOutput(new File(file.getAbsoluteFile() + ".gz")));
        }
    }

}
