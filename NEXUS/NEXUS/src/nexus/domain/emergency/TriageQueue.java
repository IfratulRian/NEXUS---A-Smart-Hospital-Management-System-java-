package nexus.domain.emergency;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Data Structures & Strategy: PriorityQueue ordered by triage severity rank (1 is highest),
 * then FIFO by arrival time for equal triage levels.
 */
public class TriageQueue {
    private static final Comparator<EmergencyCase> PRIORITY_COMPARATOR =
            Comparator.comparingInt((EmergencyCase c) -> c.getTriageLevel().getSeverityRank())
                    .thenComparing(EmergencyCase::getArrivalTime);

    private final PriorityQueue<EmergencyCase> queue = new PriorityQueue<>(PRIORITY_COMPARATOR);

    public synchronized void enqueue(EmergencyCase emergencyCase) {
        queue.offer(emergencyCase);
    }

    public synchronized Optional<EmergencyCase> pollNext() {
        return Optional.ofNullable(queue.poll());
    }

    public synchronized Optional<EmergencyCase> peekNext() {
        return Optional.ofNullable(queue.peek());
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized boolean remove(String caseId) {
        return queue.removeIf(c -> c.getId().equalsIgnoreCase(caseId));
    }

    public synchronized List<EmergencyCase> toList() {
        // Create a copy to drain in prioritized order without altering internal state
        PriorityQueue<EmergencyCase> copy = new PriorityQueue<>(queue);
        List<EmergencyCase> ordered = new ArrayList<>();
        while (!copy.isEmpty()) {
            ordered.add(copy.poll());
        }
        return ordered;
    }
}
