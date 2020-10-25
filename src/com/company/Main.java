package com.company;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String GREEN = "GREEN";
    private static final String ORANGE = "ORANGE";
    private static final String RED = "RED";
    private static final CurrentColor currentColor = new CurrentColor(0);

    private static boolean stop = false;

    static class CurrentColor {

        private Integer currentId;

        public CurrentColor(Integer currentId){
            this.currentId = currentId;
        }

    }

    static class MyThread extends Thread {

        private final Integer myId;
        private final Integer threadsCount;

        public MyThread(Integer id, Integer threadsCount) {
            this.myId = id;
            this.threadsCount = threadsCount;
        }

        @Override
        public void run() {
            while (!stop) {
                synchronized (currentColor) {
                    while (!myId.equals(currentColor.currentId)) {
                        try {
                            currentColor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(myId + 1);
//                    System.out.println("Counter value: " + counter);

                    if (myId < threadsCount - 1)
                        currentColor.currentId++;
                    else
                        currentColor.currentId = 0;

                    currentColor.notifyAll();
                }
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hi Jake");
        int threadCount = 6;

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            threads.add(new MyThread(i, threadCount));
        }

        startThreads(threads);

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("I'm done. Bye.");
    }

    private static void startThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.start();
        }
    }

}
