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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SymbolModifier {

    // Symbol flags
    REMOVED(0x0001, 'R'),
    LONG(0x0002, 'L'),

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
