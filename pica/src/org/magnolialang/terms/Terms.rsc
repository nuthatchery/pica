module org::magnolialang::terms::Terms

import ParseTree;
import Node;
import List;

data AST = leaf(str strVal) | var(str name) | seq(list[AST] args);

data XaToken = token(str chars) | space(str chars) | comment(str chars) | child(int index) | sep(XaToken tok, list[XaToken] separator);
anno loc AST@\loc;
anno loc AST@\from;
anno list[XaToken] AST@concrete;
anno str AST@category;

@doc{Implode a parse tree to a term}
@javaClass{org.magnolialang.terms.Terms}
public java AST implode(Tree tree);

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
public java str unparse(AST tree, str skin, bool fallback);

@doc{Convert a term to a string.}
public str yieldTerm(value tree) {
	return yieldTerm(tree, true);
}

@doc{Convert from value() type.}
@javaClass{org.magnolialang.terms.Terms}
public java &T fromValue(value v, &T t);

public AST astValue(value v) {
	if(AST a := v)
		return a;
	else
		throw "not an AST: <v>";
}

@doc{Set a child of a node, with functional update}
@javaClass{org.magnolialang.terms.Terms}
public java &T <: node setChild(&T <: node n, int i, value v);

public &T <: node mapAll(&T (&T) f, &T <: node x)
{
	a = arity(x);
	i = 0;
	while (i < a) {
		v = x[i];
		if (&T tv := v) {
			x = setChild(x, i, f(tv));
		}
		i += 1;
	}
	return x;
}

public &T <: node mapAllList(&T (&T) f, &T <: node x)
{
	a = arity(x);
	i = 0;
	while (i < a) {
		v = x[i];
		if (&T tv := v) {
			x = setChild(x, i, f(tv));
		} else if (list[&T] lst := v) {
			x = setChild(x, i, [ ((&T tc := c) ? f(tc) : c) | c <- lst ]);
		}
		i += 1;
	}
	return x;
}

@doc{Convert a term to a string.}
@javaClass{org.magnolialang.terms.Terms}
public java str yieldTerm(value tree, bool withAnnos);

@doc{Convert a term to a string but leave var() as a base pattern variable.}
@javaClass{org.magnolialang.terms.Terms}
public java str yieldTermPattern(value tree);

@doc{Print a list of values on the output stream.}
@javaClass{org.magnolialang.terms.Terms}
public java void termPrintln(value V...);

public &T debugTerm(&T tree) {
	termPrintln("DEBUG", tree);
	return tree;
}

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

public str consIdOf(AST term) {
	return "<getName(term)>/<arityOf(term)>";
}

public int arityOf(AST term) {
	if(var(_) := term || leaf(_) := term)
		return 0;
	else if(seq(s) := term)
		return size(s);
	else
		return size(getChildren(term));
}