package lab8;

import java.util.Locale;

public class MathUtils {
    public static double[] generateLinearSpace(double start, double end, int count) {
        double[] result = new double[count];
        for (int i = 0; i < count; i++) {
            result[i] = start + (end - start) * i / (count - 1);
        }
        return result;
    }

    public static double[] generateIterationIndices(int count) {
        double[] indices = new double[count];
        for (int i = 0; i < count; i++) {
            indices[i] = i + 1;
        }
        return indices;
    }

    public static double[] toLog10(double[] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Math.log10(Math.max(values[i], 1e-16));
        }
        return result;
    }

    public static String format(double value, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", value);
    }

    public static String formatScientific(double value) {
        return String.format(Locale.US, "%.3e", value);
    }
}