package de.yoyosource;

import de.yoyosource.rules.Rule;
import de.yoyosource.rules.RuleComponent;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.SymbolModifier;
import de.yoyosource.types.Type;
import lombok.Getter;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class ScanRule {

    private Set<List<Type>> innerTypes = new HashSet<>();
    private Set<List<Type>> endTypes = new HashSet<>();
    private EnumMap<SymbolModifier, Predicate<Character>> symbolsChecker = new EnumMap<>(SymbolModifier.class);
    private List<Rule> alwaysRules = new ArrayList<>();
    private List<Rule> sometimesRules = new ArrayList<>();

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
        // System.out.println(innerTypes + " " + endTypes);

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
        // System.out.println(symbolsChecker);

        YAPIONArray rulesAlwaysObject = yapionObject.getArray("rules-always");
        createRules(rulesAlwaysObject, alwaysRules);
        // System.out.println(alwaysRules);

        YAPIONArray rulesSometimesObject = yapionObject.getArray("rules-sometimes");
        createRules(rulesSometimesObject, sometimesRules);
        // System.out.println(sometimesRules);
    }

    private void createRules(YAPIONArray yapionArray, List<Rule> rules) {
        yapionArray.forEach(yapionAnyType -> {
            YAPIONArray ruleArray = (YAPIONArray) yapionAnyType;
            List<String[]> strings = new ArrayList<>();
            ruleArray.forEach(ruleElement -> {
                String[] ruleElements = ((YAPIONValue<String>) ruleElement).get().split(">");
                if (ruleElements.length != 2) return;
                for (int i = 0; i < ruleElements.length; i++) {
                    ruleElements[i] = ruleElements[i].trim();
                }
                strings.add(ruleElements);
            });

            List<RuleComponent> ruleComponents = new ArrayList<>();
            for (String[] ruleElement : strings) {

                String first = ruleElement[0];
                Predicate<Symbol> symbolPredicate;
                if (first.length() == 1) {
                    symbolPredicate = symbol -> symbol.getC() == first.charAt(0);
                } else {
                    symbolPredicate = symbol -> symbolsChecker.get(SymbolModifier.valueOf(first.toUpperCase())).test(symbol.getC());
                }

                String second = ruleElement[1];
                SymbolModifier result;
                if (second.equalsIgnoreCase("retain")) {
                    result = null;
                } else {
                    result = SymbolModifier.valueOf(second.toUpperCase());
                }

                ruleComponents.add(new RuleComponent(symbolPredicate, result));
            }

            rules.add(new Rule(ruleComponents.toArray(new RuleComponent[0])));
        });
    }

}
