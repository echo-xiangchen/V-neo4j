package apoc.expr.composite;

import apoc.expr.visitor.Visitor;

public class Disjunction extends BinaryExpr {
	public Disjunction(Expr expr1, Expr expr2) {
		super(expr1, expr2);
	}
	
	public void accept(Visitor v) {
		v.visitOr(this);
	}
}
