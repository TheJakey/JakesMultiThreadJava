package com.company;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Current CURRENT = new Current(0, 1);

    private static boolean stop = false;


    static class Current {

        private Integer currentId;
        public int counter;

        public Current(Integer currentId, int counter){
            this.currentId = currentId;
            this.counter = counter;
        }

    }

    static class MyThread extends Thread {

        private final Integer myId;
        private Integer myCounter;

        public MyThread(Integer id, Integer myCounter) {
            this.myId = id;
            this.myCounter = myCounter;
        }

        @Override
        public void run() {

            synchronized (CURRENT) {
                while (!stop) {

                    while (myCounter == 0) {
                        try {
                            CURRENT.wait();
                            myCounter = 10;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (CURRENT.counter < 31)
                        System.out.println("Som vlakvno: " + myId + "   Counter je: " + CURRENT.counter);
                    else
                        stop = true;

                    CURRENT.counter++;
                    myCounter--;

                    if (myCounter < 1) {
                        CURRENT.notifyAll();
                    }
                }
            }
        }

    }


        public static void main(String[] args) throws InterruptedException {
            System.out.println("Hi Jake");

            List<Thread> threads = new ArrayList<>();

            threads.add(new MyThread(0, 0));
            threads.add(new MyThread(1, 10));

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
