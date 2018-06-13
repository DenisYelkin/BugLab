package com.elkin.bug;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Elkin
 * @version $Id$
 */
public class BugMaze extends AbstractMaze {

    private static final transient Random random = new Random(System.currentTimeMillis());

    private boolean[][] grid = new boolean[HEIGHT][WIDTH];

    public BugMaze() {
        recursiveBacktracking();
    }

    public BugMaze(boolean[][] bugGrid) {
        this.grid = bugGrid;
    }

    @Override
    public boolean[][] getBugGrid() {
        return grid;
    }

    private void stupidRandom() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                grid[i][j] = random.nextBoolean();
            }
        }
        grid[0][0] = false;
        grid[HEIGHT - 1][WIDTH - 1] = false;
    }

    private void recursiveBacktracking() {
        int WALL = -1;
        int BLANK = 0;
        int grid[][] = new int[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if ((i & 1) == 0 && (j & 1) == 0) {
                    grid[i][j] = BLANK;
                } else {
                    grid[i][j] = WALL;
                }
            }
        }
        recursiveBacktracking(0, 0, grid);


        for (int y = 0; y < HEIGHT; y += 2) {
            for (int x = 0; x < WIDTH; x += 2) {
                for (Direction direction : Direction.values) {
                    if ((grid[y][x] & direction.weight) != 0) {
                        int dx = x + direction.dx;
                        int dy = y + direction.dy;
                        grid[dy][dx] = BLANK;
                    }
                }
            }
        }

        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                this.grid[i][j] = grid[i][j] == WALL;
            }
        }
    }

    private void print(int[][] grid) {
        System.out.print(' ');
        for (int i = 0; i < (WIDTH * 2 - 1); i += 2) {
            System.out.print('_');
        }
        System.out.println();
        for (int y = 0; y < HEIGHT; y += 2) {
            System.out.print('|');
            for (int x = 0; x < WIDTH; x += 2) {
                System.out.print(((grid[y][x] & Direction.DOWN.weight) != 0) ? " " : "_");
                if ((grid[y][x] & Direction.RIGHT.weight) != 0) {
                    System.out.print(((grid[y][x] | grid[y][x + 2]) & Direction.DOWN.weight) != 0 ? " " : "_");
                } else {
                    System.out.print("|");
                }
            }
            System.out.println();
        }
    }

    private void recursiveBacktracking(int x, int y, int[][] grid) {
        Direction[] directions = Direction.getShuffledValues();
        for (Direction direction : directions) {
            int dx = x + direction.dx * 2;
            int dy = y + direction.dy * 2;
            if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && grid[dy][dx] == 0) {
                grid[y][x] |= direction.weight;
                grid[dy][dx] |= direction.getOppositeByWeight();
                recursiveBacktracking(dx, dy, grid);
            }
        }
    }

    @Override
    public void mutate(int count) {
        for (int i = 0; i < count; i++) {
            Map.Entry<Integer, Integer> mutateCellCoords = getMutateCellCoords();
            grid[mutateCellCoords.getValue()][mutateCellCoords.getKey()] = random.nextBoolean();
        }
    }

    private Map.Entry<Integer, Integer> getMutateCellCoords() {
        int x = 0;
        int y = 0;
        while (y == 0 && x == 0 || y == (HEIGHT - 1) && x == (WIDTH - 1)) {
            x = random.nextInt(WIDTH);
            y = random.nextInt(HEIGHT);
        }
        return new AbstractMap.SimpleEntry<>(x, y);
    }

    @Override
    public void crossingover(AbstractMaze parent) {
        BugMaze partner = (BugMaze) parent;
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (random.nextBoolean()) {
                    if (grid[i][j] != partner.grid[i][j]) {
                        grid[i][j] = partner.grid[i][j];
                        if (!hasRoute(grid)) {
                            grid[i][j] = !partner.grid[i][j];
                        }
                    }
                }
            }
        }
    }

    @Override
    public void print() {
        print(grid);
    }
}
