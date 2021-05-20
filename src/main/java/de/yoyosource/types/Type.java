package de.yoyosource.types;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Type {
    L('-'),
    K('•'),
    E('×');

    public final char printChar;
}
