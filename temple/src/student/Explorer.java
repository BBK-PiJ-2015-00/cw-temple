package student;

import game.EscapeState;
import game.ExplorationState;

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

    private List<game.Node> vertices;
    private int maxX = 0;
    private int maxY = 0;

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

        vertices = (List<game.Node>) state.getVertices();
        ArrayList<game.Pair<Coord, game.Node>> pairs = new ArrayList<>();

        for(game.Node node : vertices) {
            int x = node.getTile().getColumn();
            int y = node.getTile().getColumn();
            maxX = Math.max(x, maxX);
            maxY = Math.max(x, maxY);
            pairs.add(new game.Pair<>(new Coord(x,y), node));
        }

        game.Node[][] nodeMaze = new game.Node[maxX][maxY];
        boolean[][] wasHere = new boolean[maxX][maxY];
        boolean[][] path = new boolean[maxX][maxY];
        for(game.Pair<Coord, game.Node> pair : pairs) {
            int x = pair.getFirst().x;
            int y = pair.getFirst().y;
            game.Node node = pair.getSecond();
            nodeMaze[x][y] = node;
            wasHere[x][y] = false;
            path[x][y] = false;
        }
    }

    private class Coord {
        public int x;
        public int y;

        Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
