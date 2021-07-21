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

package de.yoyosource.generation;

import yapion.hierarchy.output.FileGZIPOutput;
import yapion.hierarchy.output.Indentator;
import yapion.hierarchy.output.LengthOutput;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.io.IOException;

public class ResourceCompressor {

    public static void main(String[] args) throws IOException {
        for (File file : new File("./src/main/resources/verseschemes").listFiles((dir, name) -> name.endsWith(".scanrule"))) {
            System.out.println("COMPRESSING: " + file);
            YAPIONObject yapionObject = YAPIONParser.parse(file);
            yapionObject.getArray("rules-always").forEach(yapionAnyType -> {
                YAPIONArray yapionArray = (YAPIONArray) yapionAnyType;
                yapionArray.allKeys().forEach(integer -> {
                    yapionArray.set(integer, yapionArray.getValue(integer, String.class).get().replace(" ", ""));
                });
            });
            yapionObject.getArray("rules-sometimes").forEach(yapionAnyType -> {
                YAPIONArray yapionArray = (YAPIONArray) yapionAnyType;
                yapionArray.allKeys().forEach(integer -> {
                    yapionArray.set(integer, yapionArray.getValue(integer, String.class).get().replace(" ", ""));
                });
            });
            System.out.println("OBJECT:      " + yapionObject);
            File fileGz = new File(file.getAbsoluteFile() + ".gz");
            yapionObject.toYAPION(new FileGZIPOutput(fileGz)).close();
            LengthOutput lengthOutput = new LengthOutput();
            lengthOutput.setIndentator(Indentator.QUAD_SPACE);
            long compressedLength = fileGz.length();
            long uncompressedLength = yapionObject.toYAPION(lengthOutput).getPrettifiedLength();
            System.out.println("COMPRESSION: " + ((int) (10000 - compressedLength / (double) uncompressedLength * 10000.0) / 100.0) + "%   " + compressedLength + " " + uncompressedLength);
        }
    }

}
