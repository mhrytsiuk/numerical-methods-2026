package lab9;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HookeJeevesOptimizer {
    public static class TrajectoryPoint {
        public final double[] coordinates;
        public final double objectiveValue;

        public TrajectoryPoint(double[] coordinates, double objectiveValue) {
            this.coordinates = coordinates.clone();
            this.objectiveValue = objectiveValue;
        }
    }

    public static class OptimizationResult {
        public final double[] solution;
        public final double objectiveValue;
        public final List<TrajectoryPoint> trajectory;
        public final int trajectorySteps;
        public final int functionEvaluations;

        public OptimizationResult(double[] solution, double objectiveValue, List<TrajectoryPoint> trajectory, int functionEvaluations) {
            this.solution = solution.clone();
            this.objectiveValue = objectiveValue;
            this.trajectory = trajectory;
            this.trajectorySteps = trajectory.size() - 1;
            this.functionEvaluations = functionEvaluations;
        }
    }

    private static class ExploratoryOutcome {
        final double[] point;
        final double value;
        final double[] step;
        final int evaluations;

        ExploratoryOutcome(double[] point, double value, double[] step, int evaluations) {
            this.point = point;
            this.value = value;
            this.step = step;
            this.evaluations = evaluations;
        }
    }

    public static OptimizationResult minimize(
            ObjectiveFunction function,
            double[] initialPoint,
            double[] initialStep,
            double stepShrinkFactor,
            double patternFactor,
            double epsilonStep,
            double epsilonFunction,
            int maxIterations
    ) {
        int dimension = initialPoint.length;
        double[] basePoint = initialPoint.clone();
        double[] step = initialStep.clone();
        double baseValue = function.evaluate(basePoint);
        int evaluations = 1;

        List<TrajectoryPoint> trajectory = new ArrayList<>();
        trajectory.add(new TrajectoryPoint(basePoint, baseValue));

        int iterations = 0;
        while (iterations < maxIterations) {
            ExploratoryOutcome exploratory = exploratorySearch(function, basePoint, step, stepShrinkFactor, epsilonStep, true);
            evaluations += exploratory.evaluations;
            iterations++;

            double[] currentPoint = exploratory.point;
            double currentValue = exploratory.value;
            step = exploratory.step;

            if (Arrays.equals(currentPoint, basePoint)) {
                break;
            }

            trajectory.add(new TrajectoryPoint(currentPoint, currentValue));

            if (MathUtils.euclideanNorm(step) < epsilonStep && Math.abs(currentValue - baseValue) < epsilonFunction) {
                basePoint = currentPoint;
                baseValue = currentValue;
                break;
            }

            while (iterations < maxIterations) {
                double[] patternPoint = new double[dimension];
                for (int i = 0; i < dimension; i++) {
                    patternPoint[i] = currentPoint[i] + patternFactor * (currentPoint[i] - basePoint[i]);
                }

                ExploratoryOutcome patternSearch = exploratorySearch(function, patternPoint, step, stepShrinkFactor, epsilonStep, false);
                evaluations += patternSearch.evaluations;
                iterations++;

                if (patternSearch.value < currentValue) {
                    basePoint = currentPoint;
                    baseValue = currentValue;
                    currentPoint = patternSearch.point;
                    currentValue = patternSearch.value;
                    trajectory.add(new TrajectoryPoint(currentPoint, currentValue));

                    if (MathUtils.euclideanNorm(step) < epsilonStep && Math.abs(currentValue - baseValue) < epsilonFunction) {
                        basePoint = currentPoint;
                        baseValue = currentValue;
                        return new OptimizationResult(basePoint, baseValue, trajectory, evaluations);
                    }
                } else {
                    basePoint = currentPoint;
                    baseValue = currentValue;
                    break;
                }
            }
        }

        return new OptimizationResult(basePoint, baseValue, trajectory, evaluations);
    }

    private static ExploratoryOutcome exploratorySearch(
            ObjectiveFunction function,
            double[] startPoint,
            double[] initialStep,
            double stepShrinkFactor,
            double epsilonStep,
            boolean allowShrink
    ) {
        int dimension = startPoint.length;
        double[] point = startPoint.clone();
        double[] step = initialStep.clone();
        double currentValue = function.evaluate(point);
        int evaluations = 1;

        for (int i = 0; i < dimension; i++) {
            while (true) {
                double originalCoordinate = point[i];

                point[i] = originalCoordinate + step[i];
                double plusValue = function.evaluate(point);
                evaluations++;
                if (plusValue < currentValue) {
                    currentValue = plusValue;
                    break;
                }

                point[i] = originalCoordinate - step[i];
                double minusValue = function.evaluate(point);
                evaluations++;
                if (minusValue < currentValue) {
                    currentValue = minusValue;
                    break;
                }

                point[i] = originalCoordinate;
                if (!allowShrink) {
                    break;
                }

                step[i] /= stepShrinkFactor;
                if (step[i] < epsilonStep) {
                    break;
                }
            }
        }

        return new ExploratoryOutcome(point, currentValue, step, evaluations);
    }

    public static void saveTrajectoryToFile(List<TrajectoryPoint> trajectory, String filePath) throws IOException {
        StringBuilder builder = new StringBuilder("step;x1;x2;Phi(x1,x2)\n");
        for (int i = 0; i < trajectory.size(); i++) {
            TrajectoryPoint point = trajectory.get(i);
            builder.append(i)
                    .append(';')
                    .append(String.format(Locale.US, "%.8f", point.coordinates[0]))
                    .append(';')
                    .append(String.format(Locale.US, "%.8f", point.coordinates[1]))
                    .append(';')
                    .append(String.format(Locale.US, "%.6e", point.objectiveValue))
                    .append('\n');
        }
        Files.writeString(Paths.get(filePath), builder.toString());
    }
}
