package apoc.expr.composite;

import apoc.expr.visitor.Visitor;

public class BoolFalse extends Const {
	public BoolFalse(String name) {
		super(name);
	}
	
	public void accept(Visitor v) {
		v.visitBoolFalse(this);
	}
}
