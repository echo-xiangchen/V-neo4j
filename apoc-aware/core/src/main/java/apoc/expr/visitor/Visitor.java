package apoc.expr.visitor;

import apoc.expr.composite.*;

public interface Visitor {

	void visitAnd(Conjunction conjunction);

	void visitOr(Disjunction disjunction);

	void visitNot(Negation negation);

	void visitBoolFalse(BoolFalse boolFalse);

	void visitBoolTrue(BoolTrue boolTrue);

	void visitBoolVar(BoolVar boolVar);
}
