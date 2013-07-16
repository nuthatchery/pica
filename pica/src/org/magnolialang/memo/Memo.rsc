module org::magnolialang::memo::Memo
import IO;

@reflect{Probably needed...}
@javaClass{org.magnolialang.memo.RscMemo}
public java &T memo(&T fun);



public str foo0() {
	println("foo0");
	return "bar";
}

public str foo1(str x) {
	println("foo1");
	return "bar<x>";
}

public str foo1(int x) {
	println("foo1");
	return "bar<x>";
}

public str foo2(str x, int y) {
	println("foo2");
	return "bar<x><y>";
}