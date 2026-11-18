package lab8;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Locale;
import util.Chart;

public class Main {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        String outputDir = "lab8/output";
        new File(outputDir).mkdirs();

        double epsilon = 1e-10;
        int maxIterations = 500;

        System.out.println("Лабораторна робота №8 (Розв'язування нелінійних рівнянь)");

        double startX = 0.0;
        double endX = 4.0;
        double step = 0.1;
        String tabulationPath = outputDir + "/tabulation.txt";

        double[][] tabulatedData = TranscendentalEquation.tabulateAndSave(startX, endX, step, tabulationPath);
        List<TranscendentalEquation.RootInterval> intervals = TranscendentalEquation.findRootIntervals(tabulatedData[0], tabulatedData[1]);

        printTabulationSummary(intervals, tabulationPath);

        TranscendentalEquation.RootInterval firstInterval = intervals.get(0);
        TranscendentalEquation.RootInterval secondInterval = intervals.get(1);

        NonlinearSolvers.SolverResult[] firstRootResults = runTranscendentalSolvers(firstInterval, epsilon, maxIterations);
        NonlinearSolvers.SolverResult[] secondRootResults = runTranscendentalSolvers(secondInterval, epsilon, maxIterations);

        System.out.println("\nУточнення першого кореня (F'(x) > 0, інтервал [0.3; 0.4], eps = 1e-10)");
        printSolverTable(firstRootResults);

        System.out.println("\nУточнення другого кореня (F'(x) < 0, інтервал [2.2; 2.3], eps = 1e-10)");
        printSolverTable(secondRootResults);

        plotTranscendentalFunction(startX, endX, tabulatedData, firstRootResults[1].root, secondRootResults[1].root, outputDir);
        plotConvergenceChart("Збіжність методів для першого кореня x1* (F'(x) > 0)", firstRootResults, outputDir + "/02_convergence_root1.png");
        plotConvergenceChart("Збіжність методів для другого кореня x2* (F'(x) < 0)", secondRootResults, outputDir + "/03_convergence_root2.png");

