package apoc.path;

import apoc.algo.Cover;
import apoc.result.GraphResult;
import apoc.result.MapResult;
import apoc.result.NodeResult;
import apoc.result.PathResult;
import apoc.util.Util;

import org.neo4j.graphalgo.impl.util.PathImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.internal.helpers.collection.Iterables;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.UserFunction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static apoc.path.AwarePathExplorer.NodeFilter.*;
import static info.scce.addlib.cudd.Cudd.Cudd_bddAnd;

import apoc.expr.Antlr2Expr;
import apoc.expr.visitor.*;
import static info.scce.addlib.cudd.Cudd.*;

public class AwarePathExplorer {
	public static final Uniqueness UNIQUENESS = Uniqueness.RELATIONSHIP_PATH;
	public static final boolean BFS = true;
	
	/* Initialize DDManager with default values */
	public static long ddManager = Cudd_Init(0, 0, CUDD_UNIQUE_SLOTS, CUDD_CACHE_SLOTS, 0);
	
	/* true and false BDD*/
	public static long FF = Cudd_ReadLogicZero(ddManager);
	public static long TT = Cudd_ReadOne(ddManager);
	
	// hashmap stores the <pc, bddAddress> Map
	public static Map<String, Long> exprMap = new HashMap<>();
	// hashmap stores the <path, bddAddress> Map
	public static Map<Path, Long> pathMap = new HashMap<>();
	
	/* index for BDD variables */
	public static int varIndex = 0;
	
	@Context
    public Transaction tx;

