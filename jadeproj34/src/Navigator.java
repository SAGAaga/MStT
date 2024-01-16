import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.Console;
import java.util.*;

public class Navigator extends Agent {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "NAV";

    Cave cave;

    MyPathfinder pathfinder;

    protected void setup() {
        Helper.registerYellow(Helper.TYPE, NAME, this);
        addBehaviour(Helper.getWaitBehaviour(this, Speleologist.NAME, new ThinkBehaviour()));
        pathfinder = new MyPathfinder();
        cave = new Cave();
        cave.initUnknown();
    }

    protected void takeDown() {
        Helper.deregister(this);
        System.out.println("agent " + getAID().getName() + " terminating.");
    }

    private String feelToAction(String feels) {
        String room = KB.feelsToRoom(feels);
        if (feels.contains(KB.PER_SC)) {
            cave.clearWampus();
        }
        cave.update(room);
        cave.rationalize();
        cave.print();
        System.out.println("Agent direction: " + cave.dir);

        if (cave.wampus != null) {
            System.out.println(NAME + ": wampus at " + cave.wampus.x + ", " + cave.wampus.y);
            return killWampus();
        }
        if (room.contains(KB.GOLD)) {
            return grabAndRun();
        }
        Cell ok = findSafeCell(cave.rooms);
        if (ok == null) {
            return quit(new StringBuilder());
        }
        Cell current = cave.getCurrentRoom();
        Deque<Cell> path = pathfinder.bfsLee(MyPathfinder.caveToMap(cave.rooms), current, ok);
        return pathToAction(path);
    }

    private String grabAndRun() {
        StringBuilder action = new StringBuilder();
        action.append(KB.GRAB).append(", ");
        return quit(action);
    }

    private String quit(StringBuilder action) {
        Deque<Cell> path = pathfinder.bfsLee(MyPathfinder.caveToMap(cave.rooms), cave.getCurrentRoom(), new Cell(0, 3, 0));
        action.append(pathToAction(path)).append(", ");
        action.append(KB.CLIMB);
        return action.toString();
    }

    private String killWampus() {
        Deque<Cell> path = pathfinder.bfsLee(MyPathfinder.caveToMap(cave.rooms), cave.getCurrentRoom(), cave.lastStench);
        String pathAct = pathToAction(path);
        String faceAndKill = faceAndKill();
        return pathAct + ", " + faceAndKill;
    }

    private String faceAndKill() {
        Cell pos = cave.getCurrentRoom();

        Cave.Dir metaDir = getDir(cave.wampus.x - pos.x, cave.wampus.y - pos.y);

        int dif = metaDir.val - cave.dir.val;
        cave.setDir(metaDir);
        return switch (dif) {
            case -3, 1 -> KB.TURN_RIGHT + ", " + KB.SHOOT;
            case -1, 3 -> KB.TURN_LEFT + ", " + KB.SHOOT;
            case 0 -> KB.SHOOT;
            default -> KB.TURN_AROUND + ", " + KB.SHOOT;
        };
    }

    private String pathToAction(Deque<Cell> path) {
        if (path.isEmpty()) {
            return "";
        }
        StringBuilder action = new StringBuilder();
        Cell prev = path.pop();
        while (!path.isEmpty()) {
            Cell c = path.pop();
            action.append(getActionOf(c.x - prev.x, c.y - prev.y, cave.dir));
            action.append(", ");
            prev = c;
        }
        cave.setPos(prev.x, prev.y);
        if (action.length() > 0) {
            action.delete(action.length() - 2, action.length());
        }
        return action.toString();
    }

    public String getActionOf(int dx, int dy, Cave.Dir facing) {
        Cave.Dir metaDir = getDir(dx, dy);
        cave.setDir(metaDir);
        int dif = metaDir.val - facing.val;
        return switch (dif) {
            case -3, 1 -> KB.TURN_RIGHT + ", " + KB.FORWARD;
            case -1, 3 -> KB.TURN_LEFT + ", " + KB.FORWARD;
            case 0 -> KB.FORWARD;
            default -> KB.TURN_AROUND + ", " + KB.FORWARD;
        };
    }

    private Cave.Dir getDir(int dx, int dy) {
        if (dx < 0) return Cave.Dir.W;
        if (dx > 0) return Cave.Dir.E;
        if (dy < 0) return Cave.Dir.N;
        if (dy > 0) return Cave.Dir.S;
        return Cave.Dir.W;
    }

    private Cell findSafeCell(String[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                if (arr[i][j].contains(KB.SAFE) && !arr[i][j].contains(KB.VISITED)) {
                    return new Cell(j, i, 0);
                }
            }
        }
        return null;
    }


    private class ThinkBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;

        public void action() {
            ACLMessage inform = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (inform != null) {
                String result = feelToAction(inform.getContent());
                Helper.sendReply(inform, ACLMessage.PROPOSE, result, myAgent);
            } else {
                block();
            }
        }
    }
}