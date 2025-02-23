import java.util.*
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

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
        val threads = List(N_OF_THREADS) {
            Thread {
                // Cada thread adiciona N_OF_REPS elementos à LinkedList
                repeat(N_OF_REPS) {
                    sharedList.add(it)
                }
            }
        }

        // Inicia todas as threads
        threads.forEach { it.start() }

        // Aguarda todas as threads terminarem
        threads.forEach { it.join() }

        // Verifica se o tamanho da lista é diferente do esperado
        // Devido à concorrência, o tamanho pode ser menor que o esperado
        println("Expected sharedList size: ${N_OF_THREADS * N_OF_REPS}")
        println("Real sharedList size: ${sharedList.size}")
        assertNotEquals(N_OF_THREADS * N_OF_REPS, sharedList.size)
    }
}