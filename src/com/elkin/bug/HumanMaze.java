package com.elkin.bug;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Elkin
 * @version $Id$
 */
public class HumanMaze extends AbstractMaze {

    private static final transient int WIDTH = (AbstractMaze.WIDTH + 1) / 2;
    private static final transient int HEIGHT = (AbstractMaze.HEIGHT + 1) / 2;

    private static final transient Random random = new Random(System.currentTimeMillis());

    private int[][] grid;

    public HumanMaze() {
        grid = new int[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++) {
            Arrays.fill(grid[i], 0);
        }
        recursiveBacktracking(0, 0);
    }

    public HumanMaze(boolean[][] bugGrid) {
        this();
    }

    private void recursiveBacktracking(int x, int y) {
        Direction[] directions = Direction.getShuffledValues();
        for (Direction direction : directions) {
            int dx = x + direction.dx;
            int dy = y + direction.dy;
            if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && grid[dy][dx] == 0) {
                grid[y][x] |= direction.weight;
                grid[dy][dx] |= direction.getOppositeByWeight();
                recursiveBacktracking(dx, dy);
            }
        }
    }

    @Override
    public boolean[][] getBugGrid() {
        boolean[][] result = new boolean[AbstractMaze.HEIGHT][AbstractMaze.WIDTH];
        for (int i = 0; i < AbstractMaze.HEIGHT; i++) {
            Arrays.fill(result[i], true);
        }

        for (int y = 0; y < AbstractMaze.HEIGHT; y += 2) {
            for (int x = 0; x < AbstractMaze.WIDTH; x += 2) {
                result[y][x] = false;
                for (Direction direction : Direction.VALUES) {
                    if ((grid[y / 2][x / 2] & direction.weight) != 0) {
                        int dx = x + direction.dx;
                        int dy = y + direction.dy;
                        if (dx >= 0 && dx < AbstractMaze.WIDTH && dy >= 0 && dy < AbstractMaze.HEIGHT) {
                            result[dy][dx] = false;
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void print() {
        System.out.print(' ');
        for (int i = 0; i < (WIDTH - 1); i++) {
            System.out.print('_');
        }
        System.out.println();
        for (int y = 0; y < HEIGHT; y++) {
            System.out.print('|');
            for (int x = 0; x < WIDTH; x++) {
                System.out.println(((grid[y][x] & Direction.DOWN.weight) != 0) ? ' ' : '_');
                if ((grid[y][x] & Direction.RIGHT.weight) != 0) {
                    System.out.print(((grid[y][x] | grid[y][x + 1]) & Direction.DOWN.weight) != 0 ? ' ' : '_');
                } else {
                    System.out.print('|');
                }
            }
            System.out.println();
        }
    }

    @Override
    public void mutate(int count) {
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            grid[y][x] = Direction.random().weight;
        }
    }

    @Override
    public void crossingover(AbstractMaze partner) {
        HumanMaze parent = (HumanMaze) partner;
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (random.nextBoolean()) {
                    grid[i][j] = parent.grid[i][j];
                }
            }
        }
    }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream("bug.zip"))) {
            pw.print(' ');
            for (int i = 0; i < (WIDTH - 1); i++) {
                pw.print('_');
            }
            pw.print('\n');
            for (int y = 0; y < HEIGHT; y++) {
                pw.print('|');
                for (int x = 0; x < WIDTH; x++) {
                    pw.print(((grid[y][x] & Direction.DOWN.weight) != 0) ? ' ' : '_');
                    if ((grid[y][x] & Direction.RIGHT.weight) != 0 && x != (WIDTH - 1)) {
                        pw.print(((grid[y][x] | grid[y][x + 1]) & Direction.DOWN.weight) != 0 ? ' ' : '_');
                    } else {
                        pw.print('|');
                    }
                }
                pw.print('\n');
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
