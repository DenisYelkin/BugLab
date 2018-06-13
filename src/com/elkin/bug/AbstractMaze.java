package com.elkin.bug;

import java.io.*;
import java.math.BigDecimal;

/**
 * @author Elkin
 * @version $Id$
 */
public abstract class AbstractMaze implements Comparable<AbstractMaze>, Serializable {
    private static final long serialVersionUID = -4526029720651458606L;

    public static final transient int WIDTH = 29;
    public static final transient int HEIGHT = 19;
    private static final String MAZE_FILE_NAME = "bug.zip";

    public BigDecimal fitness = BigDecimal.ZERO;

    public abstract boolean[][] getBugGrid();

    public abstract void mutate(int count);

    public abstract void crossingover(AbstractMaze partner);

    public abstract void print();

    public AbstractMaze() {
    }

    public AbstractMaze(boolean[][] bugGrid) {
    }

    @Override
    public int compareTo(AbstractMaze o) {
        return o.fitness.compareTo(this.fitness);
    }

    public void print(boolean[][] grid) {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (i == 0 && j == 0) {
                    System.out.print("Ж");
                } else if (i == (HEIGHT - 1) && j == (WIDTH - 1)) {
                    System.out.println("X");
                } else {
                    System.out.print(grid[i][j] ? "*" : "-");
                }
            }
            System.out.println();
        }
    }

    public static void save(boolean[][] bugGrid) {
        int HEIGHT = bugGrid.length;
        int WIDTH = bugGrid[0].length;
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(MAZE_FILE_NAME))) {
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    pw.print(bugGrid[i][j] ? "*" : "-");
                }
                pw.print('\n');
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean[][] open() {
        boolean[][] grid = new boolean[HEIGHT][WIDTH];
        try (BufferedReader reader = new BufferedReader(new FileReader(MAZE_FILE_NAME))) {
            for (int i = 0; i < HEIGHT; i++) {
                String row = reader.readLine();
                char[] chars = row.toCharArray();
                for (int j = 0; j < WIDTH; j++) {
                    grid[i][j] = chars[j] == '*';
                }
            }
            grid[0][0] = false;
            grid[HEIGHT - 1][WIDTH - 1] = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grid;
    }

    public static boolean hasRoute(boolean[][] bugGrid) {
        return dfs(0, 0, bugGrid);
    }

    private static boolean dfs(int x, int y, boolean[][] grid) {
        for (Direction direction: Direction.values) {
            int dx = x + direction.dx;
            int dy = y + direction.dy;
            if (dx < 0 || dy < 0 || dx >= WIDTH || dy >= HEIGHT) {
                continue;
            }

            if (dx == (WIDTH - 1) && dy == (HEIGHT - 1)) {
                return true;
            }

            if (!grid[dy][dx]) {
                grid[dy][dx] = true;
                if (dfs(dx, dy, grid)) {
                    return true;
                }
            }
        }
        return false;
    }

//    public boolean hasRoute(boolean[][] bugGrid) {
//        int WALL = -1;
//        int BLANK = -2;
//        int d = 0;
//        boolean found;
//        int[][] grid = new int[BugMaze.HEIGHT][BugMaze.WIDTH];
//        for (int i = 0; i < BugMaze.HEIGHT; i++) {
//            for (int j = 0; j < BugMaze.WIDTH; j++) {
//                if (bugGrid[i][j]) {
//                    grid[i][j] = WALL;
//                } else {
//                    grid[i][j] = BLANK;
//                }
//            }
//        }
//        if (grid[0][0] == WALL || grid[BugMaze.HEIGHT - 1][BugMaze.WIDTH - 1] == WALL) {
//            throw new RuntimeException("INVALID data");
//        }
//
//        grid[0][0] = 0;
//
//        do {
//            found = true;
//            for (int y = 0; y < BugMaze.HEIGHT; y++) {
//                for (int x = 0; x < BugMaze.WIDTH; x++) {
//                    if (grid[y][x] == d) {
//                        for (Direction direction : Direction.values) {
//                            int dy = y + direction.dy;
//                            int dx = x + direction.dx;
//                            if (dy >= 0 && dy < BugMaze.HEIGHT && dx >= 0 && dx < BugMaze.WIDTH
//                                    && grid[dy][dx] == BLANK) {
//                                found = false;
//                                grid[dy][dx] = d + 1;
//                            }
//                        }
//                    }
//                }
//            }
//            d++;
//        } while (!found && grid[BugMaze.HEIGHT - 1][BugMaze.WIDTH - 1] == BLANK);
//
//        return grid[BugMaze.HEIGHT - 1][BugMaze.WIDTH - 1] != BLANK;
//    }
}
