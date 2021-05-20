package de.yoyosource;

import de.yoyosource.rules.Rule;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.TypedSymbol;
import yapion.exceptions.parser.YAPIONParserException;
import yapion.hierarchy.output.FileGZIPOutput;
import yapion.hierarchy.output.Indentator;
import yapion.hierarchy.output.LengthOutput;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.util.List;

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
                ScanRule scanRule = new ScanRule(yapionObject);
                // System.out.println(yapionObject);
                File file = new File("./src/main/resources/standard.scanrule.gz");
                yapionObject.toYAPION(new FileGZIPOutput(file)).close();
                LengthOutput lengthOutput = new LengthOutput();
                lengthOutput.setIndentator(Indentator.QUAD_SPACE);
                // System.out.println(yapionObject.toYAPION(lengthOutput).getPrettifiedLength() + " " + lengthOutput.getLength() + " " + file.length());
                // List<Symbol> symbolList = Symbol.toSymbols("In nova fert animus mutatas dicere formas", scanRule);
                List<Symbol> symbolList = Symbol.toSymbols("aspirate meis primaque ab origine mundi", scanRule);
                Rule.apply(symbolList, scanRule).forEach(symbols -> {
                    TypedSymbol.create(symbols, scanRule);
                });
            } catch (YAPIONParserException e) {
                System.out.println("INVALID INPUT");
            }
            break;
        }
    }

}
