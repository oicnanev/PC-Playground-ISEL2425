package org.example

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.CountDownLatch
import java.nio.channels.CompletionHandler

private val logger = LoggerFactory.getLogger("nio2")

// AsynchronousSocketChannel creation and connection %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
object Ex1 {

    @JvmStatic
    fun main(args: Array<String>) {
        val socket = AsynchronousSocketChannel.open()
        val latch = CountDownLatch(1)
        logger.info("connecting...")
        socket.connect(InetSocketAddress("httpbin.org", 80), Unit, object : CompletionHandler<Void, Unit> {

            override fun completed(result: Void?, attachment: Unit) {
                logger.info("connected")
                latch.countDown()
            }

            override fun failed(exc: Throwable, attachment: Unit) {
                logger.info("connect failed - {}", exc.message)
                latch.countDown()
            }
        })
        logger.info("after connect call")
        latch.await()
    }
}
/*
1. What is the third argument to the connect function?
It is a CompletionHandler that will be called when the connection is established or fails.

2. Run this code and observe the result
In which thread is the connected message logged? Where was this thread created?
The connected message is logged in the thread that handles the completion of the connection,
which is typically a thread from the I/O thread pool (Thread-17).
This thread is created by the AsynchronousSocketChannel implementation.

3. What is the main thread doing while the connection is being established?
The main thread is waiting on the CountDownLatch, effectively blocking until the connection is established or fails.

4. Why is the CountDownLatch required?
The CountDownLatch is used to block the main thread until the connection is either established or fails.

5. What happens if the usage of the CountDownLatch is removed?
The main thread will not wait for the connection to complete, and the program may terminate before the connection is established or fails.

6. Change the port to 81 and observe the result
How is the error reported, namely in which thread?
The error is reported in the same thread that handles the completion of the connection,
which is typically a thread from the I/O thread pool. [Thread-17].

7. Change the host name to httpbin.org2
How is the error reported, namely in which thread?
The error is reported in the main thread because the connection attempt fails immediately [main]
 */

object Ex2 {
    // Run this code and observe the result.
    @JvmStatic
    fun main(args: Array<String>) {
        val socket = AsynchronousSocketChannel.open()
        val latch = CountDownLatch(1)
        logger.info("connecting...")
        socket.connect(InetSocketAddress("httpbin.org", 80), Unit, object : CompletionHandler<Void, Unit> {
            override fun completed(result: Void?, attachment: Unit) {
                logger.info("connected")
                val bytes = "GET /get HTTP/1.1\r\nHost: httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()
                val writeBuffer = ByteBuffer.wrap(bytes)
                socket.write(writeBuffer, Unit, object : CompletionHandler<Int, Unit> {

                    override fun completed(result: Int, attachment: Unit?) {
                        logger.info("write completed - {}", result)
                        val readBuffer = ByteBuffer.allocate(1024)
                        socket.read(readBuffer, Unit, object : CompletionHandler<Int, Unit> {

                            override fun completed(result: Int, attachment: Unit?) {
                                readBuffer.flip()
                                val s = String(readBuffer.array(), 0, result)
                                logger.info("read completed - {}", s)
                                latch.countDown()
                            }

                            override fun failed(exc: Throwable, attachment: Unit?) {
                                logger.info("read failed - {}", exc.message)
                                latch.countDown()
                            }
                        })
                    }

                    override fun failed(exc: Throwable, attachment: Unit?) {
                        logger.info("write failed - {}", exc.message)
                        latch.countDown()
                    }
                })
            }

            override fun failed(exc: Throwable, attachment: Unit) {
                logger.info("connect failed - {}", exc.message)
                latch.countDown()
            }
        })
        latch.await()
    }
}

// Break the connection, writing, and reading code into distinct functions.
object Ex2_DistinctFunctions {
    @JvmStatic
    fun main(args: Array<String>) {
        val socket = AsynchronousSocketChannel.open()
        val latch = CountDownLatch(1)
        logger.info("connecting...")
        socket.connect(InetSocketAddress("httpbin.org", 80), Unit, object : CompletionHandler<Void, Unit> {
            override fun completed(result: Void?, attachment: Unit) {
                logger.info("connected")
                write(socket, latch)
            }

            override fun failed(exc: Throwable, attachment: Unit) {
                logger.info("connect failed - {}", exc.message)
                latch.countDown()
            }
        })
        latch.await()
    }

    private fun write(socket: AsynchronousSocketChannel, latch: CountDownLatch) {
        val bytes = "GET /get HTTP/1.1\r\nHost: httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()
        val writeBuffer = ByteBuffer.wrap(bytes)
        socket.write(writeBuffer, Unit, object : CompletionHandler<Int, Unit> {

            override fun completed(result: Int, attachment: Unit?) {
                logger.info("write completed - {}", result)
                read(socket, latch)
            }

            override fun failed(exc: Throwable, attachment: Unit?) {
                logger.info("write failed - {}", exc.message)
                latch.countDown()
            }
        })
    }

