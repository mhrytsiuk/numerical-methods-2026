package lab8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TranscendentalEquation {
    public static class RootInterval {
        public final double left;
        public final double right;
        public final double initialGuess;
        public final double derivativeAtGuess;
        public final boolean isIncreasing;

        public RootInterval(double left, double right, double initialGuess, double derivativeAtGuess, boolean isIncreasing) {
            this.left = left;
            this.right = right;
            this.initialGuess = initialGuess;
            this.derivativeAtGuess = derivativeAtGuess;
            this.isIncreasing = isIncreasing;
        }
    }

    public static double evaluate(double x) {
        return Math.sin(x) - 0.2 * x - 0.3;
    }

    public static double evaluateFirstDerivative(double x) {
        return Math.cos(x) - 0.2;
    }

    public static double evaluateSecondDerivative(double x) {
        return -Math.sin(x);
    }

    public static double[][] tabulateAndSave(double start, double end, double step, String filePath) throws IOException {
        int nodeCount = (int) Math.round((end - start) / step) + 1;
        double[] xValues = new double[nodeCount];
        double[] yValues = new double[nodeCount];
        StringBuilder builder = new StringBuilder("i;x;F(x);F'(x)\n");

        for (int i = 0; i < nodeCount; i++) {
            xValues[i] = Math.round((start + i * step) * 10000.0) / 10000.0;
            yValues[i] = evaluate(xValues[i]);
            builder.append(i)
                    .append(';')
                    .append(String.format(Locale.US, "%.4f", xValues[i]))
                    .append(';')
                    .append(String.format(Locale.US, "%.8f", yValues[i]))
                    .append(';')
                    .append(String.format(Locale.US, "%.8f", evaluateFirstDerivative(xValues[i])))
                    .append('\n');
        }

        Files.writeString(Paths.get(filePath), builder.toString());
        return new double[][]{xValues, yValues};
    }

    public static List<RootInterval> findRootIntervals(double[] xValues, double[] yValues) {
        List<RootInterval> intervals = new ArrayList<>();
        for (int i = 0; i < xValues.length - 1; i++) {
            if (yValues[i] * yValues[i + 1] <= 0.0) {
                double left = xValues[i];
                double right = xValues[i + 1];
                double midpoint = 0.5 * (left + right);
                double derivative = evaluateFirstDerivative(midpoint);
                intervals.add(new RootInterval(left, right, midpoint, derivative, derivative > 0.0));
            }
        }
        return intervals;
    }
}