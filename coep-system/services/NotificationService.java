package services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Notification service demonstrating Concurrent Programming.
 * Uses ExecutorService to send notifications asynchronously.
 */
public class NotificationService {

    // Thread pool with 3 threads for concurrent notifications
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    /**
     * Sends an async notification (simulates email/SMS).
     * Runs in a separate thread — caller is NOT blocked.
     */
    public void sendAsync(String recipientName, String message) {
        executor.submit(() -> {
            try {
                // Simulate network delay (real system would call email/SMS API here)
                Thread.sleep(300);
                System.out.println("\n  [NOTIFICATION -> " + recipientName + "] " + message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Notify multiple users in parallel using threads.
     */
    public void broadcastAsync(String[] recipients, String message) {
        for (String recipient : recipients) {
            sendAsync(recipient, message);
        }
    }

    /**
     * Gracefully shuts down the thread pool.
     * Call this when the application exits.
     */
    public void shutdown() {
        executor.shutdown();
    }

    // ── Convenience helpers ──────────────────────────────────────────────────

    public void notifyGraded(String studentName, String assignmentTitle, int marks) {
        sendAsync(studentName,
            "Your assignment '" + assignmentTitle + "' has been graded. Marks: " + marks);
    }

    public void notifyResultPublished(String studentName, String courseId, String grade) {
        sendAsync(studentName,
            "Result published for course " + courseId + ". Your grade: " + grade);
    }

    public void notifyExamFormApproved(String studentName, String courseId) {
        sendAsync(studentName,
            "Your exam form for course " + courseId + " has been APPROVED!");
    }

    public void notifyEnrollment(String studentName, String courseId) {
        sendAsync(studentName, "You have successfully enrolled in course: " + courseId);
    }
}
