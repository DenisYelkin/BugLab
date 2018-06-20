package com.elkin;

import com.elkin.bug.Bug;

public class Main {

    public static void main(String[] args) {
        Bug bug = new Bug();
        bug.solveParallel();
//        AbstractMaze[] population = bug.readState();
//        for (int i = 0; i < population.length; i++) {
//            population[i].fitness = bug.calcFitness(population[i].getBugGrid());
//            System.out.println(i + " - " + population[i].fitness);
//        }
    }
}
