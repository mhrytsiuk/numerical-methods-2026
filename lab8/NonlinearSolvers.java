package lab8;

import java.util.ArrayList;
import java.util.List;

public class NonlinearSolvers {
    public static class SolverResult {
        public final String methodName;
        public final double root;
        public final int iterations;
        public final double functionValue;
        public final double lastStepDifference;
        public final double[] errorHistory;

        public SolverResult(String methodName, double root, int iterations, double functionValue, double lastStepDifference, List<Double> history) {
            this.methodName = methodName;
            this.root = root;
            this.iterations = iterations;
            this.functionValue = functionValue;
            this.lastStepDifference = lastStepDifference;
            this.errorHistory = new double[history.size()];
            for (int i = 0; i < history.size(); i++) {
                this.errorHistory[i] = history.get(i);
            }
        }
    }

    private static boolean isConverged(double nextX, double currentX, double epsilon) {
        return Math.abs(TranscendentalEquation.evaluate(nextX)) < epsilon
                && Math.abs(nextX - currentX) < epsilon;
    }

    public static SolverResult solveSimpleIteration(double initialGuess, double tau, double epsilon, int maxIterations) {
        double currentX = initialGuess;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double nextX = currentX + tau * TranscendentalEquation.evaluate(currentX);
            stepDifference = Math.abs(nextX - currentX);
            history.add(Math.max(stepDifference, 1e-16));
            if (isConverged(nextX, currentX, epsilon)) {
                currentX = nextX;
                break;
            }
            currentX = nextX;
        }

        return new SolverResult("Метод простої ітерації", currentX, iterations, Math.abs(TranscendentalEquation.evaluate(currentX)), stepDifference, history);
    }

    public static SolverResult solveNewton(double initialGuess, double epsilon, int maxIterations) {
        double currentX = initialGuess;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double nextX = currentX - TranscendentalEquation.evaluate(currentX) / TranscendentalEquation.evaluateFirstDerivative(currentX);
            stepDifference = Math.abs(nextX - currentX);
            history.add(Math.max(stepDifference, 1e-16));
            if (isConverged(nextX, currentX, epsilon)) {
                currentX = nextX;
                break;
            }
            currentX = nextX;
        }

        return new SolverResult("Метод Ньютона", currentX, iterations, Math.abs(TranscendentalEquation.evaluate(currentX)), stepDifference, history);
    }

    public static SolverResult solveChebyshev(double initialGuess, double epsilon, int maxIterations) {
        double currentX = initialGuess;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double fValue = TranscendentalEquation.evaluate(currentX);
            double firstDerivative = TranscendentalEquation.evaluateFirstDerivative(currentX);
            double secondDerivative = TranscendentalEquation.evaluateSecondDerivative(currentX);
            double nextX = currentX - (fValue / firstDerivative) - (fValue * fValue * secondDerivative) / (2.0 * firstDerivative * firstDerivative * firstDerivative);
            stepDifference = Math.abs(nextX - currentX);
            history.add(Math.max(stepDifference, 1e-16));
            if (isConverged(nextX, currentX, epsilon)) {
                currentX = nextX;
                break;
            }
            currentX = nextX;
        }

        return new SolverResult("Метод Чебишева", currentX, iterations, Math.abs(TranscendentalEquation.evaluate(currentX)), stepDifference, history);
    }

    public static SolverResult solveChords(double previousX, double currentX, double epsilon, int maxIterations) {
        double x0 = previousX;
        double x1 = currentX;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double f0 = TranscendentalEquation.evaluate(x0);
            double f1 = TranscendentalEquation.evaluate(x1);
            double nextX = x1 - f1 * (x1 - x0) / (f1 - f0);
            stepDifference = Math.abs(nextX - x1);
            history.add(Math.max(stepDifference, 1e-16));
            if (isConverged(nextX, x1, epsilon)) {
                x1 = nextX;
                break;
            }
            x0 = x1;
            x1 = nextX;
        }

        return new SolverResult("Метод хорд", x1, iterations, Math.abs(TranscendentalEquation.evaluate(x1)), stepDifference, history);
    }

    public static SolverResult solveParabolas(double x0, double x1, double x2, double epsilon, int maxIterations) {
        double xm2 = x0;
        double xm1 = x1;
        double xn = x2;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double fn = TranscendentalEquation.evaluate(xn);
            double fnm1 = TranscendentalEquation.evaluate(xm1);
            double fnm2 = TranscendentalEquation.evaluate(xm2);

            double firstDividedDiff1 = (fn - fnm1) / (xn - xm1);
            double firstDividedDiff2 = (fnm1 - fnm2) / (xm1 - xm2);
            double secondDividedDiff = (firstDividedDiff1 - firstDividedDiff2) / (xn - xm2);

            double kFactor = firstDividedDiff1 + (xn - xm1) * secondDividedDiff;
            double discriminant = Math.sqrt(Math.max(0.0, kFactor * kFactor - 4.0 * fn * secondDividedDiff));

            double denominator1 = kFactor + discriminant;
            double denominator2 = kFactor - discriminant;
            double delta = Math.abs(denominator1) >= Math.abs(denominator2) ? -2.0 * fn / denominator1 : -2.0 * fn / denominator2;

            double nextX = xn + delta;
            stepDifference = Math.abs(nextX - xn);
            history.add(Math.max(stepDifference, 1e-16));
            if (isConverged(nextX, xn, epsilon)) {
                xn = nextX;
                break;
            }
            xm2 = xm1;
            xm1 = xn;
            xn = nextX;
        }

        return new SolverResult("Метод парабол", xn, iterations, Math.abs(TranscendentalEquation.evaluate(xn)), stepDifference, history);
    }

    public static SolverResult solveInverseInterpolation(double x0, double x1, double x2, double epsilon, int maxIterations) {
        double xm2 = x0;
        double xm1 = x1;
        double xn = x2;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double y0 = TranscendentalEquation.evaluate(xm2);
            double y1 = TranscendentalEquation.evaluate(xm1);
            double y2 = TranscendentalEquation.evaluate(xn);

            double term0 = (y1 * y2) / ((y0 - y1) * (y0 - y2)) * xm2;
            double term1 = (y0 * y2) / ((y1 - y0) * (y1 - y2)) * xm1;
            double term2 = (y0 * y1) / ((y2 - y0) * (y2 - y1)) * xn;
            double nextX = term0 + term1 + term2;

            stepDifference = Math.abs(nextX - xn);
            history.add(Math.max(stepDifference, 1e-16));
            if (isConverged(nextX, xn, epsilon)) {
                xn = nextX;
                break;
            }
            xm2 = xm1;
            xm1 = xn;
            xn = nextX;
        }

        return new SolverResult("Метод зворотної інтерполяції", xn, iterations, Math.abs(TranscendentalEquation.evaluate(xn)), stepDifference, history);
    }
}