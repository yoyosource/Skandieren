package de.yoyosource;

import de.yoyosource.percentage.Percentage;
import de.yoyosource.rules.Rule;
import de.yoyosource.rules.RuleComponent;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.SymbolModifier;
import de.yoyosource.symbols.TypedSymbol;
import de.yoyosource.types.Type;
import de.yoyosource.types.TypeComposition;
import lombok.Getter;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class ScanRule {

    private YAPIONObject yapionObject;
    private List<List<List<TypeComposition>>> types = new ArrayList<>();
    private Map<Integer, Set<List<TypeComposition>>> typesMap = new HashMap<>();
    private EnumMap<SymbolModifier, Predicate<Character>> symbolsChecker = new EnumMap<>(SymbolModifier.class);
    private List<Rule> alwaysRules = new ArrayList<>();
    private List<Rule> sometimesRules = new ArrayList<>();
    private List<Percentage> percentageRules = new ArrayList<>();

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

        if (yapionObject.containsKey("percentages")) {
            YAPIONObject current = yapionObject.getObject("percentages");
            if (current.containsKey("vocal")) {
                generateVocalPercentages(current.getArray("vocal"));
            }
            if (current.containsKey("char")) {
                generateCharPercentages(current.getArray("char"));
            }
            if (current.containsKey("special")) {
                generateSpecialPercentages(current.getArray("special"));
            }
        }
    }

    private void generateTypeArrays(List<List<TypeComposition>> types) {
        int[] indices = new int[types.size()];
        do {
            List<TypeComposition> typeCompositions = current(indices, types);
            typesMap.computeIfAbsent(length(typeCompositions), i -> new LinkedHashSet<>()).add(typeCompositions);
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

    private void generateVocalPercentages(YAPIONArray yapionArray) {
        yapionArray.streamObject().forEach(yapionObject -> {
            int points = yapionObject.getPlainValue("points");
            Predicate<List<TypedSymbol>> predicate = typedSymbols -> true;
            for (String s : yapionObject.getArray("").streamValue().map(YAPIONValue::get).map(Object::toString).collect(Collectors.toList())) {
                String[] strings = s.split(":");
                if (strings.length != 2) {
                    return;
                }
                int index = Integer.parseInt(strings[0].trim());
                try {
                    SymbolModifier symbolModifier = SymbolModifier.valueOf(strings[1].trim());
                    predicate = predicate.and(typedSymbols -> typedSymbols.get(index).getSymbol().is(symbolModifier));
                } catch (Exception e) {
                    Type type = Type.valueOf(strings[1].trim());
                    predicate = predicate.and(typedSymbols -> typedSymbols.get(index).getType() == type);
                }
            }
            Predicate<List<TypedSymbol>> finalPredicate = predicate;
            percentageRules.add(typedSymbolList -> {
                List<TypedSymbol> typedSymbols = typedSymbolList.stream().filter(t -> !t.getSymbol().ignored()).filter(t -> t.getType() != null).collect(Collectors.toList());
                if (finalPredicate.test(typedSymbols)) {
                    return points;
                }
                return 0;
            });
        });
    }

    private void generateCharPercentages(YAPIONArray yapionArray) {
        yapionArray.streamObject().forEach(yapionObject -> {
            int points = yapionObject.getPlainValue("points");
            BiPredicate<Integer, List<TypedSymbol>> predicate = (i, typedSymbols) -> true;
            List<String> stringList = yapionObject.getArray("").streamValue().map(YAPIONValue::get).map(Object::toString).collect(Collectors.toList());
            int index = 0;
            for (String s : stringList) {
                Predicate<TypedSymbol> typeOrModifer = typedSymbol -> true;
                if (s.contains(":")) {
                    String[] strings = s.split(":");
                    if (strings.length != 2) {
                        return;
                    }
                    s = strings[0].trim();
                    String current = strings[1].trim();
                    try {
                        SymbolModifier symbolModifier = SymbolModifier.valueOf(current);
                        typeOrModifer = typedSymbol -> typedSymbol.getSymbol().is(symbolModifier);
                    } catch (Exception e) {
                        Type type = Type.valueOf(current);
                        typeOrModifer = typedSymbol -> typedSymbol.getType() == type;
                    }
                }

                String currentChar = s;
                Predicate<Character> charPredicate;
                if (currentChar.length() == 1) {
                    charPredicate = character -> character == currentChar.charAt(0);
                } else {
                    SymbolModifier symbolModifier = SymbolModifier.valueOf(currentChar);
                    charPredicate = symbolsChecker.get(symbolModifier);
                }

                int finalIndex = index;
                Predicate<TypedSymbol> finalTypeOrModifer = typeOrModifer;
                predicate = predicate.and((integer, typedSymbols) -> {
                    if (!charPredicate.test(typedSymbols.get(integer + finalIndex).getSymbol().getC())) {
                        return false;
                    }
                    return finalTypeOrModifer.test(typedSymbols.get(integer + finalIndex));
                });
                index++;
            }
            BiPredicate<Integer, List<TypedSymbol>> finalPredicate1 = predicate;
            percentageRules.add(typedSymbolList -> {
                List<TypedSymbol> typedSymbols = typedSymbolList.stream().filter(t -> !t.getSymbol().ignored()).collect(Collectors.toList());
                int totalPoints = 0;
                for (int i = 0; i < typedSymbols.size() - stringList.size(); i++) {
                    if (finalPredicate1.test(i, typedSymbols)) {
                        totalPoints += points;
                    }
                }
                return totalPoints;
            });
        });
    }

    private void generateSpecialPercentages(YAPIONArray yapionArray) {
        yapionArray.streamObject().forEach(yapionObject -> {
            int points = yapionObject.getPlainValue("points");
            BiPredicate<Integer, List<TypedSymbol>> predicate = (i, typedSymbols) -> true;
            List<String> stringList = yapionObject.getArray("").streamValue().map(YAPIONValue::get).map(Object::toString).collect(Collectors.toList());
            int index = 0;
            for (String s : stringList) {
                Predicate<TypedSymbol> typeOrModifer = typedSymbol -> true;
                if (s.contains(":")) {
                    String[] strings = s.split(":");
                    if (strings.length != 2) {
                        return;
                    }
                    s = strings[0].trim();
                    String current = strings[1].trim();
                    try {
                        SymbolModifier symbolModifier = SymbolModifier.valueOf(current);
                        typeOrModifer = typedSymbol -> typedSymbol.getSymbol().is(symbolModifier);
                    } catch (Exception e) {
                        Type type = Type.valueOf(current);
                        typeOrModifer = typedSymbol -> typedSymbol.getType() == type;
                    }
                }

                String currentChar = s;
                Predicate<Character> charPredicate;
                if (currentChar.length() == 1) {
                    charPredicate = character -> character == currentChar.charAt(0);
                } else {
                    SymbolModifier symbolModifier = SymbolModifier.valueOf(currentChar);
                    charPredicate = symbolsChecker.get(symbolModifier);
                }

                int finalIndex = index;
                Predicate<TypedSymbol> finalTypeOrModifer = typeOrModifer;
                predicate = predicate.and((integer, typedSymbols) -> {
                    if (!charPredicate.test(typedSymbols.get(integer + finalIndex).getSymbol().getC())) {
                        return false;
                    }
                    return finalTypeOrModifer.test(typedSymbols.get(integer + finalIndex));
                });
                index++;
            }
            BiPredicate<Integer, List<TypedSymbol>> finalPredicate1 = predicate;
            percentageRules.add(typedSymbolList -> {
                List<TypedSymbol> typedSymbols = typedSymbolList.stream().filter(t -> !t.getSymbol().is(SymbolModifier.IGNORED)).collect(Collectors.toList());
                int totalPoints = 0;
                for (int i = 0; i < typedSymbols.size() - stringList.size(); i++) {
                    if (finalPredicate1.test(i, typedSymbols)) {
                        totalPoints += points;
                    }
                }
                return totalPoints;
            });
        });
    }

}
