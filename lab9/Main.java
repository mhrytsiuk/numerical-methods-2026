package lab9;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Locale;
import util.Chart;

public class Main {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        String outputDir = "lab9/output";
        new File(outputDir).mkdirs();

        double stepShrinkFactor = 2.0;
        double patternFactor = 2.0;
        double epsilonStep = 1e-7;
        double epsilonFunction = 1e-12;
        int maxIterations = 10000;

        System.out.println("Лабораторна робота №9 (Метод Хука-Дживса багатовимірної оптимізації)");

        double[] rosenbrockStart = {-1.2, 0.0};
        double[] initialStep = {0.5, 0.5};
        HookeJeevesOptimizer.OptimizationResult rosenbrockResult = HookeJeevesOptimizer.minimize(
                NonlinearSystem::evaluateRosenbrock,
                rosenbrockStart,
                initialStep,
                stepShrinkFactor,
                patternFactor,
                epsilonStep,
                epsilonFunction,
                maxIterations
        );

        String rosenbrockTrajectoryPath = outputDir + "/rosenbrock_trajectory.txt";
        HookeJeevesOptimizer.saveTrajectoryToFile(rosenbrockResult.trajectory, rosenbrockTrajectoryPath);
        printRosenbrockSummary(rosenbrockStart, rosenbrockResult, rosenbrockTrajectoryPath);

        double[] systemStart = {0.5, 0.0};
        HookeJeevesOptimizer.OptimizationResult systemResult = HookeJeevesOptimizer.minimize(
                NonlinearSystem::evaluateSystemObjective,
                systemStart,
                initialStep,
                stepShrinkFactor,
                patternFactor,
                epsilonStep,
                epsilonFunction,
                maxIterations
        );

        String systemTrajectoryPath = outputDir + "/system_trajectory.txt";
        HookeJeevesOptimizer.saveTrajectoryToFile(systemResult.trajectory, systemTrajectoryPath);
        printSystemSummary(systemStart, systemResult, systemTrajectoryPath);

