package apoc.expr.composite;

import apoc.expr.visitor.*;

public abstract class Expr {
	// variable name
	public String name;
	
	public void accept(Visitor v){};
}
