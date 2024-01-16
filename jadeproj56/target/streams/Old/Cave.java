package Old;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Cave {
    public static final String[][] temp = {
            {"", "", "", ""},
            {"", "", "", ""},
            {"F", "F", "", ""},
            {"AF", "F", "", ""}
    };
    public String[][] rooms;
    public final int w = 4, h = 4;
    public long time;
    int x, y;
    Dir dir;

    Cell wampus = null;
    Cell lastStench = null;
    public boolean scream = false;
    public boolean gold = true;

    public String getAgentRoom() {
        return rooms[y][x];
    }

    public void eatAction(String content) {
        content = content
                .replaceAll("[)( ]", "")
                .replaceAll("Action", "")
                .replaceAll("Turn", "");
        String[] actions = content.split(",");
        int dx = 0;
        int dy = 0;
        for (String action : actions) {
            time++;
            if (KB.TURN_RIGHT.contains(action)) {
                dir = Dir.right(dir);
            }
            if (KB.TURN_LEFT.contains(action)) {
                dir = Dir.left(dir);
            }
            switch (action) {
                case KB.FORWARD -> setPos(x + getDx(dir), y + getDy(dir));
                case KB.SHOOT -> {
                    clearWampus();
                    scream = true;
                }
                case KB.GRAB -> {
                    rooms[y][x] = rooms[y][x].replace(KB.GOLD, "");
                    gold = false;
                }
            }
        }
    }

    public void clearWampus() {
        if (wampus != null) {
            rooms[wampus.y][wampus.x] = rooms[wampus.y][wampus.x].replaceAll(KB.PIT, "");
            rooms[wampus.y][wampus.x] = combine(rooms[wampus.y][wampus.x], KB.SAFE);
        }
        wampus = null;
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms.length; j++) {
                rooms[i][j] = rooms[i][j].replaceAll("[" + KB.WAMPUS + KB.STENCH + "]", "");
            }
        }
    }

    int getDx(Dir dir) {
        return switch (dir) {
            case W -> -1;
            case E -> 1;
            default -> 0;
        };
    }

    int getDy(Dir dir) {
        return switch (dir) {
            case S -> 1;
            case N -> -1;
            default -> 0;
        };
    }

    enum Dir {
        N(0), E(1), S(2), W(3);
        public final int val;

        public static Dir of(int val) {
            val %= 4;
            return switch (val) {
                case 1 -> E;
                case 2 -> S;
                case 3 -> W;
                default -> N;
            };
        }

        public static Dir left(Dir dir) {
            int val = dir.val - 1;
            if (val < 0) val += 4;
            return of(val);
        }

        public static Dir right(Dir dir) {
            int val = dir.val + 1;
            return of(val);
        }

        Dir(int i) {
            val = i;
        }
    }

    public Cave() {
        this.x = 0;
        this.y = 3;
        this.dir = Dir.E;
        initRooms();
    }

    public void initRooms() {
        this.rooms = new String[h][];
        for (int i = 0; i < h; i++) {
            rooms[i] = temp[i].clone();
        }
    }

    public void initUnknown() {
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms.length; j++) {
                if (rooms[i][j].isEmpty()) {
                    rooms[i][j] = KB.UNKNOWN;
                }
            }
        }
    }

    public void open() {
        placeStaff(rooms);
        atmosphere(rooms);
        simplify(rooms);
    }

    public void rationalize() {
        visit();
        markNotSure();
        collapse();
    }

    public void update(String room) {
        rooms[y][x] = combine(rooms[y][x], room);
    }

    public void setPos(int x, int y) {
        rooms[this.y][this.x] = rooms[this.y][this.x].replace(KB.AGENT, "");
        this.x = x;
        this.y = y;
        rooms[y][x] = combine(rooms[y][x], KB.AGENT);
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }

    public Cell getCurrentRoom() {
        return new Cell(x, y, 0);
    }

    public void visit() {
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms.length; j++) {
                if (rooms[i][j].contains(KB.AGENT) && !rooms[i][j].contains(KB.VISITED)) {
                    rooms[i][j] = rooms[i][j].concat(KB.VISITED);
                    rooms[i][j] = rooms[i][j].replace(KB.UNKNOWN, "");
                }
            }
        }
    }

    public void markNotSure() {
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms.length; j++) {
                if (rooms[i][j].contains(KB.BREEZE)) {
                    markAdjoinUnknown(rooms, j, i, KB.PIT);
                }
                if (rooms[i][j].contains(KB.STENCH)) {
                    markAdjoinUnknown(rooms, j, i, KB.WAMPUS);
                }
            }
        }
    }

    public void collapse() {
        Cell stench = null;
        int wampusCounter = 0;
        Cell lastWampus = null;
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms[i].length; j++) {
                if (rooms[i][j].contains(KB.VISITED) && !rooms[i][j].contains(KB.BREEZE)) {
                    unmarkAdjoinUnknown(rooms, j, i, KB.PIT);
                }
                if (rooms[i][j].contains(KB.VISITED) && !rooms[i][j].contains(KB.STENCH)) {
                    unmarkAdjoinUnknown(rooms, j, i, KB.WAMPUS);
                }
                if (!rooms[i][j].contains(KB.STENCH) && !rooms[i][j].contains(KB.BREEZE) && rooms[i][j].contains(KB.VISITED)) {
                    markAdjoinUnknown(rooms, j, i, KB.SAFE);
                    unmarkAdjoinUnknown(rooms, j, i, KB.UNKNOWN);
                }
                if (rooms[i][j].contains(KB.STENCH)) {
                    if (stench == null) {
                        stench = new Cell(j, i, 0);
                        lastStench = stench;
                    } else {
                        markWampus(stench, new Cell(j, i, 0));
                    }
                }
                if (rooms[i][j].contains(KB.WAMPUS)) {
                    wampusCounter++;
                    lastWampus = new Cell(j, i, 0);
                }
            }
        }
        if (wampusCounter == 1) {
            wampus = lastWampus;
        }
    }

    private void markWampus(Cell stench1, Cell stench2) {
        if (!rooms[stench1.y][stench2.x].contains(KB.VISITED)) {
            rooms[stench1.y][stench2.x] = combine(rooms[stench1.y][stench2.x], KB.WAMPUS);
            rooms[stench1.y][stench2.x] = rooms[stench1.y][stench2.x].replace(KB.UNKNOWN, "");
            wampus = new Cell(stench2.x, stench1.y, 0);
        } else if (!rooms[stench2.y][stench1.x].contains(KB.VISITED)) {
            rooms[stench2.y][stench1.x] = combine(rooms[stench2.y][stench1.x], KB.WAMPUS);
            rooms[stench2.y][stench1.x] = rooms[stench2.y][stench1.x].replace(KB.UNKNOWN, "");
            wampus = new Cell(stench1.x, stench2.y, 0);
        }
    }

    public void markAdjoinUnknown(String[][] arr, int x, int y, String ch) {
        inBoundsMarkUnknown(arr, x + 1, y, ch);
        inBoundsMarkUnknown(arr, x - 1, y, ch);
        inBoundsMarkUnknown(arr, x, y + 1, ch);
        inBoundsMarkUnknown(arr, x, y - 1, ch);
    }

    public void unmarkAdjoinUnknown(String[][] arr, int x, int y, String ch) {
        inBoundsUnmarkUnknown(arr, x + 1, y, ch);
        inBoundsUnmarkUnknown(arr, x - 1, y, ch);
        inBoundsUnmarkUnknown(arr, x, y + 1, ch);
        inBoundsUnmarkUnknown(arr, x, y - 1, ch);
    }

    public void inBoundsMarkUnknown(String[][] arr, int x, int y, String ch) {
        if (x >= 0 && y >= 0 && x < w && y < h && unknown(arr[y][x])) {
            arr[y][x] = combine(arr[y][x], ch);
        }
    }

    public void inBoundsUnmarkUnknown(String[][] arr, int x, int y, String ch) {
        if (x >= 0 && y >= 0 && x < w && y < h && unknown(arr[y][x])) {
            arr[y][x] = arr[y][x].replace(ch, "");
        }
    }

    public boolean unknown(String s) {
        return s.contains(KB.UNKNOWN);
    }

    public static String combine(String a, String b) {
        Set<String> set = new HashSet<>();
        String s = a.concat(b);
        for (char c : s.toCharArray()) {
            set.add(c + "");
        }
        return String.join("", set);
    }

    public void placeStaff(String[][] arr) {
        placeRandom(arr, KB.GOLD, KB.SAFE + KB.AGENT);

        placeRandom(arr, KB.WAMPUS, KB.SAFE + KB.AGENT + KB.PIT + KB.GOLD);
        placeRandom(arr, KB.PIT, KB.SAFE + KB.AGENT + KB.PIT + KB.GOLD + KB.WAMPUS);
        placeRandom(arr, KB.PIT, KB.SAFE + KB.AGENT + KB.PIT + KB.GOLD + KB.WAMPUS);
        placeRandom(arr, KB.PIT, KB.SAFE + KB.AGENT + KB.PIT + KB.GOLD + KB.WAMPUS);
    }

    public void atmosphere(String[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j].contains(KB.WAMPUS)) {
                    blow(arr, j, i, KB.STENCH);
                }
                if (arr[i][j].contains(KB.PIT)) {
                    blow(arr, j, i, KB.BREEZE);
                }
            }
        }
    }

    public void blow(String[][] arr, int x, int y, String ch) {
        inBoundsBlow(arr, x + 1, y, ch);
        inBoundsBlow(arr, x - 1, y, ch);
        inBoundsBlow(arr, x, y + 1, ch);
        inBoundsBlow(arr, x, y - 1, ch);
    }

    public void inBoundsBlow(String[][] arr, int x, int y, String ch) {
        if (x >= 0 && y >= 0 && x < w && y < h) {
            arr[y][x] = arr[y][x] + ch;
        }
    }

    public void simplify(String[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                arr[i][j] = Exclude(arr[i][j], KB.PIT, KB.STENCH);
                arr[i][j] = IncludeIfNot(arr[i][j], KB.SAFE, KB.SAFE);
                arr[i][j] = Exclude(arr[i][j], KB.PIT, KB.SAFE);
                arr[i][j] = Exclude(arr[i][j], KB.WAMPUS, KB.SAFE);
                arr[i][j] = arr[i][j].replace("BB", "B");
                arr[i][j] = arr[i][j].replace("FF", "F");
            }
        }
    }

    public String Exclude(String s, String a, String b) {
        if (s.contains(a)) {
            for (char c : b.toCharArray()) {
                s = s.replace(c + "", "");
            }
        }
        return s;
    }

    public String IncludeIfNot(String s, String a, String b) {
        if (!s.contains(a)) {
            return s + b;
        }
        return s;
    }

    public void placeRandom(String[][] arr, String ch, String not) {
        Random random = new Random();
        boolean fine = true;
        while (true) {
            fine = true;
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            for (char c : not.toCharArray()) {
                if (arr[y][x].contains(c + "")) {
                    fine = false;
                    break;
                }
            }
            if (fine) {
                arr[y][x] = arr[y][x] + ch;
                break;
            }
        }
    }

    public void print() {
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms[i].length; j++) {
                System.out.printf("%5s", rooms[i][j]);
            }
            System.out.println();
        }
        System.out.println("=================================================");
    }

    public String get(int x, int y) {
        return rooms[y][x];
    }
}
