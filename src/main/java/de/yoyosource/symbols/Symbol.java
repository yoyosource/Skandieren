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
