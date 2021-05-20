package de.yoyosource.symbols;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.EnumSet;

@RequiredArgsConstructor
@ToString
@Getter
public class Symbol {
    private final char c;
    private final EnumSet<SymbolModifier> modifiers = EnumSet.allOf(SymbolModifier.class);
}
