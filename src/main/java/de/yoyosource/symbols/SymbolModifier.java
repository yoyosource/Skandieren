package de.yoyosource.symbols;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SymbolModifier {

    // Symbol flags
    REMOVED(0x0001, 'R'),
    LONG(0x0002, 'L'),

    // DIPHTHONG,

    // Symbol Types
    SPACE(0x0004, 's'),
    SPECIAL(0x0008, 'p'),
    VOCAL(0x0010, 'v'),
    NON_VOCAL(0x0020, 'n'),
    MUTA(0x0040, 'm'),
    LIQUIDA(0x0080, 'l'),
    UNKNOWN(0x0100, 'u'),
    IGNORED(0x0200, 'i');

    final int bit;
    final char printChar;

}
