package de.yoyosource.types;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Type {
    L('-', '̄'),
    S('·', '̆'),
    E('⨯', '̉');

    public final char printChar;
    public final char aboveChar;
}
