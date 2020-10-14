package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final String GREEN = "GREEN";
    private static final String RED = "RED";
    private static final MyColor currentColor = new MyColor(RED);

    private static Integer counter = 0;

    static class MyColor {

        private String color;

        public MyColor(String color){
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
            while (counter < 9) {
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

                    currentColor.color = myColor.equals(RED) ? GREEN : RED;
                    counter++;

                    currentColor.notifyAll();
                }
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hi Jake");

        List<Thread> threads = new ArrayList<>();

        threads.add(new MyThread(RED));
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
