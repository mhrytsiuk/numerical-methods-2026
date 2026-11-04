package lab6;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import util.Chart;

public class Main {
    public static void main(String[] args) throws Exception {
        String outputDir = "lab6/output";
        new File(outputDir).mkdirs();

        int n = 100;
        double exactValue = 2.5;
        double eps0 = 1e-14;

        System.out.println("Лабораторна робота №6 (LU-розклад. Ітераційні методи уточнення розв'язку СЛАР)");

        String matrixFilePath = outputDir + "/matrix_A.txt";
        String vectorFilePath = outputDir + "/vector_B.txt";
        String luFilePath = outputDir + "/lu_decomposition.txt";

        double[] exactSolution = generateAndSaveSystem(n, exactValue, matrixFilePath, vectorFilePath);

        double[][] aMatrix = MatrixFileIO.readMatrixFromFile(matrixFilePath);
        double[] bVector = MatrixFileIO.readVectorFromFile(vectorFilePath);

        LuDecompositionSolver.LuResult luResult = performAndSaveLuDecomposition(aMatrix, luFilePath);

        double[] initialSolution = solveAndEvaluateInitial(aMatrix, bVector, luResult, exactSolution);

        IterativeRefinementSolver.RefinementResult refinementResult = runIterativeRefinement(
                aMatrix, bVector, luResult, initialSolution, exactSolution, eps0, outputDir);

        plotCharts(aMatrix, bVector, luResult, initialSolution, refinementResult, exactSolution, outputDir);
    }

