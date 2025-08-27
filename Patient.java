import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.io.*;

/**
 * Represents an ER Patient with:
 * - Personal details (name, initials)
 * - Treatment timeline (arrival, discharge, waiting/treatment times)
 * - Medical priority (1-3, with 1 being highest)
 * - Queue of required treatments
 */
public class Patient implements Comparable<Patient> {
    // Personal details
    private final String name;
    private final String initials;
    
    // Timeline tracking
    private final int arrivalTime;
    private int dischargeTime;
    private int totalWaitTime;
    private int totalTreatmentTime;
    
    // Medical priority (1 = highest, 3 = lowest)
    private final int priority;
    
    // Treatment plan
    private final Queue<Treatment> treatments;
    private final Map<String, Integer> waitTimesPerDept = new HashMap<>();

    /**
     * Constructs a new Patient
     * @param time Current simulation time when patient arrives
     * @param priority Medical priority (1-3)
     * @param firstName Patient's first name
     * @param lastName Patient's last name
     * @param treatments Queue of required treatments
     */
    public Patient(int time, int priority, String firstName, String lastName, Queue<Treatment> treatments) {
        this.arrivalTime = time;
        this.priority = priority;
        this.name = firstName + " " + lastName;
        this.initials = firstName.substring(0,1) + lastName.substring(0,1);
        this.treatments = new ArrayDeque<>(treatments); // Defensive copy
    }

    // ================== Timeline Methods ==================
    
    public void setDischargeTime(int time) { 
        this.dischargeTime = time; 
    }
    
    public int getSystemTime() {
        return dischargeTime - arrivalTime;
    }
    
    // ================== Priority Methods ==================
    
    public int getPriority() { 
        return priority; 
    }
    
    @Override
    public int compareTo(Patient other) {
        if (this.priority != other.priority) {
            return Integer.compare(this.priority, other.priority);
        }
        return Integer.compare(this.arrivalTime, other.arrivalTime);
    }
    

    // ================== Treatment Progress Methods ==================
    
    public void waitForATick() {
        totalWaitTime++;
    }
    
    public void advanceCurrentTreatmentByTick() {
        if (treatments.isEmpty()) {
            throw new IllegalStateException("No treatments remaining: " + this);
        }
        
        Treatment current = treatments.peek();
        if (current.getTimeRemaining() == 0) {
            throw new IllegalStateException("Current treatment already finished: " + this);
        }
        
        totalTreatmentTime++;
        current.advanceTime();
    }
    
    public boolean currentTreatmentFinished() {
        return !treatments.isEmpty() && treatments.peek().getTimeRemaining() == 0;
    }
    
    public boolean allTreatmentsCompleted() {
        return treatments.isEmpty();
    }
    
    public String getCurrentDepartment() {
        if (treatments.isEmpty()) {
            throw new IllegalStateException("No current department - treatments completed: " + this);
        }
        return treatments.peek().getDepartment();
    }
    
    public void removeCurrentTreatment() {
        if (treatments.isEmpty()) {
            throw new IllegalStateException("No treatments to remove: " + this);
        }
        treatments.poll();
    }

    // ================== Wait Time Tracking ==================
    
    public void recordWait(String department, int waitTime) {
        waitTimesPerDept.merge(department, waitTime, Integer::sum);
    }
    
    public int getTotalWaitingTime() { 
        return totalWaitTime; 
    }
    
    public int getTotalTreatmentTime() { 
        return totalTreatmentTime; 
    }
    
    public int getCurrentWaitTime() {
        return totalWaitTime - totalTreatmentTime;
    }
    
    public Map<String, Integer> getWaitTimesPerDept() {
        return new HashMap<>(waitTimesPerDept); // Return defensive copy
    }

    // ================== Visual Representation ==================
    
    /**
     * Draws the patient representation
     * @param x Center x-coordinate
     * @param y Bottom y-coordinate
     */
    public void redraw(double x, double y) {
        // Set color based on priority
        switch (priority) {
            case 1: UI.setColor(Color.RED); break;
            case 2: UI.setColor(Color.ORANGE); break;
            default: UI.setColor(Color.GREEN); break;
        }
        
        // Draw patient figure
        UI.fillOval(x-3, y-28, 6, 8);
        UI.fillRect(x-3, y-20, 6, 20);
        
        // Draw outline
        UI.setColor(Color.BLACK);
        UI.drawOval(x-3, y-28, 6, 8);
        UI.drawRect(x-3, y-20, 6, 20);
        
        // Draw initials
        UI.setFontSize(10);
        UI.drawString(String.valueOf(initials.charAt(0)), x-3, y-10);
        UI.drawString(String.valueOf(initials.charAt(1)), x-3, y-1);
    }

    @Override
    public String toString() {
        return String.format("%s (Priority %d) | Arrived: %d | Wait: %d | Treatment: %d | %d treatments remaining",
                            name, priority, arrivalTime, totalWaitTime, 
                            totalTreatmentTime, treatments.size());
    }


    /**
     * Draw the patient:
     * 6 units wide, 28 units high
     * x,y specifies center of the base
     
    public void redraw(double x, double y){
        if (priority == 1) UI.setColor(Color.red);
        else if (priority == 2) UI.setColor(Color.orange);
        else UI.setColor(Color.green);
        UI.fillOval(x-3, y-28, 6, 8);
        UI.fillRect(x-3, y-20, 6, 20);
        UI.setColor(Color.black);
        UI.drawOval(x-3, y-28, 6, 8);
        UI.drawRect(x-3, y-20, 6, 20);
        UI.setFontSize(10);
        UI.drawString(""+initials.charAt(0), x-3,y-10);
        UI.drawString(""+initials.charAt(1), x-3,y-1);
    }
    */
}
