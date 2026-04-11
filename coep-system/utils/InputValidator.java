package utils;

import java.util.Scanner;

/**
 * Utility for safe terminal input reading with exception handling.
 */
public class InputValidator {

    private static final Scanner scanner = new Scanner(System.in);

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a valid integer.");
            }
        }
    }

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a valid number.");
            }
        }
    }

    public static Scanner getScanner() {
        return scanner;
    }
}
