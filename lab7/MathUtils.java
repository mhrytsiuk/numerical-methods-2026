package lab7;

import java.util.Locale;

public class MathUtils {
    public static double[] multiplyMatrixVector(double[][] matrix, double[] vector) {
        int n = matrix.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < vector.length; j++) {
                sum += matrix[i][j] * vector[j];
            }
            result[i] = sum;
        }
        return result;
    }

    public static double vectorNorm(double[] vector) {
        double maxAbs = 0.0;
        for (double val : vector) {
            double absVal = Math.abs(val);
            if (absVal > maxAbs) {
                maxAbs = absVal;
            }
        }
        return maxAbs;
    }

    public static double matrixNorm(double[][] matrix) {
        double maxRowSum = 0.0;
        for (double[] row : matrix) {
            double sum = 0.0;
            for (double val : row) {
                sum += Math.abs(val);
            }
            if (sum > maxRowSum) {
                maxRowSum = sum;
            }
        }
        return maxRowSum;
    }

    public static double residualNorm(double[][] aMatrix, double[] xVector, double[] fVector) {
        double[] ax = multiplyMatrixVector(aMatrix, xVector);
        double[] diff = new double[fVector.length];
        for (int i = 0; i < fVector.length; i++) {
            diff[i] = ax[i] - fVector[i];
        }
        return vectorNorm(diff);
    }

    public static double maxErrorFromExact(double[] xVector, double[] exactX) {
        double[] diff = new double[xVector.length];
        for (int i = 0; i < xVector.length; i++) {
            diff[i] = xVector[i] - exactX[i];
        }
        return vectorNorm(diff);
    }

    public static String format(double value, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", value);
    }

    public static String formatScientific(double value) {
        return String.format(Locale.US, "%.6e", value);
    }
}
