# Exercices about uthreads

## `uthreads-0-minimal` folder

* When does a call to `ut_run` returns?
i. Always before any other thread runs.
ii. After `context_switch` is called once on each thread.
iii. After the main thread calls `ut_yield`.
iv. When there aren't anymore threads in the ready state. 

    The corretc is the iv. When there aren't anymore threads in the ready state

    - The ```ut_run``` function initializes the main thread and starts the scheduling process by calling ```schedule```.
    - The ```schedule``` function switches control to the next thread in the ```queue_ready```.
    - As threads complete their execution, they call ```schedule_and_free_current```, which frees the current thread and switches to the next thread.
    - When all threads have completed and the ```queue_ready``` is empty, ```schedule``` will set ```next_thread``` to ```thread_main```.
    - Control will eventually return to the main thread (```thread_main```), and ```ut_run``` will return when there are no more threads in the ready state.

* What does `ut_init` does?
i. Allocate the stack for the main thread.
ii. Creates the main thread.
iii. Initializes the ready queue.
iv. Does a context_switch.

    The correct answer is:

    iii. Initializes the ready queue.

    - The ```ut_init``` function contains a single call to ```list_init(&queue_ready)```;. This function initializes the queue_ready list, which is used to keep track of the threads that are ready to run.


* When is the memory for the thread descriptor de-allocated?
i. Never.
ii. When `ut_free` is called.
iii. When `context_switch_and_free` is called.
iv. when `ut_run` returns.

    The correct answer is:

    iii. When context_switch_and_free is called.

    - The ```context_switch_and_free``` function is designed to switch the context to the next thread and free the memory of the current thread. This function is called in ```schedule_and_free_current``, which is invoked when a thread completes its execution in ```internal_start```.
    - When ```context_switch_and_free`` is called, it performs the context switch and de-allocates the memory for the current thread descriptor.

* The location of each thread stack and descriptor is
i. Unrelated.
ii. The descriptor is located at the top of the memory area reserved for the stack.
iii. The descriptor is located at the bottom of the memory area reserved for the stack.
iv. The descriptor is always located at the _top of the stack_.

    The correct answer is:

    iii. The descriptor is located at the bottom of the memory area reserved for the stack.
    
    - In the ```ut_create``` function, the thread descriptor (```uthread_t```) is allocated first, and then the context (```uthread_context_t```) is placed at the top of the allocated memory area. This means the descriptor is at the bottom of the memory area reserved for the stack.
    -  In the code of ```ut_create```, thread is allocated with ```malloc(STACK_SIZE)```, and context is placed at the top of this allocated memory area. Therefore, the descriptor is located at the bottom of the memory area reserved for the stack.

* On the `context_switch_and_free`, why is the call to the `free` function done _after_ the instruction `movq (%rsi), %rsp`?

    - The call to the ```free``` function is done after the instruction ```movq (%rsi), %rsp``` in ```context_switch_and_free``` because the stack pointer (```%rsp```) needs to be switched to the next thread's stack before calling ```free```. This ensures that the ```free``` function is executed on the next thread's stack, not the current thread's stack, which is about to be freed.
    -  If the ```free``` function were called before switching the stack pointer, it would attempt to free the memory of the current thread while still using the current thread's stack. This could lead to undefined behavior, as the stack being used for the function call would be deallocated.
    -  By switching the stack pointer first, the ```free``` function call is safely executed on the next thread's stack, avoiding any issues with deallocating the current thread's stack while it is still in use.

