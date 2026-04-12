package services;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification service demonstrating Concurrent Programming.
 * Uses explicit Thread objects to send notifications concurrently.
 */

public class NotificationService {

    // List to track active notification threads
    private final List<Thread> notificationThreads = new ArrayList<>();
    // Lock for thread-safe operations
    private final Object threadLock = new Object();

    /**
     * Sends a notification in a separate thread (caller is NOT blocked).
     * Demonstrates explicit multithreading for concurrent notification delivery.
     */
    
    public void sendInThread(String recipientName, String message) {
        // Create notification thread
        Thread notificationThread = new Thread(() -> {
            try {
                // Simulate network delay (real system would call email/SMS API here)
                Thread.sleep(300);
                System.out.println("\n  [NOTIFICATION -> " + recipientName + "] " + message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Set thread name for debugging
        notificationThread.setName("NotificationThread-" + recipientName);
        
        // Register thread for tracking
        synchronized (threadLock) {
            notificationThreads.add(notificationThread);
        }
        
        // Start the notification thread (runs concurrently)
        notificationThread.start();
    }

    /**
     * Notify multiple users in parallel using separate threads.
     * Each recipient gets their own notification thread.
     */
    public void broadcastInThreads(String[] recipients, String message) {
        for (String recipient : recipients) {
            sendInThread(recipient, message);
        }
    }

    /**
     * Gracefully waits for all active notification threads to complete.
     * Call this when the application exits.
     */
    public void shutdown() {
        synchronized (threadLock) {
            for (Thread thread : notificationThreads) {
                try {
                    if (thread.isAlive()) {
                        thread.join(); // Wait for thread to complete
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            notificationThreads.clear();
        }
        System.out.println("  [INFO] All notification threads completed and shut down.");
    }

    // ── Convenience helpers ──────────────────────────────────────────────────

    public void notifyGraded(String studentName, String assignmentTitle, int marks) {
        sendInThread(studentName,
            "Your assignment '" + assignmentTitle + "' has been graded. Marks: " + marks);
    }

    public void notifyResultPublished(String studentName, String courseId, String grade) {
        sendInThread(studentName,
            "Result published for course " + courseId + ". Your grade: " + grade);
    }

    public void notifyExamFormApproved(String studentName, String courseId) {
        sendInThread(studentName,
            "Your exam form for course " + courseId + " has been APPROVED!");
    }

    public void notifyEnrollment(String studentName, String courseId) {
        sendInThread(studentName, "You have successfully enrolled in course: " + courseId);
    }
}
