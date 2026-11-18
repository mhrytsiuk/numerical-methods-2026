package lab8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PolynomialSolvers {
    public static class LinResult {
        public final double alpha;
        public final double beta;
        public final double p;
        public final double q;
        public final double remainderB0;
        public final double remainderB1;
        public final int iterations;
        public final double lastStepDifference;

        public LinResult(double alpha, double beta, double p, double q, double remainderB0, double remainderB1, int iterations, double lastStepDifference) {
            this.alpha = alpha;
            this.beta = beta;
            this.p = p;
            this.q = q;
            this.remainderB0 = remainderB0;
            this.remainderB1 = remainderB1;
            this.iterations = iterations;
            this.lastStepDifference = lastStepDifference;
        }
    }

    public static void saveCoefficientsToFile(double[] coefficients, String filePath) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < coefficients.length; i++) {
            builder.append(String.format(Locale.US, "%.6f%n", coefficients[i]));
        }
        Files.writeString(Paths.get(filePath), builder.toString());
    }

    public static double[] readCoefficientsFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<Double> values = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                String[] parts = trimmed.split("=");
                String valueToken = parts.length > 1 ? parts[1].trim() : parts[0].trim();
                values.add(Double.parseDouble(valueToken));
            }
        }
        double[] coefficients = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            coefficients[i] = values.get(i);
        }
        return coefficients;
    }

    public static double evaluatePolynomial(double[] coefficients, double x) {
        double result = 0;
        for (int i = coefficients.length - 1; i >= 0; i--) {
            result = result * x + coefficients[i];
        }
        return result;
    }

    public static NonlinearSolvers.SolverResult solveNewtonHorner(double[] coefficients, double initialGuess, double epsilon, int maxIterations) {
        int degree = coefficients.length - 1;
        double currentX = initialGuess;
        List<Double> history = new ArrayList<>();
        int iterations = 0;
        double stepDifference = Double.MAX_VALUE;
        double polynomialValue = Double.MAX_VALUE;

        while (iterations < maxIterations) {
            iterations++;
            double[] bCoefficients = new double[degree + 1];
            double[] cCoefficients = new double[degree + 1];

            bCoefficients[degree] = coefficients[degree];
            cCoefficients[degree] = bCoefficients[degree];
            for (int i = degree - 1; i >= 1; i--) {
                bCoefficients[i] = coefficients[i] + currentX * bCoefficients[i + 1];
                cCoefficients[i] = bCoefficients[i] + currentX * cCoefficients[i + 1];
            }
            bCoefficients[0] = coefficients[0] + currentX * bCoefficients[1];

            double nextX = currentX - bCoefficients[0] / cCoefficients[1];
            stepDifference = Math.abs(nextX - currentX);
            polynomialValue = evaluatePolynomial(coefficients, nextX);
            history.add(Math.max(stepDifference, 1e-16));

            if (Math.abs(polynomialValue) < epsilon && stepDifference < epsilon) {
                currentX = nextX;
                break;
            }
            currentX = nextX;
        }

        return new NonlinearSolvers.SolverResult("Метод Ньютона (схема Горнера)", currentX, iterations, Math.abs(polynomialValue), stepDifference, history);
    }

    public static LinResult solveLin(double[] coefficients, double initialAlpha, double initialBeta, double epsilon, int maxIterations) {
        int degree = coefficients.length - 1;
        double alpha = initialAlpha;
        double beta = initialBeta;
        double p = -2.0 * alpha;
        double q = alpha * alpha + beta * beta;

        int iterations = 0;
        double lastDifference = Double.MAX_VALUE;
        double b0 = 0.0;
        double b1 = 0.0;

        while (iterations < maxIterations) {
            iterations++;
            double[] bCoefficients = new double[degree + 3];
            for (int i = degree; i >= 2; i--) {
                bCoefficients[i] = coefficients[i] - p * bCoefficients[i + 1] - q * bCoefficients[i + 2];
            }

            double nextQ = coefficients[0] / bCoefficients[2];
            double nextP = (coefficients[1] * bCoefficients[2] - coefficients[0] * bCoefficients[3]) / (bCoefficients[2] * bCoefficients[2]);

            b1 = coefficients[1] - p * bCoefficients[2] - q * bCoefficients[3];
            b0 = coefficients[0] - q * bCoefficients[2];

            double nextAlpha = -nextP / 2.0;
            double discriminant = nextQ - nextAlpha * nextAlpha;
            double nextBeta = Math.sqrt(Math.max(0.0, discriminant));

            double deltaAlpha = Math.abs(nextAlpha - alpha);
            double deltaBeta = Math.abs(nextBeta - beta);
            lastDifference = Math.max(deltaAlpha, deltaBeta);

            alpha = nextAlpha;
            beta = nextBeta;
            p = nextP;
            q = nextQ;

            if (deltaAlpha <= epsilon && deltaBeta <= epsilon) {
                break;
            }
        }

        return new LinResult(alpha, beta, p, q, b0, b1, iterations, lastDifference);
    }
}