    private fun read(socket: AsynchronousSocketChannel, latch: CountDownLatch) {
        val readBuffer = ByteBuffer.allocate(1024)
        socket.read(readBuffer, Unit, object : CompletionHandler<Int, Unit> {

            override fun completed(result: Int, attachment: Unit?) {
                readBuffer.flip()
                val s = String(readBuffer.array(), 0, result)
                logger.info("read completed - {}", s)
                latch.countDown()
            }

            override fun failed(exc: Throwable, attachment: Unit?) {
                logger.info("read failed - {}", exc.message)
                latch.countDown()
            }
        })
    }

}

// Change the size of the read buffer to 16 bytes and modify the program so that a full response is still presented.
object Ex2_readBuffer_16B {
    @JvmStatic
    fun main(args: Array<String>) {
        val socket = AsynchronousSocketChannel.open()
        val latch = CountDownLatch(1)
        logger.info("connecting...")
        socket.connect(InetSocketAddress("httpbin.org", 80), Unit, object : CompletionHandler<Void, Unit> {
            override fun completed(result: Void?, attachment: Unit) {
                logger.info("connected")
                write(socket, latch)
            }

            override fun failed(exc: Throwable, attachment: Unit) {
                logger.info("connect failed - {}", exc.message)
                latch.countDown()
            }
        })
        latch.await()
    }
    private fun write(socket: AsynchronousSocketChannel, latch: CountDownLatch) {
        val bytes = "GET /get HTTP/1.1\r\nHost: httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()
        val writeBuffer = ByteBuffer.wrap(bytes)
        socket.write(writeBuffer, Unit, object : CompletionHandler<Int, Unit> {

            override fun completed(result: Int, attachment: Unit?) {
                logger.info("write completed - {}", result)
                read(socket, latch)
            }

            override fun failed(exc: Throwable, attachment: Unit?) {
                logger.info("write failed - {}", exc.message)
                latch.countDown()
            }
        })
    }
    private fun read(socket: AsynchronousSocketChannel, latch: CountDownLatch) {
        val readBuffer = ByteBuffer.allocate(16)
        val sb = StringBuilder()
        socket.read(readBuffer, Unit, object : CompletionHandler<Int, Unit> {

            override fun completed(result: Int, attachment: Unit?) {
                if (result == -1) {
                    logger.info("read completed - {}", sb.toString())
                    latch.countDown()
                    return
                }
                readBuffer.flip()
                sb.append(String(readBuffer.array(), 0, result))
                readBuffer.clear()
                socket.read(readBuffer, Unit, this)
            }

            override fun failed(exc: Throwable, attachment: Unit?) {
                logger.info("read failed - {}", exc.message)
                latch.countDown()
            }
        })
    }
}

// Add support for cancellation in the suspend functions.
object Ex2_cancellation {
    @JvmStatic
    fun main(args: Array<String>) {
        val socket = AsynchronousSocketChannel.open()
        val latch = CountDownLatch(1)
        logger.info("connecting...")
        socket.connect(InetSocketAddress("httpbin.org", 80), Unit, object : CompletionHandler<Void, Unit> {
            override fun completed(result: Void?, attachment: Unit) {
                logger.info("connected")
                write(socket, latch)
            }

            override fun failed(exc: Throwable, attachment: Unit) {
                logger.info("connect failed - {}", exc.message)
                latch.countDown()
            }
        })
        latch.await()
    }

    private fun write(socket: AsynchronousSocketChannel, latch: CountDownLatch) {
        val bytes = "GET /get HTTP/1.1\r\nHost: httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()
        val writeBuffer = ByteBuffer.wrap(bytes)
        socket.write(writeBuffer, Unit, object : CompletionHandler<Int, Unit> {

            override fun completed(result: Int, attachment: Unit?) {
                logger.info("write completed - {}", result)
                read(socket, latch)
            }

            override fun failed(exc: Throwable, attachment: Unit?) {
                logger.info("write failed - {}", exc.message)
                latch.countDown()
            }
        })
    }
    private fun read(socket: AsynchronousSocketChannel, latch: CountDownLatch) {
        val readBuffer = ByteBuffer.allocate(16)
        val sb = StringBuilder()
        socket.read(readBuffer, Unit, object : CompletionHandler<Int, Unit> {

            override fun completed(result: Int, attachment: Unit?) {
                if (result == -1) {
                    logger.info("read completed - {}", sb.toString())
                    latch.countDown()
                    return
                }
                readBuffer.flip()
                sb.append(String(readBuffer.array(), 0, result))
                readBuffer.clear()
                socket.read(readBuffer, Unit, this)
            }

            override fun failed(exc: Throwable, attachment: Unit?) {
                logger.info("read failed - {}", exc.message)
                latch.countDown()
            }
        })
    }
    private fun cancel(socket: AsynchronousSocketChannel) {
        socket.close()
        logger.info("socket closed")
    }
    private fun cancelRead(socket: AsynchronousSocketChannel) {
        socket.close()
        logger.info("socket closed")
    }
    private fun cancelWrite(socket: AsynchronousSocketChannel) {
        socket.close()
        logger.info("socket closed")
    }
    private fun cancelConnect(socket: AsynchronousSocketChannel) {
        socket.close()
        logger.info("socket closed")
    }
}
