package de.yoyosource;

import de.yoyosource.symbols.SymbolModifier;
import de.yoyosource.types.Type;
import lombok.Getter;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class ScanRule {

    private List<List<Type>> innerTypes = new ArrayList<>();
    private List<List<Type>> endTypes = new ArrayList<>();
    private EnumMap<SymbolModifier, Predicate<Character>> symbolsChecker = new EnumMap<>(SymbolModifier.class);

    public ScanRule(YAPIONObject yapionObject) {
        YAPIONArray typesObject = yapionObject.getArray("types");
        typesObject.forEach(yapionAnyType -> {
            try {
                String type = ((YAPIONValue<String>) yapionAnyType).get();
                Type[] types = new Type[type.length()];
                for (int i = 0; i < type.length(); i++) {
                    types[i] = Type.valueOf((type.charAt(i) + "").toUpperCase());
                }
                if (type.contains("E")) {
                    endTypes.add(Arrays.asList(types));
                } else {
                    innerTypes.add(Arrays.asList(types));
                }
            } catch (IllegalArgumentException e) {
                // Ignored
            }
        });
        System.out.println(innerTypes + " " + endTypes);

        YAPIONObject symbolsObject = yapionObject.getObject("symbols");
        symbolsObject.forEach((s, yapionAnyType) -> {
            try {
                SymbolModifier symbolModifier = SymbolModifier.valueOf(s.toUpperCase());
                String type = ((YAPIONValue<String>) yapionAnyType).get().toLowerCase();
                symbolsChecker.put(symbolModifier, character -> type.contains(Character.toLowerCase(character) + ""));
            } catch (IllegalArgumentException e) {
                // Ignored
            }
        });
        System.out.println(symbolsChecker);

        YAPIONArray rulesAlwaysObject = yapionObject.getArray("rules-always");
        System.out.println(rulesAlwaysObject);

        YAPIONArray rulesSometimesObject = yapionObject.getArray("rules-sometimes");
        System.out.println(rulesSometimesObject);
    }

}
