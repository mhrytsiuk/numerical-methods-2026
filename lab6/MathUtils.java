package lab6;

import java.math.BigDecimal;
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

    public static double evaluateResidualAccuracy(double[][] aMatrix, double[] xVector, double[] bVector) {
        double[] ax = multiplyMatrixVector(aMatrix, xVector);
        double[] diff = new double[bVector.length];
        for (int i = 0; i < bVector.length; i++) {
            diff[i] = ax[i] - bVector[i];
        }
        return vectorNorm(diff);
    }

    public static double[] computeResidualHighPrecision(double[][] aMatrix, double[] xVector, double[] bVector) {
        int n = bVector.length;
        double[] residual = new double[n];
        for (int i = 0; i < n; i++) {
            BigDecimal sum = BigDecimal.valueOf(bVector[i]);
            for (int j = 0; j < n; j++) {
                BigDecimal product = BigDecimal.valueOf(aMatrix[i][j]).multiply(BigDecimal.valueOf(xVector[j]));
                sum = sum.subtract(product);
            }
            residual[i] = sum.doubleValue();
        }
        return residual;
    }

    public static double formatMaxSolutionError(double[] xVector, double[] exactX) {
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