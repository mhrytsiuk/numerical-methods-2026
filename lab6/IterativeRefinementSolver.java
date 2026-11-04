package lab6;

import java.util.ArrayList;
import java.util.List;

public class IterativeRefinementSolver {
    public static class IterationStep {
        public final int iteration;
        public final double deltaXNorm;
        public final double residualNorm;
        public final double exactResidualNorm;
        public final double solutionErrorNorm;

        public IterationStep(int iteration, double deltaXNorm, double residualNorm, double exactResidualNorm, double solutionErrorNorm) {
            this.iteration = iteration;
            this.deltaXNorm = deltaXNorm;
            this.residualNorm = residualNorm;
            this.exactResidualNorm = exactResidualNorm;
            this.solutionErrorNorm = solutionErrorNorm;
        }
    }

    public static class RefinementResult {
        public final double[] refinedSolution;
        public final int iterationsCount;
        public final List<IterationStep> history;

        public RefinementResult(double[] refinedSolution, int iterationsCount, List<IterationStep> history) {
            this.refinedSolution = refinedSolution;
            this.iterationsCount = iterationsCount;
            this.history = history;
        }
    }

    public static RefinementResult refine(double[][] aMatrix, double[] bVector,
                                          LuDecompositionSolver.LuResult lu,
                                          double[] initialSolution, double[] exactSolution,
                                          double eps0, int maxIterations) {
        double[] currentX = initialSolution.clone();
        List<IterationStep> history = new ArrayList<>();
        int completedIterations = 0;

        for (int iter = 1; iter <= maxIterations; iter++) {
            double[] rVector = MathUtils.computeResidualHighPrecision(aMatrix, currentX, bVector);
            double[] deltaX = LuDecompositionSolver.solveWithLu(lu, rVector);

            for (int i = 0; i < currentX.length; i++) {
                currentX[i] += deltaX[i];
            }

            double deltaXNorm = MathUtils.vectorNorm(deltaX);
            double residualNorm = MathUtils.evaluateResidualAccuracy(aMatrix, currentX, bVector);
            double exactResidualNorm = MathUtils.vectorNorm(MathUtils.computeResidualHighPrecision(aMatrix, currentX, bVector));
            double solErrNorm = MathUtils.formatMaxSolutionError(currentX, exactSolution);

            history.add(new IterationStep(iter, deltaXNorm, residualNorm, exactResidualNorm, solErrNorm));
            completedIterations = iter;

            if (deltaXNorm <= eps0 && residualNorm <= eps0) {
                break;
            }
        }

        return new RefinementResult(currentX, completedIterations, history);
    }
}