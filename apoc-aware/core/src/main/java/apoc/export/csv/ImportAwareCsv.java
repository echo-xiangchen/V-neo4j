package apoc.export.csv;

import apoc.Pools;
import apoc.export.util.ProgressReporter;
import apoc.result.ProgressInfo;
import apoc.util.Util;
import apoc.expr.Antlr2Expr;
import apoc.expr.visitor.*;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ImportAwareCsv {
    @Context
    public GraphDatabaseService db;

    @Context
    public Pools pools;

    @Context
    public Log log;

    public ImportAwareCsv(GraphDatabaseService db) {
        this.db = db;
    }

    public ImportAwareCsv() {
    }

    @Procedure(name = "apoc.aware.import.csv", mode = Mode.SCHEMA)
    @Description("apoc.aware.import.csv(nodes, relationships, config) - imports nodes and relationships from the provided CSV files with given labels and types"
    		+ "and automatically parse the presence condition into BDDs")
    public Stream<ProgressInfo> importCsv(
            @Name("nodes") List<Map<String, Object>> nodes,
            @Name("relationships") List<Map<String, String>> relationships,
            @Name("config") Map<String, Object> config
    ) throws Exception {
        ProgressInfo result =
                Util.inThread(pools, () -> {
                    final ProgressReporter reporter = new ProgressReporter(null, null, new ProgressInfo("progress.csv", "file", "csv"));

                    final CsvLoaderConfig clc = CsvLoaderConfig.from(config);
                    final CsvEntityAwareLoader loader = new CsvEntityAwareLoader(clc, reporter, log);

                    final Map<String, Map<String, Long>> idMapping = new HashMap<>();
                    
                    // initialize antlr2Expr and bddbuilder for parsing
                    final Antlr2Expr antlr2Expr = new Antlr2Expr();
                    final BDDbuilder bddBuilder = new BDDbuilder();
                    
                    // create the bdd field - idSpace is simply reserve (means nothing)
                    CsvHeaderField bdd = new CsvHeaderField(0, "bdd", "LONG", false, "reserve");
                    
                    // create a node to store the CUDD initializer - ddManager
                    List<String> ddLabel = new ArrayList<String>();
                    ddLabel.add("ddManager");
                    
                    Map<String, Object> ddProperty = new LinkedHashMap<String, Object>();
                    ddProperty.put("name", "ddManager");
                    ddProperty.put("ddManagerAddress", bddBuilder.ddManager);
                    
                    loader.addNode(ddLabel, ddProperty, db);
                    
                    for (Map<String, Object> node : nodes) {
                        final String fileName = (String) node.get("fileName");
                        final List<String> labels = (List<String>) node.get("labels");
                        loader.loadNodes(fileName, labels, db, idMapping, antlr2Expr, bddBuilder,
                        		bdd);
                    }
                   
                    for (Map<String, String> relationship : relationships) {
                        final String fileName = relationship.get("fileName");
                        final String type = relationship.get("type");
                        // remember to send bddbuillder to loader for parsing
                        loader.loadRelationships(fileName, type, db, idMapping, antlr2Expr, bddBuilder,
                        		bdd);
                    }

                    return reporter.getTotal();
                });
        return Stream.of(result);
    }


}
