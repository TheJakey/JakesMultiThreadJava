package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    private static final Current myCurrent = new Current();

    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Condition waitingCondition = mutex.newCondition();
    private static boolean inBarrier = false;

    private static final ReentrantLock usedBucketsMutex = new ReentrantLock();
    private static int usedBucketsCount = 0;
    private volatile static int currentlyWaiting = 0;


    private static boolean stop = false;


    public static void main(String[] args) {
        int i;

        List<Thread> painters = new ArrayList<>();

        for (i = 0; i < 10; i++) painters.add(new PainterThread(i));

        for (Thread thread : painters) {
            thread.start();
        }

        sleep(30_000);
        stop = true;

        mutex.lock();
        waitingCondition.signalAll();
        mutex.unlock();

        try {
            for (Thread thread : painters) {
                System.out.println("Painter " + ((PainterThread)thread).getMyId()
                                   + " used: " + ((PainterThread)thread).getMyUsedBuckets());
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("All used: " + usedBucketsCount);
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
    private static void waitForNotify(Current queue) {
        try {
            queue.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Current {
        public int leftForBreak = 0;
//        public int currentlyWaiting = 0;
        public boolean needToWaitOthers = true;
    }

    static class PainterThread extends Thread {

        private final Integer myId;
        private Integer myUsedBuckets = 0;
        private Integer bucketsSinceLastBreak = 0;

        public PainterThread(Integer id) {
            this.myId = id;
        }

        public void paint() {
            Main.sleep(2_000);
        }

        public void fillBucket() {
            Main.sleep(1_000);

            myUsedBuckets++;
            bucketsSinceLastBreak++;

            usedBucketsMutex.lock();
            usedBucketsCount++;
            usedBucketsMutex.unlock();
        }

        private void takeBreak() {
            Main.sleep(2_000);
        }

        @Override
        public void run() {
            while(!stop) {

                paint();

                if (stop)
                    return;

                fillBucket();

                if (bucketsSinceLastBreak == 4) {
                    bucketsSinceLastBreak = 0;

                    waitForColleagues(3);

                    System.out.println(myId+" --- taking break");
                    if (stop) return;
                    takeBreak();
                    System.out.println(myId+" --- leaving break");
                }
            }
        }

        private void waitForColleagues(int numberOfColleaguesOnBreak) {
            mutex.lock();
            try {
                while (inBarrier && !stop)
                    awaitForSignal(waitingCondition);
                if (stop) return;

                currentlyWaiting++;

                if (currentlyWaiting == numberOfColleaguesOnBreak) {
                    inBarrier = true;
                    waitingCondition.signalAll();
                }
                while (!inBarrier && !stop)
                    awaitForSignal(waitingCondition);
                if (stop) return;

                currentlyWaiting--;

                if (currentlyWaiting == 0) {
                    inBarrier = false;
                    waitingCondition.signalAll();
                } else {
                    while (inBarrier && !stop)
                        awaitForSignal(waitingCondition);
                    if (stop) return;
                }
            } finally {
                mutex.unlock();
            }
        }

        public Integer getMyUsedBuckets() {
            return myUsedBuckets;
        }

        public Integer getMyId() {
            return myId;
        }
    }

}
