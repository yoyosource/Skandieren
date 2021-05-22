package de.yoyosource.symbols;

import de.yoyosource.ScanRule;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Getter
public class Symbol {
    private final char c;
    private int modifiers = 0;

    public void add(SymbolModifier symbolModifier) {
        if (symbolModifier == null) return;
        modifiers |= symbolModifier.bit;
    }

    public boolean ignored() {
        return is(SymbolModifier.REMOVED) || is(SymbolModifier.IGNORED);
    }

    public boolean is(SymbolModifier symbolModifier) {
        return (modifiers & symbolModifier.bit) != 0;
    }

    public static List<Symbol> toSymbols(String s, ScanRule scanRule) {
        List<Symbol> symbols = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            Symbol symbol = new Symbol(c);
            symbols.add(symbol);
            scanRule.getSymbolsChecker().forEach((symbolModifier, characterPredicate) -> {
                if (characterPredicate.test(c)) {
                    symbol.add(symbolModifier);
                }
            });
        }
        return symbols;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder().append(c).append(":");
        st.append(is(SymbolModifier.REMOVED) ? SymbolModifier.REMOVED.printChar : ' ');
        st.append(is(SymbolModifier.LONG) ? SymbolModifier.LONG.printChar : ' ');
        return st.toString();
    }

    public static List<Symbol> copy(List<Symbol> list) {
        return list.stream().map(Symbol::copy).collect(Collectors.toList());
    }

    public Symbol copy() {
        return new Symbol(c, modifiers);
    }
}
