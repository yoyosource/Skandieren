package de.yoyosource.generation;

import yapion.hierarchy.output.FileGZIPOutput;
import yapion.hierarchy.output.Indentator;
import yapion.hierarchy.output.LengthOutput;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.io.IOException;

public class ResourceCompressor {

    public static void main(String[] args) throws IOException {
        for (File file : new File("./src/main/resources").listFiles((dir, name) -> name.endsWith(".scanrule"))) {
            System.out.println("COMPRESSING: " + file);
            YAPIONObject yapionObject = YAPIONParser.parse(new File("./src/main/resources/standard.scanrule"));
            yapionObject.getArray("rules-always").forEach(yapionAnyType -> {
                YAPIONArray yapionArray = (YAPIONArray) yapionAnyType;
                yapionArray.allKeys().forEach(integer -> {
                    yapionArray.set(integer, yapionArray.getValue(integer, String.class).get().replace(" ", ""));
                });
            });
            yapionObject.getArray("rules-sometimes").forEach(yapionAnyType -> {
                YAPIONArray yapionArray = (YAPIONArray) yapionAnyType;
                yapionArray.allKeys().forEach(integer -> {
                    yapionArray.set(integer, yapionArray.getValue(integer, String.class).get().replace(" ", ""));
                });
            });
            System.out.println("OBJECT:      " + yapionObject);
            yapionObject.toYAPION(new FileGZIPOutput(new File(file.getAbsoluteFile() + ".gz"))).close();
            LengthOutput lengthOutput = new LengthOutput();
            lengthOutput.setIndentator(Indentator.QUAD_SPACE);
            System.out.println("COMPRESSION: " + ((int) (10000 - new File(file.getAbsoluteFile() + ".gz").length() / (double) yapionObject.toYAPION(lengthOutput).getPrettifiedLength() * 10000.0) / 100.0) + "%");
        }
    }

}
