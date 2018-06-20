package com.elkin.bug;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;

/**
 * @author Elkin
 * @version $Id$
 */
public abstract class AbstractMaze implements Comparable<AbstractMaze>, Serializable {
    public static final transient int WIDTH = 29;
    public static final transient int HEIGHT = 19;
    private static final long serialVersionUID = -4526029720651458606L;
    private static final String MAZE_FILE_NAME = "bug.zip";

    public BigDecimal fitness = BigDecimal.ZERO;

    public AbstractMaze() {
    }

    public AbstractMaze(boolean[][] bugGrid) {
    }

    public static void save(boolean[][] bugGrid) {
        int HEIGHT = bugGrid.length;
        int WIDTH = bugGrid[0].length;
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(MAZE_FILE_NAME))) {
            for (boolean[] aBugGrid : bugGrid) {
                for (int i = 0; i < WIDTH; i++) {
                    pw.print(aBugGrid[i] ? "*" : "-");
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
        boolean[][] grid = new boolean[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++) {
            grid[i] = Arrays.copyOf(bugGrid[i], WIDTH);
        }
        return dfs(0, 0, grid);
    }

    private static boolean dfs(int x, int y, boolean[][] grid) {
        for (Direction direction : Direction.VALUES) {
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

    public abstract boolean[][] getBugGrid();

    public abstract void mutate(int count);

    public abstract void crossingover(AbstractMaze partner);

    public abstract void print();

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

//    public static boolean hasRoute(boolean[][] bugGrid) {
//        int WALL = -1;
//        int BLANK = -2;
//        int d = 0;
//        boolean found;
//        int[][] grid = new int[HEIGHT][WIDTH];
//        for (int i = 0; i < HEIGHT; i++) {
//            for (int j = 0; j < WIDTH; j++) {
//                if (bugGrid[i][j]) {
//                    grid[i][j] = WALL;
//                } else {
//                    grid[i][j] = BLANK;
//                }
//            }
//        }
//        if (grid[0][0] == WALL || grid[HEIGHT - 1][WIDTH - 1] == WALL) {
//            throw new RuntimeException("INVALID data");
//        }
//
//        grid[0][0] = 0;
//
//        do {
//            found = true;
//            for (int y = 0; y < HEIGHT; y++) {
//                for (int x = 0; x < WIDTH; x++) {
//                    if (grid[y][x] == d) {
//                        for (Direction direction : Direction.VALUES) {
//                            int dy = y + direction.dy;
//                            int dx = x + direction.dx;
//                            if (dy >= 0 && dy < HEIGHT && dx >= 0 && dx < WIDTH
//                                    && grid[dy][dx] == BLANK) {
//                                found = false;
//                                grid[dy][dx] = d + 1;
//                            }
//                        }
//                    }
//                }
//            }
//            d++;
//        } while (!found && grid[HEIGHT - 1][WIDTH - 1] == BLANK);
//
//        return grid[HEIGHT - 1][WIDTH - 1] != BLANK;
//    }

    public static boolean hasPath(int x, int y, boolean[][] map) {
        Cell target = new Cell(WIDTH - 1, HEIGHT - 1);
        Queue<Cell> q = new ArrayDeque<>();
        q.add(new Cell(x, y));
        int[][] dist = new int[HEIGHT][WIDTH];
        for (int[] ints : dist) {
            for (int i = 0; i < WIDTH; i++) {
                ints[i] = -1;
            }
        }
        dist[y][x] = 0;
        while (!q.isEmpty()) {
            Cell cur = q.remove();
            for (Direction direction : Direction.VALUES) {
                Cell to = cur.add(direction);
                if (!inside(to) || !isEmpty(to, target, map) || dist[to.y][to.x] != -1) {
                    continue;
                }
                dist[to.y][to.x] = dist[cur.y][cur.x] + 1;
                q.add(to);
            }
        }
        return false;
    }

    private static boolean inside(Cell to) {
        return to.x >= 0 && to.x < WIDTH && to.y >= 0 && to.y < HEIGHT;
    }

    private static boolean isEmpty(Cell cell, Cell target, boolean[][] map) {
        if (cell.equals(target)) {
            return true;
        }
        return !map[cell.y][cell.x];
    }

    public static class Cell {
        final int x;
        final int y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Cell add(Direction direction) {
            return new Cell(x + direction.dx, y + direction.dy);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return x == cell.x &&
                    y == cell.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
