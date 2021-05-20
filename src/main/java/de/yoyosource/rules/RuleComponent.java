package de.yoyosource.rules;

import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.SymbolModifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Predicate;

@RequiredArgsConstructor
@ToString
@Getter
public class RuleComponent {
    private final Predicate<Symbol> checkPredicate;
    private final SymbolModifier result;
}