    private static double[] generateAndSaveSystem(int n, double exactValue, String matrixFile, String vectorFile) throws Exception {
        Random rng = new Random(42L);
        double[][] aMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double val = -0.25 + 0.5 * rng.nextDouble();
                if (i == j && Math.abs(val) < 0.05) {
                    val = (val >= 0 ? 0.125 : -0.125) + val;
                }
                aMatrix[i][j] = val;
            }
        }

        double[] exactX = new double[n];
        Arrays.fill(exactX, exactValue);

        double[] bVector = MathUtils.multiplyMatrixVector(aMatrix, exactX);

        MatrixFileIO.writeMatrixToFile(aMatrix, matrixFile);
        MatrixFileIO.writeVectorToFile(bVector, vectorFile);

        System.out.println("\n1. Генерація випадкової матриці A (" + n + "x" + n + ") та вектора вільних членів B (при x_j = " + exactValue + ")");
        System.out.println("Матрицю A записано у файл: " + matrixFile);
        System.out.println("Вектор B записано у файл:  " + vectorFile);
        System.out.println(String.format(Locale.US, "Перші 5 елементів вектора B: [%.6f, %.6f, %.6f, %.6f, %.6f]",
                bVector[0], bVector[1], bVector[2], bVector[3], bVector[4]));

        return exactX;
    }

    private static LuDecompositionSolver.LuResult performAndSaveLuDecomposition(double[][] aMatrix, String luFile) throws Exception {
        LuDecompositionSolver.LuResult lu = LuDecompositionSolver.decompose(aMatrix);
        MatrixFileIO.writeLuToFile(lu.lMatrix, lu.uMatrix, luFile);

        int n = aMatrix.length;
        double maxReconstructionError = 0.0;
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                double sum = 0.0;
                int limit = Math.min(i, k);
                for (int j = 0; j <= limit; j++) {
                    sum += lu.lMatrix[i][j] * lu.uMatrix[j][k];
                }
                maxReconstructionError = Math.max(maxReconstructionError, Math.abs(aMatrix[i][k] - sum));
            }
        }

        System.out.println("\n2. Знаходження LU-розкладу матриці A (де u_ii = 1) та запис у файл");
        System.out.println("LU-розклад записано у файл: " + luFile);
        System.out.println("Перевірка точності розкладу max|A - L*U| = " + MathUtils.formatScientific(maxReconstructionError));
        System.out.println("Фрагмент кута 3x3 матриці L та матриці U:");
        for (int i = 0; i < 3; i++) {
            System.out.println(String.format(Locale.US, "  L[%d][0..2] = [%10.6f, %10.6f, %10.6f]   |   U[%d][0..2] = [%10.6f, %10.6f, %10.6f]",
                    i, lu.lMatrix[i][0], lu.lMatrix[i][1], lu.lMatrix[i][2],
                    i, lu.uMatrix[i][0], lu.uMatrix[i][1], lu.uMatrix[i][2]));
        }

        return lu;
    }

    private static double[] solveAndEvaluateInitial(double[][] aMatrix, double[] bVector,
                                                    LuDecompositionSolver.LuResult lu, double[] exactSolution) {
        double[] x0 = LuDecompositionSolver.solveWithLu(lu, bVector);
        double eps = MathUtils.evaluateResidualAccuracy(aMatrix, x0, bVector);
        double maxSolErr = MathUtils.formatMaxSolutionError(x0, exactSolution);

        System.out.println("\n3-4. Розв'язок СЛАР AX = B за допомогою LU-розкладу та оцінка точності");
        System.out.println(String.format(Locale.US, "Перші 5 компонент X^(0): [%.12f, %.12f, %.12f, %.12f, %.12f]",
                x0[0], x0[1], x0[2], x0[3], x0[4]));
        System.out.println("Норма нев'язки eps = max_i |sum(a_ij * x_j) - b_i| = " + MathUtils.formatScientific(eps));
        System.out.println("Максимальне відхилення від точного розв'язку max_i |x_i^(0) - 2.5| = " + MathUtils.formatScientific(maxSolErr));

        return x0;
    }

    private static IterativeRefinementSolver.RefinementResult runIterativeRefinement(
            double[][] aMatrix, double[] bVector, LuDecompositionSolver.LuResult lu,
            double[] initialSolution, double[] exactSolution, double eps0, String outputDir) {
        System.out.println("\n5. Ітераційне уточнення розв'язку СЛАР до точності eps_0 = " + String.format(Locale.US, "%.0e", eps0));
        System.out.println(" Ітер. |    ||ΔX^(k)||    | ||AX - B|| (double) | ||AX - B|| (точна)  | max|X^(k) - X*|");

        IterativeRefinementSolver.RefinementResult result = IterativeRefinementSolver.refine(
                aMatrix, bVector, lu, initialSolution, exactSolution, eps0, 10);

        for (IterativeRefinementSolver.IterationStep step : result.history) {
            System.out.println(String.format(Locale.US, "   %2d  | %16.6e | %19.6e | %19.6e | %16.6e",
                    step.iteration, step.deltaXNorm, step.residualNorm, step.exactResidualNorm, step.solutionErrorNorm));
        }

        System.out.println("Необхідна кількість ітерацій для досягнення точності eps_0 = 10^-14: k = " + result.iterationsCount);
        double[] xr = result.refinedSolution;
        System.out.println(String.format(Locale.US, "Перші 5 компонент уточненого X: [%.15f, %.15f, %.15f, %.15f, %.15f]",
                xr[0], xr[1], xr[2], xr[3], xr[4]));

        return result;
    }

    private static void plotCharts(double[][] aMatrix, double[] bVector,
                                   LuDecompositionSolver.LuResult luResult,
                                   double[] initialSolution,
                                   IterativeRefinementSolver.RefinementResult refinementResult,
                                   double[] exactSolution, String outputDir) throws Exception {
        int n = initialSolution.length;
        double[] indices = new double[n];
        double[] initialErrors = new double[n];
        double[] refinedErrors = new double[n];
        double[] logInitialErrors = new double[n];
        double[] logRefinedErrors = new double[n];

        for (int i = 0; i < n; i++) {
            indices[i] = i + 1;
            initialErrors[i] = Math.abs(initialSolution[i] - exactSolution[i]) * 1e10;
            refinedErrors[i] = Math.abs(refinementResult.refinedSolution[i] - exactSolution[i]);
            logInitialErrors[i] = Math.log10(Math.max(1e-16, Math.abs(initialSolution[i] - exactSolution[i])));
            logRefinedErrors[i] = Math.log10(Math.max(1e-16, refinedErrors[i]));
        }

        new Chart("Похибка компонент початкового розв'язку |x_i^(0) - 2.5| (LU-розклад)", "Індекс компоненти i", "|x_i^(0) - 2.5| × 10^10")
                .line("|x_i^(0) - 2.5|", indices, initialErrors, new Color(0x1f77b4), false)
                .points("Вузли", indices, initialErrors, new Color(0xd62728))
                .save(outputDir + "/01_initial_component_errors.png");

        new Chart("Порівняння похибок розв'язку до та після ітераційного уточнення", "Індекс компоненти i", "log10(|x_i - 2.5|)")
                .line("Початковий X^(0) (LU)", indices, logInitialErrors, new Color(0x1f77b4), false)
                .line("Уточнений X^(2)", indices, logRefinedErrors, new Color(0x2ca02c), false)
                .save(outputDir + "/02_errors_before_after.png");

        int totalSteps = 4;
        double[] iterNumbers = new double[totalSteps + 1];
        double[] logResidualNorms = new double[totalSteps + 1];
        double[] iterDxNumbers = new double[totalSteps];
        double[] logDxNorms = new double[totalSteps];

        double[] currentX = initialSolution.clone();
        iterNumbers[0] = 0.0;
        logResidualNorms[0] = Math.log10(Math.max(1e-16,
                MathUtils.vectorNorm(MathUtils.computeResidualHighPrecision(aMatrix, currentX, bVector))));

        for (int iter = 1; iter <= totalSteps; iter++) {
            double[] rVector = MathUtils.computeResidualHighPrecision(aMatrix, currentX, bVector);
            double[] deltaX = LuDecompositionSolver.solveWithLu(luResult, rVector);
            for (int i = 0; i < n; i++) {
                currentX[i] += deltaX[i];
            }
            double[] rAfter = MathUtils.computeResidualHighPrecision(aMatrix, currentX, bVector);
            iterNumbers[iter] = iter;
            logResidualNorms[iter] = Math.log10(Math.max(1e-16, MathUtils.vectorNorm(rAfter)));
            iterDxNumbers[iter - 1] = iter;
            logDxNorms[iter - 1] = Math.log10(Math.max(1e-16, MathUtils.vectorNorm(deltaX)));
        }

        new Chart("Збіжність ітераційного уточнення розв'язку СЛАР", "Номер ітерації k", "log10(норма)")
                .line("log10 ||AX^(k) - B||", iterNumbers, logResidualNorms, new Color(0x1f77b4), false)
                .line("log10 ||ΔX^(k)||", iterDxNumbers, logDxNorms, new Color(0xd62728), true)
                .points("Ітерації", iterNumbers, logResidualNorms, new Color(0x1f77b4))
                .save(outputDir + "/03_refinement_convergence.png");
    }
}
