package apoc.expr.composite;

import apoc.expr.visitor.*;

public class Conjunction extends BinaryExpr {

	
	public Conjunction(Expr expr1, Expr expr2) {
		super(expr1, expr2);
	}
	
	public void accept(Visitor v) {
		v.visitAnd(this);
	}
}
