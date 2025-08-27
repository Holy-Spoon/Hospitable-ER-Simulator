
import ecs100.*;
import java.util.*;

/**
 * Represents a hospital department that manages patient treatment
 * Tracks patients in treatment and waiting queues with statistics
 */
public class Department {
    // Configuration
    private final String name;
    private final int maxPatients;
    
    // Patient management
    private final Set<Patient> treatmentRoom;
    private final Queue<Patient> waitingRoom;
    
    // Statistics
    private int totalWaitingTime = 0;
    private int totalPatientsServed = 0;
    private int maxQueueLength = 0;

    /**
     * Creates a new department
     * @param name Department name (ER, X-Ray, etc)
     * @param maxPatients Maximum concurrent patients in treatment
     * @param usePriQueue Whether to use priority queue for waiting patients
     */
    public Department(String name, int maxPatients, boolean usePriQueue) {
        this.name = name;
        this.maxPatients = maxPatients;
        this.treatmentRoom = new HashSet<>();
        this.waitingRoom = usePriQueue ? new PriorityQueue<>() : new ArrayDeque<>();
    }

    // ================== Core Operations ==================
    
    /**
     * Adds a patient to the waiting queue
     * @param patient Patient to enqueue
     */
    public void enqueue(Patient patient) {
        waitingRoom.offer(patient);
        updateQueueStats();
    }

    /**
     * Advances treatment by one tick for all patients in treatment
     */
    public void tickTreatment() {
        treatmentRoom.forEach(Patient::advanceCurrentTreatmentByTick);
    }

    /**
     * Advances waiting time by one tick for all waiting patients
     */
    public void tickWaiting() {
        waitingRoom.forEach(Patient::waitForATick);
        updateQueueStats();
    }

    // ================== Patient Admission ==================
    
    /**
     * Admits patients from waiting to treatment while space is available
     * Prioritizes critical (Priority 1) patients
     */
    public void admitWhileSpace() {
        admitPriorityPatients();
        admitRegularPatients();
    }

    private void admitPriorityPatients() {
        Iterator<Patient> it = waitingRoom.iterator();
        while (it.hasNext() && treatmentRoom.size() < maxPatients) {
            Patient p = it.next();
            if (p.getPriority() == 1) {
                admitPatient(p);
                it.remove();
            }
        }
    }

    private void admitRegularPatients() {
        while (treatmentRoom.size() < maxPatients && !waitingRoom.isEmpty()) {
            admitPatient(waitingRoom.poll());
        }
    }

    private void admitPatient(Patient patient) {
        treatmentRoom.add(patient);
        totalWaitingTime += patient.getTotalWaitingTime();
        totalPatientsServed++;
    }

    /**
     * Forces treatment of a priority 1 patient who waited too long
     * urgentPatient The patient to prioritize
     * return true if patient was admitted
     */
    public boolean forcePriority1Treatment(Patient urgentPatient) {
        if (treatmentRoom.size() < maxPatients && waitingRoom.remove(urgentPatient)) {
            admitPatient(urgentPatient);
            return true;
        }
        return false;
    }

    // ================== Treatment Completion ==================
    
    /**
     * Collects patients who finished their current treatment
     * return List of completed patients
     */
    public List<Patient> collectFinished() {
        List<Patient> finished = new ArrayList<>();
        
        Iterator<Patient> it = treatmentRoom.iterator();
        while (it.hasNext()) {
            Patient p = it.next();
            if (p.currentTreatmentFinished()) {
                finished.add(p);
                it.remove();
            }
        }
        
        return finished;
    }

    // ================== Statistics & Monitoring ==================
    
    private void updateQueueStats() {
        maxQueueLength = Math.max(maxQueueLength, waitingRoom.size());
    }

    public Collection<Patient> getWaitingPatients() {
        return new ArrayList<>(waitingRoom); // Defensive copy
    }

    // ================== Getters ==================
    
    public String getName() { return name; }
    public int getTotalWaitingTime() { return totalWaitingTime; }
    public int getTotalPatientsServed() { return totalPatientsServed; }
    public int getMaxQueueLength() { return maxQueueLength; }

    // ================== Visualization ==================
    
    /**
     * Draw the department: the patients being treated and the patients waiting
     * You may need to change the names if your fields had different names
     */
    public void redraw(double y){
        UI.setFontSize(14);
        UI.drawString(name, 0, y-35);
        double x = 10;
        UI.drawRect(x-5, y-30, maxPatients*10, 30);  // box to show max number of patients
        for(Patient p : treatmentRoom){
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for(Patient p : waitingRoom){
            p.redraw(x, y);
            x += 10;
        }
    }

}
