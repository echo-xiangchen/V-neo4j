package apoc.expr.composite;

import apoc.expr.visitor.Visitor;

public class Negation extends UnaryExpr {

	public Negation(Expr expr) {
		super(expr);
	}

	public void accept(Visitor v) {
		v.visitNot(this);
	}
}
