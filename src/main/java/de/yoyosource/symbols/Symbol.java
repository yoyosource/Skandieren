package de.yoyosource.symbols;

import de.yoyosource.ScanRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class Symbol {
    private final char c;
    private int modifiers = 0;

    public void add(SymbolModifier symbolModifier) {
        modifiers |= symbolModifier.bit;
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
        StringBuilder st = new StringBuilder();
        st.append("Symbol{");
        st.append("'").append(c).append("':'");
        for (SymbolModifier value : SymbolModifier.values()) {
            st.append(is(value) ? value.printChar : ' ');
        }
        st.append("'}");
        return st.toString();
    }
}
