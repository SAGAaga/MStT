package legacy;

import java.util.Deque;

public class Navigator implements Processable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "NAV";
    Cave cave;
    MyPathfinder pathfinder;

    public Navigator() {
        setup();
    }

    protected void setup() {
        pathfinder = new MyPathfinder();
        cave = new Cave();
        cave.initUnknown();
    }

    @Override
    public String process(String msg) {
        return feelToAction(msg);
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
}