        plotSystemCurves(systemStart, systemResult.solution, outputDir);
        plotTrajectory(
                "Траєкторія спуску для функції Розенброка",
                rosenbrockResult.trajectory,
                rosenbrockStart,
                rosenbrockResult.solution,
                outputDir + "/02_rosenbrock_trajectory.png"
        );
        plotTrajectory(
                "Траєкторія спуску для системи нелінійних рівнянь",
                systemResult.trajectory,
                systemStart,
                systemResult.solution,
                outputDir + "/03_system_trajectory.png"
        );
        plotConvergence(rosenbrockResult.trajectory, systemResult.trajectory, outputDir);
    }

    private static void printRosenbrockSummary(double[] startPoint, HookeJeevesOptimizer.OptimizationResult result, String filePath) {
        System.out.println("\n1. Тестування методу Хука-Дживса на функції Розенброка");
        System.out.println("f(x1, x2) = 100*(x1^2 - x2)^2 + (x1 - 1)^2, q = 2, p = 2");
        System.out.printf(Locale.US, "Початкове наближення: X^(0) = (%.1f; %.1f), f(X^(0)) = %.4f%n",
                startPoint[0], startPoint[1], NonlinearSystem.evaluateRosenbrock(startPoint));
        System.out.printf(Locale.US, "Знайдений мінімум:    X*    = (%.8f; %.8f)%n",
                result.solution[0], result.solution[1]);
        System.out.printf(Locale.US, "Значення f(X*) = %s, кроків траєкторії = %d, обчислень = %d%n",
                MathUtils.formatScientific(result.objectiveValue), result.trajectorySteps, result.functionEvaluations);
        System.out.println("Траєкторію збережено у файл: " + filePath);
    }

    private static void printSystemSummary(double[] startPoint, HookeJeevesOptimizer.OptimizationResult result, String filePath) {
        System.out.println("\n2. Розв'язування системи нелінійних рівнянь методом Хука-Дживса");
        System.out.println("f1(x1, x2) = x2 - cos(x1) - 0.5 = 0;  f2(x1, x2) = x2 - 0.5*x1^2 + 0.5 = 0");
        System.out.printf(Locale.US, "Початкове наближення: X^(0) = (%.1f; %.1f), Phi(X^(0)) = %.4f%n",
                startPoint[0], startPoint[1], NonlinearSystem.evaluateSystemObjective(startPoint));
        System.out.printf(Locale.US, "Знайдений розв'язок:  X*    = (%.8f; %.8f)%n",
                result.solution[0], result.solution[1]);
        System.out.printf(Locale.US, "Phi(X*) = %s, |f1(X*)| = %s, |f2(X*)| = %s%n",
                MathUtils.formatScientific(result.objectiveValue),
                MathUtils.formatScientific(Math.abs(NonlinearSystem.evaluateFirstEquation(result.solution))),
                MathUtils.formatScientific(Math.abs(NonlinearSystem.evaluateSecondEquation(result.solution))));
        System.out.printf(Locale.US, "Кількість кроків на траєкторії спуску = %d, обчислень = %d%n",
                result.trajectorySteps, result.functionEvaluations);
        System.out.println("Траєкторію спуску збережено у файл: " + filePath);
    }

    private static void plotSystemCurves(double[] startPoint, double[] solution, String outputDir) throws Exception {
        int pointsCount = 300;
        double[] x1Grid = MathUtils.generateLinearSpace(0.0, 2.2, pointsCount);
        double[] firstCurveY = new double[pointsCount];
        double[] secondCurveY = new double[pointsCount];

        for (int i = 0; i < pointsCount; i++) {
            firstCurveY[i] = NonlinearSystem.evaluateFirstCurve(x1Grid[i]);
            secondCurveY[i] = NonlinearSystem.evaluateSecondCurve(x1Grid[i]);
        }

        new Chart("Графіки рівнянь системи та локалізація розв'язку", "x1", "x2")
                .line("x2 = cos(x1) + 0.5", x1Grid, firstCurveY, new Color(0x1f77b4), false)
                .line("x2 = 0.5*x1^2 - 0.5", x1Grid, secondCurveY, new Color(0x2ca02c), false)
                .points("Початкова точка X^(0)", new double[]{startPoint[0]}, new double[]{startPoint[1]}, new Color(0xff7f0e))
                .points("Розв'язок X*", new double[]{solution[0]}, new double[]{solution[1]}, new Color(0xd62728))
                .save(outputDir + "/01_system_curves.png");
    }

    private static void plotTrajectory(
            String title,
            List<HookeJeevesOptimizer.TrajectoryPoint> trajectory,
            double[] startPoint,
            double[] solution,
            String outputPath
    ) throws Exception {
        int size = trajectory.size();
        double[] x1Values = new double[size];
        double[] x2Values = new double[size];

        for (int i = 0; i < size; i++) {
            x1Values[i] = trajectory.get(i).coordinates[0];
            x2Values[i] = trajectory.get(i).coordinates[1];
        }

        new Chart(title, "x1", "x2")
                .line("Траєкторія спуску", x1Values, x2Values, new Color(0x1f77b4), false)
                .points("Початкова точка X^(0)", new double[]{startPoint[0]}, new double[]{startPoint[1]}, new Color(0xff7f0e))
                .points("Знайдений мінімум X*", new double[]{solution[0]}, new double[]{solution[1]}, new Color(0xd62728))
                .save(outputPath);
    }

    private static void plotConvergence(
            List<HookeJeevesOptimizer.TrajectoryPoint> rosenbrockTrajectory,
            List<HookeJeevesOptimizer.TrajectoryPoint> systemTrajectory,
            String outputDir
    ) throws Exception {
        double[] rosenbrockSteps = new double[rosenbrockTrajectory.size()];
        double[] rosenbrockLogValues = new double[rosenbrockTrajectory.size()];
        for (int i = 0; i < rosenbrockTrajectory.size(); i++) {
            rosenbrockSteps[i] = i;
            rosenbrockLogValues[i] = Math.log10(Math.max(rosenbrockTrajectory.get(i).objectiveValue, 1e-16));
        }

        double[] systemSteps = new double[systemTrajectory.size()];
        double[] systemLogValues = new double[systemTrajectory.size()];
        for (int i = 0; i < systemTrajectory.size(); i++) {
            systemSteps[i] = i;
            systemLogValues[i] = Math.log10(Math.max(systemTrajectory.get(i).objectiveValue, 1e-16));
        }

        new Chart("Збіжність цільової функції log10(Phi(X^(k)))", "Номер кроку k", "log10(Phi(X^(k)))")
                .line("Функція Розенброка", rosenbrockSteps, rosenbrockLogValues, new Color(0x1f77b4), false)
                .line("Система рівнянь (m = 2)", systemSteps, systemLogValues, new Color(0xd62728), false)
                .save(outputDir + "/04_convergence_phi.png");
    }
}
