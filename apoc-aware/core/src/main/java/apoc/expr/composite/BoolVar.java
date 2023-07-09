package apoc.expr.composite;

import apoc.expr.visitor.*;

public class BoolVar extends Expr {
	public BoolVar(String name) {
		this.name = name;
		
	}
	
	public void accept(Visitor v) {
		v.visitBoolVar(this);
	}
}
