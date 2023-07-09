package apoc.path;

import static info.scce.addlib.cudd.Cudd.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import apoc.antlr.PCparserLexer;
import apoc.antlr.PCparserParser;
import apoc.expr.Antlr2Expr;
import apoc.expr.visitor.BDDbuilder;

public class BDDMapper {

	 	// create static antlr2Expr and bddBuilder object
	 	private Antlr2Expr antlr2Expr;
	 	
	 	private PCparserLexer lexer;
	    private PCparserParser parser;
	    
	    public BDDMapper(Antlr2Expr antlr2Expr) {
			//this.bddBuilder = bddBuilder;
			this.antlr2Expr = antlr2Expr;
			
			lexer = new PCparserLexer(null);
	        parser = new PCparserParser(null);
		}
	    
	    /**
	     * Parse the specific presence condition, and add it to bddMap
	     * @author Xiang Chen
	     * @since 2021.9
	     * @param pc the String of presence condition
	     */
	    public long pcToBDD(String pc) {
	    	/* this pc has not been parsed before */
	    	if (!AwarePathExplorer.exprMap.containsKey(pc)) {
	    		// parse pc
	    		CharStream input = CharStreams.fromString(pc);
				lexer = new PCparserLexer(input);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				parser = new PCparserParser(tokens);
		        parser.setBuildParseTree(true);      // tell ANTLR to build a parse tree
		        ParseTree tree = parser.stat(); // parse
		        
		        // generate the BDD value for this pc
		        Long bddValue = antlr2Expr.visit(tree.getChild(0));

		        // add it to exprMap
		        AwarePathExplorer.exprMap.put(pc, bddValue);
		        return bddValue;
			} else { /* this pc has already been parsed before */
				return AwarePathExplorer.exprMap.get(pc);
			}
		}
	    
	    /**
	     * check SAT on the path
	     * @author Xiang Chen
	     * @since 2021.12
	     */
	    public boolean checkPathSAT(Path path) {
	    	// get the iterator for the path
	    	Iterator<Entity> entities = path.iterator();
	    	
	    	String nextPC = (String) entities.next().getProperty("condition", "True");
	    	
	    	// accumulative BDD of the path
	    	long accBDD;
	    	// BDD for next pc
	    	long nextPCBDD;
	    	
	    	// check to see if "nextPC" is true
        	if (nextPC.isEmpty() || nextPC.equals("True") || nextPC.equals("true") || nextPC.equals("TRUE")) {
        		accBDD = AwarePathExplorer.TT;
        	} else { /* nextPC is not empty and is not true, call pcToBDD to get its BDD value */
        		accBDD = pcToBDD(nextPC);
			}

	    	while (entities.hasNext()) {
	    		nextPC = (String) entities.next().getProperty("condition", "True");
	    		
	    		// check to see if "nextPC" is true
        		if (nextPC.isEmpty() || nextPC.equals("True") || nextPC.equals("true") || nextPC.equals("TRUE")) {
        			continue;
        		} else {/* nextPC is not empty and is not true, call pcToBDD to get its BDD value */
        			nextPCBDD = pcToBDD(nextPC);
        			
        			if (accBDD == nextPCBDD) {
						continue;
					} else {
						accBDD = Cudd_bddAnd(AwarePathExplorer.ddManager, accBDD, nextPCBDD);
						Cudd_Ref(accBDD);
						
						if (accBDD == AwarePathExplorer.FF) {
							return false;
						}
					}
				}
			}
	    	return (accBDD != AwarePathExplorer.FF);
	    }

	    /**
	     * Parse the specific presence condition, and add it to bddMap
	     * @author Xiang Chen
	     * @since 2021.9
	     * @param pc the String of presence condition
	     */
	    public void parsePC(String pc) {
	    	//ANTLRInputStream input = new ANTLRInputStream(pc);
	    	CharStream input = CharStreams.fromString(pc);
			lexer = new PCparserLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new PCparserParser(tokens);
	        parser.setBuildParseTree(true);      // tell ANTLR to build a parse tree
	        ParseTree tree = parser.stat(); // parse
	        
	        // generate the Expr hierarchy for initial string
	        Long bddValue = antlr2Expr.visit(tree.getChild(0));

	        AwarePathExplorer.exprMap.put(pc, bddValue);
		}
	    
