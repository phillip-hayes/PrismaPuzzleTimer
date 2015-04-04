// reference: http://hackvalue.de/hv_atmel_stackmat

package com.puzzletimer.timer;

import java.util.Date;

import com.puzzletimer.models.Timing;
import com.puzzletimer.state.TimerManager;

import au.com.emc.cubing.stackmat.StackmatManager;
import au.com.emc.cubing.stackmat.StackmatListener;
import au.com.emc.cubing.stackmat.StackmatState;


class StackmatTimerGen3Listener implements StackmatListener {
    private TimerManager timerManager;
    
    public StackmatTimerGen3Listener(TimerManager theTimer) {
        timerManager = theTimer;
    }
    
    public void stateUpdate(StackmatState oldState, StackmatState newState) {
        
        // left hand state
        if (newState.getLeftHand()) {
            this.timerManager.pressLeftHand();
        } else {
            this.timerManager.releaseLeftHand();
        }
        
        // right hand state
        if (newState.getRightHand()) {
            this.timerManager.pressRightHand();
        } else {
            this.timerManager.releaseRightHand();
        }        
        
        // update the solution timing
        long time = 60000 * newState.getMinutes()
                + 1000 * newState.getSeconds()
                + newState.getThousands();
        Date end = new Date();
        Date start = new Date(end.getTime() - time);
        Timing timing = new Timing(start, end);  
            
        if (newState.isReset()) {
            this.timerManager.resetTimer();
        } else if (oldState != null) {
            if (newState.isRunning() && ! oldState.isRunning()) {
                this.timerManager.startSolution();
            } else if (! newState.isRunning() && oldState.isRunning()) {
                this.timerManager.finishSolution(timing);
            } else {
                this.timerManager.updateSolutionTiming(timing);                
            }
        }
        
        
//        // hands status
//        if (data[0] == 'A' || data[0] == 'L' || data[0] == 'C') {
//            this.timerManager.pressLeftHand();
//        } else {
//            this.timerManager.releaseLeftHand();
//        }
//
//        if (data[0] == 'A' || data[0] == 'R' || data[0] == 'C') {
//            this.timerManager.pressRightHand();
//        } else {
//            this.timerManager.releaseRightHand();
//        }
//
//        // time
//        int minutes = data[1] - '0';
//        int seconds = 10 * (data[2] - '0') + data[3] - '0';
//        int centiseconds = 10 * (data[4] - '0') + data[5] - '0';
//
//        long time = 60000 * minutes + 1000 * seconds + 10 * centiseconds;
//        Date end = new Date();
//        Date start = new Date(end.getTime() - time);
//        Timing timing = new Timing(start, end);
//
//        this.start = start;
//
//        // state transitions
//        switch (this.state) {
//            case NOT_READY:
//                // timer initialized
//                if (time == 0) {
//                    this.timerManager.resetTimer();
//
//                    this.state = this.inspectionEnabled ?
//                        StackmatTimerGen3.State.RESET_FOR_INSPECTION : StackmatTimerGen3.State.RESET;
//                }
//                break;
//
//            case RESET_FOR_INSPECTION:
//                // some pad pressed
//                if (data[0] == 'L' || data[0] == 'R' || data[0] == 'C') {
//                    this.timerManager.startInspection();
//
//                    this.state = StackmatTimerGen3.State.RESET;
//                }
//                break;
//
//            case RESET:
//                // ready to start
//                if (data[0] == 'A') {
//                    this.state = StackmatTimerGen3.State.READY;
//                }
//
//                // timing started
//                if (time > 0) {
//                    this.timerManager.startSolution();
//
//                    this.state = StackmatTimerGen3.State.RUNNING;
//                }
//                break;
//
//            case READY:
//                // timing started
//                if (time > 0) {
//                    this.timerManager.startSolution();
//
//                    this.state = StackmatTimerGen3.State.RUNNING;
//                }
//                break;
//
//            case RUNNING:
//                // timer reset during solution
//                if (time == 0) {
//                    this.state = StackmatTimerGen3.State.NOT_READY;
//                }
//
//                // timer stopped
//                if (data[0] == 'C' || data[0] == 'S') {
//                    this.state = StackmatTimerGen3.State.NOT_READY;
//                    this.timerManager.updateSolutionTiming(timing);
//
//                    this.timerManager.finishSolution(timing);
//                }
//                break;
//        }        
        
        
    }
}

public class StackmatTimerGen3 implements Timer {

    private TimerManager timerManager;
    private boolean inspectionEnabled;
    private StackmatListener stackmatListener;

    public StackmatTimerGen3(TimerManager theTimerManager) {
        this.timerManager = timerManager;
        this.inspectionEnabled = false;
        this.timerManager = theTimerManager;
    }

    @Override
    public String getTimerId() {
        return "STACKMAT-GEN3-TIMER";
    }

    @Override
    public void setInspectionEnabled(boolean inspectionEnabled) {
        this.inspectionEnabled = inspectionEnabled;
    }

    @Override
    public void start() {
        StackmatManager smm = StackmatManager.getInstance();
        this.stackmatListener = new StackmatTimerGen3Listener(timerManager);        
        smm.register(this.stackmatListener);
        smm.start();
    }

    @Override
    public void stop() {
        StackmatManager smm = StackmatManager.getInstance();
        smm.unregister(this.stackmatListener);
        smm.stop();
    }
}
