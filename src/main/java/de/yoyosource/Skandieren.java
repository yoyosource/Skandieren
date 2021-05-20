package de.yoyosource;

import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.io.IOException;

public class Skandieren {

    public static void main(String[] args) throws IOException {
        YAPIONObject yapionObject = YAPIONParser.parse(new File("/Users/jojo/IdeaProjects/Skandieren2.0/src/main/resources/standard.scanrule"));
        System.out.println(yapionObject);
    }

}
