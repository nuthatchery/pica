module org::magnolialang::terms::Terms

import ParseTree;
import Node;
import List;

data AST = leaf(str strVal) | var(str name) | seq(list[AST] args);

data XaToken = token(str chars) | space(str chars) | comment(str chars) | child(int index) | sep(XaToken tok, str chars);
anno loc AST@\loc;
anno list[XaToken] AST@concrete;

@doc{Implode a parse tree to a term}
@javaClass{org.magnolialang.terms.Terms}
public AST java implode(Tree tree);

@doc{Unparse a term to a string}
public str unparse(AST tree) {
	return unparse(tree, "", false);
}

@doc{Unparse a term to a string. Fallback to concrete annotations if fallback is true.}
public str unparse(AST tree, bool fallback) {
	return unparse(tree, "", fallback);
}

@doc{Unparse a term to a string}
public str unparse(AST tree, str skin) {
	return unparse(tree, skin, false);
}

@doc{Unparse a term to a string. Fallback to concrete annotations if fallback is true.}
@javaClass{org.magnolialang.terms.Terms}
public str java unparse(AST tree, str skin, bool fallback);

@doc{Convert a term to a string.}
public str yieldTerm(value tree) {
	return yieldTerm(tree, true);
}

@doc{Convert a term to a string.}
@javaClass{org.magnolialang.terms.Terms}
public str java yieldTerm(value tree, bool withAnnos);

@doc{Print a list of values on the output stream.}
@javaClass{org.magnolialang.terms.Terms}
public void java termPrintln(value V...);

@doc{Print and return true, for debugging complex expressions}
public bool termPrint(value V...) 
{
  termPrintln(V);
  return true;
}

public bool isTerm(value v) {
	return AST a := v;
}

public bool isCons(value v) {
	return (node n := v) && !(seq(_) := v) && !(var(_) := v) && !(leaf(_) := v);
}

public bool isVar(value v) {
	return var(_) := v;
}

public bool isSeq(value v) {
	return seq(_) := v;
}

public bool isLeaf(value v) {
	return leaf(_) := v;
}

public str consOf(AST term) {
	return getName(term);
}

public int arityOf(AST term) {
	if(var(_) := term || leaf(_) := term)
		return 0;
	else if(seq(s) := term)
		return size(s);
	else
		return size(getChildren(term));
}