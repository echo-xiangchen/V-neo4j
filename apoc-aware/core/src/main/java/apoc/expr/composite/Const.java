package apoc.expr.composite;

public abstract class Const extends Expr {
	// used for normal constant declaration
	public Const(String name) {
		this.name = name;
	}
}
