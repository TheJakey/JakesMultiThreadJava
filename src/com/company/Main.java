package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static boolean stop = false;

    private static final ReentrantLock mutex = new ReentrantLock();
    private static final int THREADS_COUNT = 10;

    static class Current {

        public static int counter;

    }

    static class MyThread extends Thread {

        private final Integer myId;

        public MyThread(Integer id) {
            this.myId = id;
        }

        @Override
        public void run() {

            while (!stop) {
                mutex.lock();

                if (!stop) {
                    Current.counter++;
                    System.out.println("New value: " + Current.counter + "\nMyId: " + myId);
                    System.out.println();
                }

                mutex.unlock();

                if (myId == THREADS_COUNT)
                    stop = true;

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        for (int i = 1; i <= THREADS_COUNT; i++)
            threads.add(new MyThread(i));

        startThreads(threads);

        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static void startThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.start();
        }
    }

}
