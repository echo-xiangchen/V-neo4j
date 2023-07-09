package apoc.path;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.neo4j.graphdb.traversal.Evaluation.EXCLUDE_AND_CONTINUE;
import static org.neo4j.graphdb.traversal.Evaluation.INCLUDE_AND_CONTINUE;

// when no commas present, acts as a pathwide label filter
public class AwareLabelSequenceEvaluator implements Evaluator {
    private List<AwareLabelMatcherGroup> sequenceMatchers;

    private Evaluation whitelistAllowedEvaluation;
    private boolean endNodesOnly;
    private boolean filterStartNode;
    private boolean beginSequenceAtStart;
    private long minLevel = -1;
    
    // create static bddMapper object
  	private static BDDMapper bddMapper;

    public AwareLabelSequenceEvaluator(String labelSequence, boolean filterStartNode, boolean beginSequenceAtStart, int minLevel, BDDMapper bddMapper) {
    	// first pass the bddMapper object
    	this.bddMapper = bddMapper;
    	
        List<String> labelSequenceList;

        // parse sequence
        if (labelSequence != null && !labelSequence.isEmpty()) {
            labelSequenceList = Arrays.asList(labelSequence.split(","));
        } else {
            labelSequenceList = Collections.emptyList();
        }

        initialize(labelSequenceList, filterStartNode, beginSequenceAtStart, minLevel);
    }

    public AwareLabelSequenceEvaluator(List<String> labelSequenceList, boolean filterStartNode, boolean beginSequenceAtStart, int minLevel, BDDMapper bddMapper) {
    	// first pass the bddMapper object
    	this.bddMapper = bddMapper;
    	
    	initialize(labelSequenceList, filterStartNode, beginSequenceAtStart, minLevel);
    }

    private void initialize(List<String> labelSequenceList, boolean filterStartNode, boolean beginSequenceAtStart, int minLevel) {
        this.filterStartNode = filterStartNode;
        this.beginSequenceAtStart = beginSequenceAtStart;
        this.minLevel = minLevel;
        sequenceMatchers = new ArrayList<>(labelSequenceList.size());

        for (String labelFilterString : labelSequenceList) {
            AwareLabelMatcherGroup matcherGroup = new AwareLabelMatcherGroup(bddMapper).addLabels(labelFilterString.trim());
            sequenceMatchers.add(matcherGroup);
            endNodesOnly = endNodesOnly || matcherGroup.isEndNodesOnly();
        }

        // if true for one matcher, need to set true for all matchers
        if (endNodesOnly) {
            for (AwareLabelMatcherGroup group : sequenceMatchers) {
                group.setEndNodesOnly(endNodesOnly);
            }
        }

        whitelistAllowedEvaluation = endNodesOnly ? EXCLUDE_AND_CONTINUE : INCLUDE_AND_CONTINUE;
    }

    @Override
    public Evaluation evaluate(Path path) {
        int depth = path.length();
        Node node = path.endNode();
        // get the pc for this node
        String pc = (String) node.getProperty("condition", "True");
        // also need to check if the value of "condition" attribute is empty string
        if (pc.isEmpty()) {
        	pc = "True";
        }
        
        boolean belowMinLevel = depth < minLevel;
        
        // if start node shouldn't be filtered, exclude/include based on if using termination/endnode filter or not
        // minLevel evaluator will separately enforce exclusion if we're below minLevel
        if (depth == 0 && (!filterStartNode || !beginSequenceAtStart)) {
        	/*
        	 *  if depth == 0, there will be only one node in the path (which is the start node/end node)
        	 *  simply parse this node's pc and add it to the bddMap - so in the future if we see the same pc we don't need to parse it again
        	 */
        	if (!AwarePathExplorer.exprMap.containsKey(pc)) {
				bddMapper.parsePC(pc);
			}
            return whitelistAllowedEvaluation;
        }

        // the user may want the sequence to begin at the start node (default), or the sequence may only apply from the next node on
        AwareLabelMatcherGroup matcherGroup = sequenceMatchers.get((beginSequenceAtStart ? depth : depth - 1) % sequenceMatchers.size());

        return matcherGroup.evaluate(path, node, belowMinLevel);
    }
}
