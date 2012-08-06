module org::magnolialang::memo::Resources
import org::magnolialang::terms::Terms;

alias Resources = value;

@reflect{Throwing exceptions}
@javaClass{org.magnolialang.memo.RscResources}
public java loc getModuleLoc(AST moduleName, Resources res);

@reflect{Throwing exceptions}
@javaClass{org.magnolialang.memo.RscResources}
public java AST getModuleName(loc moduleLoc, Resources res);

@reflect{Throwing exceptions}
@javaClass{org.magnolialang.memo.RscResources}
public java AST getModuleAST(AST moduleName, Resources res);

@reflect{Throwing exceptions}
@javaClass{org.magnolialang.memo.RscResources}
public java Resources getProjectResources(str projectName);

@reflect{Throwing exceptions}
@javaClass{org.magnolialang.memo.RscResources}
public java void initializeMagnolia();

public boolean moduleNameLocAxiom(AST moduleName, Resources res) {
	return getModuleName(getModuleLoc(moduleName, res), res) == moduleName;
}
public boolean moduleLocNameAxiom(loc moduleLoc, Resources res) {
	return getModuleLoc(getModuleName(moduleLoc, res), res) == moduleLoc;
}
