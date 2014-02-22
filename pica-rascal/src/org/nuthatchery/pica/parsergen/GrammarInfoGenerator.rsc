module org::nuthatchery::pica::parsergen::GrammarInfoGenerator

import lang::rascal::grammar::definition::Parameters;
import lang::rascal::grammar::definition::Productions;
import org::magnolialang::compiler::Util;
import ParseTree;
import Grammar;

import IO;
import String;
import List;
import Set;
import Map;


data XaToken = token(str chars) | space(str chars) | comment(str chars) | child(int index) | sep(XaToken tok, list[XaToken] separator);


@doc{Returns a table mapping consnames/arity to a tuple of pretty-print information,
a syntax rule and the sort name + a string of AST declarations for the grammar + a string of PP functions + a string of Nuthatch pattern builders.}
public tuple[rel[str, int, list[XaToken], str, Production], str, str, str] grammar2info(Grammar g) {
	rel[str, int, list[XaToken], str, Production] tbl = {};
	set[str] astDecl = {};
	set[str] ppStrDecl = {};
	set[str] ppTokensDecl = {};
	set[str] patBuilders = {};
	top-down-break visit(g) {
		case p:prod(def, syms, attrs): {
			if(label(name, sym) := def) {
				<toks, i> = concrete(syms);
				tbl += {<name, i, toks, sym2name(def), p>};

				astDecl += "data AST = <name>(<strJoin(["<t> <a>" | <t, a> <- prodArgs(syms)], ", ")>); // <sym>\n";
				ppStrDecl += "public str pp(<name>(<strJoin(["<a>" | <t, a> <- prodArgs(syms)], ", ")>)) {\n"
						+ "  return \"<mkStrPP(toks)>\";\n"
						+ "}\n\n";
				ppTokensDecl += "public Tseq pp(<name>(<strJoin(["<a>" | <t, a> <- prodArgs(syms)], ", ")>), Tseq stream) {\n"
						+ "  return ast2stream(stream, pp<mkTokensPP(toks)>);\n"
						+ "}\n\n";
				patBuilders += "\tpublic static Pattern\<IValue, Type\> <name>("
						+ "<strJoin(["Pattern\<IValue, Type\> <a>" | <t, a> <- prodArgs(syms)], ", ")>) {\n"
						+ "\t\treturn pf.cons(TermFactory.consType(\"<name>\", <size(prodArgs(syms))>)<strJoin([", <a>" | <t, a> <- prodArgs(syms)])>);\n"
						+ "\t}\n\n";
						;
			}
		}
	}

	return <tbl, strJoin(sort(toList(astDecl)), ""), strJoin(sort(toList(ppTokensDecl)), ""), strJoin(sort(toList(patBuilders)), "")>;
}



list[tuple[str,str]] prodArgs(list[Symbol] syms) {
	int i = 0;
	return for (x <- syms) {
		if(label(str name, Symbol sym) := x || <name, sym> := <"arg<i>", x>) {
     		switch(sym) {
     			case \lit(_):	;
     			case \layouts(_): ;
	   			default: {
     				i += 1;
					append <"AST", name>;
				}
			} 
		}
	}
}

tuple[list[XaToken], int] concrete(list[Symbol] syms) {
	int i = 0;
	toks = for (x <- syms) {
		if(label(_, Symbol sym) := x || sym := x) {
			XaToken tok = sym2token(sym, i);
       		if(child(_) := tok || sep(child(_), _) := tok)
         		i = i + 1;
      		append tok;   
     	}
	}
	return <toks, i>;
}

str mkTokensPP(list[XaToken] toks) {
	str pp = "";
	for(x <- toks) {
		switch(x) {
			case child(i):
				pp += ", arg<i>";
			case token(s):
				pp += ", Text(\"<visit(s) { case /<c:[\<\>\\]>/ => "\\<c>" }>\")";
			case space(s):
				pp += ", Space(\"<visit(s) { case /<c:[\<\>\\]>/ => "\\<c>" }>\")";
			case sep(child(i),s): 
				pp += ", SepBy(arg<i><mkTokensPP(s)>)";
/*			case child(i):
				pp += "  stream = pp(arg<i>, stream);\n";
			case token(s):
				pp += "  stream = put(Text(\"<visit(s) { case /<c:[\<\>\\]>/ => "\\<c>" }>\"), stream);\n";
			case space(s):
				pp += "  stream = put(Space(\"<visit(s) { case /<c:[\<\>\\]>/ => "\\<c>" }>\"));\n";
			case sep(child(i),s): {
				pp += "  for(i \<- index(arg<i>.args)) {\n";
				pp += "    if(i \> 0) {\n  <mkTokensPP(s)>    }\n";
				pp += "    stream = pp(arg<i>.args[i], stream);\n";
				pp += "  }\n";
			}
*/			default:
				throw "Unknown token <x>";	
		}
	}
	return pp;
}
str mkStrPP(list[XaToken] toks) {
	str pp = "";
	for(x <- toks) {
		switch(x) {
			case child(i):
				pp += "\<pp(arg<i>)\>";
			case token(s):
				pp += visit(s) { case /<c:[\<\>\\]>/ => "\\<c>" }
			case space(s):
				pp += s;
			case sep(child(i),s):
				pp += "\<sepBy([pp(x) | x \<- arg<i>.args], \"<mkStrPP(s)>\")\>";
			default:
				throw "Unknown token <x>";	
		}
	}
	return pp;
}

XaToken sym2token(Symbol sym, int childNum) {
	XaToken tok;
	switch (sym) {
		case \label(_, s):
			return sym2token(s, childNum);
		case \regular(s):
			return sym2token(s, childNum);
		case \layouts(_):
			tok = \space(" ");
		case \lit(l):
			tok = \token(l);
		case \iter-seps(_, ll):
			tok = sep(child(childNum), [sym2token(l,0) | l <- ll]);
		case \iter-star-seps(_, ll):
			tok = sep(child(childNum), [sym2token(l,0) | l <- ll]);
		default:
			tok = child(childNum);
	}
	switch(tok) {
		case sep(c, [space(_)]):
			tok = c;
		case sep(c, [space(_), s*, space(_)]):
			tok = sep(c, s);
	}
	return tok;
}

str sym2name(Symbol sym) {
	switch (sym) {
	case \label(_, s): return sym2name(s);
	case \regular(s): return sym2name(s);
    	case \sort(str s): return s;
    	case \lex(str s): return s;
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
   throw "Unexpected symbol <sym>";
}

