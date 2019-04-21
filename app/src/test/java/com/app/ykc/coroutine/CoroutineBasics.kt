package com.app.ykc.coroutine

import org.junit.Test

import kotlinx.coroutines.*
import org.junit.Assert.*


class CoroutineBasics {

    @Test
    fun myFirstCoroutine() {
        GlobalScope.launch {
            // launch new coroutine in background and continue
            delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
            println("World!") // print after delay
        }
        println("Hello,") // main thread continues while coroutine is delayed
        Thread.sleep(2000L) // block main thread for 2 seconds to keep JVM alive
    }

    @Test
    fun runBlocking() {
        GlobalScope.launch { // launch new coroutine in background and continue
            delay(1000L)
            println("World!")
        }
        println("Hello,") // main thread continues here immediately
        runBlocking {     // but this expression blocks the main thread
            delay(2000L)  // ... while we delay for 2 seconds to keep JVM alive
        }
    }

    @Test
    fun more_idiomatic_way() {
        runBlocking<Unit> { // start main coroutine
            GlobalScope.launch { // launch new coroutine in background and continue
                delay(1000L)
                println("World!")
            }
            println("Hello,") // main coroutine continues here immediately
            delay(2000L)      // delaying for 2 seconds to keep JVM alive
        }
    }

    /**
     * This is also a way to write unit-tests for suspending functions:
     */
    class MyTest {
        @Test
        fun testMySuspendingFunction() = runBlocking<Unit> {
            // here we can use suspending functions using any assertion style that we like
        }
    }


    /**
     * Now the result is still the same, but the code of the main coroutine is not tied to the duration of the background job in any way. Much better.
     */
    @Test
    fun waiting_for_a_job() = runBlocking {
        //sampleStart
        val job = GlobalScope.launch { // launch new coroutine and keep a reference to its Job
            delay(3000L)
            println("World!")
        }
        println("Hello,")
        job.join() // wait until child coroutine completes
        //sampleEnd
    }


    @Test
    fun structured_concurrency() = runBlocking { // this: CoroutineScope
        launch { // launch new coroutine in the scope of runBlocking
            delay(1000L)
            println("World!")
        }
        println("Hello,")
    }


    /**
          Task from coroutine scope
          Task from runBlocking
          Task from nested launch
          Coroutine scope is over

          The main difference between runBlocking and coroutineScope is that the latter does not block
          the current thread while waiting for all children to complete

          runBlocking과 coroutineScope의 가장 큰 차이점은 coroutineScope는 모든 자식이 완료되기를 기다리는
          동안 현재 스레드를 차단하지 않는다는 점입니다
     */
    @Test
    fun scope_builder() = runBlocking { // this: CoroutineScope
        launch {
            delay(200L)
            println("Task from runBlocking")
        }

        coroutineScope { // Creates a new coroutine scope
            launch {
                delay(500L)
                println("Task from nested launch")
            }

            delay(100L)
            println("Task from coroutine scope") // This line will be printed before nested launch
        }

        println("Coroutine scope is over") // This line is not printed until nested launch completes
    }

    @Test
    fun extract_function_refactoring() = runBlocking {
        launch { doWorld() }
        println("Hello,")
    }

    // this is your first suspending function
    suspend fun doWorld() {
        delay(1000L)
        println("World!")
    }


    /**
     * It launches 100K coroutines and, after a second, each coroutine prints a dot. Now, try that with threads.
     * What would happen? (Most likely your code will produce some sort of out-of-memory error)
     */
    @Test
    fun coroutines_ARE_light_weight() = runBlocking {
        repeat(100_000) { // launch a lot of coroutines
            launch {
                delay(1000L)
                print("${Thread.currentThread().name} .")
            }
        }
    }

    /**
     * Active coroutines that were launched in GlobalScope do not keep the process alive. They are like daemon threads.
     */
    @Test
    fun global_coroutines_are_like_daemon_threads() = runBlocking {
        //sampleStart
        GlobalScope.launch {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // just quit after delay
        //sampleEnd
    }



}
