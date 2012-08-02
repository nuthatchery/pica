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
