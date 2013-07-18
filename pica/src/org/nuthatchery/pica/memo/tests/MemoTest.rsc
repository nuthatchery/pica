module org::nuthatchery::pica::memo::tests::MemoTest
import org::nuthatchery::pica::memo::Memo;
import IO;


public int fun0() {
	return 1;
}

public int() fun0memo = memo(fun0);

public int fun1(int i) {
	println("fun1");
	return i + 1;
}

public int(int) fun1memo = memo(fun1);

test bool fun0test() {
	for(i <- [0..10])
		assert fun0() == fun0memo();
	return true;
}

test bool fun1test() {
	for(i <- [0..10]) {
		x = arbInt(10);
		assert fun1(x) == fun1memo(x);
	}
	return true;
}
