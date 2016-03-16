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

    private ArrayList<game.Node> vertices;
    private int maxX = 0;
    private int maxY = 0;

    private int endX;
    private int endY;

    private game.Node[][] nodeMaze;
    private boolean[][] wasHere;
    private boolean[][] path;
    private boolean[][] isOpen;
    private int[][] graph;

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

        vertices = new ArrayList<>(state.getVertices());
        System.out.println("Size: " + vertices.size());
        ArrayList<game.Pair<Coord, game.Node>> pairs = new ArrayList<>();

        for(game.Node node : vertices) {
            int x = node.getTile().getColumn();
            int y = node.getTile().getColumn();
            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);
            pairs.add(new game.Pair<>(new Coord(x,y), node));
        }
        //add walls to graph
        maxX+=2;
        maxY+=2;

        graph = new int[maxX][maxY];
        nodeMaze = new game.Node[maxX][maxY];
        wasHere = new boolean[maxX][maxY];
        path = new boolean[maxX][maxY];
        isOpen = new boolean[maxX][maxY];
        for(game.Pair<Coord, game.Node> pair : pairs) {
            int x = pair.getFirst().getX();
            int y = pair.getFirst().getY();
            game.Node node = pair.getSecond();
            wasHere[x][y] = false;
            path[x][y] = false;
            nodeMaze[x][y] = node;
            isOpen[x][y] = true;
        }
        for(int y = 0; y < maxY; y++) {
            for(int x = 0; x < maxX; x++) {
                System.out.print((isOpen[x][y])? 1 : 0);
            }
            System.out.print("\n");
        }
        for(int x = 0; x < maxX; x++) {
            for(int y = 0; y < maxY; y++) {
                if(x==0 || y==0 || x==maxX-1 || y==maxY-1) {
                    wasHere[x][y] = false;
                    path[x][y] = false;
                    nodeMaze[x][y] = null;
                    isOpen[x][y] = false;
                }
            }
        }

        game.Node start = state.getCurrentNode();
        game.Node end = state.getExit();
        int startX = start.getTile().getColumn();
        int startY = start.getTile().getRow();
        endX = end.getTile().getColumn();
        endY = end.getTile().getRow();
        nodeMaze[endX][endY] = end;
        isOpen[endX][endY] = true;


        if(!findPath(startX, startY)) {
            //throw some exception if cannot find path

            throw new IllegalArgumentException("Could not find path");
        }

        for(int y = 0; y < maxY; y++) {
            for(int x = 0; x < maxX; x++) {
                String out = (path[x][y])? "o":"-";
                System.out.print(out);
            }
            System.out.print("\n");
        }

        //walk path found
        //walkPath(startX, startY, state);

        int currentX = startX;
        int currentY = startY;
        while(currentX!=endX && currentY!=endY) {
            if(currentX!=0 && path[currentX-1][currentY]) { //move left
                state.moveTo(nodeMaze[currentX-1][currentY]);
                continue;
            }
            if(currentX<maxX && path[currentX+1][currentY]) { //move right
                state.moveTo(nodeMaze[currentX+1][currentY]);
                continue;
            }
            if(currentY!=0 && path[currentX][currentY-1]) { //move up
                state.moveTo(nodeMaze[currentX][currentY-1]);
                continue;
            }
            if(currentY<maxY && path[currentX][currentY+1]) { //move down
                state.moveTo(nodeMaze[currentX][currentY+1]);
                continue;
            }
        }
    }
    /*
    private boolean walkPath(int x, int y, EscapeState state) {
        if(x==endX && y==endY) {
            return true;
        }
        if(x>0 && path[x-1][y]) {
            state.moveTo(nodeMaze[x-1][y]);
            return walkPath(x-1, y, state);
        }
        if(y>0 && path[x][y-1]) {
            state.moveTo(nodeMaze[x][y-1]);
            return walkPath(x, y-1, state);
        }
        if(x<maxX && path[x+1][y]) {
            state.moveTo(nodeMaze[x+1][y]);
            return walkPath(x+1, y, state);
        }
        if(y<maxY && path[x][y+1]) {
            state.moveTo(nodeMaze[x][y+1]);
            return walkPath(x, y+1, state);
        }
        return false;
    }
    */
    private boolean findPath(int x, int y) {
        if (x == endX && y == endY) return true; // If you reached the end
        if (!isOpen[x][y] || wasHere[x][y]) return false;
        // If you are on a wall or already were here
        wasHere[x][y] = true;
        if (x != 0) // Checks if not on left edge
            if (findPath(x-1, y)) { // Recalls method one to the left
                path[x][y] = true; // Sets that path value to true;
                return true;
            }
        if (x != maxX-1) // Checks if not on right edge
            if (findPath(x+1, y)) { // Recalls method one to the right
                path[x][y] = true;
                return true;
            }
        if (y != 0)  // Checks if not on top edge
            if (findPath(x, y-1)) { // Recalls method one up
                path[x][y] = true;
                return true;
            }
        if (y != maxY-1) // Checks if not on bottom edge
            if (findPath(x, y+1)) { // Recalls method one down
                path[x][y] = true;
                return true;
            }
        return false;
    }

    public class Coord {
        private int x;
        private int y;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int getX() { return x; }
        public int getY() { return y; }
    }
}
