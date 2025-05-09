package org.example

import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private val logger = LoggerFactory.getLogger("labs")

fun logJobState(job: Job) {
    logger.info("isActive={}, isCancelled={}, isCompleted={}", job.isActive, job.isCancelled, job.isCompleted)
}

object Ex0 {
    @JvmStatic
    fun main(args: Array<String>) {
        // alternative: runBlocking(Dispatchers.Default) {
        runBlocking(Dispatchers.Default) {
            logger.info("top coroutine started")
            launch {
                logger.info("child coroutine started")
            }
            logger.info("Before thread sleep")
            Thread.sleep(1000)
            // alternative: delay(1000)
            logger.info("After sleep/delay on top-coroutine")
        }
    }
}

object Ex1 {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val continuationInterceptor: ContinuationInterceptor = coroutineContext[ContinuationInterceptor]
                ?: throw IllegalStateException("coroutine context does not have expected element")

            logger.info("step 0")  // this is the main coroutine
            withContext(Dispatchers.Default) {
                logger.info("step 1")  // this is the child coroutine on the DefaultDispatcher
                withContext(continuationInterceptor) {
                    logger.info("step 2") // this is the child coroutine on the ContinuationInterceptor (same as main)
                }
                logger.info("step 3") // this is the child coroutine on the DefaultDispatcher
            }
            logger.info("step 4") // this is the main coroutine
        }
    }
}

object Ex2 {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val job = launch {
                try {
                    logger.info("Inner coroutine started")
                    delay(2000)
                } catch (ex: Throwable) {
                    logger.info("Caught exception {} - {}", ex.javaClass.simpleName, ex.message)
                }
            }
            delay(1000)
            logJobState(job)
            logger.info("Cancelling inner coroutine")
            job.cancel()
            logJobState(job)
            delay(1)
            logJobState(job)
        }
        logger.info("after runBlocking")
    }
}

object Ex3 {
    @JvmStatic
    fun main(args: Array<String>) {
        val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
        try {
            runBlocking {
                logger.info("top coroutine starting")
                val job = launch {
                    try {
                        //suspendCancellableCoroutine { continuation ->
                        suspendCoroutine { continuation ->
                            scheduledExecutor.schedule(
                                {
                                    logger.info("Calling continuation")
                                    continuation.resume(Unit)
                                },
                                1000,
                                TimeUnit.MILLISECONDS
                            )
                        }
                        logger.info("After suspendCancellableCoroutine")
                    } catch (ex: Throwable) {
                        logger.info("Caught exception {} - {}", ex.javaClass.simpleName, ex.message)
                    }
                }
                delay(500)
                job.cancel()
            }
        } finally {
            scheduledExecutor.shutdown()
        }
    }
}

object Ex4 {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            logger.info("top starting")
            val job1 = launch {
                try {
                    delay(1000)
                    throw Exception("Oh No!")
                    logger.info("inner 1 ending")
                } catch (ex: Throwable) {
                    logger.info("inner 1: caught exception {} - {}", ex.javaClass.simpleName, ex.message)
                    throw ex
                }
            }

            val job2 = launch {
                try {
                    delay(2000)
                    logger.info("inner 2 ending")
                } catch (ex: Throwable) {
                    logger.info("inner 2: caught exception {} - {}", ex.javaClass.simpleName, ex.message)
                }
            }

            try {
                delay(2000)
            } catch (ex: Throwable) {
                logger.info("top: caught exception {} - {}", ex.javaClass.simpleName, ex.message)
            }
            logger.info("top ending")
        }
    }
}
