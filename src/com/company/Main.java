package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {
    private static final String GREEN = "GREEN";
    private static final String ORANGE = "ORANGE";
    private static final String RED = "RED";
    private static final CurrentColor currentColor = new CurrentColor(RED);

    private static boolean stop = false;

    private static final Semaphore mutex = new Semaphore(1);


    static class CurrentColor {

        private String color;

        public CurrentColor(String color){
            this.color = color;
        }

    }

    static class MyThread extends Thread {

        private final String myColor;

        public MyThread(String color) {
            myColor = color;
        }

        @Override
        public void run() {
            while (!stop) {
//                    System.out.println("Counter value: " + counter);
                try {
//                    System.out.println("Asking for mutex: " + myColor);
                    mutex.acquire();


                    switch (myColor) {
                        case RED: currentColor.color = GREEN; break;
//                        case ORANGE: currentColor.color = GREEN; break;
                        case GREEN: currentColor.color = RED; break;
                    }



                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {

                    mutex.release();
                }
                System.out.println(myColor);

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

        threads.add(new MyThread(GREEN));
//        threads.add(new MyThread(ORANGE));
        threads.add(new MyThread(RED));

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