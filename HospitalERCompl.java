// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2025T2, Assignment 3
 * Name: Matthew McGowan
 * Username:mcgowamatt1
 * ID:300672872
 */

import ecs100.*;
import java.util.*;
import java.io.*;

/**
 * Simulation of a Hospital ER
 * 
 * The hospital has a collection of Departments, including the ER department, each of which has
 *  and a treatment room.
 * 
 * When patients arrive at the hospital, they are immediately assessed by the
 *  triage team who determine the priority of the patient and (unrealistically) a sequence of treatments 
 *  that the patient will need.
 *
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 *
 *  READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl {
    private Map<String, Department> departments = new HashMap<String, Department>();

    // Statistics fields
    private int numDischarged = 0;
    private int totalWait = 0;
    private int maxWait = 0;
    private int maxWaitPri1 = 0;
    
    // Priority-1 stats
    private int numDischargedPri1 = 0;
    private int totalWaitPri1 = 0;
    private int pri1AtRiskCount = 0;
    private int pri1TreatedQuickly = 0;  
    
    private static final int CRITICAL_WAIT_THRESHOLD = 500; 
    private static final int PRI1_TIMEOUT = 100; 

    // Simulation fields
    private boolean running = false;
    private int time = 0;
    private int delay = 300;

    public void reset(boolean usePriorityQueues) {
        running = false;
        UI.sleep(2*delay);
        time = 0;

        departments = new LinkedHashMap<>();
        departments.put("ER", new Department("ER", 8, usePriorityQueues));
        departments.put("X-Ray", new Department("X-Ray", 3, usePriorityQueues));
        departments.put("MRI", new Department("MRI", 1, usePriorityQueues));
        departments.put("UltraSound", new Department("UltraSound", 2, usePriorityQueues));
        departments.put("Surgery", new Department("Surgery", 3, usePriorityQueues));

        // Reset all statistics
        numDischarged = 0;
        totalWait = 0;
        maxWait = 0;
        maxWaitPri1 = 0;
        numDischargedPri1 = 0;
        totalWaitPri1 = 0;
        pri1AtRiskCount = 0;
        pri1TreatedQuickly = 0;

        UI.clearGraphics();
        UI.clearText();
    }

    public void run() {
        if (running) { return; }
        running = true;

        while (running) {
            //Collecting and routing finished patients
            for (Department dept : departments.values()) {
                List<Patient> finishedHere = dept.collectFinished();
                for (Patient p : finishedHere) {
                    p.removeCurrentTreatment();
                    if (p.allTreatmentsCompleted()) {
                        discharge(p);
                    } else {
                        String nextDeptName = p.getCurrentDepartment();
                        Department nextDept = departments.get(nextDeptName);
                        if (nextDept != null) {
                            nextDept.enqueue(p);
                        } else {
                            UI.println(time + ": WARNING unknown department '" + 
                                     nextDeptName + "' for patient: " + p);
                        }
                    }
                }
            }

            //Process treatment ticks
            for (Department dept : departments.values()) {
                dept.tickTreatment();
            }

            //Process waiting ticks
            for (Department dept : departments.values()) {
                dept.tickWaiting();
            }

            //Check for priority 1 patients waiting too long (added this incase a paitent has waited too long(
            for (Department dept : departments.values()) {
                for (Patient p : dept.getWaitingPatients()) {
                    if (p.getPriority() == 1 && p.getCurrentWaitTime() > PRI1_TIMEOUT) {
                        if (dept.forcePriority1Treatment(p)) {
                            break; // Only force one per department per tick
                        }
                    }
                }
            }

            //Normal admission process
            for (Department dept : departments.values()) {
                dept.admitWhileSpace();
            }

            //new arrivals
            Patient newPatient = PatientGenerator.getNextPatient(time);
            if (newPatient != null) {
                UI.println(time + ": Arrived: " + newPatient);
                String firstDeptName = newPatient.getCurrentDepartment();
                Department firstDept = departments.get(firstDeptName);
                if (firstDept != null) {
                    firstDept.enqueue(newPatient);
                } else {
                    UI.println(time + ": WARNING unknown first department '" +
                             firstDeptName + "' for patient: " + newPatient);
                }
            }

            //Update display and time
            redraw();
            time++;
            UI.sleep(delay);
        }

        reportStatistics();
    }

    public void discharge(Patient p) {
        numDischarged++;
        totalWait += p.getTotalWaitingTime();
        p.setDischargeTime(time);
        
        int w = p.getTotalWaitingTime();
        if (w > maxWait) maxWait = w;
        
        if (p.getPriority() == 1) {
            numDischargedPri1++;
            totalWaitPri1 += w;
            if (w > maxWaitPri1) maxWaitPri1 = w;
            if (w > CRITICAL_WAIT_THRESHOLD) {
                pri1AtRiskCount++;
            }
            if (w <= PRI1_TIMEOUT) {
                pri1TreatedQuickly++;
            }
        }

        UI.println(time + ": Discharge: " + p 
                + " | TotalWait=" + p.getTotalWaitingTime()
                + " | SystemTime=" + p.getSystemTime());
    }

    public void reportStatistics() {
        UI.println("----- Statistics -----");
        UI.println("Simulated Time: " + time);
        UI.println("Total patients treated: " + numDischarged);
        UI.println("Max waiting time: " + maxWait);
    
        if (numDischarged > 0) {
            double avgWait = (double) totalWait / numDischarged;
            UI.println("Average waiting time: " + avgWait);
        }
        UI.println("\n----- Priority 1 Patients -----");
        UI.println("Priority 1 patients treated: " + numDischargedPri1);
        if (numDischargedPri1 > 0) {
            double avgWait = (double) totalWait / numDischarged;
            double AvgWaitCompared = Math.floor(avgWait * 100) / 100;
            double avgWaitPri1 = (double) totalWaitPri1 / numDischargedPri1;
            UI.println("Average waiting time (Priority 1): " + avgWaitPri1 + " (" + AvgWaitCompared + "% fater than average wait time)");
        }
        
        UI.println("Max waiting time (Priority 1): " + maxWaitPri1);
        
        UI.println("Priority 1 patients at risk (> " + CRITICAL_WAIT_THRESHOLD + 
                  " wait): " + pri1AtRiskCount);
        UI.println("Priority 1 patients treated within " + PRI1_TIMEOUT + 
                  " ticks: " + pri1TreatedQuickly + "/" + numDischargedPri1);
        
                  
        UI.println("\n--- Department Stats ---");
        for (Department dept : departments.values()) {
            int served = dept.getTotalPatientsServed();
            double avgWait = served == 0 ? 0.0 : (double)dept.getTotalWaitingTime()/served;
            UI.println(dept.getName() + 
                      " | Patients served: " + served + 
                      " | Avg wait: " + String.format("%.1f", avgWait) + 
                      " | Max queue: " + dept.getMaxQueueLength());
        }
    }



    // METHODS FOR THE GUI AND VISUALISATION

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI(){
        UI.addButton("Reset (Queue)", () -> {this.reset(false); });
        UI.addButton("Reset (Pri Queue)", () -> {this.reset(true);});
        UI.addButton("Start", ()->{if (!running){ run(); }});   //don't start if already running!
        UI.addButton("Pause & Report", ()->{running=false;});
        UI.addSlider("Speed", 1, 400, (401-delay),
            (double val)-> {delay = (int)(401-val);});
        UI.addSlider("Av arrival interval", 1, 50, PatientGenerator.getArrivalInterval(),
                     PatientGenerator::setArrivalInterval);
        UI.addSlider("Prob of Pri 1", 1, 100, PatientGenerator.getProbPri1(),
                     PatientGenerator::setProbPri1);
        UI.addSlider("Prob of Pri 2", 1, 100, PatientGenerator.getProbPri2(),
                     PatientGenerator::setProbPri2);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000,600);
        UI.setDivider(0.5);
    }

    /**
     * Redraws all the departments
     */
    public void redraw(){
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0,32,400, 32);
        double y = 80;
        for (Department dept : departments.values()){
            dept.redraw(y);
            UI.drawLine(0,y+2,400, y+2);
            y += 50;
        }
    }

    /**
     * Construct a new HospitalER object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments){
        HospitalERCompl er = new HospitalERCompl();
        er.setupGUI();
        er.reset(true);   // initialise with an ordinary queue.
    }        


}
