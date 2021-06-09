package de.yoyosource;

import de.yoyosource.rules.Rule;
import de.yoyosource.symbols.Symbol;
import de.yoyosource.symbols.TypedSymbol;
import yapion.hierarchy.types.YAPIONObject;
import yapion.parser.YAPIONParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestSentences {

    public static void main(String[] args) throws Exception {
        String[] text = new String[]{
                "aethera digreditur Ciconumque Hymenaeus ad oras",
                "Tendit et orpheaque Hymenaeus vocatur",
                "Inde Hymenaeus coceo velatus amictu",
                "Tendit Ciconumque Hymenaeus et vocatur",
                "Tendit Ciconumque Hymenaeus ad vocatur",
                "Inde per digreditur Ciconumque Hymenaeus vocatur",
                "orphea vocatur est Ciconumque Hymenaeus ad oras",
                "Hanc quoque phoebus amat positaque in stipite dextra",
                "tum quoque visa decens: nudabant corpora venti,",
                "fugit cumque ipso verba imperfecta reliquit;",
                "obviaque adversas vibrabant flamina vestes,",
                "imminet et crinem sparsum cervicibus afflat.",
                "ei mihi, quod nullis amor est sanabilis herbis,",
                "et levis impulsos retro dabat aura capillos;",
                "auctaque forma fuga est. Sed enim non sustinet ultra",
                "perdere blanditias iuvenis deus, utque movebat",
                "ipse amor, admisso sequitur vestigia passu.",
                "Ut canis in vacuo leporem cum Gallicus arvo",
                "vidit et hic praedam pedibus petit, ille salutem,",
                "sic deus et virgo: est hic spe celer, illa timore.",
                "Qui tamen insequitur, pennis adiutus amoris",
                "\"Fer pater\", inquit, \"opem! tellus\", ait, \"hisce vel istam,",
                "Inde latet silvis nulloque in monte videtur,",
                "In nova fert animus mutatas dicere formas",
                "corpora. Di, coeptis - nam vos mutastis et illas -",
                "aspirate meis primaque ab origine mundi",
                "ad mea perpetuum deducite tempora carmen.",
                "quae facit, ut laedar, mutando perde figuram!\"",
                "te coma, te citharae, te nostrae, laura, pharetrae",
                "Utque meum intonsis caput est iuvenale capillis,",
                "Dixerat Anchises natumque unaque Sibyllam",
                "conventus trahit in medios turbamque sonantem",
                "et tumulum capit unde omnis longo ordine posset",
                "adversos legere et venientum discere vultus.",
                "Germanum fugiens. Longa est iniuria longae",
                "Pygmalion, scelere ante alios immanior omnis"
        };

        YAPIONObject yapionObject = YAPIONParser.parse(new File("./src/main/resources/verseschemes/standard.scanrule"));
        ScanRule scanRule = new ScanRule(yapionObject);
        List<String> failed = new ArrayList<>();

        for (String s : text) {
            List<Symbol> symbolList = Symbol.toSymbols(s, scanRule);
            List<List<TypedSymbol>> result = new ArrayList<>();
            System.out.println("");
            System.out.println(s);
            Rule.apply(symbolList, scanRule).forEach(symbols -> {
                List<List<TypedSymbol>> lists = TypedSymbol.create(symbols, scanRule);
                lists.forEach(typedSymbols -> System.out.println(typedSymbols.stream().map(TypedSymbol::toString).collect(Collectors.joining())));
                result.addAll(lists);
            });
            if (result.isEmpty()) {
                failed.add(s);
            }
        }

        if (!failed.isEmpty()) {
            System.out.println();
            System.out.println("Failed");
            failed.forEach(System.out::println);
        }
    }

}
