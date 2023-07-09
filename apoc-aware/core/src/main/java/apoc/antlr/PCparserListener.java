package apoc.antlr;

// Generated from PCparser.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PCparserParser}.
 */
public interface PCparserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PCparserParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(PCparserParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link PCparserParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(PCparserParser.StatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Not}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterNot(PCparserParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Not}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitNot(PCparserParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterOr(PCparserParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitOr(PCparserParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterAnd(PCparserParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitAnd(PCparserParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BoolFalse}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterBoolFalse(PCparserParser.BoolFalseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BoolFalse}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitBoolFalse(PCparserParser.BoolFalseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BoolTrue}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterBoolTrue(PCparserParser.BoolTrueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BoolTrue}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitBoolTrue(PCparserParser.BoolTrueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BoolVar}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterBoolVar(PCparserParser.BoolVarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BoolVar}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitBoolVar(PCparserParser.BoolVarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Paren}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void enterParen(PCparserParser.ParenContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Paren}
	 * labeled alternative in {@link PCparserParser#boolExpr}.
	 * @param ctx the parse tree
	 */
	void exitParen(PCparserParser.ParenContext ctx);
}