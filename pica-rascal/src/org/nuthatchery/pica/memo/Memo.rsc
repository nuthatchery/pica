module org::nuthatchery::pica::memo::Memo
import IO;

@deprecated
public &T memo(&T fun) {
	return fun;
}
