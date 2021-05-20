package de.yoyosource.symbols;

import de.yoyosource.ScanRule;
import de.yoyosource.types.Type;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TypedSymbol {

    private final Symbol symbol;
    private Type type = null;

    public TypedSymbol(Symbol symbol) {
        this.symbol = symbol;
        type = symbol.is(SymbolModifier.LONG) ? Type.L : null;
    }

    public static void create(List<Symbol> symbolList, ScanRule scanRule) {
        List<TypedSymbol> typedSymbols = symbolList.stream().map(TypedSymbol::new).collect(Collectors.toList());
        // TODO: implement type System
        System.out.println(typedSymbols.stream().map(TypedSymbol::toString).collect(Collectors.joining()));
    }

    @Override
    public String toString() {
        if (type == null) {
            return "" + symbol.getC();
        }
        return "[" + type.printChar + symbol.getC() + "]";
    }
}
