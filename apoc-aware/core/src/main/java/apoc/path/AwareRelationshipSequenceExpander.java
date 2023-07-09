package apoc.path;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterators;
import org.neo4j.internal.helpers.collection.NestingIterator;
import org.neo4j.internal.helpers.collection.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An expander for repeating sequences of relationships. The sequence provided should be a string consisting of
 * relationship type/direction patterns (exactly the same as the `relationshipFilter`), separated by commas.
 * Each comma-separated pattern represents the relationships that will be expanded with each step of expansion, which
 * repeats indefinitely (unless otherwise stopped by `maxLevel`, `limit`, or terminator filtering from the other expander config options).
 * The exception is if `beginSequenceAtStart` is false. This indicates that the sequence should not begin from the start node,
 * but from one node distant. In this case, we may still need a restriction on the relationship used to reach the start node
 * of the sequence, so when `beginSequenceAtStart` is false, then the first relationship step in the sequence given will not
 * actually be used as part of the sequence, but will only be used once to reach the starting node of the sequence.
 * The remaining relationship steps will be used as the repeating relationship sequence.
 */
public class AwareRelationshipSequenceExpander implements PathExpander {
    private final List<List<Pair<RelationshipType, Direction>>> relSequences = new ArrayList<>();
    private List<Pair<RelationshipType, Direction>> initialRels = null;
    
 	// create static bddMapper object
 	private static BDDMapper bddMapper;


    public AwareRelationshipSequenceExpander(String relSequenceString, boolean beginSequenceAtStart, BDDMapper bddMapper) {
    	// first pass the bddMapper object
    	this.bddMapper = bddMapper;
    	
        int index = 0;
        
        /*
         *  split the string on ","
         *  e.g., "WRITE>,CONTAIN>" will become an array that has two elements
         *  array[0] = "WRITE>", array[1] = CONTAIN>
         */
        for (String sequenceStep : relSequenceString.split(",")) {
            sequenceStep = sequenceStep.trim();
            
            /*
             * relDirIterable is an arraylist that each element is a pair
             * and for each pair, pair.first = relationship type, pair.other = direction
             * e.g., for "WRITE>", pair.first = "WRITE", pair.other = "OUTGOING"
             */
            Iterable<Pair<RelationshipType, Direction>> relDirIterable = RelationshipTypeAndDirections.parse(sequenceStep);

            // copy the content from relDirIterable to stepRels
            List<Pair<RelationshipType, Direction>> stepRels = new ArrayList<>();
            for (Pair<RelationshipType, Direction> pair : relDirIterable) {
                stepRels.add(pair);
            }

            if (!beginSequenceAtStart && index == 0) {
                initialRels = stepRels;
            } else {
                relSequences.add(stepRels);
            }

            index++;
        }
    }

    public AwareRelationshipSequenceExpander(List<String> relSequenceList, boolean beginSequenceAtStart, BDDMapper bddMapper) {
    	/// first pass the bddMapper object
    	this.bddMapper = bddMapper;
    	
    	int index = 0;

        for (String sequenceStep : relSequenceList) {
            sequenceStep = sequenceStep.trim();
            Iterable<Pair<RelationshipType, Direction>> relDirIterable = RelationshipTypeAndDirections.parse(sequenceStep);

            List<Pair<RelationshipType, Direction>> stepRels = new ArrayList<>();
            for (Pair<RelationshipType, Direction> pair : relDirIterable) {
                stepRels.add(pair);
            }

            if (!beginSequenceAtStart && index == 0) {
                initialRels = stepRels;
            } else {
                relSequences.add(stepRels);
            }

            index++;
        }
    }
    

    @Override
    public Iterable<Relationship> expand( Path path, BranchState state ) {
    	
        final Node node = path.endNode();
        int depth = path.length();
        List<Pair<RelationshipType, Direction>> stepRels;

        if (depth == 0 && initialRels != null) {
            stepRels = initialRels;
        } else {
            stepRels = relSequences.get((initialRels == null ? depth : depth - 1) % relSequences.size());
        }
        
        // get the big conjunction of current path's pc's BDDs
        if (!AwarePathExplorer.pathMap.containsKey(path)) {
        	bddMapper.parsePath(path);
		}
		long pathConjunc = AwarePathExplorer.pathMap.get(path);

        

        return Iterators.asList(
         new NestingIterator<Relationship, Pair<RelationshipType, Direction>>(
                stepRels.iterator() )
        {
            @Override
            protected Iterator<Relationship> createNestedIterator(
                    Pair<RelationshipType, Direction> entry )
            {
                RelationshipType type = entry.first();
                Direction dir = entry.other();
                
//                if (type != null) {
//                    return ((dir == Direction.BOTH) ?  node.getRelationships(type) :
//                    	node.getRelationships(dir, type)).iterator();
//                } else {
//                    return ((dir == Direction.BOTH) ? node.getRelationships() :
//                    	node.getRelationships(dir)).iterator();
//                }
                
                if (type != null) {
                    return ((dir == Direction.BOTH) ?  bddMapper.checkSATandGetRelationships(node.getRelationships(type),pathConjunc) :
                    	bddMapper.checkSATandGetRelationships(node.getRelationships(dir, type),pathConjunc)).iterator();
                } else {
                    return ((dir == Direction.BOTH) ? bddMapper.checkSATandGetRelationships(node.getRelationships(),pathConjunc) :
                    	bddMapper.checkSATandGetRelationships(node.getRelationships(dir),pathConjunc)).iterator();
                }
            }
        });
    }

    @Override
    public PathExpander reverse() {
        throw new RuntimeException("Not implemented");
    }
    
}
