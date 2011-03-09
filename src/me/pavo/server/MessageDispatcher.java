package me.pavo.server;

import java.util.Vector;

public class MessageDispatcher {
    private Vector listeners;

    public void fireMessageArrivedEvent(Message message) {
        if(listeners == null || listeners.size() == 0) {
            return;
        }
        
        MessageListener[] array;
        synchronized(this) {
            array = new MessageListener[listeners.size()];
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter] = (MessageListener)listeners.elementAt(iter);
            }
        }
        fireMessageArrivedSync(array, message);
    }
    
    private void fireMessageArrivedSync(MessageListener[] array, Message message) {
        for(int iter = 0 ; iter < array.length ; iter++) {
            array[iter].messageArrived(message);
        }
    }
    
    public synchronized void removeListener(Object listener) {
        if(listeners != null) {
            listeners.removeElement(listener);
        }
    }
    
    public synchronized void addListener(Object listener) {
        if(listeners == null) {
            listeners = new Vector();
        }
        listeners.addElement(listener);
    }
}
