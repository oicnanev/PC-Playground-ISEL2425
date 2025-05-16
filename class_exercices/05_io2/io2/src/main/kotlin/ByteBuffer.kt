package org.example

import org.slf4j.LoggerFactory
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer

private val logger = LoggerFactory.getLogger("nio2")

private fun logByteBuffer(bb: ByteBuffer) {
    logger.info("position:{}, limit: {}, capacity: {}", bb.position(), bb.limit(), bb.capacity())
}

object Ex0 {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create
        // Allocate a ByteBuffer with a capacity of 16 bytes directly creating a new buffer
        // We could user wrap() to wrap an existing byte array:
        //static ByteBuffer wrap(byte[] array) -≥ Wraps a byte array into a buffer. Or
        //static ByteBuffer wrap(byte[] array, int offset, int length) -≥ Wraps a byte array into a buffer.
        val bb = ByteBuffer.allocate(16)
        logByteBuffer(bb)                               // INFO nio2 - position:0, limit: 16, capacity: 16

        // Write - escrever ou ler é para o position

        bb.put(1)
        logByteBuffer(bb)                               // INFO nio2 - position:1, limit: 16, capacity: 16
        bb.put(2)
        logByteBuffer(bb)                               // INFO nio2 - position:2, limit: 16, capacity: 16

        // Read
        bb.flip()                                       // Sem o flip lê para a frente, onde não esxiste nada
        logByteBuffer(bb)                               // INFO nio2 - position:0, limit: 2, capacity: 16
        logger.info("get(): {}", bb.get())              // INFO nio2 - get(): 1
        logByteBuffer(bb)                               // INFO nio2 - position:1, limit: 2, capacity: 16
        logger.info("get(): {}", bb.get())              // INFO nio2 - get(): 2
        logByteBuffer(bb)                               // INFO nio2 - position:2, limit: 2, capacity: 16
        try {
            logger.info("get(): {}", bb.get())
            logByteBuffer(bb)
        } catch (ex: BufferUnderflowException) {
            logger.info("BufferUnderflowException - {}", ex.message)  // INFO nio2 - BufferUnderflowException - null
        }
    }
}

// What needs to be done to start writing again in the beginning of the ByteBuffer?
// Answer: We need to call the clear() method to reset the position and limit of the buffer.