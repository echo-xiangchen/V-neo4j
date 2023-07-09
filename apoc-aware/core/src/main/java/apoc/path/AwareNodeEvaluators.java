package apoc.path;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static factory methods for obtaining node evaluators
 */
public final class AwareNodeEvaluators {
    // non-instantiable
    private AwareNodeEvaluators() {};
    
    

    /**
     * Returns an evaluator which handles end nodes and terminator nodes
     * Returns null if both lists are empty
     */
    public static Evaluator endAndTerminatorNodeEvaluator(boolean filterStartNode, int minLevel, List<Node> endNodes, List<Node> terminatorNodes, BDDMapper bddMapper) {
        Evaluator endNodeEvaluator = null;
        Evaluator terminatorNodeEvaluator = null;

        if (!endNodes.isEmpty()) {
            Node[] nodes = endNodes.toArray(new Node[endNodes.size()]);
            endNodeEvaluator = Evaluators.includeWhereEndNodeIs(nodes);
        }

        if (!terminatorNodes.isEmpty()) {
            Node[] nodes = terminatorNodes.toArray(new Node[terminatorNodes.size()]);
            terminatorNodeEvaluator = Evaluators.pruneWhereEndNodeIs(nodes);
        }

        if (endNodeEvaluator != null || terminatorNodeEvaluator != null) {
            return new EndAndTerminatorNodeEvaluator(filterStartNode, minLevel, endNodeEvaluator, terminatorNodeEvaluator, bddMapper);
        }

        return null;
    }

    public static Evaluator whitelistNodeEvaluator(boolean filterStartNode, int minLevel, List<Node> whitelistNodes, BDDMapper bddMapper) {
        return new WhitelistNodeEvaluator(filterStartNode, minLevel, whitelistNodes, bddMapper);
    }

    public static Evaluator blacklistNodeEvaluator(boolean filterStartNode, int minLevel, List<Node> blacklistNodes, BDDMapper bddMapper) {
        return new BlacklistNodeEvaluator(filterStartNode, minLevel, blacklistNodes, bddMapper);
    }

    // The evaluators from pruneWhereEndNodeIs and includeWhereEndNodeIs interfere with each other, this makes them play nice
    private static class EndAndTerminatorNodeEvaluator implements Evaluator {
        private boolean filterStartNode;
        private int minLevel;
        private Evaluator endNodeEvaluator;
        private Evaluator terminatorNodeEvaluator;
        
        // create static bddMapper object
      	private static BDDMapper bddMapper;

        public EndAndTerminatorNodeEvaluator(boolean filterStartNode, int minLevel, Evaluator endNodeEvaluator, Evaluator terminatorNodeEvaluator, BDDMapper bddMapper) {
            this.filterStartNode = filterStartNode;
            this.minLevel = minLevel;
            this.endNodeEvaluator = endNodeEvaluator;
            this.terminatorNodeEvaluator = terminatorNodeEvaluator;
            this.bddMapper = bddMapper;
        }

        @Override
        public Evaluation evaluate(Path path) {
            if ((path.length() == 0 && !filterStartNode) || path.length() < minLevel) {
                return Evaluation.EXCLUDE_AND_CONTINUE;
            }

            // at least one has to give a thumbs up to include
            boolean includes = evalIncludes(endNodeEvaluator, path) || evalIncludes(terminatorNodeEvaluator, path);
            // prune = terminatorNodeEvaluator != null && !terminatorNodeEvaluator.evaluate(path).continues()
            // negate this to get continues result
            boolean continues = terminatorNodeEvaluator == null || terminatorNodeEvaluator.evaluate(path).continues();

            return Evaluation.of(includes, continues);
        }

        private boolean evalIncludes(Evaluator eval, Path path) {
            return eval != null && eval.evaluate(path).includes();
        }
    }

    private static class BlacklistNodeEvaluator extends PathExpanderNodeEvaluator {
        private Set<Node> blacklistSet;

        public BlacklistNodeEvaluator(boolean filterStartNode, int minLevel, List<Node> blacklistNodes, BDDMapper bddMapper) {
            super(filterStartNode, minLevel, bddMapper);
            blacklistSet = new HashSet<>(blacklistNodes);
        }

        @Override
        public Evaluation evaluate(Path path) {
            return path.length() == 0 && !filterStartNode ? Evaluation.INCLUDE_AND_CONTINUE :
                    blacklistSet.contains(path.endNode()) ? Evaluation.EXCLUDE_AND_PRUNE : Evaluation.INCLUDE_AND_CONTINUE;
        }
    }

    private static class WhitelistNodeEvaluator extends PathExpanderNodeEvaluator {
        private Set<Node> whitelistSet;

        public WhitelistNodeEvaluator(boolean filterStartNode, int minLevel, List<Node> whitelistNodes, BDDMapper bddMapper) {
            super(filterStartNode, minLevel, bddMapper);
            whitelistSet = new HashSet<>(whitelistNodes);
        }

        @Override
        public Evaluation evaluate(Path path) {
            return (path.length() == 0 && !filterStartNode) ? Evaluation.INCLUDE_AND_CONTINUE :
            whitelistSet.contains(path.endNode()) ? Evaluation.INCLUDE_AND_CONTINUE : Evaluation.EXCLUDE_AND_PRUNE;
        }
    }

    private static abstract class PathExpanderNodeEvaluator implements Evaluator {
        public final boolean filterStartNode;
        public final int minLevel;
        
        // create static bddMapper object
      	private static BDDMapper bddMapper;

        private PathExpanderNodeEvaluator(boolean filterStartNode, int minLevel, BDDMapper bddMapper) {
            this.filterStartNode = filterStartNode;
            this.minLevel = minLevel;
            this.bddMapper = bddMapper;
        }
    }
}