	@Context
    public Log log;
	
	
	// add procedure name
    @Procedure("aware.cypher.run")
    @Description("aware.cypher.run(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareCypherRun(@Name("query") String query) {
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.directICBC")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointDirectICBC() {
    	String query = "MATCH path=(a:cVariable)-[:parWrite]->(b:cVariable) WHERE a.comp <> b.comp RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
//    @Procedure("aware.endpoint.indirectICBC")
//    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
//    public Stream<MapResult> awareEndpointIndirectICBC() {
//    	String query = "MATCH path=(:cVariable)-[:parWrite]->(:cVariable)-[:intraICBC]->(:cVariable)-[:parWrite]->(:cVariable) "
//    			+ "WITH path, nodes(path) AS nodes\n"
//    			+ "WHERE nodes[0].comp <> nodes[1].comp AND nodes[0].comp <> nodes[2].comp AND nodes[0].comp <> nodes[3].comp AND nodes[1].comp <> nodes[3].comp AND\n"
//    			+ "nodes[2].comp <> nodes[3].comp RETURN path";
//    	Result result = tx.execute(query);
//    	
//    	// initialize antlr2Expr and bddbuilder for parsing
//    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
//    	
//    	// initialize BDDMapper
//    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
//    			
//    	Predicate<Map<String, Object>> filterFunction = r -> {
//    	    Path path = (Path) r.get("path");
//
//    	    return bddMapper.checkPathSAT(path);
//    	};
//    	
//    	return result.stream().parallel()
//    			.filter(filterFunction)
//    			.map(MapResult::new);
//    }
    
//    @Procedure("aware.endpoint.LD")
//    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
//    public Stream<MapResult> awareEndpointLD() {
//    	String query = "MATCH path=(:cVariable)-[:parWrite]->(:cVariable)-[:intraICBC]->(:cVariable)-[:parWrite]->(:cVariable) WITH path, nodes(path) AS nodes\n"
//    			+ "WHERE nodes[0].comp <> nodes[1].comp AND nodes[0].comp <> nodes[2].comp AND nodes[0].comp = nodes[3].comp AND\n"
//    			+ "nodes[1].comp <> nodes[3].comp AND\n"
//    			+ "nodes[2].comp <> nodes[3].comp RETURN path";
//    	Result result = tx.execute(query);
//    	
//    	// initialize antlr2Expr and bddbuilder for parsing
//    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
//    	
//    	// initialize BDDMapper
//    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
//    			
//    	Predicate<Map<String, Object>> filterFunction = r -> {
//    	    Path path = (Path) r.get("path");
//
//    	    return bddMapper.checkPathSAT(path);
//    	};
//    	
//    	return result.stream().parallel()
//    			.filter(filterFunction)
//    			.map(MapResult::new);
//    }
    
//    @Procedure("aware.endpoint.BA")
//    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
//    public Stream<MapResult> awareEndpointBA() {
//    	String query = "MATCH path=(:cVariable)-[:varWrite]->(:cVariable)-[:intraBA]->(:cVariable)-[:varInfFunc]->(:cFunction) WITH path, nodes(path) AS nodes\n"
//    			+ "WHERE nodes[0].comp = nodes[1].comp AND nodes[0].comp <> nodes[2].comp AND\n"
//    			+ "nodes[1].comp <> nodes[2].comp AND\n"
//    			+ "nodes[2].comp = nodes[3].comp RETURN path";
//    	Result result = tx.execute(query);
//    	
//    	// initialize antlr2Expr and bddbuilder for parsing
//    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
//    	
//    	// initialize BDDMapper
//    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
//    			
//    	Predicate<Map<String, Object>> filterFunction = r -> {
//    	    Path path = (Path) r.get("path");
//
//    	    return bddMapper.checkPathSAT(path);
//    	};
//    	
//    	return result.stream().parallel()
//    			.filter(filterFunction)
//    			.map(MapResult::new);
//    }
    
    
    @Procedure("aware.endpoint.indirectICBC")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointIndirectICBC() {
    	String query = "MATCH path=(:cVariable)-[:crossCompFlow]->(:cVariable)-[:parWrite]->(:cVariable) "
    			+ "WITH path, nodes(path) AS nodes\n"
    			+ "WHERE nodes[0].comp <> nodes[2].comp AND nodes[1].comp <> nodes[2].comp RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.LD")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointLD() {
    	String query = "MATCH path=(:cVariable)-[:crossCompFlow]->(:cVariable)-[:parWrite]->(:cVariable) WITH path, nodes(path) AS nodes\n"
    			+ "WHERE nodes[0].comp = nodes[2].comp AND nodes[1].comp <> nodes[2].comp RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.BA")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointBA() {
    	String query = "MATCH path=(:cVariable)-[:initAssign]->(:cVariable)-[:crossCompFlow]->(:cVariable)-[:varInfFunc]->(:cFunction) WITH path, nodes(path) AS nodes\n"
    			+ "WHERE nodes[0].comp = nodes[1].comp AND nodes[0].comp <> nodes[2].comp AND\n"
    			+ "nodes[1].comp <> nodes[2].comp AND\n"
    			+ "nodes[2].comp = nodes[3].comp RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.MC")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointMC() {
    	String query = "MATCH path=(a:cVariable)-[:parWrite]->(b:cVariable)<-[:parWrite]-(c:cVariable) "
    			+ "WHERE a.comp <> b.comp and b.comp <> c.comp and a.comp <> c.comp RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.RC")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointRC() {
    	String query = "MATCH path=(:cVariable)-[:parWrite]->(:cVariable)-[:varWrite]->(:cVariable)<-[:varWrite]-(:cVariable)<-[:parWrite]-(:cVariable) \n"
    			+ "WITH path, nodes(path) AS nodes\n"
    			+ "WHERE nodes[0].comp <> nodes[1].comp AND nodes[0].comp <> nodes[2].comp AND nodes[0].comp <> nodes[3].comp AND nodes[0].comp <> nodes[4].comp\n"
    			+ "AND nodes[0].id <> nodes[1].id AND nodes[0].id <> nodes[2].id AND nodes[0].id <> nodes[3].id AND nodes[0].id <> nodes[4].id\n"
    			+ "\n"
    			+ "AND nodes[1].comp = nodes[2].comp AND nodes[1].comp = nodes[3].comp AND nodes[1].comp <> nodes[4].comp\n"
    			+ "AND nodes[1].id <> nodes[2].id AND nodes[1].id <> nodes[3].id AND nodes[1].id <> nodes[4].id\n"
    			+ "\n"
    			+ "AND nodes[2].comp = nodes[3].comp AND nodes[2].comp <> nodes[4].comp\n"
    			+ "AND nodes[2].id <> nodes[3].id AND nodes[2].id <> nodes[4].id\n"
    			+ "\n"
    			+ "AND nodes[3].comp <> nodes[4].comp AND nodes[3].id <> nodes[4].id RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.DR")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointDR() {
    	String query = "MATCH path=(a:cFunction)-[:call]->(a) RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.endpoint.TSCP")
    @Description("aware.endpoint(fragment) yield value - executes reading fragment with the given query - currently no schema operations")
    public Stream<MapResult> awareEndpointTSCP() {
    	String query = "MATCH path=(a:cVariable)-[:VPwrite]->(b:cVariable)-[:VPwrite]->(c:cVariable)-[:VPwrite]->(a) "
    			+ "WHERE a.id <> b.id AND b.id <> c.id AND c.id <> a.id "
    			+ "RETURN path";
    	Result result = tx.execute(query);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
	
	@Procedure("run.shortest")
    @Description("run.shortest(startNode, endNode, rel) yield value - executes running shortest path with the given parameters")
    public Stream<MapResult> run(@Name("startNode") String startNode, @Name("endNode") String endNode, @Name("rel") String rel) {
    	String start = startNode.split(":")[0];
    	String end = endNode.split(":")[0];
    	String statement = "MATCH (" + startNode + "), (" + endNode + "),\n"
    			+ "  path=shortestPath((" + start + ")-[:" + rel + "*]->(" + end + ")) \n"
    			+ "  WHERE " + start + ".id <> " + end + ".id\n"
    			+ "  RETURN path";
    	Result result = tx.execute(statement);
        return result.stream().map(MapResult::new);
    }
    
    // add procedure name
    @Procedure("aware.run.shortest")
    @Description("aware.run.shortest(fragment) yield value - executes reading fragment with the given parameters - currently no schema operations")
    public Stream<MapResult> runAware(@Name("startNode") String startNode, @Name("endNode") String endNode, @Name("rel") String rel) {
    	String start = startNode.split(":")[0];
    	String end = endNode.split(":")[0];
    	String statement = "MATCH (" + startNode + "), (" + endNode + "),\n"
    			+ "  path=shortestPath((" + start + ")-[:" + rel + "*]->(" + end + ")) \n"
    			+ "  WHERE " + start + ".id <> " + end + ".id\n"
    			+ "  RETURN path";
    	Result result = tx.execute(statement);
    	
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    			
    	Predicate<Map<String, Object>> filterFunction = r -> {
    	    Path path = (Path) r.get("path");

    	    return bddMapper.checkPathSAT(path);
    	};
    	
    	return result.stream().parallel()
    			.filter(filterFunction)
    			.map(MapResult::new);
    }
    
    @Procedure("aware.ICBC.filterComp")
	@Description("aware.ICBC.filterComp(startNode <id>|Node|list, {minLevel,maxLevel,uniqueness,relationshipFilter,labelFilter,uniqueness:'RELATIONSHIP_PATH',bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield path - " +
			"expand from start node following the given relationships from min to max-level adhering to the label filters. ")
	public Stream<PathResult> awareICBCfilterComp(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
		
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
		
		Map<String,Object> config3 = new HashMap<>();
		config3.put("relationshipFilter", "parWrite>,compVPwrite>,parWrite>");
		config3.put("labelFilter", "cVariable");
		config3.put("minLevel", "3");
		config3.put("maxLevel", "3");
		
		Map<String,Object> config4 = new HashMap<>();
		config4.put("relationshipFilter", "parWrite>,compVPwrite>,parWrite>,compVPwrite>,parWrite>");
		config4.put("labelFilter", "cVariable");
		config4.put("minLevel", "5");
		config4.put("maxLevel", "5");
		
		Predicate<Path> filter3comp = path -> {
			
			// Convert the Iterable<Node> to a List<Node>
            List<Node> nodeList = StreamSupport.stream(path.nodes().spliterator(), false)
                    .collect(Collectors.toList());
			
			return !nodeList.get(0).getProperty("comp").equals(nodeList.get(1).getProperty("comp"))
					&& !nodeList.get(1).getProperty("comp").equals(nodeList.get(3).getProperty("comp"));
			
		};
		
		Predicate<Path> filter4comp = path -> {
			
			// Convert the Iterable<Node> to a List<Node>
            List<Node> nodeList = StreamSupport.stream(path.nodes().spliterator(), false)
                    .collect(Collectors.toList());
			
            return !nodeList.get(0).getProperty("comp").equals(nodeList.get(1).getProperty("comp"))
					&& !nodeList.get(0).getProperty("comp").equals(nodeList.get(3).getProperty("comp"))
					&& !nodeList.get(1).getProperty("comp").equals(nodeList.get(3).getProperty("comp"))
					&& !nodeList.get(1).getProperty("comp").equals(nodeList.get(5).getProperty("comp"))
					&& !nodeList.get(3).getProperty("comp").equals(nodeList.get(5).getProperty("comp"));
			
		};

		// don't use .parallel() for comp3 and comp4, it will be much slower.
		Stream<Path> comp3 = expandConfigPrivate(start, config3, bddMapper).filter(filter3comp);
		Stream<Path> comp4 = expandConfigPrivate(start, config4, bddMapper).filter(filter4comp);
		
		// Combine the results
        Stream<Path> combinedResults = Stream.concat(comp3, comp4).parallel();

        // Filter the combined results and map to PathResult
        return combinedResults.map(PathResult::new);
	}
    
    @Procedure("aware.BA.filterComp")
	@Description("aware.BA.filterComp(startNode <id>|Node|list, {minLevel,maxLevel,uniqueness,relationshipFilter,labelFilter,uniqueness:'RELATIONSHIP_PATH',bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield path - " +
			"expand from start node following the given relationships from min to max-level adhering to the label filters. ")
	public Stream<PathResult> awareBAfilterComp(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
    	// initialize antlr2Expr and bddbuilder for parsing
    	final Antlr2Expr antlr2Expr = new Antlr2Expr();
    	
    	// initialize BDDMapper
    	final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
    	
    	Map<String,Object> config3 = new HashMap<>();
		config3.put("relationshipFilter", "varWrite>,compVPwrite>,parWrite>,compVPwrite>,parWrite>,compVPwrite>,varInfFunc>");
		config3.put("minLevel", "7");
		config3.put("maxLevel", "7");
		
		Predicate<Path> filter3comp = path -> {
			
			// Convert the Iterable<Node> to a List<Node>
            List<Node> nodeList = StreamSupport.stream(path.nodes().spliterator(), false)
                    .collect(Collectors.toList());
			
			return nodeList.get(0).getProperty("comp").equals(nodeList.get(1).getProperty("comp"))
					&& !(nodeList.get(0).getId() == nodeList.get(1).getId())
					&& !nodeList.get(1).getProperty("comp").equals(nodeList.get(3).getProperty("comp"))
					&& !nodeList.get(1).getProperty("comp").equals(nodeList.get(5).getProperty("comp"))
					&& !nodeList.get(3).getProperty("comp").equals(nodeList.get(5).getProperty("comp"))
					&& nodeList.get(5).getProperty("comp").equals(nodeList.get(7).getProperty("comp"));
			
		};
		
		return expandConfigPrivate(start, config3, bddMapper).parallel().filter(filter3comp).map(PathResult::new);
	}

    
    @UserFunction("apoc.aware.path.combine")
    @Description("apoc.aware.path.combine(path1, path2) - combines the paths into one if the connecting node matches")
    public Path awareCombine(@Name("first") Path first, @Name("second") Path second) {
        if (first == null || second == null) return null;

        if (!first.endNode().equals(second.startNode()))
            throw new IllegalArgumentException("Paths don't connect on their end and start-nodes "+first+ " with "+second);

        // initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
		
		// get the big conjunction of first's PC and its BDD
        if (!AwarePathExplorer.pathMap.containsKey(first)) {
        	bddMapper.parsePath(first);
		}
		long conj1 = AwarePathExplorer.pathMap.get(first);
		
		// get the big conjunction of first's PC and its BDD
        if (!AwarePathExplorer.pathMap.containsKey(second)) {
        	bddMapper.parsePath(second);
		}
		long conj2 = AwarePathExplorer.pathMap.get(second);
		
		if (Cudd_bddAnd(ddManager, conj1, conj2) != FF) {
			PathImpl.Builder builder = new PathImpl.Builder(first.startNode());
	        for (Relationship rel : first.relationships()) builder = builder.push(rel);
	        for (Relationship rel : second.relationships()) builder = builder.push(rel);
	        return builder.build();
		}
		return null;
    }
    
    @Procedure("apoc.aware.path.extend")
	@Description("apoc.aware.path.extend(startPath <id>|Node|list, {minLevel,maxLevel,uniqueness,relationshipFilter,labelFilter,uniqueness:'RELATIONSHIP_PATH',bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield path - " +
			"expand from start node following the given relationships from min to max-level adhering to the label filters. ")
	public Stream<PathResult> extendConfig(@Name("start") Path startPath, @Name("config") Map<String,Object> config) throws Exception {
		Node startNode = startPath.endNode();  // get the end node of the startPath
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
				
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
				
		Stream<Path> paths = expandConfigPrivate(startNode, config, bddMapper)
	            .map(path -> awareCombine(startPath, path))
	            .flatMap(path -> {
	                if (path == null) {
	                    return Stream.empty();
	                } else {
	                    return Stream.of(path);
	                }
	            });  // concatenate startPath and each result path
	
	    return paths.map(PathResult::new);  // convert each concatenated path to a PathResult object and return a stream
	}

	@Procedure("apoc.aware.path.expand")
	@Description("apoc.aware.path.expand(startNode <id>|Node|list, 'TYPE|TYPE_OUT>|<TYPE_IN', '+YesLabel|-NoLabel', minLevel, maxLevel ) yield path - expand from start node following the given relationships from min to max-level adhering to the label filters")
	public Stream<PathResult> explorePath(@Name("start") Object start
			                   , @Name("relationshipFilter") String pathFilter
			                   , @Name("labelFilter") String labelFilter
			                   , @Name("minLevel") long minLevel
			                   , @Name("maxLevel") long maxLevel ) throws Exception {
		List<Node> nodes = startToNodes(start);
		
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
		
		return explorePathPrivate(nodes, pathFilter, labelFilter, minLevel, maxLevel, BFS, UNIQUENESS, false, -1, 
				null, null, true, bddMapper).map( PathResult::new );
	}

	//
	@Procedure("apoc.aware.path.expandConfig")
	@Description("apoc.aware.path.expandConfig(startNode <id>|Node|list, {minLevel,maxLevel,uniqueness,relationshipFilter,labelFilter,uniqueness:'RELATIONSHIP_PATH',bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield path - " +
			"expand from start node following the given relationships from min to max-level adhering to the label filters. ")
	public Stream<PathResult> expandConfig(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
		
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
				
//		Stream<Path> path = expandConfigPrivate(start, config, bddMapper);
		return expandConfigPrivate(start, config, bddMapper).parallel().map( PathResult::new );
	}

	@Procedure("apoc.aware.path.subgraphNodes.inICBC")
	@Description("apoc.aware.path.subgraphNodes.inICBC(startNode <id>|Node|list, {maxLevel,relationshipFilter,labelFilter,bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield node - expand the subgraph nodes reachable from start node following relationships to max-level adhering to the label filters")
	public Stream<NodeResult> subgraphNodesInICBC(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
		Map<String, Object> configMap = new HashMap<>(config);
		configMap.put("uniqueness", "NODE_GLOBAL");

		if (config.containsKey("minLevel") && !config.get("minLevel").equals(0l) && !config.get("minLevel").equals(1l)) {
			throw new IllegalArgumentException("minLevel can only be 0 or 1 in subgraphNodes()");
		}
	
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
		
		Predicate<Path> filter = path -> {
			
			// Convert the Iterable<Node> to a List<Node>
            List<Node> nodeList = StreamSupport.stream(path.nodes().spliterator(), false)
                    .collect(Collectors.toList());
			
			return  nodeList.size() == 4
					&& nodeList.get(0).getProperty("comp") != nodeList.get(1).getProperty("comp") 
					&& nodeList.get(0).getProperty("comp") != nodeList.get(2).getProperty("comp") 
					&& nodeList.get(0).getProperty("comp") != nodeList.get(3).getProperty("comp") 
					&& nodeList.get(1).getProperty("comp") != nodeList.get(3).getProperty("comp") 
					&& nodeList.get(2).getProperty("comp") != nodeList.get(3).getProperty("comp");
			
		};
		
		return expandConfigPrivate(start, configMap, bddMapper).parallel().filter(filter).map( path -> path == null ? new NodeResult(null) : new NodeResult(path.endNode()) );
	}
	
	@Procedure("apoc.aware.path.subgraphNodes")
	@Description("apoc.aware.path.subgraphNodes(startNode <id>|Node|list, {maxLevel,relationshipFilter,labelFilter,bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield node - expand the subgraph nodes reachable from start node following relationships to max-level adhering to the label filters")
	public Stream<NodeResult> subgraphNodes(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
		Map<String, Object> configMap = new HashMap<>(config);
		configMap.put("uniqueness", "NODE_GLOBAL");

		if (config.containsKey("minLevel") && !config.get("minLevel").equals(0l) && !config.get("minLevel").equals(1l)) {
			throw new IllegalArgumentException("minLevel can only be 0 or 1 in subgraphNodes()");
		}
	
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
		
		return expandConfigPrivate(start, configMap, bddMapper).parallel().map( path -> path == null ? new NodeResult(null) : new NodeResult(path.endNode()) );
	}

	@Procedure("apoc.aware.path.subgraphAll")
	@Description("apoc.aware.path.subgraphAll(startNode <id>|Node|list, {maxLevel,relationshipFilter,labelFilter,bfs:true, filterStartNode:false, limit:-1, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield nodes, relationships - expand the subgraph reachable from start node following relationships to max-level adhering to the label filters, and also return all relationships within the subgraph")
	public Stream<GraphResult> subgraphAll(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
		Map<String, Object> configMap = new HashMap<>(config);
		configMap.remove("optional"); // not needed, will return empty collections anyway if no results
		configMap.put("uniqueness", "NODE_GLOBAL");

		if (config.containsKey("minLevel") && !config.get("minLevel").equals(0l) && !config.get("minLevel").equals(1l)) {
			throw new IllegalArgumentException("minLevel can only be 0 or 1 in subgraphAll()");
		}
		
		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
		
		List<Node> subgraphNodes = expandConfigPrivate(start, configMap, bddMapper).map( Path::endNode ).collect(Collectors.toList());
		List<Relationship> subgraphRels = Cover.coverNodes(subgraphNodes).collect(Collectors.toList());

		return Stream.of(new GraphResult(subgraphNodes, subgraphRels));
	}

	@Procedure("apoc.aware.path.spanningTree")
	@Description("apoc.aware.path.spanningTree(startNode <id>|Node|list, {maxLevel,relationshipFilter,labelFilter,bfs:true, filterStartNode:false, limit:-1, optional:false, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield path - expand a spanning tree reachable from start node following relationships to max-level adhering to the label filters")
	public Stream<PathResult> spanningTree(@Name("start") Object start, @Name("config") Map<String,Object> config) throws Exception {
		Map<String, Object> configMap = new HashMap<>(config);
		configMap.put("uniqueness", "NODE_GLOBAL");

		if (config.containsKey("minLevel") && !config.get("minLevel").equals(0l) && !config.get("minLevel").equals(1l)) {
			throw new IllegalArgumentException("minLevel can only be 0 or 1 in spanningTree()");
		}

		// initialize antlr2Expr and bddbuilder for parsing
		final Antlr2Expr antlr2Expr = new Antlr2Expr();
		//final BDDbuilder bddBuilder = new BDDbuilder();
		
		// initialize BDDMapper
		final BDDMapper bddMapper = new BDDMapper(antlr2Expr);
				
		return expandConfigPrivate(start, configMap, bddMapper).map( PathResult::new );
	}

	private Uniqueness getUniqueness(String uniqueness) {
		for (Uniqueness u : Uniqueness.values()) {
			if (u.name().equalsIgnoreCase(uniqueness)) return u;
		}
		return UNIQUENESS;
	}
	
	
	/*
	 * Get the address of DD manager (used for satisfiability checking
	 * @author Xiang Chen
     * @since 2021.12
     * @return if the DD manager address exists, return the address, otherwise return -1
	 */
	private Object getDdManager() {
		// using getProperty(String key, Object defaultValue) to avoid an exception for an unknown key and instead get null back 
		return tx.getNodeById(0).getProperty("ddManagerAddress", -1);
	}
	
	

	/*
    , @Name("relationshipFilter") String pathFilter
    , @Name("labelFilter") String labelFilter
    , @Name("minLevel") long minLevel
    , @Name("maxLevel") long maxLevel ) throws Exception {
     */
	@SuppressWarnings("unchecked")
	private List<Node> startToNodes(Object start) throws Exception {
		if (start == null) return Collections.emptyList();
		if (start instanceof Node) {
			return Collections.singletonList((Node) start);
		}
		if (start instanceof Path) {
			return Collections.singletonList((Node) ((Path) start).endNode());
		}
		if (start instanceof Number) {
			return Collections.singletonList(tx.getNodeById(((Number) start).longValue()));
		}
		if (start instanceof List) {
			List list = (List) start;
			if (list.isEmpty()) return Collections.emptyList();

			Object first = list.get(0);
			if (first instanceof Node) return (List<Node>)list;
			if (first instanceof Number) {
                List<Node> nodes = new ArrayList<>();
                for (Number n : ((List<Number>)list)) nodes.add(tx.getNodeById(n.longValue()));
                return nodes;
            }
		}
		throw new Exception("Unsupported data type for start parameter a Node or an Identifier (long) of a Node must be given!");
	}

	private Stream<Path> expandConfigPrivate(@Name("start") Object start, @Name("config") Map<String,Object> config, 
			@Name("BDDMapper") BDDMapper bddMapper ) throws Exception {
		List<Node> nodes = startToNodes(start);

		String uniqueness = (String) config.getOrDefault("uniqueness", UNIQUENESS.name());
		String relationshipFilter = (String) config.getOrDefault("relationshipFilter", null);
		String labelFilter = (String) config.getOrDefault("labelFilter", null);
		long minLevel = Util.toLong(config.getOrDefault("minLevel", "-1"));
		long maxLevel = Util.toLong(config.getOrDefault("maxLevel", "-1"));
		boolean bfs = Util.toBoolean(config.getOrDefault("bfs",true));
		boolean filterStartNode = Util.toBoolean(config.getOrDefault("filterStartNode", false));
		long limit = Util.toLong(config.getOrDefault("limit", "-1"));
		boolean optional = Util.toBoolean(config.getOrDefault("optional", false));
		String sequence = (String) config.getOrDefault("sequence", null);
		boolean beginSequenceAtStart = Util.toBoolean(config.getOrDefault("beginSequenceAtStart", true));

		List<Node> endNodes = startToNodes(config.get("endNodes"));
		List<Node> terminatorNodes = startToNodes(config.get("terminatorNodes"));
		List<Node> whitelistNodes = startToNodes(config.get("whitelistNodes"));
		List<Node> blacklistNodes = startToNodes(config.get("blacklistNodes"));
		EnumMap<NodeFilter, List<Node>> nodeFilter = new EnumMap<>(NodeFilter.class);

		if (endNodes != null && !endNodes.isEmpty()) {
			nodeFilter.put(END_NODES, endNodes);
		}

		if (terminatorNodes != null && !terminatorNodes.isEmpty()) {
			nodeFilter.put(TERMINATOR_NODES, terminatorNodes);
		}

		if (whitelistNodes != null && !whitelistNodes.isEmpty()) {
			nodeFilter.put(WHITELIST_NODES, whitelistNodes);
		}

		if (blacklistNodes != null && !blacklistNodes.isEmpty()) {
			nodeFilter.put(BLACKLIST_NODES, blacklistNodes);
		}

		Stream<Path> results = explorePathPrivate(nodes, relationshipFilter, labelFilter, minLevel, maxLevel, bfs, 
				getUniqueness(uniqueness), filterStartNode, limit, nodeFilter, sequence, beginSequenceAtStart, 
				bddMapper);

		if (optional) {
			return optionalStream(results);
		} else {
			return results;
		}
	}

	private Stream<Path> explorePathPrivate(Iterable<Node> startNodes,
											String pathFilter,
											String labelFilter,
											long minLevel,
											long maxLevel,
											boolean bfs,
											Uniqueness uniqueness,
											boolean filterStartNode,
											long limit,
											EnumMap<NodeFilter, List<Node>> nodeFilter,
											String sequence,
											boolean beginSequenceAtStart,
											BDDMapper bddMapper) {

		Traverser traverser = traverse(tx.traversalDescription(), startNodes, pathFilter, labelFilter, minLevel, maxLevel, 
				uniqueness,bfs,filterStartNode, nodeFilter, sequence, beginSequenceAtStart, bddMapper);

		if (limit == -1) {
			return Iterables.stream(traverser);
		} else {
			return Iterables.stream(traverser).limit(limit);
		}
	}

	/**
	 * If the stream is empty, returns a stream of a single null value, otherwise returns the equivalent of the input stream
	 * @param stream the input stream
	 * @return a stream of a single null value if the input stream is empty, otherwise returns the equivalent of the input stream
	 */
	private Stream<Path> optionalStream(Stream<Path> stream) {
		Stream<Path> optionalStream;
		Iterator<Path> itr = stream.iterator();
		if (itr.hasNext()) {
			optionalStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 0), false);
		} else {
			List<Path> listOfNull = new ArrayList<>();
			listOfNull.add(null);
			optionalStream = listOfNull.stream();
		}

		return optionalStream;
	}

