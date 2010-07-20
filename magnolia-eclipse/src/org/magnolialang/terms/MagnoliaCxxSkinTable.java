package org.magnolialang.terms;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.imp.pdb.facts.IList;

import static org.magnolialang.terms.TermFactory.*;

class MagnoliaCxxSkinTable {
	public static Map<String,IList> getMap() {
		Map<String,IList> tbl = new HashMap<String,IList>();

		// Stat ::= "return" Expr ";"
		tbl.put("Yield/1:Stat", vf.list(token("return"), space(" "), child(0), space(" "), token(";")));

		// SemiDecl ::= Modifier* ExprDeclarative SubClause* "{" "return" Expr ";" "}"
		tbl.put("DefDecl/4:SemiDecl", vf.list(child(0), space(" "), child(1), space(" "), child(2), space(" "), token("{"), space(" "), token("return"), space(" "), child(3), space(" "), token(";"), space(" "), token("}")));

		// Expr ::= "list" "(" {Expr ","}* ")"
		tbl.put("ListCons/1:Expr", vf.list(token("list"), space(" "), token("("), space(" "), sep(child(0),","), space(" "), token(")")));

		// AssertClause ::= "qed" "(" ")"
		tbl.put("QED/0:AssertClause", vf.list(token("qed"), space(" "), token("("), space(" "), token(")")));

		// Expr ::= Literal
		tbl.put("Literal/1:Expr", vf.list(child(0)));

		// Expr ::= Fun "(" {Expr ","}* ")"
		tbl.put("Apply/2:Expr", vf.list(child(0), space(" "), token("("), space(" "), sep(child(1),","), space(" "), token(")")));

		// Stat ::= Identifier "=" Expr ";"
		tbl.put("Assign/2:Stat", vf.list(child(0), space(" "), token("="), space(" "), child(1), space(" "), token(";")));

		// Stat ::= "while" "(" Expr ")" "{" Stat* "}"
		tbl.put("While/2:Stat", vf.list(token("while"), space(" "), token("("), space(" "), child(0), space(" "), token(")"), space(" "), token("{"), space(" "), child(1), space(" "), token("}")));

		// TypeClause ::= "struct" TypeIdentifier
		tbl.put("TypeClause/1:TypeClause", vf.list(token("struct"), space(" "), child(0)));

		// Name ::= ID
		tbl.put("Name/1:Name", vf.list(child(0)));

		// PredClause ::= "predicate" FunIdentifier FunctionParamList
		tbl.put("PredClause/2:PredClause", vf.list(token("predicate"), space(" "), child(0), space(" "), child(1)));

		// BraceDecl ::= Modifier* DeclDeclarative SubClause* DeclBody
		tbl.put("DefDecl/4:BraceDecl", vf.list(child(0), space(" "), child(1), space(" "), child(2), space(" "), child(3)));

		// Literal ::= OctNumeral
		tbl.put("Oct/1:Literal", vf.list(child(0)));

		// BlockStat ::= "{" Stat* "}"
		tbl.put("Block/1:BlockStat", vf.list(token("{"), space(" "), child(0), space(" "), token("}")));

		// AssertClause ::= "," Expr
		tbl.put("By/1:AssertClause", vf.list(token(","), space(" "), child(0)));

		// Var ::= Name
		tbl.put("Var/1:Var", vf.list(child(0)));

		// Literal ::= FloatNumeral
		tbl.put("Real/1:Literal", vf.list(child(0)));

		// Decl ::= Modifier* ExprDeclarative SubClause* ";"
		tbl.put("NoDefDecl/3:Decl", vf.list(child(0), space(" "), child(1), space(" "), child(2), space(" "), token(";")));

		// LetClause ::= Type Identifier "=" Expr ";"
		tbl.put("VarDef/3:LetClause", vf.list(child(0), space(" "), child(1), space(" "), token("="), space(" "), child(2), space(" "), token(";")));

		// Literal ::= HexNumeral
		tbl.put("Hex/1:Literal", vf.list(child(0)));

		// Decl ::= Modifier* "typedef" TypeDeclarative SubClause* "=" Type ";"
		tbl.put("DefDecl/4:Decl", vf.list(child(0), space(" "), token("typedef"), space(" "), child(1), space(" "), child(2), space(" "), token("="), space(" "), child(3), space(" "), token(";")));

		// Stat ::= "break" ";"
		tbl.put("Break/0:Stat", vf.list(token("break"), space(" "), token(";")));

		// Stat ::= Proc "(" {Expr ","}* ")" ";"
		tbl.put("Call/2:Stat", vf.list(child(0), space(" "), token("("), space(" "), sep(child(1),","), space(" "), token(")"), space(" "), token(";")));

		// Stat ::= "if" "(" Expr ")" "{" Stat* "}" "else" "{" Stat* "}"
		tbl.put("If/3:Stat", vf.list(token("if"), space(" "), token("("), space(" "), child(0), space(" "), token(")"), space(" "), token("{"), space(" "), child(1), space(" "), token("}"), space(" "), token("else"), space(" "), token("{"), space(" "), child(2), space(" "), token("}")));

		// ProcedureParamList ::= "(" {ProcedureParam ","}* ")"
		tbl.put("Dummy/1:ProcedureParamList", vf.list(token("("), space(" "), sep(child(0),","), space(" "), token(")")));

		// Type ::= "void"
		tbl.put("NilType/0:Type", vf.list(token("void")));

		// Expr ::= Expr "in" Expr
		tbl.put("In/2:Expr", vf.list(child(0), space(" "), token("in"), space(" "), child(1)));

		// AxiomClause ::= "axiom" Identifier FunctionParamList
		tbl.put("AxiomClause/2:AxiomClause", vf.list(token("axiom"), space(" "), child(0), space(" "), child(1)));

		// Name ::= Name "::" ID
		tbl.put("QName/2:Name", vf.list(child(0), space(" "), token("::"), space(" "), child(1)));

		// Fun ::= FunName
		tbl.put("Fun/1:Fun", vf.list(child(0)));

		// ProcedureParam ::= Type Identifier
		tbl.put("ObsParam/2:ProcedureParam", vf.list(child(0), space(" "), child(1)));

		// FunctionParamList ::= "(" {FunctionParam ","}* ")"
		tbl.put("Dummy/1:FunctionParamList", vf.list(token("("), space(" "), sep(child(0),","), space(" "), token(")")));

		// Expr ::= Expr "." DecNumeral
		tbl.put("DotOp/2:Expr", vf.list(child(0), space(" "), token("."), space(" "), child(1)));

		// Expr ::= "_"
		tbl.put("Undefined/0:Expr", vf.list(token("_")));

		// Stat ::= ";"
		tbl.put("Nop/0:Stat", vf.list(token(";")));

		// Literal ::= BinNumeral
		tbl.put("Bin/1:Literal", vf.list(child(0)));

		// Expr ::= "list" "(" {Expr ","}* "," Expr ")"
		tbl.put("ListCons/2:Expr", vf.list(token("list"), space(" "), token("("), space(" "), sep(child(0),","), space(" "), token(","), space(" "), child(1), space(" "), token(")")));

		// Stat ::= "return" ";"
		tbl.put("Return/0:Stat", vf.list(token("return"), space(" "), token(";")));

		// Type ::= "struct" DeclBody
		tbl.put("Struct/1:Type", vf.list(token("struct"), space(" "), child(0)));

		// Expr ::= Expr "[" {Expr ","}* "]"
		tbl.put("Index/2:Expr", vf.list(child(0), space(" "), token("["), space(" "), sep(child(1),","), space(" "), token("]")));

		// DeclBody ::= "{" Decl* "}"
		tbl.put("DeclBody/1:DeclBody", vf.list(token("{"), space(" "), child(0), space(" "), token("}")));

		// Type ::= Name
		tbl.put("Type/1:Type", vf.list(child(0)));

		// Proc ::= ProcName
		tbl.put("Proc/1:Proc", vf.list(child(0)));

		// ProcClause ::= "void" ProcIdentifier ProcedureParamList
		tbl.put("ProcClause/2:ProcClause", vf.list(token("void"), space(" "), child(0), space(" "), child(1)));

		// StringLiteral ::= """ String """
		tbl.put("String/1:StringLiteral", vf.list(token("\""), space(" "), child(0), space(" "), token("\"")));

		// Identifier ::= ID
		tbl.put("Name/1:Identifier", vf.list(child(0)));

		// VarClause ::= Type VarIdentifier
		tbl.put("VarClause/2:VarClause", vf.list(child(0), space(" "), child(1)));

		// Expr ::= Expr "not" "in" Expr
		tbl.put("NotIn/2:Expr", vf.list(child(0), space(" "), token("not"), space(" "), token("in"), space(" "), child(1)));

		// ProcedureParam ::= Type
		tbl.put("AnonParam/1:ProcedureParam", vf.list(child(0)));

		// Stat ::= "{" LetClause* Stat* "}"
		tbl.put("Let/2:Stat", vf.list(token("{"), space(" "), child(0), space(" "), child(1), space(" "), token("}")));

		// Program ::= Decl*
		tbl.put("CxxTree/1:Program", vf.list(child(0)));

		// FunctionParam ::= Type VarIdentifier
		tbl.put("Param/2:FunctionParam", vf.list(child(0), space(" "), child(1)));

		// ProcIdentifier ::= "operator" "="
		tbl.put("Assign/0:ProcIdentifier", vf.list(token("operator"), space(" "), token("=")));

		// Expr ::= LGNOT Expr
		tbl.put("PreOp/2:Expr", vf.list(child(0), space(" "), child(1)));

		// FunClause ::= Type FunIdentifier FunctionParamList
		tbl.put("FunClause/3:FunClause", vf.list(child(0), space(" "), child(1), space(" "), child(2)));

		// Literal ::= DecNumeral
		tbl.put("Int/1:Literal", vf.list(child(0)));

		// Stat ::= "assert" "(" Expr AssertClause* ")" ";"
		tbl.put("Assert/2:Stat", vf.list(token("assert"), space(" "), token("("), space(" "), child(0), space(" "), child(1), space(" "), token(")"), space(" "), token(";")));

		// ProcedureParam ::= Type VarIdentifier
		tbl.put("Param/2:ProcedureParam", vf.list(child(0), space(" "), child(1)));

		// Stat ::= "for" "(" Decl Expr ";" Expr ")"
		tbl.put("CxxFor/3:Stat", vf.list(token("for"), space(" "), token("("), space(" "), child(0), space(" "), child(1), space(" "), token(";"), space(" "), child(2), space(" "), token(")")));

		// Expr ::= Expr LGIMP Expr
		tbl.put("BinOp/3:Expr", vf.list(child(0), space(" "), child(1), space(" "), child(2)));

		// Expr ::= Expr "?" Expr ":" Expr
		tbl.put("IfThenElseExpr/3:Expr", vf.list(child(0), space(" "), token("?"), space(" "), child(1), space(" "), token(":"), space(" "), child(2)));

		// GuardClause ::= "guard" Expr
		tbl.put("Guard/1:GuardClause", vf.list(token("guard"), space(" "), child(0)));

		return tbl;
	}
}
