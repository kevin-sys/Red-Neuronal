package com.inaoe.rna.utils;

public class HopfieldUtils {
    public static Object[] getDistanceBounds(double[][] D) {
        int n = D.length;
        double dL = D[0][1];
        double dU = D[0][1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    if (dL > D[i][j]) dL = D[i][j];
                    if (dU < D[i][j]) dU = D[i][j];
                }
            }
        }
        return new Object[]{dL, dU};
    }
}
