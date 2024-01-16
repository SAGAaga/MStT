import java.util.*;

class MyPathfinder {
    static int ROW = 4;
    static int COL = 4;
    static int rowNum[] = {-1, 0, 0, 1};
    static int colNum[] = {0, -1, 1, 0};

    // Check whether given cell(row,col) is a valid cell or not
    boolean checkValid(int row, int col) {
        return ((row >= 0) && (row < ROW) && (col >= 0) && (col < COL));
    }

    public Deque<Cell> bfsLee(int mat[][], Cell src, Cell dest) {

        if (mat[src.y][src.x] != 1 || mat[dest.y][dest.x] != 1)
            return new ArrayDeque<>();

        boolean[][] visited = new boolean[ROW][COL];
        visited[src.y][src.x] = true;
        int[][] distance = new int[ROW][COL];
        Deque<Cell> q = new ArrayDeque<>();
        q.add(src);

        while (!q.isEmpty()) {
            Cell pt = q.peek();

            if (pt.x == dest.x && pt.y == dest.y) {
                dest = pt;
                break;
            }

            q.pop();
            // Otherwise enqueue its adjacent cells with value 1
            for (int i = 0; i < 4; i++) {
                int col = pt.x + rowNum[i];
                int row = pt.y + colNum[i];

                // Enqueue valid adjacent cell that is not visited
                if (checkValid(row, col) && mat[row][col] == 1 && !visited[row][col]) {
                    distance[row][col] = pt.dist + 1;
                    visited[row][col] = true;
                    Cell adjcell = new Cell(col, row, pt.dist + 1);
                    q.add(adjcell);
                }
            }
        }
        //assemble path
        q.clear();
        q.push(dest);
        while (q.peek().dist > 0) {
            Cell curr = q.peek();
            if (curr.x == src.x && curr.y == src.y) {
                break;
            }
            for (int i = 0; i < 4; i++) {
                int col = curr.x + rowNum[i];
                int row = curr.y + colNum[i];

                // Enqueue valid adjacent cell that is not visited
                if (checkValid(row, col) && mat[row][col] == 1 && visited[row][col] && distance[row][col] == curr.dist - 1) {
                    q.push(new Cell(col, row, distance[row][col]));
                    break;
                }
            }
        }

        return q;
    }

    public static int[][] caveToMap(String[][] cave) {
        int[][] map = new int[ROW][COL];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = KB.isOK(cave[i][j])?1:0;
            }
        }
        return map;
    }
}