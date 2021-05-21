package de.yoyosource;

import de.yoyosource.rules.Rule;
import de.yoyosource.rules.RuleComponent;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.SymbolModifier;
import de.yoyosource.types.Type;
import de.yoyosource.types.TypeComposition;
import lombok.Getter;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class ScanRule {

    private Set<TypeComposition> innerTypes = new HashSet<>();
    private Set<TypeComposition> endTypes = new HashSet<>();
    private Map<Integer, Set<List<TypeComposition>>> typesMap = new HashMap<>();
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
                    endTypes.add(new TypeComposition(Arrays.asList(types)));
                } else {
                    innerTypes.add(new TypeComposition(Arrays.asList(types)));
                }
            } catch (IllegalArgumentException e) {
                // Ignored
            }
        });
        int metricalFoots = yapionObject.getPlainValueOrDefault("metrical-foots", 6);
        generateTypeArrays(metricalFoots);
        // System.out.println(innerTypes + " " + endTypes + " " + metricalFoots);

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

    private void generateTypeArrays(int metricalFoots) {
        int[] indices = new int[metricalFoots];
        List<TypeComposition> innerTypes = new ArrayList<>(this.innerTypes);
        List<TypeComposition> endTypes = new ArrayList<>(this.endTypes);

        do {
            List<TypeComposition> typeCompositions = current(indices, innerTypes, endTypes);
            typesMap.computeIfAbsent(length(typeCompositions), i -> new HashSet<>()).add(typeCompositions);
        } while (update(indices, innerTypes.size(), endTypes.size()));
    }

    private List<TypeComposition> current(int[] indices, List<TypeComposition> innerTypes, List<TypeComposition> endTypes) {
        List<TypeComposition> result = new ArrayList<>();
        for (int i = 0; i < indices.length; i++) {
            if (i == indices.length - 1) {
                result.add(endTypes.get(indices[i]));
            } else {
                result.add(innerTypes.get(indices[i]));
            }
        }
        return result;
    }

    private boolean update(int[] indices, int lengthInner, int lengthEnd) {
        for (int i = 0; i < indices.length; i++) {
            indices[i] += 1;
            if (i == indices.length - 1) {
                return indices[i] < lengthEnd;
            } else {
                if (indices[i] < lengthInner) {
                    return true;
                }
                indices[i] = 0;
            }
        }
        return true;
    }

    private int length(List<TypeComposition> typeCompositions) {
        return typeCompositions.stream().mapToInt(typeComposition -> typeComposition.getTypeList().size()).sum();
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
                // TODO: implement new parsing System
                Predicate<Symbol> symbolPredicate;
                if (first.length() == 1) {
                    symbolPredicate = symbol -> Character.toLowerCase(symbol.getC()) == Character.toLowerCase(first.charAt(0));
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
