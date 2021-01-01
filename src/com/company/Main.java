package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    private static final ReentrantLock mutex = new ReentrantLock();

    private static final Condition baronCondition = mutex.newCondition();
    private static final Condition slaveCondition = mutex.newCondition();

    private static final ReentrantLock baronBowMutex = new ReentrantLock();
    private static final ReentrantLock slaveBowMutex = new ReentrantLock();
    private static int baronBowCount = 0;
    private static int slaveBowCount = 0;

    private static int baronsCount = 0;
    private static int slavesCount = 0;

    private static int baronsCurrentlyBowing = 0;

    private static boolean stop = false;


    public static void main(String[] args) {
        int i;

        List<Thread> barrons = new ArrayList<>();
        List<Thread> slaves = new ArrayList<>();

        for (i = 0; i < 4; i++) barrons.add(new BaronThread(i));
        for (i = 0; i < 10; i++) slaves.add(new SlaveThread(i));

        startThreads(barrons);
        startThreads(slaves);

        sleep(30_000);
        stop = true;

        mutex.lock();
        baronCondition.signalAll();
        slaveCondition.signalAll();
        mutex.unlock();

        try {
            for (Thread thread : barrons) {
                thread.join();
            }
            for (Thread thread : slaves) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Baron bow: " + baronBowCount);
        System.out.println("Slave bow: " + slaveBowCount);
    }

    public static void sleep(int miliseconds) {
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
    private static void awaitForSignal(Condition queue) {
        try {
            queue.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    static class BaronThread extends Thread {

        private final Integer myId;
        private int countIn = 0;

        public BaronThread(Integer id) {
            this.myId = id;
        }

        public void bow() {
            Main.sleep(1_000);
            baronBowMutex.lock();
            baronBowCount++;
            baronBowMutex.unlock();
        }

        private void takeBreak() {
            Main.sleep(4_000);
        }

        @Override
        public void run() {
            while(!stop) {

                mutex.lock();
                try {
                    baronsCount++;
                    while ((slavesCount != 0 || baronsCurrentlyBowing > 1) && !stop)
                        awaitForSignal(slaveCondition);
                    if (stop)
                        break;

                    baronsCurrentlyBowing++;

                    System.out
                        .println("Klania sa BARON            Dnu ich je: " + baronsCurrentlyBowing);
                    mutex.unlock();

                    if (stop)
                        break;
                    bow();

                    System.out.println("Doklanal sa BARON");

                    mutex.lock();

                    baronsCurrentlyBowing--;
                    baronsCount--;
                    baronCondition.signalAll();
                    slaveCondition.signalAll();
                } finally {
                    mutex.unlock();
                }

                if (stop) break;
                System.out.println("BARON ma prestavku ----------- Ostava baronov na zaciatku " + baronsCount + " =========== ostava Baronov DNU: "+ baronsCurrentlyBowing);
                takeBreak();
            }
        }

    }

    static class SlaveThread extends Thread {

        private final Integer myId;

        public SlaveThread(Integer id) {
            this.myId = id;
        }

        public void bow() {
            Main.sleep(1_000);
            slaveBowMutex.lock();
            slaveBowCount++;
            slaveBowMutex.unlock();
        }

        private void takeBreak() {
            Main.sleep(4_000);
        }

        @Override
        public void run() {
            while(!stop) {

                mutex.lock();
                try {
                    while (baronsCount != 0 && !stop)
                        awaitForSignal(baronCondition);
                    if (stop)
                        break;

                    slavesCount++;
                    mutex.unlock();

                    if (stop)
                        break;
                    System.out.println("Klania sa slave");
                    bow();

                    mutex.lock();
                    slavesCount--;
                    slaveCondition.signalAll();

                } finally {
                    mutex.unlock();
                }
                if (stop) break;
                System.out.println("Slave ma prestavku ----------- ");
                takeBreak();
            }
        }

    }

}
