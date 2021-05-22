package de.yoyosource.symbols;

import de.yoyosource.ScanRule;
import de.yoyosource.types.Type;
import de.yoyosource.types.TypeComposition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TypedSymbol {

    private final Symbol symbol;
    private Type type = null;

    public TypedSymbol(Symbol symbol) {
        this.symbol = symbol;
        type = (!symbol.ignored() && symbol.is(SymbolModifier.LONG)) ? Type.L : null;
    }

    public static List<List<TypedSymbol>> create(List<Symbol> symbolList, ScanRule scanRule) {
        List<TypedSymbol> typedSymbols = symbolList.stream().map(Symbol::copy).map(TypedSymbol::new).collect(Collectors.toList());
        int length = (int) symbolList.stream().filter(symbol -> symbol.is(SymbolModifier.VOCAL) && !symbol.ignored()).count();
        Set<List<TypeComposition>> typeCompositions = scanRule.getTypesMap().get(length);
        if (typeCompositions == null) {
            // System.out.println("TO LONG " + length);
            return Collections.emptyList();
        }

        List<List<TypedSymbol>> result = new ArrayList<>();
        for (List<TypeComposition> typeCompositionList : typeCompositions) {
            List<TypedSymbol> current = typedSymbols.stream().map(TypedSymbol::copy).collect(Collectors.toList());
            List<TypedSymbol> vocals = current.stream().filter(typedSymbol -> typedSymbol.getSymbol().is(SymbolModifier.VOCAL) && !typedSymbol.getSymbol().ignored()).collect(Collectors.toList());
            List<Type> types = typeCompositionList.stream().flatMap(typeComposition -> typeComposition.getTypeList().stream()).collect(Collectors.toList());
            int index = 0;
            while (index < vocals.size()) {
                TypedSymbol vocal = vocals.get(index);
                Type type = types.get(index);

                if (vocal.type == null || type == Type.E) {
                    vocal.type = type;
                } else if (vocal.type != type) {
                    break;
                }
                index++;
            }
            if (index >= vocals.size()) {
                result.add(current);
            }
        }
        if (result.isEmpty()) {
            // System.out.println("NO RESULT   " + typedSymbols.stream().map(TypedSymbol::toString).collect(Collectors.joining()));
            return Collections.emptyList();
        }
        // System.out.println();
        for (List<TypedSymbol> typedSymbolList : result) {
            System.out.println(typedSymbolList.stream().map(TypedSymbol::toString).collect(Collectors.joining()));
        }
        return result;
    }

    public TypedSymbol copy() {
        return new TypedSymbol(symbol, type);
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(symbol.getC());
        if (symbol.is(SymbolModifier.REMOVED)) {
            if (symbol.is(SymbolModifier.VOCAL)) {
                st.append("̶");
            } else {
                st.setCharAt(st.length() - 1, '_');
            }
        }
        if (type != null) {
            st.append(type.aboveChar);
        }
        return st.toString();
    }
}
