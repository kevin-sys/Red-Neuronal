package com.inaoe.rna;

import com.inaoe.rna.utils.TSPUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
public class Hopfield {

    private List<Double> energy;
    private double[][] V;
    private double[][] U;
    private double graph[][];
    private final double tao = 1;
    private int n;
    private double u0 = 0.02;
    private double delta = 0.0001;
    private final double u00 = 0.0;
    private final double iterations = 2000;

    private double nPrime = 15;
    private double A = 500;
    private double B = 500;
    private double C = 200;
    private double D = 500;

    public Hopfield(int n, double[][] graph) {
        this.n = n;
        this.graph = graph;
        init();
    }


    public void init() {
        this.V = new double[n][n];
        this.U = new double[n][n];
        this.energy = new ArrayList<>();

        double min = -0.1 * u0;
        double max = 0.1 * u0;
        var rnd = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                U[i][j] = u00 + (max - min) * rnd.nextDouble() + min;
            }
        }
    }

    public void setConstants(double A, double B, double C, double D, double nPrime) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.nPrime = nPrime;
    }

    public void updateOutputs(double[][] outputs) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                outputs[i][j] = outputNeuron(U[i][j]);
            }
        }
    }

    public Object[] force() {
        Object[] tuple = null;
        for (int i = 0; i < iterations; i++) {
            tuple = start();
            var valid = (boolean) tuple[1];
            if (valid) {
                return tuple;
            }
            init();
        }
        return tuple;
    }

    public Object[] start() {
        int iter = 0;

        for (; iter < iterations; iter++) {
            double[][] tmpV = new double[n][n];
            updateOutputs(tmpV);
            energy.add(energyFunction(tmpV));

            for (int x = 0; x < n; x++) {
                for (int i = 0; i < n; i++) {
                    U[x][i] = U[x][i] +  du(x, i) * delta;
                }
            }
            if (TSPUtils.verifyStability(V, tmpV)) {
                break;
            }
            V = tmpV;
        }
        var tour = getTour(V);
        return new Object[]{tour, isValid(tour)};
    }

    public static void printMatrix(double[][] V) {
        System.out.println("\n");
        for (int i = 0; i < V.length; i++) {
            System.out.println(Arrays.toString(V[i]));
        }
        System.out.println("\n");
    }


    public double outputNeuron(double uxi) {
        var tanh = Math.tanh(uxi / u0);
        return 0.5 * (1 + tanh);
    }

    public double[][] getState() {
        double[][] state = new double[n][n];
        for (int x = 0; x < n; x++) {
            for (int i = 0; i < n; i++) {
                state[x][i] = outputNeuron(U[x][i]);
            }
        }
        return state;
    }

    public double du(int x, int i) {
        double term1 = U[x][i] / tao;
        double term2 = 0;
        double term3 = 0;
        double term4 = 0;
        double term5 = 0;

        //computing second term
        for (int j = 0; j < n; j++) {
            if (j != i) {
                term2 += V[x][j];
            }
        }
        term2 = A * term2;

        //computing third term
        for (int y = 0; y < n; y++) {
            if (y != x) {
                term3 += V[y][i];
            }
        }
        term3 = B * term3;

        //computing fourth term
        for (int x_tmp = 0; x_tmp < n; x_tmp++) {
            for (int j = 0; j < n; j++) {
                term4 += V[x_tmp][j];
            }
        }
        term4 = term4 - (nPrime);
        term4 = C * term4;

        //computing fifth term
        for (int y = 0; y < n; y++) {
            if (i > 0) {
                term5 += graph[x][y] * (V[y][(i + 1) % n] + V[y][(i - 1) % n]);
            } else {
                term5 += graph[x][y] * (V[y][(i + 1) % n] + V[y][n - 1]);
            }
        }
        term5 = D * term5;

        return -term1 - term2 - term3 - term4 - term5;
    }

    public int[] getTour(double[][] state) {
        List<Integer> tour = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            double max = 0;
            int city = 0;
            for (int i = 0; i < n; i++) {
                if (state[i][j] != 0 && state[i][j] > max) {
                    max = state[i][j];
                    city = i;
                }
            }
            if (max != 0) tour.add(city);
        }
        return tour.stream().mapToInt(i -> i).toArray();
    }

    public boolean isValid(int[] tour) {
        if (tour.length != n) {
            return false;
        }

        int[] repetitions = new int[n];
        for (int i = 0; i < n; i++) {
            if (repetitions[tour[i]] > 0) {
                return false;
            }
            repetitions[tour[i]]++;
        }
        return true;
    }

    public double energyFunction(double[][] V) {
        double term1 = 0f;
        double term2 = 0f;
        double term3 = 0f;
        double term4 = 0f;

        // computing first term
        for (int x = 0; x < n; x++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        term1 += V[x][i] * V[x][j];
                    }
                }
            }
        }
        term1 = A / 2 * term1;

        // computing second term
        for (int i = 0; i < n; i++) {
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    if (y != x) {
                        term2 += V[x][i] * V[y][i];
                    }
                }
            }
        }
        term2 = B / 2 * term2;

        // computing third term
        for (int x = 0; x < n; x++) {
            for (int i = 0; i < n; i++) {
                term3 += V[x][i];
            }
        }
        term3 = Math.pow(term3 - n, 2);
        term3 = C / 2 * term3;

        // computing fourth term
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (y != x) {
                    for (int i = 0; i < n; i++) {
                        if (i != x) {
                            if (i > 0) {
                                term4 += graph[x][y] * V[x][i] * (V[y][(i + 1) % n] + V[y][(i - 1) % n]);
                            } else {
                                term4 += graph[x][y] * V[x][i] * (V[y][(i + 1) % n] + V[y][n - 1]);
                            }
                        }
                    }
                }
            }
        }
        term4 = D / 2 * term4;

        return term1 + term2 + term3 + term4;
    }
}