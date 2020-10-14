package com.company;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String GREEN = "GREEN";
    private static final String ORANGE = "ORANGE";
    private static final String RED = "RED";
    private static final CurrentColor currentColor = new CurrentColor(RED);

    private static boolean stop = false;

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
                synchronized (currentColor) {
                    while (!myColor.equals(currentColor.color)) {
                        try {
                            currentColor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(myColor);
//                    System.out.println("Counter value: " + counter);

                    switch (myColor) {
                        case RED: currentColor.color = ORANGE; break;
                        case ORANGE: currentColor.color = GREEN; break;
                        case GREEN: currentColor.color = RED; break;
                    }

                    currentColor.notifyAll();
                }
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hi Jake");

        List<Thread> threads = new ArrayList<>();

        threads.add(new MyThread(RED));
        threads.add(new MyThread(ORANGE));
        threads.add(new MyThread(GREEN));

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
