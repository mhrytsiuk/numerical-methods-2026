package lab7;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import util.Chart;

public class Main {
    public static void main(String[] args) throws Exception {
        String outputDir = "lab7/output";
        new File(outputDir).mkdirs();

        int n = 100;
        double exactValue = 2.5;
        double initialGuessValue = 1.0;
        double eps0 = 1e-14;

        System.out.println("Лабораторна робота №7 (Ітераційні методи розв'язку систем лінійних алгебраїчних рівнянь)");

        String matrixFilePath = outputDir + "/matrix_A.txt";
        String vectorFilePath = outputDir + "/vector_B.txt";

        double[] exactSolution = generateAndSaveDiagonallyDominantSystem(n, exactValue, matrixFilePath, vectorFilePath);

        double[][] aMatrix = MatrixFileIO.readMatrixFromFile(matrixFilePath);
        double[] bVector = MatrixFileIO.readVectorFromFile(vectorFilePath);

        double[] initialGuess = createInitialGuess(n, initialGuessValue, aMatrix, bVector);

        IterativeSolvers.SolverResult simpleResult = IterativeSolvers.solveSimpleIteration(
                aMatrix, bVector, initialGuess, exactSolution, eps0, 1000);
        IterativeSolvers.SolverResult jacobiResult = IterativeSolvers.solveJacobi(
                aMatrix, bVector, initialGuess, exactSolution, eps0, 1000);
        IterativeSolvers.SolverResult seidelResult = IterativeSolvers.solveSeidel(
                aMatrix, bVector, initialGuess, exactSolution, eps0, 1000);

        printResultsSummary(aMatrix, simpleResult, jacobiResult, seidelResult, eps0);
        plotCharts(aMatrix, bVector, initialGuess, simpleResult, jacobiResult, seidelResult, outputDir);
    }

