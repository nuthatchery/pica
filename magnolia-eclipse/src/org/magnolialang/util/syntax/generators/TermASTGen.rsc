module org::magnolialang::util::syntax::generators::TermASTGen

import lang::rascal::syntax::ASTGen;
import lang::rascal::syntax::Parameters;
import org::magnolialang::compiler::Util;
import lang::rascal::syntax::Grammar2Rascal;
import ParseTree;
import Grammar;

import IO;
import String;
import List;
import Set;

data XaToken = token(str chars) | space(str chars) | comment(str chars) | child(int index) | sep(XaToken tok, list[XaToken] separator);

public set[AST] grammar2asts(set[Production] prods) {
  map[str, set[Sig]] m = ();
  set[Sig] sigs = {};
  set[AST] asts = {};
  for (p <- prods) {
    s = sortName(p);
    if (isLexical(p)) {
       asts += {leaf(s)};
    }
    else if (hasCons(p)) {
       args = productionArgsRsc(p);
       m[s]?sigs += {sig(consName(p), args)};
    }
    else {
      ;//throw "<p> is not lexical but has no cons";
    }
  }
  for (sn <- m) {
    asts += ast(sn, m[sn]);
  }
  
  return asts;
}

@doc{Returns a table mapping consnames/arity to a tuple of pretty-print information,
a syntax rule and the sort name.}
public rel[str, int, list[XaToken], str, Production] grammar2info(set[Production] prods) {
	rel[str, int, list[XaToken], str, Production] tbl = {};
	for (p <- prods) {
		s = sortName(p);
		if (hasCons(p)) {
			<toks, i> = concrete(p);
			tbl += {<consName(p), i, toks, sym2name(p.rhs), p>};
		} 
    }
  return tbl;
}


list[Arg] productionArgsRsc(Production p) {
   int i = 0;
   return for (x <- p.lhs) {
     if(label(str name, Symbol sym) := x || <name, sym> := <"arg<i>", x>) {
       a = arg("", name);
       switch (sym) {
         case \sort(str s): a.typ = "AST"; 
         case \iter(\sort(str s)): a.typ = "AST";  
         case \iter-star(\sort(str s)): a.typ = "AST";
         case \iter-seps(\sort(str s), _): a.typ = "AST";
         case \iter-star-seps(\sort(str s), _): a.typ = "AST";
         case \parameterized-sort(str s, [sort(str z)]): a.typ = "AST";
         case \iter(\parameterized-sort(str s, [sort(str z)])): a.typ = "AST";  
         case \iter-star(\parameterized-sort(str s, [sort(str z)])): a.typ = "AST";
         case \iter-seps(\parameterized-sort(str s, [sort(str z)]), _): a.typ = "AST";
         case \iter-star-seps(\parameterized-sort(str s, [sort(str z)]), _): a.typ = "AST";
       }
       if(a.typ != "") {
         i = i + 1;
         append a;   
       }
     }
   }
}

tuple[list[XaToken], int] concrete(Production p) {
	int i = 0;
	toks = for (x <- p.lhs) {
		if(label(_, Symbol sym) := x || sym := x) {
			XaToken tok = sym2token(sym, i);
       		if(child(_) := tok || sep(child(_), _) := tok)
         		i = i + 1;
      		append tok;   
     	}
	}
	return <toks, i>;
}

XaToken sym2token(Symbol sym, int childNum) {
	XaToken tok;
	switch (sym) {
		case \layouts(_): tok = \space(" ");
		case \lit(l): tok = \token(l);
		case \iter-seps(_, ll): tok = sep(child(childNum), [sym2token(l,0) | l <- ll]);
		case \iter-star-seps(_, ll): tok = sep(child(childNum), [sym2token(l,0) | l <- ll]);
		default: tok = child(childNum);
	}
	switch(tok) {
		case sep(c, [space(_)]): tok = c;
		case sep(c, [space(_), s*, space(_)]): tok = sep(c, s);
	}
	return tok;
}

str sym2name(Symbol sym) {
	switch (sym) {
    	case \sort(str s): return s;
        case \iter(\sort(str s)): return "<s>*";
        case \iter-star(\sort(str s)): return "<s>*";
        case \iter-seps(\sort(str s), _): return "<s>*";
        case \iter-star-seps(\sort(str s), _): return "<s>*";
        case \parameterized-sort(str s, [sort(str z)]): return "<s>[<z>]";
        case \iter(\parameterized-sort(str s, [sort(str z)])): return "<s>[<z>]*";
        case \iter-star(\parameterized-sort(str s, [sort(str z)])): return "<s>[<z>]*";
        case \iter-seps(\parameterized-sort(str s, [sort(str z)]), _): return "<s>[<z>]*";
        case \iter-star-seps(\parameterized-sort(str s, [sort(str z)]), _): return "<s>[<z>]*";
   }
}

public str asts2rascal(set[AST] asts) {
	result = ""; 
	for(ast(typ, sigs) <- asts) {
		result += "           // <typ>\n";
		result += "data AST = <strJoin(["<cons>(<strJoin(["<t> <a>" | arg(t, a) <- args], ", ")>)" | sig(cons, args) <- sigs], " | ")>;\n";
	}
	return result;
} 
