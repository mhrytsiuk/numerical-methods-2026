package lab9;

public class NonlinearSystem {
    public static double evaluateFirstEquation(double[] x) {
        return x[1] - Math.cos(x[0]) - 0.5;
    }

    public static double evaluateSecondEquation(double[] x) {
        return x[1] - 0.5 * x[0] * x[0] + 0.5;
    }

    public static double evaluateSystemObjective(double[] x) {
        double f1 = evaluateFirstEquation(x);
        double f2 = evaluateSecondEquation(x);
        return f1 * f1 + f2 * f2;
    }

    public static double evaluateFirstCurve(double x1) {
        return Math.cos(x1) + 0.5;
    }

    public static double evaluateSecondCurve(double x1) {
        return 0.5 * x1 * x1 - 0.5;
    }

    public static double evaluateRosenbrock(double[] x) {
        double term1 = x[0] * x[0] - x[1];
        double term2 = x[0] - 1.0;
        return 100.0 * term1 * term1 + term2 * term2;
    }
}
