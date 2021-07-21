/*
 * This file is a part of the Skandieren software.
 *
 * Copyright (C) 2020  YoyoSource
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.yoyosource;

import de.yoyosource.rules.Rule;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.TypedSymbol;
import yapion.exceptions.parser.YAPIONParserException;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.util.List;

public class Scansion {

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
                List<Symbol> symbolList = Symbol.toSymbols("In nova fert animus mutatas dicere formas", scanRule);
                // List<Symbol> symbolList = Symbol.toSymbols("aspirate meis primaque ab origine mundi", scanRule);
                // List<Symbol> symbolList = Symbol.toSymbols("Utque meum intonsis caput est iuvenale capillis,", scanRule);
                // List<Symbol> symbolList = Symbol.toSymbols("te coma, te citharae, te nostrae, laura, pharetrae", scanRule);
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
