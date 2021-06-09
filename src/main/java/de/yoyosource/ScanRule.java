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

    private YAPIONObject yapionObject;
    private List<List<List<TypeComposition>>> types = new ArrayList<>();
    private Map<Integer, Set<List<TypeComposition>>> typesMap = new HashMap<>();
    private EnumMap<SymbolModifier, Predicate<Character>> symbolsChecker = new EnumMap<>(SymbolModifier.class);
    private List<Rule> alwaysRules = new ArrayList<>();
    private List<Rule> sometimesRules = new ArrayList<>();

    public ScanRule(YAPIONObject yapionObject) {
        this.yapionObject = yapionObject;

        YAPIONArray typesObject = yapionObject.getArray("types");
        typesObject.forEach(outerArray -> {
            YAPIONArray innerArray = (YAPIONArray) outerArray;
            List<List<TypeComposition>> current = new ArrayList<>();
            innerArray.forEach(yapionAnyType -> {
                try {
                    List<TypeComposition> typeCompositions = new ArrayList<>();
                    YAPIONArray yapionArray = (YAPIONArray) yapionAnyType;
                    yapionArray.forEach(yat -> {
                        String type = ((YAPIONValue<String>) yat).get();
                        Type[] types = new Type[type.length()];
                        for (int i = 0; i < type.length(); i++) {
                            types[i] = Type.valueOf((type.charAt(i) + "").toUpperCase());
                        }
                        typeCompositions.add(new TypeComposition(Arrays.asList(types)));
                    });
                    current.add(typeCompositions);
                } catch (IllegalArgumentException e) {
                    // Ignored
                }
            });
            types.add(current);
        });
        for (List<List<TypeComposition>> current : types) {
            generateTypeArrays(current);
        }
        // System.out.println(innerTypes + " " + endTypes + " " + metricalFeet);

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

    private void generateTypeArrays(List<List<TypeComposition>> types) {
        int[] indices = new int[types.size()];
        do {
            List<TypeComposition> typeCompositions = current(indices, types);
            typesMap.computeIfAbsent(length(typeCompositions), i -> new HashSet<>()).add(typeCompositions);
        } while (update(indices, types));
    }

    private List<TypeComposition> current(int[] indices, List<List<TypeComposition>> types) {
        List<TypeComposition> result = new ArrayList<>();
        for (int i = 0; i < indices.length; i++) {
            result.add(types.get(i).get(indices[i]));
        }
        return result;
    }

    private boolean update(int[] indices, List<List<TypeComposition>> types) {
        for (int i = 0; i < indices.length; i++) {
            indices[i] += 1;
            if (i == indices.length - 1) {
                return indices[i] < types.get(i).size();
            } else {
                if (indices[i] < types.get(i).size()) {
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
                Predicate<Symbol> symbolPredicate;
                if (first.length() == 1) {
                    symbolPredicate = symbol -> Character.toLowerCase(symbol.getC()) == Character.toLowerCase(first.charAt(0));
                } else {
                    int index = first.indexOf('|');
                    int lastIndex = first.lastIndexOf('|');
                    if (first.contains("|") && index == lastIndex) {
                        SymbolModifier current = SymbolModifier.valueOf(first.substring(0, index).trim().toUpperCase());
                        symbolPredicate = symbol -> symbolsChecker.get(current).test(symbol.getC());

                        String[] second = first.substring(index + 1).trim().split(",");
                        symbolPredicate = symbolPredicate.and(Arrays.stream(second).map(String::trim).filter(s -> !s.isEmpty()).map(s -> {
                            if (s.length() == 1) {
                                return (Predicate<Symbol>) symbol -> Character.toLowerCase(symbol.getC()) == Character.toLowerCase(s.charAt(0));
                            } else {
                                SymbolModifier now = SymbolModifier.valueOf(s.toUpperCase());
                                return (Predicate<Symbol>) symbol -> symbolsChecker.get(now).test(symbol.getC());
                            }
                        }).reduce(symbol -> true, Predicate::and).negate());
                    } else {
                        SymbolModifier current = SymbolModifier.valueOf(first.toUpperCase());
                        symbolPredicate = symbol -> symbolsChecker.get(current).test(symbol.getC());
                    }
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
