package lab6;

public class LuDecompositionSolver {
    public static class LuResult {
        public final double[][] lMatrix;
        public final double[][] uMatrix;

        public LuResult(double[][] lMatrix, double[][] uMatrix) {
            this.lMatrix = lMatrix;
            this.uMatrix = uMatrix;
        }
    }

    public static LuResult decompose(double[][] aMatrix) {
        int n = aMatrix.length;
        double[][] lMatrix = new double[n][n];
        double[][] uMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            uMatrix[i][i] = 1.0;
        }

        for (int k = 0; k < n; k++) {
            for (int i = k; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < k; j++) {
                    sum += lMatrix[i][j] * uMatrix[j][k];
                }
                lMatrix[i][k] = aMatrix[i][k] - sum;
            }

            if (Math.abs(lMatrix[k][k]) < 1e-300) {
                throw new ArithmeticException("Нульовий діагональний елемент l_kk на кроці k = " + k);
            }

            for (int i = k + 1; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < k; j++) {
                    sum += lMatrix[k][j] * uMatrix[j][i];
                }
                uMatrix[k][i] = (aMatrix[k][i] - sum) / lMatrix[k][k];
            }
        }

        return new LuResult(lMatrix, uMatrix);
    }

    public static double[] solveWithLu(LuResult lu, double[] bVector) {
        double[][] lMatrix = lu.lMatrix;
        double[][] uMatrix = lu.uMatrix;
        int n = bVector.length;

        double[] zVector = new double[n];
        zVector[0] = bVector[0] / lMatrix[0][0];
        for (int k = 1; k < n; k++) {
            double sum = 0.0;
            for (int j = 0; j < k; j++) {
                sum += lMatrix[k][j] * zVector[j];
            }
            zVector[k] = (bVector[k] - sum) / lMatrix[k][k];
        }

        double[] xVector = new double[n];
        xVector[n - 1] = zVector[n - 1];
        for (int k = n - 2; k >= 0; k--) {
            double sum = 0.0;
            for (int j = k + 1; j < n; j++) {
                sum += uMatrix[k][j] * xVector[j];
            }
            xVector[k] = zVector[k] - sum;
        }

        return xVector;
    }
}