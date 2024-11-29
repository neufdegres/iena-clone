package ienaclone.gui.util;

import java.util.ArrayDeque;

import ienaclone.util.StopDisruption;

public class InfosCircularQueue {
    private ArrayDeque<StopDisruption> queue;

    public InfosCircularQueue() {
        queue = new ArrayDeque<>();
    }

    public void enqueue(StopDisruption value) {
        queue.addLast(value); // Ajout à la fin
    }

    public StopDisruption dequeue() {
        if (queue.isEmpty()) throw new IllegalStateException("Queue vide");
        StopDisruption value = queue.removeFirst(); // Retrait en tête
        queue.addLast(value); // Ajout à la fin pour rendre circulaire
        return value;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