	public static Traverser traverse(TraversalDescription traversalDescription,
									 Iterable<Node> startNodes,
									 String pathFilter,
									 String labelFilter,
									 long minLevel,
									 long maxLevel,
									 Uniqueness uniqueness,
									 boolean bfs,
									 boolean filterStartNode,
									 EnumMap<NodeFilter, List<Node>> nodeFilter,
									 String sequence,
									 boolean beginSequenceAtStart,
									 BDDMapper bddMapper) {
		TraversalDescription td = traversalDescription;
		// based on the pathFilter definition now the possible relationships and directions must be shown

		td = bfs ? td.breadthFirst() : td.depthFirst();

		// if `sequence` is present, it overrides `labelFilter` and `relationshipFilter`
		if (sequence != null && !sequence.trim().isEmpty())	{
			String[] sequenceSteps = sequence.split(",");
			List<String> labelSequenceList = new ArrayList<>();
			List<String> relSequenceList = new ArrayList<>();

			for (int index = 0; index < sequenceSteps.length; index++) {
				List<String> seq = (beginSequenceAtStart ? index : index - 1) % 2 == 0 ? labelSequenceList : relSequenceList;
				seq.add(sequenceSteps[index]);
			}
			
			// create new relationship expander
			AwareRelationshipSequenceExpander reExpander = new AwareRelationshipSequenceExpander(relSequenceList, beginSequenceAtStart, bddMapper);
			td = td.expand(reExpander);
			
			// create new label sequence evaluator
			AwareLabelSequenceEvaluator labelSequenceEvaluator = new AwareLabelSequenceEvaluator(labelSequenceList, filterStartNode, beginSequenceAtStart, (int) minLevel, bddMapper);
			td = td.evaluator(labelSequenceEvaluator);
		} else {
			if (pathFilter != null && !pathFilter.trim().isEmpty()) {
				AwareRelationshipSequenceExpander reExpander = new AwareRelationshipSequenceExpander(pathFilter.trim(), beginSequenceAtStart, bddMapper);
				td = td.expand(reExpander);
			}

			if (labelFilter != null && sequence == null && !labelFilter.trim().isEmpty()) {
				AwareLabelSequenceEvaluator labelSequenceEvaluator = new AwareLabelSequenceEvaluator(labelFilter.trim(), filterStartNode, beginSequenceAtStart, (int) minLevel, bddMapper);
				td = td.evaluator(labelSequenceEvaluator);
			} 
			// if user does not specify labelfilters, consider it as accepting all labels
			// this is necesarry for the SAT check
			else if ( sequence == null && (labelFilter == null || labelFilter.trim().isEmpty() )) {
				AwareLabelSequenceEvaluator labelSequenceEvaluator = new AwareLabelSequenceEvaluator("*", filterStartNode, beginSequenceAtStart, (int) minLevel, bddMapper);
				td = td.evaluator(labelSequenceEvaluator);
			}
		}

		if (minLevel != -1) td = td.evaluator(Evaluators.fromDepth((int) minLevel));
		if (maxLevel != -1) td = td.evaluator(Evaluators.toDepth((int) maxLevel));


		if (nodeFilter != null && !nodeFilter.isEmpty()) {
			List<Node> endNodes = nodeFilter.getOrDefault(END_NODES, Collections.EMPTY_LIST);
			List<Node> terminatorNodes = nodeFilter.getOrDefault(TERMINATOR_NODES, Collections.EMPTY_LIST);
			List<Node> blacklistNodes = nodeFilter.getOrDefault(BLACKLIST_NODES, Collections.EMPTY_LIST);
			List<Node> whitelistNodes;

			if (nodeFilter.containsKey(WHITELIST_NODES)) {
				// need to add to new list since we may need to add to it later
				// encounter "can't add to abstractList" error if we don't do this
				whitelistNodes = new ArrayList<>(nodeFilter.get(WHITELIST_NODES));
			} else {
				whitelistNodes = Collections.EMPTY_LIST;
			}

			if (!blacklistNodes.isEmpty()) {
				td = td.evaluator(AwareNodeEvaluators.blacklistNodeEvaluator(filterStartNode, (int) minLevel, blacklistNodes, bddMapper));
			}

			Evaluator endAndTerminatorNodeEvaluator = AwareNodeEvaluators.endAndTerminatorNodeEvaluator(filterStartNode, (int) minLevel, endNodes, terminatorNodes, bddMapper);
			if (endAndTerminatorNodeEvaluator != null) {
				td = td.evaluator(endAndTerminatorNodeEvaluator);
			}

			if (!whitelistNodes.isEmpty()) {
				// ensure endNodes and terminatorNodes are whitelisted
				whitelistNodes.addAll(endNodes);
				whitelistNodes.addAll(terminatorNodes);
				td = td.evaluator(AwareNodeEvaluators.whitelistNodeEvaluator(filterStartNode, (int) minLevel, whitelistNodes, bddMapper));
			}
		}

		td = td.uniqueness(uniqueness); // this is how Cypher works !! Uniqueness.RELATIONSHIP_PATH
		// uniqueness should be set as last on the TraversalDescription
		return td.traverse(startNodes);
	}

	// keys to node filter map
	enum NodeFilter {
		WHITELIST_NODES,
		BLACKLIST_NODES,
		END_NODES,
		TERMINATOR_NODES
	}
	
	public static class Output {
        public List<Path> paths;

        Output() {
            this.paths = new ArrayList<>();
        }
    }

}
