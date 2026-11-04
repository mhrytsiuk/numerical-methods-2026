package lab6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class MatrixFileIO {
    public static void writeMatrixToFile(double[][] matrix, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (double[] row : matrix) {
            for (int j = 0; j < row.length; j++) {
                if (j > 0) {
                    sb.append(' ');
                }
                sb.append(String.format(Locale.US, "%.16e", row[j]));
            }
            sb.append('\n');
        }
        Files.writeString(Paths.get(filePath), sb.toString());
    }

    public static double[][] readMatrixFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        int rowCount = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                rowCount++;
            }
        }
        double[][] matrix = new double[rowCount][];
        int r = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            matrix[r] = new double[parts.length];
            for (int c = 0; c < parts.length; c++) {
                matrix[r][c] = Double.parseDouble(parts[c]);
            }
            r++;
        }
        return matrix;
    }

    public static void writeVectorToFile(double[] vector, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (double val : vector) {
            sb.append(String.format(Locale.US, "%.16e", val)).append('\n');
        }
        Files.writeString(Paths.get(filePath), sb.toString());
    }

    public static double[] readVectorFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        int count = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                count++;
            }
        }
        double[] vector = new double[count];
        int idx = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                vector[idx++] = Double.parseDouble(trimmed);
            }
        }
        return vector;
    }

    public static void writeLuToFile(double[][] lMatrix, double[][] uMatrix, String filePath) throws IOException {
        int n = lMatrix.length;
        StringBuilder sb = new StringBuilder();
        sb.append("# Нижня трикутна матриця L (розмірність ").append(n).append("x").append(n).append(")\n");
        for (double[] row : lMatrix) {
            for (int j = 0; j < n; j++) {
                if (j > 0) {
                    sb.append(' ');
                }
                sb.append(String.format(Locale.US, "%13.6e", row[j]));
            }
            sb.append('\n');
        }
        sb.append("\n# Верхня трикутна матриця U з одиничною діагоналлю u_ii = 1 (розмірність ").append(n).append("x").append(n).append(")\n");
        for (double[] row : uMatrix) {
            for (int j = 0; j < n; j++) {
                if (j > 0) {
                    sb.append(' ');
                }
                sb.append(String.format(Locale.US, "%13.6e", row[j]));
            }
            sb.append('\n');
        }
        Files.writeString(Paths.get(filePath), sb.toString());
    }
}