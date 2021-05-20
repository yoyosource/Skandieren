package de.yoyosource.rules;

import de.yoyosource.ScanRule;
import de.yoyosource.symbols.Symbol;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@ToString
public class Rule {
    private final RuleComponent[] ruleComponents;

    public List<List<Symbol>> apply(List<List<Symbol>> symbolList, ScanRule scanRule) {
        return null;
    }
}
