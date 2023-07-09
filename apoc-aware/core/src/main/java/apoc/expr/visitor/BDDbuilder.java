package apoc.expr.visitor;

import apoc.expr.composite.*;
import static info.scce.addlib.cudd.Cudd.*;
import java.util.*;



public class BDDbuilder implements Visitor {
	
	/* Initialize DDManager with default values */
	public static long ddManager = Cudd_Init(0, 0, CUDD_UNIQUE_SLOTS, CUDD_CACHE_SLOTS, 0);
	
	private static int varIndex = 0;
	
	// hashmap stores the variables
	private static Map<String, Long> varMap = new LinkedHashMap<String, Long>();
	
	// array list that stores all the addresses
	//private static ArrayList<Long> address = new ArrayList<Long>();
	
	// long value that stores the last address
	private static long address = 0;
	
	public BDDbuilder() {

	}
	
	public long getBDDaddress() {
		return address;
	}
	
	@Override
	public void visitAnd(Conjunction conjunction) {
		BDDbuilder left = new BDDbuilder();
		BDDbuilder right = new BDDbuilder();
		
		/* need to store the left address and right address
		 * because after calling disjunction.right().accept(right) cannot make sure that 
		 * the last two element is actually the left child's address and right child's address
		*/
		conjunction.left().accept(left);
		//long leftAddress = address.get(address.size() - 1);
		long leftAddress = address;
		
		conjunction.right().accept(right);
		//long rightAddress = address.get(address.size() - 1);
		long rightAddress = address;
		
		long and = Cudd_bddAnd(ddManager, leftAddress, rightAddress);
		
		Cudd_Ref(and);
		// store the address of this disjunction
		address = and;
		System.out.println("And: " + address);
	}

	@Override
	public void visitOr(Disjunction disjunction) {
		BDDbuilder left = new BDDbuilder();
		BDDbuilder right = new BDDbuilder();
		
		/* need to store the left address and right address
		 * because after calling disjunction.right().accept(right) cannot make sure that 
		 * the last two element is actually the left child's address and right child's address
		*/
		disjunction.left().accept(left);
		//long leftAddress = address.get(address.size() - 1);
		long leftAddress = address;
		
		disjunction.right().accept(right);
		//long rightAddress = address.get(address.size() - 1);
		long rightAddress = address;
		
		long or = Cudd_bddOr(ddManager, leftAddress, rightAddress);
		
		Cudd_Ref(or);
		// store the address of this disjunction
		//address.add(or);
		address = or;
		System.out.println("Or: " + address);
	}

	@Override
	public void visitNot(Negation negation) {
		BDDbuilder bb = new BDDbuilder();
		negation.child.accept(bb);
		
		// the last added element in the arraylist is it's child address
		//long not = Cudd_Not(address.get(address.size() - 1));
		long not = Cudd_Not(address);
		
		Cudd_Ref(not);
		
		//address.add(not);
		address = not;
		System.out.println("Not: " + address);
	}

	@Override
	public void visitBoolFalse(BoolFalse boolFalse) {
		long FF = Cudd_ReadLogicZero(ddManager);
		Cudd_Ref(FF);
		
		//address.add(FF);
		address = FF;
		System.out.println(address);
	}

	@Override
	public void visitBoolTrue(BoolTrue boolTrue) {
		long TT = Cudd_ReadOne(ddManager);
		Cudd_Ref(TT);
		
		//address.add(TT);
		address = TT;
		System.out.println(address);
	}

	@Override
	public void visitBoolVar(BoolVar boolVar) {
		// need to check if the variable has been created
		if (!varMap.containsKey(boolVar.name)) {
			// create the variable and then increment the index
			long var = Cudd_bddIthVar(ddManager, varIndex);
			varIndex++;
			Cudd_Ref(var);
			
			// store the variable to the map
			varMap.put(boolVar.name, var);
		}
		
		//address.add(varMap.get(boolVar.name));
		address = varMap.get(boolVar.name);
		System.out.println(boolVar.name + ": " + address);
	}
}
