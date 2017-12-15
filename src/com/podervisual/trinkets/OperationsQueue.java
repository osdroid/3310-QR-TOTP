package com.podervisual.trinkets;
/*
 * Copyright © 2011 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
import java.util.Vector;

// Simple Operations Queue
// It runs in an independent thread and executes Operations serially
class OperationsQueue implements Runnable {
	public interface Operation {
	    // Implement here the operation to be executed
	    void execute();
	}
	
    private volatile boolean running = true;
    private final Vector operations = new Vector();

    OperationsQueue() {
        // Notice that all operations will be done in another
        // thread to avoid deadlocks with GUI thread
        new Thread(this).start();
    }

    void enqueueOperation(Operation nextOperation) {
        operations.addElement(nextOperation);
        synchronized (this) {
            notify();
        }
    }

    // stop the thread
    void abort() {
        running = false;
        synchronized (this) {
            notify();
        }
    }

    public void run() {
        while (running) {
            while (operations.size() > 0) {
                try {
                    // execute the first operation on the queue
                    ((Operation) operations.firstElement()).execute();
                } catch (Exception e) {
                    // Nothing to do. It is expected that each operations handle
                    // their own locally exception but this block is to ensure
                    // that the queue continues to operate
                }
                operations.removeElementAt(0);
            }
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // it doesn't matter
                }
            }
        }
    }
}