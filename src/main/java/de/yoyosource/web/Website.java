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

package de.yoyosource.web;

import de.yoyosource.ScanRule;
import de.yoyosource.rules.Rule;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.SymbolModifier;
import de.yoyosource.symbols.TypedSymbol;
import de.yoyosource.types.Type;
import yapion.exceptions.YAPIONException;
import yapion.hierarchy.output.StringOutput;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;
import yapion.parser.YAPIONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Website {

    private static Map<YAPIONObject, YAPIONObject> requestMap = new LinkedHashMap<YAPIONObject, YAPIONObject>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<YAPIONObject, YAPIONObject> eldest) {
            return size() > 128;
        }
    };
    private static Map<String, ScanRule> scanRuleMap = new HashMap<>();

    static {
        YAPIONArray yapionArray = new YAPIONParser(Website.class.getResourceAsStream("/verseschemes.yapion"), true).parse().resultArray();
        yapionArray.stream().filter(yapionAnyType -> YAPIONValue.class.isAssignableFrom(yapionAnyType.getClass())).map(YAPIONValue.class::cast).map(yapionValue -> (YAPIONValue<String>) yapionValue).map(YAPIONValue::get).forEach(name -> {
            try {
                ScanRule scanRule = new ScanRule(YAPIONParser.parse(Website.class.getResourceAsStream("/verseschemes/" + name)));
                if (!name.startsWith("standard")) {
                    name = name.substring(0, name.indexOf('.'));
                } else {
                    name = "";
                }
                scanRuleMap.put(name.toLowerCase(), scanRule);
            } catch (YAPIONException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > 10;
            }
        };

        initExceptionHandler(Throwable::printStackTrace);
        staticFiles.location("/website");
        notFound((request, response) -> {
            if (counts.containsKey(request.ip())) {
                counts.put(request.ip(), counts.get(request.ip()) + 1);
            } else {
                counts.put(request.ip(), 1);
            }

            if (counts.get(request.ip()) < 5) {
                return new BufferedReader(new InputStreamReader(Website.class.getResourceAsStream("/websiteerrors/404.html"))).lines().collect(Collectors.joining("\n"));
            } else {
                counts.remove(request.ip());
                return new BufferedReader(new InputStreamReader(Website.class.getResourceAsStream("/websiteerrors/418.html"))).lines().collect(Collectors.joining("\n"));
            }
        });
        internalServerError((request, response) -> {
            return new BufferedReader(new InputStreamReader(Website.class.getResourceAsStream("/websiteerrors/500.html"))).lines().collect(Collectors.joining("\n"));
        });

        post("/api/scansion", (request, response) -> {
            // Parsing request
            YAPIONObject yapionObject = YAPIONParser.parse(request.body());
            if (requestMap.containsKey(yapionObject)) {
                response.status(200);
                response.type("application/json");
                return requestMap.get(yapionObject).toJSONLossy(new StringOutput(false)).getResult();
            }
            System.out.println("Request: " + yapionObject);
            String text = yapionObject.getPlainValue("text");

            String scanRuleName = yapionObject.getPlainValueOrDefault("ruleset", "").toLowerCase();
            if (!scanRuleMap.containsKey(scanRuleName)) {
                scanRuleName = "";
            }
            ScanRule scanRule = scanRuleMap.get(scanRuleName);

            // Skandieren
            String order = yapionObject.getPlainValueOrDefault("order", "normal");
            List<Symbol> symbolList = Symbol.toSymbols(text, scanRule);
            List<List<TypedSymbol>> lists;

            switch (order) {
                case "ascending":
                    lists = ascendingScansion(symbolList, scanRule);
                    break;
                case "descending":
                    lists = descendingScansion(symbolList, scanRule);
                    break;
                default:
                case "normal":
                    lists = normalScansion(symbolList, scanRule);
                    break;
            }

            List<List<TypedSymbol>> result = new ArrayList<>();
            if (!scanRule.getPositionalLongs().isEmpty()) {
                for (List<TypedSymbol> list : lists) {
                    List<TypedSymbol> typedSymbols = list.stream().filter(symbol -> symbol.getType() != null).collect(Collectors.toList());
                    boolean invalid = false;
                    for (int i = 0; i < typedSymbols.size(); i++) {
                        TypedSymbol typedSymbol = typedSymbols.get(i);
                        if (scanRule.getPositionalLongs().contains(i)) {
                            if (typedSymbol.getType() != Type.L) {
                                invalid = true;
                                break;
                            }
                        }
                    }
                    if (invalid) {
                        continue;
                    }
                    result.add(list);
                }
            } else {
                result = lists;
            }
            lists = result;

            int max = 0;
            for (List<TypedSymbol> typedSymbols : lists) {
                if (typedSymbols.isEmpty()) {
                    continue;
                }
                int current = scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(typedSymbols)).sum();
                if (current > max) {
                    max = current;
                }
            }

            YAPIONObject resultObject = generateResult(lists, scanRule, max);
            requestMap.put(yapionObject, resultObject);
            response.status(200);
            response.type("application/json");
            return resultObject.toJSONLossy(new StringOutput(false)).getResult();
        });
        get("/api/rulesets", (request, response) -> {
            YAPIONObject yapionObject = new YAPIONObject();
            scanRuleMap.forEach((s, scanRule) -> {
                if (s.equals("")) {
                    s = "standard";
                }
                yapionObject.add(s, "");
            });

            response.status(200);
            response.type("application/json");
            return yapionObject.toJSONLossy(new StringOutput()).getResult();
        });

        after((request, response) -> {
            try {
                YAPIONObject yapionObject = YAPIONParser.parse(request.body());
                if (yapionObject.containsKey("plain", Boolean.class) && yapionObject.getValue("plain", Boolean.class).get()) {
                    return;
                }
                response.header("Content-Encoding", "gzip");
            } catch (Exception e) {
                // Ignored
            }
        });
    }

    private static List<List<TypedSymbol>> normalScansion(List<Symbol> symbolList, ScanRule scanRule) {
        List<List<List<TypedSymbol>>> listList = new ArrayList<>();
        Rule.apply(symbolList, scanRule).forEach(symbols -> {
            List<List<TypedSymbol>> current = TypedSymbol.create(symbols, scanRule);
            if (!current.isEmpty()) {
                current.sort(Comparator.comparingInt(value -> -scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(value)).sum()));
                listList.add(current);
            }
        });
        listList.sort(Comparator.comparingInt(value -> -scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(value.get(0))).sum()));

        List<List<TypedSymbol>> lists = new ArrayList<>();
        boolean b = false;
        for (List<List<TypedSymbol>> list : listList) {
            if (b) {
                lists.add(new ArrayList<>());
            }
            b = true;
            lists.addAll(list);
        }
        return lists;
    }

    private static List<List<TypedSymbol>> descendingScansion(List<Symbol> symbolList, ScanRule scanRule) {
        List<List<List<TypedSymbol>>> listList = new ArrayList<>();
        Rule.apply(symbolList, scanRule).forEach(symbols -> {
            List<List<TypedSymbol>> current = TypedSymbol.create(symbols, scanRule);
            if (!current.isEmpty()) {
                listList.add(current);
            }
        });

        List<List<TypedSymbol>> lists = new ArrayList<>();
        for (List<List<TypedSymbol>> list : listList) {
            lists.addAll(list);
        }
        lists.sort(Comparator.comparingInt(value -> -scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(value)).sum()));
        return lists;
    }

    private static List<List<TypedSymbol>> ascendingScansion(List<Symbol> symbolList, ScanRule scanRule) {
        List<List<List<TypedSymbol>>> listList = new ArrayList<>();
        Rule.apply(symbolList, scanRule).forEach(symbols -> {
            List<List<TypedSymbol>> current = TypedSymbol.create(symbols, scanRule);
            if (!current.isEmpty()) {
                listList.add(current);
            }
        });

        List<List<TypedSymbol>> lists = new ArrayList<>();
        for (List<List<TypedSymbol>> list : listList) {
            lists.addAll(list);
        }
        lists.sort(Comparator.comparingInt(value -> scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(value)).sum()));
        return lists;
    }

    private static YAPIONObject generateResult(List<List<TypedSymbol>> lists, ScanRule scanRule, int max) {
        YAPIONObject resultObject = new YAPIONObject();
        YAPIONArray allResults = new YAPIONArray();
        resultObject.add("results", allResults);
        for (List<TypedSymbol> typedSymbols : lists) {
            if (typedSymbols.isEmpty()) {
                allResults.add(new YAPIONObject());
                continue;
            }

            YAPIONObject current = new YAPIONObject();
            allResults.add(current);
            if (!scanRule.getPercentageRules().isEmpty()) {
                int points = scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(typedSymbols)).sum();
                if (points < 0) {
                    points = 0;
                }
                current.add("percent", ((int) ((points / (double) max) * 1000)) / 10.0);
                current.add("points", points);
            }

            YAPIONArray resultText = new YAPIONArray();
            current.add("text", resultText);

            boolean lastLong = false;
            int currentLong = 0;
            List<YAPIONObject> optionalCaesura = new ArrayList<>();
            for (TypedSymbol typedSymbol : typedSymbols) {
                YAPIONObject typedResult = new YAPIONObject();
                resultText.add(typedResult);
                typedResult.add("char", "" + typedSymbol.getSymbol().getC());
                if (typedSymbol.getType() != null) {
                    if (typedSymbol.getType() == Type.L && !lastLong) {
                        currentLong++;
                        lastLong = true;
                    } else {
                        lastLong = false;
                    }
                    typedResult.add("over", "" + typedSymbol.getType().printChar);
                }
                if (typedSymbol.getSymbol().is(SymbolModifier.REMOVED)) {
                    typedResult.add("removed", true);
                    if (typedSymbol.getSymbol().getC() == ' ') {
                        typedResult.add("under", true);
                    }
                } else if (typedSymbol.getSymbol().getC() == ' ' && lastLong && scanRule.getCaesuraPositions().contains(currentLong)) {
                    typedResult.add("over", "'");
                } else if (typedSymbol.getSymbol().getC() == ' ' && lastLong && scanRule.getOptionalCaesuraPositions().contains(currentLong)) {
                    optionalCaesura.add(typedResult);
                }
            }
            if (optionalCaesura.size() > 1) {
                optionalCaesura.forEach(yapionAnyTypes -> {
                    yapionAnyTypes.add("over", "'");
                });
            }
        }
        return resultObject;
    }

}
