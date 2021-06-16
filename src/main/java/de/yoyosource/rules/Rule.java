package de.yoyosource.rules;

import de.yoyosource.ScanRule;
import de.yoyosource.symbols.Symbol;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@ToString
public class Rule {
    private final RuleComponent[] ruleComponents;

    public List<List<Symbol>> applySometimes(List<Symbol> symbolList) {
        List<List<Symbol>> lists = new ArrayList<>();
        lists.add(Symbol.copy(symbolList));
        for (int i = 0; i < symbolList.size(); i++) {
            List<List<Symbol>> toAdd = new ArrayList<>();
            for (List<Symbol> symbols : lists) {
                if (!test(symbols, i)) {
                    continue;
                }
                List<Symbol> copiedList = Symbol.copy(symbols);
                set(copiedList, i);
                toAdd.add(copiedList);
            }
            lists.addAll(toAdd);
        }
        return lists;
    }

    public List<Symbol> applyAlways(List<Symbol> symbols) {
        for (int i = 0; i < symbols.size(); i++) {
            if (test(symbols, i)) {
                set(symbols, i);
            }
        }
        return symbols;
    }

    private boolean test(List<Symbol> symbols, int index) {
        int i = 0;
        while (i < ruleComponents.length) {
            int access = i + index;
            if (access >= symbols.size()) {
                return false;
            }
            if (symbols.get(access).ignored()) {
                index++;
                continue;
            }
            if (!ruleComponents[i].getCheckPredicate().test(symbols.get(access))) {
                return false;
            }
            i++;
        }
        return true;
    }

    private void set(List<Symbol> symbols, int index) {
        int i = 0;
        while (i < ruleComponents.length) {
            int access = i + index;
            if (symbols.get(access).ignored()) {
                index++;
                continue;
            }
            symbols.get(access).add(ruleComponents[i].getResult());
            i++;
        }
    }

    public static Set<List<Symbol>> apply(List<Symbol> symbols, ScanRule scanRule) {
        scanRule.getAlwaysRules().forEach(rule -> rule.applyAlways(symbols));
        Set<List<Symbol>> lists = new LinkedHashSet<>();
        lists.add(symbols);
        for (Rule rule : scanRule.getSometimesRules()) {
            List<List<Symbol>> toAdd = new ArrayList<>();
            for (List<Symbol> symbolList : lists) {
                toAdd.addAll(rule.applySometimes(symbolList));
            }
            lists.addAll(toAdd);
        }
        return lists;
    }
}