	    /**
	     * perform SAT check on the path
	     * @author Xiang Chen
	     * @since 2021.12
	     */
	    public void parsePath(Path path) {
	    	// get the iterator for the path
	    	Iterator<Entity> entities = path.iterator();
	    	
	    	String nextPC = (String) entities.next().getProperty("condition", "True");
	    	// also need to check if the value of "condition" attribute is empty string
        	if (nextPC.isEmpty()) {
        		nextPC = "True";
        	}
	    	
	    	// if the bddMap does not contains current PC, it means this PC has not been parsed before
			// then parse it and add it to the map
			if (!AwarePathExplorer.exprMap.containsKey(nextPC)) {
				parsePC(nextPC);
			}
			
			long result = AwarePathExplorer.exprMap.get(nextPC);
	    	
	    	while (entities.hasNext()) {
	    		nextPC = (String) entities.next().getProperty("condition", "True");
	    		// also need to check if the value of "condition" attribute is empty string
        		if (nextPC.isEmpty()) {
        			nextPC = "True";
        		}
	    		// if the bddMap does not contains current PC, it means this PC has not been parsed before
	    		// then parse it and add it to the map
	    		if (!AwarePathExplorer.exprMap.containsKey(nextPC)) {
	    			parsePC(nextPC);
	    		}
	    		
				result = Cudd_bddAnd(AwarePathExplorer.ddManager, result, AwarePathExplorer.exprMap.get(nextPC));
				// added 2023.1.20, maybe need to ref the bdd conjunction
				Cudd_Ref(result);
			}
	    	// add the <path, bddAddress> to the map
	    	AwarePathExplorer.pathMap.put(path, result);
	    }
	    
	    public boolean checkSAT(Path path, Node node) {
	    	// get the big conjunction of current path's pc's BDDs
	        if (!AwarePathExplorer.pathMap.containsKey(path)) {
	        	parsePath(path);
			}
			
			// get the pc for this node
	        String pc = (String) node.getProperty("condition", "True");
	        // also need to check if the value of "condition" attribute is empty string
        	if (pc.isEmpty()) {
        		pc = "True";
        	}
	        
	        if (!AwarePathExplorer.exprMap.containsKey(pc)) {
				parsePC(pc);
			}
	        
	        /*
	         * check SAT and return the result
	         */
	        long result = Cudd_bddAnd(AwarePathExplorer.ddManager, AwarePathExplorer.pathMap.get(path), AwarePathExplorer.exprMap.get(pc));
	        // added 2023.1.20, maybe need to ref the bdd conjunction
	     	Cudd_Ref(result);
	        return (result != AwarePathExplorer.FF);
	    }
	    
	    /**
	     * check SAT and create Iterator<Relationship>
	     * @author Xiang Chen
	     * @since 2021.12
	     */
	    public Iterable<Relationship> checkSATandGetRelationships (Iterable<Relationship> relationships, long pathConjunc){
	    	List<Relationship> reIterator = new ArrayList<Relationship>();
	    	
	    	String currentPC;
	    	for (Relationship relationship : relationships) {
	    		currentPC = (String) relationship.getProperty("condition", "True");
	    		// also need to check if the value of "condition" attribute is empty string
        		if (currentPC.isEmpty()) {
        			currentPC = "True";
        		}
	    		
	    		// if the bddMap does not contains current PC, it means this PC has not been parsed before
	    		// then parse it and add it to the map
	    		if (!AwarePathExplorer.exprMap.containsKey(currentPC)) {
					parsePC(currentPC);
				}
	    		long conjuncValue = Cudd_bddAnd(AwarePathExplorer.ddManager, pathConjunc, AwarePathExplorer.exprMap.get(currentPC));
	    		// added 2023.1.20, maybe need to ref the bdd conjunction
		     	Cudd_Ref(conjuncValue);
				
		     	// add the relationship to the relationship list if the conjunction is satisfiable (!= FF)
		     	if (conjuncValue != AwarePathExplorer.FF) {
					reIterator.add(relationship);
				}
			}
	    	return reIterator;
	    }
}
