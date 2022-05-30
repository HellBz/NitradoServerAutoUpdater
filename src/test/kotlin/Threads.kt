class Threads {
}

object PrintNumbers {
    @JvmStatic
    fun main(args: Array<String>) {
        // Creating 3 threads, passing thread name as arg
        val t1 = Thread(NumberRunnable(), "T1")
        val t2 = Thread(NumberRunnable(), "T2")
        val t3 = Thread(NumberRunnable())
        // setting name using setName method
        t3.name = "Thread3"
        t1.start()
        t2.start()
        t3.start()

        while (t3.isAlive) {
            //println( "T3 Alive" )
        }

        //println( Thread.getAllStackTraces().keys )
    }
}

internal class NumberRunnable : Runnable {
    override fun run() {
        // Getting thread's name
        println("Current Thread Name- " + Thread.currentThread().name)
        // Getting thread's ID
        println("Current Thread ID- " + Thread.currentThread().id + " For Thread- " + Thread.currentThread().name)
    }
}