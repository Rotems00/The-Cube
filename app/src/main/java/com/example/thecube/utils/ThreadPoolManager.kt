package com.example.thecube.util

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ThreadPoolManager {
    // Create a fixed thread pool with 4 threads.
    private val executorService = Executors.newFixedThreadPool(4)

    /**
     * Submit a task to the thread pool.
     * @param task A lambda representing the work to be done.
     */
    fun submitTask(task: () -> Unit) {
        executorService.submit(task)
    }

    /**
     * Shutdown the thread pool. Call this when your app or Activity is finishing.
     */
    fun shutdown() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow()
            }
        } catch (ex: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}
