package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
* Implementation of Producer and Consumer - Letter generator
 **/
public class Main {
    private static final Current myCurrent = new Current();

    private static final Lock lock = new ReentrantLock();
    private static final Condition isEmpty = lock.newCondition();
    private static final Condition isFull = lock.newCondition();

    private static boolean stop = false;

    private static final ReentrantLock inputCounterMutex = new ReentrantLock();
    private static final ReentrantLock outputCounterMutex = new ReentrantLock();
    private static int inputCount = 0;
    private static int outputCount = 0;

    public static char generate_letter() {
        inputCounterMutex.lock();
        inputCount++;
        inputCounterMutex.unlock();

        sleep(1000);
        return 'A';
    }

    public static void test_letter(char letter) {
        outputCounterMutex.lock();
        outputCount++;
        outputCounterMutex.unlock();

        sleep(2000);
    }

    public static void main(String[] args) {
        int i;

        List<Thread> generators = new ArrayList<>();
        List<Thread> testers = new ArrayList<>();

        for (i = 0; i < 4; i++) generators.add(new GeneratorThread(i));
        for (i = 0; i < 10; i++) testers.add(new TesterThread(i));

        startThreads(generators);
        startThreads(testers);

        sleep(30000);

        // end the misery of threads and finish them all
        lock.lock();

        stop = true;
        isEmpty.signalAll();
        isFull.signalAll();

        lock.unlock();

        try {
            for (Thread thread : generators) thread.join();
            for (Thread thread : testers) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Input count: " + inputCount);
        System.out.println("Output count: " + outputCount);
    }

    private static void sleep(int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static void startThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.start();
        }
    }
    private static void waitForSignal(Condition isFull) {
        try {
            isFull.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Current {
        public char[] table = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
        public int input_position = 0;
        public int output_position = 0;
        public int onTableCount = 0;
    }

    static class GeneratorThread extends Thread {

        private final Integer myId;

        public GeneratorThread(Integer id) {
            this.myId = id;
        }

        @Override
        public void run() {
            // till not stopped
            while (!stop) {

                // generate letter
                char letter = generate_letter();

                // lock entrence
                try {
                    lock.lock();
                    // wait till there is a space on the table while you still can
                    while (myCurrent.onTableCount == 10 && !stop)
                        waitForSignal(isEmpty);
                    // if you cant, just break cycle
                    if (stop)
                        break;

                    // put it on the table
                    myCurrent.table[myCurrent.input_position] = letter;
                    myCurrent.input_position = (myCurrent.input_position + 1) % 10;

                    // add new letter to total count
                    myCurrent.onTableCount++;

                    // signal that table is not empty anymore
                    isFull.signal();
                }
                finally {
                    // unlock entrence
                    lock.unlock();
                }
            }
//            while (!stop) {
//                mutex.lock();
//
//                if (!stop) {
//                    myCurrent.counter++;
//                    System.out.println("New value: " + myCurrent.counter + "\nMyId: " + myId);
//                    System.out.println();
//                }
//
//                mutex.unlock();
//
//                if (myId == THREADS_COUNT)
//                    stop = true;
//
//                try {
//                    sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }

    }

    static class TesterThread extends Thread {

        private final Integer myId;

        public TesterThread(Integer id) {
            this.myId = id;
        }

        @Override
        public void run() {
            // till not stopped
            while (!stop) {

                char letter = ' ';

                try {
                    lock.lock();

                    while (myCurrent.onTableCount == 0 && !stop) {
                        waitForSignal(isFull);
                    }
                    if (stop)
                        break;

                    // take the letter of the table
                    letter = myCurrent.table[myCurrent.output_position];
                    myCurrent.output_position = (myCurrent.output_position + 1) % 10;

                    myCurrent.onTableCount--;

                    // signal that letter was taken from the table
                    isEmpty.signal();
                }
                finally {
                    lock.unlock();
                }

                // test the letter
                test_letter(letter);
            }
        }

    }
}
