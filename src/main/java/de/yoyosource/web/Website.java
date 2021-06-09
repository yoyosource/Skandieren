package de.yoyosource.web;

import de.yoyosource.ScanRule;
import de.yoyosource.rules.Rule;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.TypedSymbol;
import yapion.exceptions.YAPIONException;
import yapion.hierarchy.output.StringOutput;
import yapion.hierarchy.types.YAPIONArray;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static de.yoyosource.symbols.SymbolModifier.REMOVED;
import static spark.Spark.*;

public class Website {

    private static Map<String, ScanRule> scanRuleMap = new HashMap<>();

    static {
        for (File file : new File("./src/main/resources/verseschemes").listFiles((dir, name) -> name.endsWith(".scanrule"))) {
            String name = "";
            if (!file.getName().startsWith("standard")) {
                name = file.getName().substring(0, file.getName().indexOf('.'));
            }
            try {
                scanRuleMap.put(name.toLowerCase(), new ScanRule(YAPIONParser.parse(file)));
            } catch (YAPIONException | IOException e) {
                e.printStackTrace();
            }
        }
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

            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            List<Symbol> symbolList = Symbol.toSymbols(text, scanRule);
            Rule.apply(symbolList, scanRule).forEach(symbols -> {
                List<List<TypedSymbol>> lists = TypedSymbol.create(symbols, scanRule);
                if (atomicBoolean.get() && !lists.isEmpty()) {
                    allResults.add(new YAPIONObject());
                }
                atomicBoolean.set(false);
                if (!lists.isEmpty()) {
                    atomicBoolean.set(true);
                }
                for (List<TypedSymbol> typedSymbols : lists) {
                    YAPIONObject current = new YAPIONObject();
                    allResults.add(current);

                    YAPIONArray resultText = new YAPIONArray();
                    current.add("text", resultText);

                    for (TypedSymbol typedSymbol : typedSymbols) {
                        YAPIONObject typedResult = new YAPIONObject();
                        resultText.add(typedResult);
                        typedResult.add("char", "" + typedSymbol.getSymbol().getC());
                        if (typedSymbol.getType() != null) {
                            typedResult.add("type", "" + typedSymbol.getType().printChar);
                        }
                        if (typedSymbol.getSymbol().is(REMOVED)) {
                            typedResult.add("removed", true);
                        }
                    }
                }
            });

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
