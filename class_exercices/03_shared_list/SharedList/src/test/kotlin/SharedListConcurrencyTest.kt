import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Collections.synchronizedList
import java.util.LinkedList
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SharedListConcurrencyTest {
    // LinkedList partilhada entre as threads
    private val sharedList = LinkedList<Int>()

    private val synchronizedSharedList = synchronizedList(LinkedList<Int>())

    private val lockedSharedList = LinkedList<Int>()

    private val mutex: Lock = ReentrantLock()

    // Constantes para o número de threads e repetições
    companion object {
        const val N_OF_THREADS = 100
        const val N_OF_REPS = 100_000
    }

    @Test
    fun `test concurrent modifications to SharedList`() {
        // Cria uma lista de threads
        val threads = mutableListOf<Thread>()

        // Criar as threads
        for (i in 0 until N_OF_THREADS) {
            val thread =  Thread {
                for (j in 0 until N_OF_REPS) {
                    sharedList.add(j)
                }
            }
            threads.add(thread)
        }

        // Inicia todas as threads
        threads.forEach { it.start() }

        // Aguarda todas as threads terminarem
        threads.forEach { it.join() }

        // Verifica se o tamanho da lista é diferente do esperado
        // Devido à concorrência, o tamanho pode ser menor que o esperado
        println()
        println("======= RESULTS =======")
        println("Shared Linked List Size")
        println("Expected: ${N_OF_THREADS * N_OF_REPS}")
        println("Real:     ${sharedList.size}")
        println()
        assertNotEquals(N_OF_THREADS * N_OF_REPS, sharedList.size)
    }

    @Test
    fun `test concurrent modifications to SyncronizedSharedList`() {
        val threads = mutableListOf<Thread>()

        // Criar as threads
        for (i in 0 until N_OF_THREADS) {
            val thread =  Thread {
                for (j in 0 until N_OF_REPS) {
                    synchronizedSharedList.add(j)
                }
            }
            threads.add(thread)
        }

        // Inicia todas as threads
        threads.forEach { it.start() }

        // Aguarda todas as threads terminarem
        threads.forEach { it.join() }

        // Verifica se o tamanho da lista é diferente do esperado
        // Devido à concorrência, o tamanho pode ser menor que o esperado
        println()
        println("======= RESULTS =======")
        println("Synchronized Shared Linked List Size")
        println("Expected: ${N_OF_THREADS * N_OF_REPS}")
        println("Real:     ${synchronizedSharedList.size}")
        println()
        assertEquals(N_OF_THREADS * N_OF_REPS, synchronizedSharedList.size)
    }

    @Test
    fun `test concurrent modifications to sharedList with locks`() {
        val threads = mutableListOf<Thread>()

        for (i in 0 until N_OF_THREADS) {
            val thread =  Thread {
                for (j in 0 until N_OF_REPS) {
                    mutex.withLock {
                        lockedSharedList.add(j)
                    }
                }
            }
            threads.add(thread)
        }

        // Inicia todas as threads
        threads.forEach { it.start() }

        // Aguarda todas as threads terminarem
        threads.forEach { it.join() }

        // Verifica se o tamanho da lista é diferente do esperado
        // Devido à concorrência, o tamanho pode ser menor que o esperado
        println()
        println("======= RESULTS =======")
        println("Locked Shared Linked List Size")
        println("Expected: ${N_OF_THREADS * N_OF_REPS}")
        println("Real:     ${lockedSharedList.size}")
        println()
        assertEquals(N_OF_THREADS * N_OF_REPS, lockedSharedList.size)
    }
}
