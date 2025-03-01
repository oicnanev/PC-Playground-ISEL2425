import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.LinkedList

class LinkedListConcurrencyTest {
    // LinkedList partilhada entre as threads
    private val sharedList = LinkedList<Int>()

    // Constantes para o número de threads e repetições
    companion object {
        const val N_OF_THREADS = 10
        const val N_OF_REPS = 10_000
    }

    @Test
    fun `test concurrent modifications to LinkedList`() {
        // Cria uma lista de threads
        val threads = mutableListOf<Thread>()

        // Criar as threads
        for (i in 0 until N_OF_THREADS) {
            val thread = createThread()
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
        println("Real:      ${sharedList.size}")
        println()
        assertNotEquals(N_OF_THREADS * N_OF_REPS, sharedList.size)
    }

    private fun createThread(): Thread {
        return Thread {
            // Cada thread adiciona N_OF_REPS elementos à LinkedList
            repeat(N_OF_REPS) {
                sharedList.add(it)
            }
        }
    }
}
