package utils;

import java.io.*;
import java.util.*;

/**
 * Utility class for handling CSV file read/write operations.
 * Used as the primary data persistence layer throughout the application.
 */
public class CSVHandler {

    /**
     * Reads all rows from a CSV file (including header).
     * @param filePath Path to the CSV file
     * @return List of String arrays, each array is one row
     */
    public static List<String[]> readAll(String filePath) {
        List<String[]> rows = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return rows;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(parseCsvLine(line));
                }
            }
        } catch (IOException e) {
            System.err.println("[CSVHandler] Error reading file: " + filePath + " -> " + e.getMessage());
        }
        return rows;
    }

    /**
     * Writes all rows (including header) to a CSV file, overwriting existing content.
     * @param filePath Path to the CSV file
     * @param data List of String arrays to write
     */
    public static void writeAll(String filePath, List<String[]> data) {
        ensureDirectoryExists(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String[] row : data) {
                bw.write(joinCsvRow(row));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[CSVHandler] Error writing file: " + filePath + " -> " + e.getMessage());
        }
    }

    /**
     * Appends a single row to an existing CSV file.
     * @param filePath Path to the CSV file
     * @param row String array representing one row
     */
    public static void appendRow(String filePath, String[] row) {
        ensureDirectoryExists(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(joinCsvRow(row));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[CSVHandler] Error appending to file: " + filePath + " -> " + e.getMessage());
        }
    }

    /**
     * Returns all data rows (skips the header row).
     */
    public static List<String[]> readDataRows(String filePath) {
        List<String[]> all = readAll(filePath);
        if (all.size() > 1) {
            return all.subList(1, all.size());
        }
        return new ArrayList<>();
    }

    /**
     * Creates file with a header if it doesn't exist.
     */
    public static void initFile(String filePath, String[] header) {
        File f = new File(filePath);
        if (!f.exists()) {
            ensureDirectoryExists(filePath);
            appendRow(filePath, header);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private static String joinCsvRow(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            String cell = row[i] == null ? "" : row[i];
            if (cell.contains(",") || cell.contains("\"")) {
                cell = "\"" + cell.replace("\"", "\"\"") + "\"";
            }
            sb.append(cell);
            if (i < row.length - 1) sb.append(",");
        }
        return sb.toString();
    }

    private static void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
