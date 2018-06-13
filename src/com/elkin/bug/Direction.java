package com.elkin.bug;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * @author Elkin
 * @version $Id$
 */
public enum Direction {
    UP(0, -1, 2, 1),
    DOWN(0, 1, 4, 2),
    LEFT(-1, 0, 1, 8),
    RIGHT(1, 0, 3, 4);
    public final int dx, dy;
    public final int priority;
    public final int weight;

    public static final Direction[] values = Direction.values();
    private static final Random random = new Random(System.currentTimeMillis());

    Direction(int dx, int dy, int priority, int weight) {
        this.dx = dx;
        this.dy = dy;
        this.priority = priority;
        this.weight = weight;
    }

    public int getOppositeByWeight() {
        switch (this) {
            case UP:
                return DOWN.weight;
            case DOWN:
                return UP.weight;
            case LEFT:
                return RIGHT.weight;
            case RIGHT:
                return LEFT.weight;
        }
        return 0;
    }

    public static Direction[] getShuffledValues() {
        Direction[] directions = Arrays.copyOf(values, values.length);
        Arrays.sort(directions, (o1, o2) -> {
            int i = random.nextInt(3);
            if (i == 0) {
                return -1;
            }
            if (i == 1) {
                return 0;
            }
            return 1;
        });
        return directions;
    }

    public static Direction random() {
        int i = random.nextInt(4);
        switch (i) {
            case 0:
                return UP;
            case 1:
                return DOWN;
            case 2:
                return LEFT;
            case 3:
                return RIGHT;
        }
        return UP;
    }

    public static Comparator<Direction> getComparator() {
        Comparator<Direction> c = Comparator.comparingInt(o -> o.priority);
        return c.reversed();
    }
}