* Change the source code so that
i* The `ut_create` function receives a thread name.
ii* Every `context_switch`` writes in the standard output the names of the two threads involved in that switch.

    - **Step 1** Modify the ```uthread``` structure
        
    ```c
    struct uthread
    {
      // pointer to the thread's stack
      uint64_t rsp;
    
      // the function that will be run by the thread when the thread starts
      start_routine_t start;
    
      // the argument to the start function
      uint64_t arg;
    
      // because this uthread structure will be stored in lists
      list_entry_t list_entry;
    
      // thread name
      const char *name;
    };
    ```
    
    - **Step 2** Update the ```ut_create``` function

    ```c
    uthread_t *ut_create(start_routine_t start_routine, uint64_t arg, const char *name)
    {
      uthread_t *thread = (uthread_t *)malloc(STACK_SIZE);
      uthread_context_t *context = (uthread_context_t *)(((uint8_t *)thread) + STACK_SIZE - sizeof(uthread_context_t));
    
      context->func_addr = internal_start;
    
      thread->rsp = (uint64_t)context;
      thread->start = start_routine;
      thread->arg = arg;
      thread->name = name;
    
      list_add_tail(&queue_ready, &(thread->list_entry));
    
      return thread;
    }
    ```
    
    Change the signature in ```uthread.h``` too:
    
        ```uthread_t *ut_create(start_routine_t, uint64_t arg, const char *name);``
    
    - **Step 3** Modify the ```schedule``` function, so it prints thread names

    ```c
    void schedule()
    {
      uthread_t *next_thread = list_is_empty(&queue_ready)
                                   ? thread_main
                                   : node_of(list_remove_head(&queue_ready), uthread_t, list_entry);
      if (next_thread == thread_running)
      {
        // no context switch is needed because next_thread is already running
        return;
      }
      uthread_t *current = thread_running;
      thread_running = next_thread;
      printf("switching from %s to %s\n", current->name, next_thread->name);
      context_switch(current, next_thread);
    }
    ```
    
    - **Step 4** Update the `main` function to pass thread names to `ut_create`.

    ```c
    int main()
    {
      printf("main starting\n");
      ut_init();
      for (int i = 0; i < 3; ++i)
      {
        char name[16];
        snprintf(name, sizeof(name), "thread %d", i);
        ut_create(thread_routine, i, name);
      }
      ut_run();
      printf("main ending\n");
    
      return 0;
    }
    ```
    
    Output:
    
    ```sh
    vscode ➜ .../course-jvm-concurrency/code/native/uthreads-0-minimal (main) $ ./main
    main starting
    switching from ��UUUU to thread 2
    thread 0 on iteration 0
    switching from thread 2 to thread 2
    thread 1 on iteration 0
    switching from thread 2 to thread 2
    thread 2 on iteration 0
    switching from thread 2 to thread 2
    thread 0 on iteration 1
    switching from thread 2 to thread 2
    thread 1 on iteration 1
    switching from thread 2 to thread 2
    thread 2 on iteration 1
    switching from thread 2 to thread 2
    thread 0 on iteration 2
    switching from thread 2 to thread 2
    thread 1 on iteration 2
    switching from thread 2 to thread 2
    thread 2 on iteration 2
    switching from thread 2 to thread 2
    thread 0 on iteration 3
    switching from thread 2 to thread 2
    thread 1 on iteration 3
    switching from thread 2 to thread 2
    thread 2 on iteration 3
    switching from thread 2 to thread 2
    thread 0 on iteration 4
    switching from thread 2 to thread 2
    thread 1 on iteration 4
    switching from thread 2 to thread 2
    thread 2 on iteration 4
    switching from thread 2 to thread 2
    thread 0 ending
    thread 1 ending
    thread 2 ending
    main ending
    ```
    
    --- 
    
    ## `uthreads-1-join` folder

* Why did the `context_switch_and_free` function needed to be changed for this new _uthreads_ version?
    
    - The ```context_switch_and_free``` function needed to be changed for this new uthreads version to ensure that the stack of the current thread is properly freed and the stack pointer in the thread descriptor is set to ```0``` after the thread terminates. This is necessary because the thread descriptor must remain valid for other threads to perform a join on it, even after the thread has terminated. The changes ensure that the stack is freed and the descriptor is updated correctly without saving the context of the terminating thread.

* Why is `schedule` sometimes called inside the function `ut_join`? When does the `schedule` call return?
    * The ```schedule``` function is called inside the ```ut_join``` function when the thread being joined has not yet completed. This is necessary to block the calling thread and switch to another thread that is ready to run. 
    * The ```schedule``` call returns when the thread being joined has completed its execution and the calling thread is unblocked and scheduled to run again.

* What happens if a thread `t1` joins with a thread `t2` and the `t2` thread also joins with the `t1` thread?
    * If a thread `t1` joins with a thread `t2` and the `t2` thread also joins with the `t1` thread, it will create a **deadlock** situation. Both threads will be waiting for each other to complete, and neither will be able to proceed. This is because each thread will be blocked in the `ut_join` function, waiting for the other thread to finish, which will never happen.
