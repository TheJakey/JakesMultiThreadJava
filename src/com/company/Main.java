package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
* Implementation of Producer and Consumer - Letter generator
 **/
public class Main {

    private static final Current myCurrent = new Current();

    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Condition collectorsCondition = mutex.newCondition();
    private static final Condition huntersCondition = mutex.newCondition();
    private static int collectorsCount = 0;
    private static int huntersCount = 0;

    private static final ReentrantLock hunterGiftMutex = new ReentrantLock();
    private static final ReentrantLock collectorGiftMutex = new ReentrantLock();
    private static int hunterGiftsCount = 0;
    private static int collectorGiftsCount = 0;

    private static boolean stop = false;


    public static void main(String[] args) {
        int i;

        List<Thread> hunters = new ArrayList<>();
        List<Thread> collectors = new ArrayList<>();

        for (i = 0; i < 6; i++) hunters.add(new HunterThread(i));
        for (i = 0; i < 12; i++) collectors.add(new CollectorThread(i));

        startThreads(hunters);
        startThreads(collectors);

        sleep(30000);
        stop = true;

        // end the misery of threads and finish them all
        mutex.lock();
        collectorsCondition.signalAll();
        huntersCondition.signalAll();
        mutex.unlock();

        try {
            for (Thread thread : hunters) thread.join();
            for (Thread thread : collectors) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Hunter gited: " + hunterGiftsCount);
        System.out.println("Collector gifted: " + collectorGiftsCount);
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
    private static void waitForSignal(Condition queue) {
        try {
            queue.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Current {
        public final List<Thread> queue = new ArrayList<>();
        public int inside = 0;
    }

    static class HunterThread extends Thread {

        private final Integer myId;

        public HunterThread(Integer id) {
            this.myId = id;
        }

        public void doAction() {
            Main.sleep(6000);
        }

        public void giveGift() {
            Main.sleep(2000);
            hunterGiftMutex.lock();
            hunterGiftsCount++;
            hunterGiftMutex.unlock();
        }

        @Override
        public void run() {
            // till not stopped
            while (!stop) {
                doAction();


                mutex.lock();
                while ((collectorsCount != 0 || huntersCount == 2) && !stop) {
                    waitForSignal(collectorsCondition);
                }
                if (stop) {
                    mutex.unlock();
                    break;
                }
                huntersCount++;
                System.out.println("Hunter entering \nHunterID: " + myId + "\nNumber of hunters in with me: " + huntersCount);
                System.out.println();
                mutex.unlock();


                giveGift();


                mutex.lock();
                huntersCount--;

                huntersCondition.signalAll();
                mutex.unlock();
            }
        }

    }

    static class CollectorThread extends Thread {

        private final Integer myId;

        public CollectorThread(Integer id) {
            this.myId = id;
        }

        public void doAction() {
            Main.sleep(4000);
        }

        public void giveGift() {
            Main.sleep(1000);
            collectorGiftMutex.lock();
            collectorGiftsCount++;
            collectorGiftMutex.unlock();
        }

        @Override
        public void run() {
            // till not stopped
            while (!stop) {
                doAction();


                mutex.lock();
                while ((huntersCount != 0 || collectorsCount == 4) && !stop) {
                    waitForSignal(huntersCondition);
                }
                if (stop) {
                    mutex.unlock();
                    break;
                }
                collectorsCount++;
                System.out.println("Collector entering \nCollectorID: " + myId + "\nNumber of Collector in with me: " + collectorsCount);
                System.out.println();
                mutex.unlock();

                giveGift();


                mutex.lock();
                collectorsCount--;

                collectorsCondition.signalAll();
                mutex.unlock();
            }
        }

    }

}
