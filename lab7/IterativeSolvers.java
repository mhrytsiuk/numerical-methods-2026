package lab7;

import java.util.ArrayList;
import java.util.List;

public class IterativeSolvers {
    public static class IterationRecord {
        public final int iteration;
        public final double stepNorm;
        public final double residualNorm;
        public final double exactErrorNorm;

        public IterationRecord(int iteration, double stepNorm, double residualNorm, double exactErrorNorm) {
            this.iteration = iteration;
            this.stepNorm = stepNorm;
            this.residualNorm = residualNorm;
            this.exactErrorNorm = exactErrorNorm;
        }
    }

    public static class SolverResult {
        public final String methodName;
        public final double[] solution;
        public final int iterationsCount;
        public final double iterationMatrixNorm;
        public final List<IterationRecord> history;

        public SolverResult(String methodName, double[] solution, int iterationsCount,
                            double iterationMatrixNorm, List<IterationRecord> history) {
            this.methodName = methodName;
            this.solution = solution;
            this.iterationsCount = iterationsCount;
            this.iterationMatrixNorm = iterationMatrixNorm;
            this.history = history;
        }
    }

    public static SolverResult solveSimpleIteration(double[][] aMatrix, double[] fVector,
                                                    double[] initialGuess, double[] exactSolution,
                                                    double eps0, int maxIterations) {
        int n = fVector.length;
        double normA = MathUtils.matrixNorm(aMatrix);
        double tau = 1.0 / normA;

        double[][] cMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cMatrix[i][j] = (i == j ? 1.0 : 0.0) - tau * aMatrix[i][j];
            }
        }
        double cNorm = MathUtils.matrixNorm(cMatrix);

        double[] currentX = initialGuess.clone();
        List<IterationRecord> history = new ArrayList<>();
        int completedIterations = 0;

        for (int iter = 1; iter <= maxIterations; iter++) {
            double[] nextX = new double[n];
            double[] diffX = new double[n];

            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) {
                    sum += aMatrix[i][j] * currentX[j];
                }
                nextX[i] = currentX[i] - tau * sum + tau * fVector[i];
                diffX[i] = nextX[i] - currentX[i];
            }

            currentX = nextX;
            double stepNorm = MathUtils.vectorNorm(diffX);
            double resNorm = MathUtils.residualNorm(aMatrix, currentX, fVector);
            double errNorm = MathUtils.maxErrorFromExact(currentX, exactSolution);

            history.add(new IterationRecord(iter, stepNorm, resNorm, errNorm));
            completedIterations = iter;

            if (stepNorm < eps0 && resNorm <= eps0) {
                break;
            }
        }

        return new SolverResult("Метод простої ітерації", currentX, completedIterations, cNorm, history);
    }

    public static SolverResult solveJacobi(double[][] aMatrix, double[] fVector,
                                           double[] initialGuess, double[] exactSolution,
                                           double eps0, int maxIterations) {
        int n = fVector.length;
        double[][] cMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cMatrix[i][j] = (i == j ? 0.0 : -aMatrix[i][j] / aMatrix[i][i]);
            }
        }
        double cNorm = MathUtils.matrixNorm(cMatrix);

        double[] currentX = initialGuess.clone();
        List<IterationRecord> history = new ArrayList<>();
        int completedIterations = 0;

        for (int iter = 1; iter <= maxIterations; iter++) {
            double[] nextX = new double[n];
            double[] diffX = new double[n];

            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        sum += aMatrix[i][j] * currentX[j];
                    }
                }
                nextX[i] = (fVector[i] - sum) / aMatrix[i][i];
                diffX[i] = nextX[i] - currentX[i];
            }

            currentX = nextX;
            double stepNorm = MathUtils.vectorNorm(diffX);
            double resNorm = MathUtils.residualNorm(aMatrix, currentX, fVector);
            double errNorm = MathUtils.maxErrorFromExact(currentX, exactSolution);

            history.add(new IterationRecord(iter, stepNorm, resNorm, errNorm));
            completedIterations = iter;

            if (stepNorm < eps0 && resNorm <= eps0) {
                break;
            }
        }

        return new SolverResult("Метод Якобі", currentX, completedIterations, cNorm, history);
    }

    public static SolverResult solveSeidel(double[][] aMatrix, double[] fVector,
                                           double[] initialGuess, double[] exactSolution,
                                           double eps0, int maxIterations) {
        int n = fVector.length;
        double[] currentX = initialGuess.clone();
        List<IterationRecord> history = new ArrayList<>();
        int completedIterations = 0;

        for (int iter = 1; iter <= maxIterations; iter++) {
            double[] nextX = currentX.clone();
            double[] diffX = new double[n];

            for (int i = 0; i < n; i++) {
                double sumLower = 0.0;
                for (int j = 0; j < i; j++) {
                    sumLower += aMatrix[i][j] * nextX[j];
                }
                double sumUpper = 0.0;
                for (int j = i + 1; j < n; j++) {
                    sumUpper += aMatrix[i][j] * currentX[j];
                }
                nextX[i] = (fVector[i] - sumLower - sumUpper) / aMatrix[i][i];
                diffX[i] = nextX[i] - currentX[i];
            }

            currentX = nextX;
            double stepNorm = MathUtils.vectorNorm(diffX);
            double resNorm = MathUtils.residualNorm(aMatrix, currentX, fVector);
            double errNorm = MathUtils.maxErrorFromExact(currentX, exactSolution);

            history.add(new IterationRecord(iter, stepNorm, resNorm, errNorm));
            completedIterations = iter;

            if (stepNorm < eps0 && resNorm <= eps0) {
                break;
            }
        }

        return new SolverResult("Метод Зейделя", currentX, completedIterations, 0.0, history);
    }
}