    private static double[] generateAndSaveDiagonallyDominantSystem(int n, double exactValue,
                                                                    String matrixFile, String vectorFile) throws Exception {
        Random rng = new Random(42L);
        double[][] aMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            double rowOffDiagSum = 0.0;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    double val = -0.008 + 0.012 * rng.nextDouble();
                    aMatrix[i][j] = val;
                    rowOffDiagSum += Math.abs(val);
                }
            }
            aMatrix[i][i] = (1.5 + 1.5 * (i / 100.0)) * rowOffDiagSum + 0.2;
        }

        double[] exactX = new double[n];
        Arrays.fill(exactX, exactValue);

        double[] bVector = MathUtils.multiplyMatrixVector(aMatrix, exactX);

        MatrixFileIO.writeMatrixToFile(aMatrix, matrixFile);
        MatrixFileIO.writeVectorToFile(bVector, vectorFile);

        System.out.println("\n1. Генерація матриці A (" + n + "x" + n + ") з діагональним переважанням та вектора B (при x_j = " + exactValue + ")");
        System.out.println("Матрицю A записано у файл: " + matrixFile);
        System.out.println("Вектор B записано у файл:  " + vectorFile);
        System.out.println(String.format(Locale.US, "Перші 5 діагональних елементів A[i][i]: [%.6f, %.6f, %.6f, %.6f, %.6f]",
                aMatrix[0][0], aMatrix[1][1], aMatrix[2][2], aMatrix[3][3], aMatrix[4][4]));
        System.out.println(String.format(Locale.US, "Перші 5 елементів вектора B:            [%.6f, %.6f, %.6f, %.6f, %.6f]",
                bVector[0], bVector[1], bVector[2], bVector[3], bVector[4]));

        return exactX;
    }

    private static double[] createInitialGuess(int n, double initialGuessValue, double[][] aMatrix, double[] bVector) {
        double[] x0 = new double[n];
        Arrays.fill(x0, initialGuessValue);

        double normA = MathUtils.matrixNorm(aMatrix);
        double initialResidual = MathUtils.residualNorm(aMatrix, x0, bVector);

        System.out.println("\n2-3. Перевірка умови діагонального переважання та задання початкового наближення X^(0)");
        System.out.println("Початкове наближення: x_i^(0) = " + initialGuessValue + " (для всіх i = 1.." + n + ")");
        System.out.println("Норма матриці ||A||_3 = max_i sum_j |a_ij| = " + MathUtils.format(normA, 6));
        System.out.println("Верхня межа параметра τ < 2 / ||A|| = " + MathUtils.format(2.0 / normA, 6)
                + ", вибрано τ = 1 / ||A|| = " + MathUtils.format(1.0 / normA, 6));
        System.out.println("Початкова норма нев'язки ||AX^(0) - B|| = " + MathUtils.formatScientific(initialResidual));

        return x0;
    }

    private static void printResultsSummary(double[][] aMatrix,
                                            IterativeSolvers.SolverResult simpleResult,
                                            IterativeSolvers.SolverResult jacobiResult,
                                            IterativeSolvers.SolverResult seidelResult,
                                            double eps0) {
        System.out.println("\n4. Результати розв'язання СЛАР ітераційними методами (точність eps_0 = " + String.format(Locale.US, "%.0e", eps0) + ")");
        System.out.println("Норма матриці ітерацій ||C||_3 (проста ітерація): " + MathUtils.format(simpleResult.iterationMatrixNorm, 6) + " < 1");
        System.out.println("Норма матриці ітерацій ||C||_3 (метод Якобі):     " + MathUtils.format(jacobiResult.iterationMatrixNorm, 6) + " < 1");

        System.out.println("\n Метод                   | К-сть ітерацій k | ||X^(k) - X^(k-1)|| |   ||AX^(k) - B||   | max|X^(k) - X*|");
        for (IterativeSolvers.SolverResult res : new IterativeSolvers.SolverResult[]{simpleResult, jacobiResult, seidelResult}) {
            IterativeSolvers.IterationRecord last = res.history.get(res.history.size() - 1);
            System.out.println(String.format(Locale.US, " %-23s | %16d | %19.6e | %18.6e | %15.6e",
                    res.methodName, res.iterationsCount, last.stepNorm, last.residualNorm, last.exactErrorNorm));
        }

        double[] xs = seidelResult.solution;
        System.out.println(String.format(Locale.US, "\nПерші 5 компонент знайденого розв'язку X (метод Зейделя): [%.15f, %.15f, %.15f, %.15f, %.15f]",
                xs[0], xs[1], xs[2], xs[3], xs[4]));
    }

    private static void plotCharts(double[][] aMatrix, double[] bVector, double[] initialGuess,
                                   IterativeSolvers.SolverResult simpleResult,
                                   IterativeSolvers.SolverResult jacobiResult,
                                   IterativeSolvers.SolverResult seidelResult,
                                   String outputDir) throws Exception {
        double[] simpleIters = extractIterations(simpleResult, false);
        double[] simpleStepLogs = extractStepLogs(simpleResult);
        double[] jacobiIters = extractIterations(jacobiResult, false);
        double[] jacobiStepLogs = extractStepLogs(jacobiResult);
        double[] seidelIters = extractIterations(seidelResult, false);
        double[] seidelStepLogs = extractStepLogs(seidelResult);

        new Chart("Збіжність норми кроку ||X^(k) - X^(k-1)|| ітераційних методів", "Номер ітерації k", "log10(||X^(k) - X^(k-1)||)")
                .line("Проста ітерація (k=" + simpleResult.iterationsCount + ")", simpleIters, simpleStepLogs, new Color(0x1f77b4), false)
                .line("Метод Якобі (k=" + jacobiResult.iterationsCount + ")", jacobiIters, jacobiStepLogs, new Color(0xff7f0e), true)
                .line("Метод Зейделя (k=" + seidelResult.iterationsCount + ")", seidelIters, seidelStepLogs, new Color(0x2ca02c), false)
                .save(outputDir + "/01_convergence_step_norm.png");

        double initResLog = Math.log10(Math.max(1e-16, MathUtils.residualNorm(aMatrix, initialGuess, bVector)));
        double[] simpleResIters = extractIterations(simpleResult, true);
        double[] simpleResLogs = extractResidualLogs(simpleResult, initResLog);
        double[] jacobiResIters = extractIterations(jacobiResult, true);
        double[] jacobiResLogs = extractResidualLogs(jacobiResult, initResLog);
        double[] seidelResIters = extractIterations(seidelResult, true);
        double[] seidelResLogs = extractResidualLogs(seidelResult, initResLog);

        new Chart("Збіжність норми нев'язки ||AX^(k) - B|| ітераційних методів", "Номер ітерації k", "log10(||AX^(k) - B||)")
                .line("Проста ітерація", simpleResIters, simpleResLogs, new Color(0x1f77b4), false)
                .line("Метод Якобі", jacobiResIters, jacobiResLogs, new Color(0xff7f0e), true)
                .line("Метод Зейделя", seidelResIters, seidelResLogs, new Color(0x2ca02c), false)
                .save(outputDir + "/02_convergence_residual_norm.png");

        double[] tolerancesLog = new double[13];
        double[] itersSimpleByTol = new double[13];
        double[] itersJacobiByTol = new double[13];
        double[] itersSeidelByTol = new double[13];

        for (int idx = 0; idx < 13; idx++) {
            int exp = -2 - idx;
            double tol = Math.pow(10.0, exp);
            tolerancesLog[idx] = -exp;
            itersSimpleByTol[idx] = findIterationsForTolerance(simpleResult, tol);
            itersJacobiByTol[idx] = findIterationsForTolerance(jacobiResult, tol);
            itersSeidelByTol[idx] = findIterationsForTolerance(seidelResult, tol);
        }

        new Chart("Кількість ітерацій залежно від заданої точності ε = 10^(-p)", "Порядок точності p = -log10(ε)", "Кількість ітерацій k")
                .line("Проста ітерація", tolerancesLog, itersSimpleByTol, new Color(0x1f77b4), false)
                .line("Метод Якобі", tolerancesLog, itersJacobiByTol, new Color(0xff7f0e), true)
                .line("Метод Зейделя", tolerancesLog, itersSeidelByTol, new Color(0x2ca02c), false)
                .points("Вузли (Зейдель)", tolerancesLog, itersSeidelByTol, new Color(0xd62728))
                .save(outputDir + "/03_iterations_vs_tolerance.png");
    }

    private static double[] extractIterations(IterativeSolvers.SolverResult res, boolean includeZero) {
        int size = res.history.size() + (includeZero ? 1 : 0);
        double[] arr = new double[size];
        int offset = 0;
        if (includeZero) {
            arr[0] = 0.0;
            offset = 1;
        }
        for (int i = 0; i < res.history.size(); i++) {
            arr[i + offset] = res.history.get(i).iteration;
        }
        return arr;
    }

    private static double[] extractStepLogs(IterativeSolvers.SolverResult res) {
        double[] arr = new double[res.history.size()];
        for (int i = 0; i < res.history.size(); i++) {
            arr[i] = Math.log10(Math.max(1e-16, res.history.get(i).stepNorm));
        }
        return arr;
    }

    private static double[] extractResidualLogs(IterativeSolvers.SolverResult res, double initResLog) {
        double[] arr = new double[res.history.size() + 1];
        arr[0] = initResLog;
        for (int i = 0; i < res.history.size(); i++) {
            arr[i + 1] = Math.log10(Math.max(1e-16, res.history.get(i).residualNorm));
        }
        return arr;
    }

    private static double findIterationsForTolerance(IterativeSolvers.SolverResult res, double tol) {
        for (IterativeSolvers.IterationRecord rec : res.history) {
            if (rec.stepNorm < tol && rec.residualNorm <= tol) {
                return rec.iteration;
            }
        }
        return res.iterationsCount;
    }
}
