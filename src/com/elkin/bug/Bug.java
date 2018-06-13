package com.elkin.bug;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Elkin
 * @version $Id$
 */
public class Bug {

    private static final int POPULATION_SIZE = 100;
    private static final int CORE_POOL_SIZE = POPULATION_SIZE * 10 / 100;
    private static final int MAXIMUM_POOL_SIZE = POPULATION_SIZE * 20 / 100;
    private static final int ELITE_RATE = 20; // in percentage
    private static final int ELITE_SIZE = POPULATION_SIZE * ELITE_RATE / 100;
    private static final int MUTATION_RATE = 2;
    private static final int MUTATION_SIZE = POPULATION_SIZE * MUTATION_RATE / 100;

    private static final Random random = new Random(System.currentTimeMillis());
    private static final SimpleDateFormat SDF = new SimpleDateFormat("mm:ss (SSS)");
    private static final String BUG_STATE_FILE_NAME = "bug_state.bin";

    private BigDecimal bestFitness = BigDecimal.ZERO;
    private boolean interrupted = false;

    private AbstractMaze getMaze() {
        return new BugMaze();
    }

    private AbstractMaze getMaze(boolean[][] bugMaze) {
        return new BugMaze(bugMaze);
    }

    public void solve() {
        solver(population -> {
            for (int i = 0; i < POPULATION_SIZE; i++) {
                calc(population, i).run();
            }
        });
    }

    public void solveParallel() {
        solver((population) -> {
            ThreadPoolExecutor tpe = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(POPULATION_SIZE));
            for (int i = 0; i < POPULATION_SIZE; i++) {
                tpe.execute(calc(population, i));
            }
            tpe.shutdown();
            while (!tpe.isTerminated()) ;
        });
    }

    private Runnable calc(AbstractMaze[] population, int index) {
        return () -> {
            boolean[][] bugGrid = population[index].getBugGrid();
            population[index].fitness = calcFitness(bugGrid);
            if (population[index].fitness.equals(BigDecimal.ZERO)) {
                population[index] = getMaze();
                population[index].fitness = calcFitness(bugGrid);
            }

            if (population[index].fitness.compareTo(bestFitness) > 0) {
                bestFitness = population[index].fitness;
                System.out.println("best = " + bestFitness);
                AbstractMaze.save(bugGrid);
            }
        };
    }

    private void solver(SolverAction solver) {
        AbstractMaze[] population = readState();
        if (population == null) {
            population = new AbstractMaze[POPULATION_SIZE];
//            boolean[][] bestMaze = AbstractMaze.open();
//            population[0] = getMaze(bestMaze);
            for (int i = 0; i < POPULATION_SIZE; i++) {
                population[i] = getMaze();
            }
        }

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (!"exit".equals(scanner.next())) ;
            interrupted = true;
        }).start();

        int populationIndex = 1;
        long startCycleTime = System.currentTimeMillis();
        while (!interrupted) {
            solver.run(population);
            Arrays.sort(population);
            for (int i = ELITE_SIZE; i < POPULATION_SIZE; i++) {
                population[i].crossingover(population[random.nextInt(ELITE_SIZE)]);
                population[i].mutate(MUTATION_SIZE);
            }
            populationIndex++;

            if (populationIndex % 1000 == 0) {
                System.out.println("populationIndex = " + populationIndex);

                if ((System.currentTimeMillis() - startCycleTime) > 30 * 60 * 1000) {
                    startCycleTime = System.currentTimeMillis();
                    dump(population);
                }
            }
        }
        dump(population);
    }

    private void dump(AbstractMaze[] population) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BUG_STATE_FILE_NAME))) {
            oos.writeObject(population);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AbstractMaze[] readState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BUG_STATE_FILE_NAME))) {
            return (AbstractMaze[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BigDecimal simulateBug(boolean[][] bugGrid) {
        final BigDecimal WALL = BigDecimal.valueOf(-1);
        final BigDecimal BLANK = BigDecimal.ZERO;

        BigDecimal[][] grid = new BigDecimal[BugMaze.HEIGHT][BugMaze.WIDTH];
        for (int i = 0; i < BugMaze.HEIGHT; i++) {
            for (int j = 0; j < BugMaze.WIDTH; j++) {
                if (bugGrid[i][j]) {
                    grid[i][j] = WALL;

                } else {
                    grid[i][j] = BLANK;
                }
            }
        }
        grid[0][0] = BigDecimal.ONE;

        int x = 0;
        int y = 0;
        BigDecimal steps = BigDecimal.ZERO;
        Direction direction = Direction.DOWN;
        BigDecimal globalMin = BigDecimal.valueOf(Long.MAX_VALUE);
        while (x != (BugMaze.WIDTH - 1) && y != (BugMaze.HEIGHT - 1)) {
            BigDecimal min = globalMin;
            List<Direction> dirs = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                int dx = x + dir.dx;
                int dy = y + dir.dy;
                if (dx < 0 || dy < 0 || dx == BugMaze.WIDTH || dy == BugMaze.HEIGHT) {
                    continue;
                }

                if (grid[dy][dx].equals(WALL)) {
                    continue;
                }

                if (grid[dy][dx].compareTo(min) < 0) {
                    min = grid[dy][dx];
                    dirs.clear();
                    dirs.add(dir);

                } else if (grid[dy][dx].compareTo(min) == 0) {
                    dirs.add(dir);
                }

                if (grid[dy][dx].compareTo(globalMin) > 0) {
                    globalMin = grid[dy][dx];
                }
            }


            Direction toGo;
            if (dirs.contains(direction)) {
                toGo = direction;

            } else {
                dirs.sort(Direction.getComparator());
                toGo = dirs.get(0);
                direction = toGo;
            }

            x += toGo.dx;
            y += toGo.dy;
            grid[y][x] = grid[y][x].add(BigDecimal.ONE);
            steps = steps.add(BigDecimal.ONE);
        }

        return steps;
    }

    public BigDecimal calcFitness(boolean[][] bugGrid) {
        if (!AbstractMaze.hasRoute(bugGrid)) {
            return BigDecimal.ZERO;
        }
        return simulateBug(bugGrid);
    }

    public interface SolverAction {
        void run(AbstractMaze[] population);
    }
}
