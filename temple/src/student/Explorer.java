package student;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import javafx.collections.transformation.SortedList;

import java.lang.reflect.Array;
import java.util.*;

public class Explorer {

    private LinkedList<Long> haveBeen = new LinkedList<>();
    private Stack<Long> pathStack = new Stack<>();
    private Stack<Long> forks = new Stack<>();

    /**
     * Explore the cavern, trying to find the orb in as few steps as possible.
     * Once you find the orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb,
     * it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * <p>
     * At every step, you only know your current tile's ID and the ID of all
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles).
     * <p>
     * To get information about the current state, use functions
     * getCurrentLocation(),
     * getNeighbours(), and
     * getDistanceToTarget()
     * in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * <p>
     * Use function moveTo(long id) in ExplorationState to move to a neighboring
     * tile by its ID. Doing this will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     *
     * @param state the information available at the current state
     */
    public void explore(ExplorationState state) {

        while (state.getDistanceToTarget()!=0) {
            long current = state.getCurrentLocation();

            //arraylist of neighbour id ordered by distance
            ArrayList<Long> neighbours = new ArrayList<>();
            List<game.NodeStatus> temp = (List<game.NodeStatus>) state.getNeighbours();
            Collections.sort(temp);
            temp.forEach(n -> neighbours.add(n.getId()));

            //remove neighbours that explorer has been in
            haveBeen.forEach(n -> neighbours.remove(n));

            switch (neighbours.size()) {
                case 0:
                    //go back to last fork
                    haveBeen.addFirst(current);
                    goBackToLastFork(state);
                    break;
                case 1:
                    //only one way to go
                    haveBeen.addFirst(current);
                    pathStack.push(current);
                    state.moveTo(neighbours.get(0));
                    break;
                default:
                    //save fork then move to neighbour closest to target
                    forks.push(current);
                    haveBeen.addFirst(current);
                    pathStack.push(current);
                    state.moveTo(neighbours.get(0));
                    break;
            }
        }
    }

    private void goBackToLastFork(ExplorationState state) {

        long lastFork = forks.pop();
        long there;
        do {
            there = pathStack.pop();
            state.moveTo(there);
        }
        while(lastFork!=there);
    }

    /**
     * Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph.
     * <p>
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * <p>
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold.
     *
     * @param state the information available at the current state
     */
    public void escape(EscapeState state) {

        ArrayList<Node> vertices = new ArrayList<>(state.getVertices());
        int maxX = 40;
        int maxY = 25;

        Node start = state.getCurrentNode();
        Node exit = state.getExit();

        ArrayList<Node> closedSet = new ArrayList<>();
        ArrayList<Node> openSet = new ArrayList<>();
        openSet.add(start);
        openSet.add(exit);

        Hashtable<Node, Node> cameFrom = new Hashtable<>();

        //map all nodes to have gScore of 1000 which represents infinity
        //start has a value of 0.
        Hashtable<Node, Integer> gScore = new Hashtable<>(vertices.size()+1);
        for (Node node : vertices) {
            Integer value = 500;
            if(node.equals(start)) {
                value = 0;
            }
            gScore.put(node, value);
        }

        //map all nodes to have fScore of 1000 which represents infinity
        //start has a value of the heuristic distance of start to exit.
        Hashtable<Node, Integer> fScore = new Hashtable<>(vertices.size()+1);
        for(Node node : vertices) {
            Integer value = 500;
            if(node.equals(start)) {
                value = getHeuDist(start, exit);
            }
            fScore.put(node, value);
        }

        Stack<Node> path = new Stack<>();
        System.out.println("Finding path.");
        while(!openSet.isEmpty()) {

            Iterator<Node> openSetIt = openSet.iterator();
            Node current = openSetIt.next();
            while(openSetIt.hasNext()) {
                Node next = openSetIt.next();
                current = (fScore.get(next) < fScore.get(current))? next : current;
            }
            if(current.equals(exit)) {
                // end loop and build path
                System.out.println("Generating path");
                path.push(exit);
                while(cameFrom.get(current) != null) {
                    current = cameFrom.get(current);
                    path.push(current);
                }
                break;
            }
            openSet.remove(current);
            closedSet.add(current);
            Set<Node> neighbours = current.getNeighbours();

            for(Node neighbour : neighbours) {
                if(closedSet.contains(neighbour)) {
                    continue;
                }
                int temp_gScore = gScore.get(current) + 1;
                if(!openSet.contains(neighbour)) {
                    openSet.add(neighbour);
                }
                else if (temp_gScore >= gScore.get(neighbour)) {
                    continue;
                }

                cameFrom.put(neighbour, current);
                gScore.put(neighbour, temp_gScore);
                fScore.put(neighbour, temp_gScore + getHeuDist(neighbour, exit));
            }
        }

        //walk path
        System.out.println("Walking path");
        path.pop();
        try  {
            state.pickUpGold();
        } catch (IllegalStateException ex) {
            //do nothing
        }
        while(!path.isEmpty()) {
            state.moveTo(path.pop());
            try  {
                state.pickUpGold();
            } catch (IllegalStateException ex) {
                //do nothing
            }
        }
    }

    private int getHeuDist(Node a, Node b) {

        int aX = a.getTile().getColumn();
        int aY = a.getTile().getRow();
        int bX = b.getTile().getColumn();
        int bY = b.getTile().getRow();

        return (int) Math.sqrt((aX-bX)*(aX-bX) + (aY-bY)*(aY-bY));
    }
}