        runPolynomialAnalysis(epsilon, maxIterations, outputDir);
    }

    private static void printTabulationSummary(List<TranscendentalEquation.RootInterval> intervals, String tabulationPath) {
        System.out.println("\nТабуляція функції F(x) = sin(x) - 0.2x - 0.3 на [0.0; 4.0] з кроком h = 0.1");
        System.out.println("Результати табуляції збережено у файл: " + tabulationPath);
        for (int i = 0; i < intervals.size(); i++) {
            TranscendentalEquation.RootInterval interval = intervals.get(i);
            String behavior = interval.isIncreasing ? "зростаюча (F'(x) > 0)" : "спадаюча (F'(x) < 0)";
            System.out.printf(
                    Locale.US,
                    "Корінь %d: [%.1f; %.1f], F(%.1f) = %+.6f, F(%.1f) = %+.6f, x0 = %.2f, F'(x0) = %+.6f (%s)%n",
                    i + 1,
                    interval.left,
                    interval.right,
                    interval.left,
                    TranscendentalEquation.evaluate(interval.left),
                    interval.right,
                    TranscendentalEquation.evaluate(interval.right),
                    interval.initialGuess,
                    interval.derivativeAtGuess,
                    behavior
            );
        }
    }

    private static NonlinearSolvers.SolverResult[] runTranscendentalSolvers(TranscendentalEquation.RootInterval interval, double epsilon, int maxIterations) {
        double tau = -0.85 / interval.derivativeAtGuess;
        return new NonlinearSolvers.SolverResult[]{
                NonlinearSolvers.solveSimpleIteration(interval.initialGuess, tau, epsilon, maxIterations),
                NonlinearSolvers.solveNewton(interval.initialGuess, epsilon, maxIterations),
                NonlinearSolvers.solveChebyshev(interval.initialGuess, epsilon, maxIterations),
                NonlinearSolvers.solveChords(interval.left, interval.right, epsilon, maxIterations),
                NonlinearSolvers.solveParabolas(interval.left, interval.initialGuess, interval.right, epsilon, maxIterations),
                NonlinearSolvers.solveInverseInterpolation(interval.left, interval.initialGuess, interval.right, epsilon, maxIterations)
        };
    }

    private static void plotTranscendentalFunction(double startX, double endX, double[][] tabulatedData, double firstRoot, double secondRoot, String outputDir) throws Exception {
        int gridPoints = 400;
        double[] denseX = MathUtils.generateLinearSpace(startX, endX, gridPoints);
        double[] denseY = new double[gridPoints];
        double[] zeroLine = new double[gridPoints];

        for (int i = 0; i < gridPoints; i++) {
            denseY[i] = TranscendentalEquation.evaluate(denseX[i]);
            zeroLine[i] = 0.0;
        }

        new Chart("Табуляція F(x) = sin(x) - 0.2x - 0.3 та локалізація коренів", "x", "F(x)")
                .line("F(x)", denseX, denseY, new Color(0x1f77b4), false)
                .line("y = 0", denseX, zeroLine, Color.GRAY, true)
                .points("Вузли (h = 0.1)", tabulatedData[0], tabulatedData[1], new Color(0x17becf))
                .points("Корені x1*, x2*", new double[]{firstRoot, secondRoot}, new double[]{0.0, 0.0}, new Color(0xd62728))
                .save(outputDir + "/01_transcendental_function.png");
    }

    private static void plotConvergenceChart(String title, NonlinearSolvers.SolverResult[] results, String outputPath) throws Exception {
        Chart convergenceChart = new Chart(title, "Номер ітерації k", "log10(|x_k - x_{k-1}|)");
        for (int i = 0; i < results.length; i++) {
            double[] iterations = MathUtils.generateIterationIndices(results[i].errorHistory.length);
            double[] logErrors = MathUtils.toLog10(results[i].errorHistory);
            Color seriesColor = Chart.PALETTE[i % Chart.PALETTE.length];
            convergenceChart.line(results[i].methodName, iterations, logErrors, seriesColor, false);
            convergenceChart.points("", iterations, logErrors, seriesColor);
        }
        convergenceChart.save(outputPath);
    }

    private static void runPolynomialAnalysis(double epsilon, int maxIterations, String outputDir) throws Exception {
        double[] initialCoefficients = {-10.0, 9.0, -4.0, 1.0};
        String coefficientsPath = outputDir + "/polynomial_coeffs.txt";

        PolynomialSolvers.saveCoefficientsToFile(initialCoefficients, coefficientsPath);
        double[] coefficients = PolynomialSolvers.readCoefficientsFromFile(coefficientsPath);

        System.out.println("\nАлгебраїчне рівняння 3-го степеня P(x) = a0 + a1*x + a2*x^2 + a3*x^3 = 0");
        System.out.printf(
                Locale.US,
                "Зчитані коефіцієнти з %s: a0 = %.1f, a1 = %.1f, a2 = %.1f, a3 = %.1f%n",
                coefficientsPath,
                coefficients[0],
                coefficients[1],
                coefficients[2],
                coefficients[3]
        );

        double initialGuessHorner = 3.0;
        NonlinearSolvers.SolverResult hornerResult = PolynomialSolvers.solveNewtonHorner(coefficients, initialGuessHorner, epsilon, maxIterations);
        System.out.printf(
                Locale.US,
                "Метод Ньютона (схема Горнера, x0 = %.1f): x1* = %.12f, |P(x1*)| = %s, ітерацій = %d%n",
                initialGuessHorner,
                hornerResult.root,
                MathUtils.formatScientific(hornerResult.functionValue),
                hornerResult.iterations
        );

        double initialAlpha = 0.5;
        double initialBeta = 1.5;
        PolynomialSolvers.LinResult linResult = PolynomialSolvers.solveLin(coefficients, initialAlpha, initialBeta, epsilon, maxIterations);
        System.out.printf(
                Locale.US,
                "Метод Ліна (alpha0 = %.1f, beta0 = %.1f): x2,3* = %.10f +- %.10f*i (p = %.6f, q = %.6f), ітерацій = %d%n",
                initialAlpha,
                initialBeta,
                linResult.alpha,
                linResult.beta,
                linResult.p,
                linResult.q,
                linResult.iterations
        );

        int gridPoints = 300;
        double[] gridX = MathUtils.generateLinearSpace(-0.5, 3.5, gridPoints);
        double[] gridY = new double[gridPoints];
        double[] zeroLine = new double[gridPoints];

        for (int i = 0; i < gridPoints; i++) {
            gridY[i] = PolynomialSolvers.evaluatePolynomial(coefficients, gridX[i]);
            zeroLine[i] = 0.0;
        }

        new Chart("Графік кубічного многочлена P(x) = x^3 - 4x^2 + 9x - 10", "x", "P(x)")
                .line("P(x)", gridX, gridY, new Color(0x1f77b4), false)
                .line("y = 0", gridX, zeroLine, Color.GRAY, true)
                .points("Дійсний корінь x1* = 2.0", new double[]{hornerResult.root}, new double[]{0.0}, new Color(0xd62728))
                .save(outputDir + "/04_cubic_polynomial.png");
    }

    private static void printSolverTable(NonlinearSolvers.SolverResult[] results) {
        System.out.println("Метод                        | Корінь x*        | |F(x*)|   | Ітерацій");
        for (NonlinearSolvers.SolverResult result : results) {
            System.out.printf(
                    Locale.US,
                    "%-28s | %.12f | %s | %2d%n",
                    result.methodName,
                    result.root,
                    MathUtils.formatScientific(result.functionValue),
                    result.iterations
            );
        }
    }
}