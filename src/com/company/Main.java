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
    private static final ReentrantLock voteBoxMutex = new ReentrantLock();
    private static final ReentrantLock votedMutex = new ReentrantLock();

    private static int votedCount = 0;

    private static boolean stop = false;


    public static void main(String[] args) {
        int i;

        List<Thread> voters = new ArrayList<>();

        for (i = 0; i < 100; i++) voters.add(new VoterThread(i));

        DisplayThread displayThread = new DisplayThread();
        displayThread.start();

        for (Thread voter : voters) {
            voter.start();
            sleep(1000);
        }

        try {
            for (Thread thread : voters) thread.join();

            // end also display thread
            stop = true;
            displayThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        public int currentlyVoting = 0;
    }

    static class DisplayThread extends Thread {
        @Override
        public void run() {
            while (!stop) {
                votedMutex.lock();
                System.out.println("Voted: " + votedCount);
                votedMutex.unlock();

                Main.sleep(5000);
            }
        }
    }

    static class VoterThread extends Thread {

        private final Integer myId;

        public VoterThread(Integer id) {
            this.myId = id;
        }

        public void vote() {
            Main.sleep(2000);
        }

        public void submitVote() {
            Main.sleep(1000);
            votedMutex.lock();
            votedCount++;
            votedMutex.unlock();
        }

        @Override
        public void run() {
//            voteBoxMutex.lock();
//            while (myCurrent.currentlyVoting)
            synchronized (myCurrent) {
                while (myCurrent.currentlyVoting == 3)
                    waitForNotify(myCurrent);

                myCurrent.currentlyVoting++;
//                System.out.println("MyID: " + myId + "   -- Currently voting: " + myCurrent.currentlyVoting);
            }

            vote();

            synchronized (myCurrent) {
//                System.out.println("MyID: " + myId + "   -- Voted. Submiting vote.");
                myCurrent.currentlyVoting--;
                myCurrent.notify();
            }

            mutex.lock();
            submitVote();
//            System.out.println("MyID: " + myId + "   -- I left");
            mutex.unlock();
        }

    }

}
