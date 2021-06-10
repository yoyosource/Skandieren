package de.yoyosource.web;

import de.yoyosource.ScanRule;
import de.yoyosource.rules.Rule;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.SymbolModifier;
import de.yoyosource.symbols.TypedSymbol;
import yapion.exceptions.YAPIONException;
import yapion.hierarchy.output.StringOutput;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.hierarchy.types.YAPIONValue;
import yapion.parser.YAPIONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Website {

    private static Map<String, ScanRule> scanRuleMap = new HashMap<>();

    static {
        YAPIONArray yapionArray = new YAPIONParser(Website.class.getResourceAsStream("/verseschemes.yapion"), true).parse().resultArray();
        yapionArray.streamValue().map(yapionValue -> (YAPIONValue<String>) yapionValue).map(YAPIONValue::get).forEach(name -> {
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
            System.out.println("Request: " + yapionObject);
            String text = yapionObject.getPlainValue("text");

            YAPIONObject resultObject = new YAPIONObject();
            String scanRuleName = yapionObject.getPlainValueOrDefault("ruleset", "").toLowerCase();
            if (!scanRuleMap.containsKey(scanRuleName)) {
                resultObject.add("unknown-author", scanRuleName);
                scanRuleName = "";
            }
            ScanRule scanRule = scanRuleMap.get(scanRuleName);

            // Skandieren
            YAPIONArray allResults = new YAPIONArray();
            resultObject.add("results", allResults);

            List<Symbol> symbolList = Symbol.toSymbols(text, scanRule);

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

            for (List<TypedSymbol> typedSymbols : lists) {
                if (typedSymbols.isEmpty()) {
                    allResults.add(new YAPIONObject());
                    continue;
                }

                YAPIONObject current = new YAPIONObject();
                allResults.add(current);
                if (!scanRule.getPercentageRules().isEmpty()) {
                    current.add("percent", "" + (int) ((scanRule.getPercentageRules().stream().mapToInt(percentage -> percentage.points(typedSymbols)).sum() / (double) max) * 100));
                }

                YAPIONArray resultText = new YAPIONArray();
                current.add("text", resultText);

                for (TypedSymbol typedSymbol : typedSymbols) {
                    YAPIONObject typedResult = new YAPIONObject();
                    resultText.add(typedResult);
                    typedResult.add("char", "" + typedSymbol.getSymbol().getC());
                    if (typedSymbol.getType() != null) {
                        typedResult.add("over", "" + typedSymbol.getType().printChar);
                    }
                    if (typedSymbol.getSymbol().is(SymbolModifier.REMOVED)) {
                        typedResult.add("removed", true);
                        if (typedSymbol.getSymbol().getC() == ' ') {
                            typedResult.add("under", true);
                        }
                    }
                }
            }

            response.status(200);
            response.type("application/json");
            // response.type("application/yapion");
            return resultObject.toJSON(new StringOutput(false)).getResult();
        });
        get("/api/rulesets", (request, response) -> {
            YAPIONObject yapionObject = new YAPIONObject();
            scanRuleMap.forEach((s, scanRule) -> {
                if (s.equals("")) {
                    s = "standard";
                }
                YAPIONObject rules = new YAPIONObject();
                // rules.add("metrical-feet", scanRule.getYapionObject().getYAPIONAnyType("metrical-feet").internalCopy());
                rules.add("types", scanRule.getYapionObject().getYAPIONAnyType("types").internalCopy());
                yapionObject.add(s, rules);
            });
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

}
