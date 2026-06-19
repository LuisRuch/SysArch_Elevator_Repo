public class CentralLogicClass {

    /*
     * Array-Zuordnung der eingelesenen Inputs
     *
     * levelInputs = client.ReadDiscreteInputs(Input_l1sl, 27);
     * Startadresse: IX0.1
     *
     * levelInputs[0]  = IX0.1  = Input_l1sl
     * levelInputs[1]  = IX0.2  = Input_l1r
     * levelInputs[2]  = IX0.3  = Input_l1su
     * levelInputs[3]  = IX0.4  = Input_l1au
     *
     * levelInputs[7]  = IX1.0  = Input_l2al
     * levelInputs[8]  = IX1.1  = Input_l2sl
     * levelInputs[9]  = IX1.2  = Input_l2r
     * levelInputs[10] = IX1.3  = Input_l2su
     * levelInputs[11] = IX1.4  = Input_l2au
     *
     * levelInputs[15] = IX2.0  = Input_l3al
     * levelInputs[16] = IX2.1  = Input_l3sl
     * levelInputs[17] = IX2.2  = Input_l3r
     * levelInputs[18] = IX2.3  = Input_l3su
     * levelInputs[19] = IX2.4  = Input_l3au
     *
     * levelInputs[23] = IX3.0  = Input_l4al
     * levelInputs[24] = IX3.1  = Input_l4sl
     * levelInputs[25] = IX3.2  = Input_l4r
     * levelInputs[26] = IX3.3  = Input_l4su
     *
     *
     * statusInputs = client.ReadDiscreteInputs(Input_Door_Opened, 5);
     * Startadresse: IX10.0
     *
     * statusInputs[0] = IX10.0 = Input_Door_Opened
     * statusInputs[1] = IX10.1 = Input_Door_Closed
     * statusInputs[2] = IX10.2 = Input_Motor_Ready
     * statusInputs[3] = IX10.3 = Input_Motor_On
     * statusInputs[4] = IX10.4 = Input_Motor_Error
     *
     *
     * specialInputs:
     *
     * specialInputs[0] = Input_Cycles    // ID1
     * specialInputs[1] = Input_AufzugID  // IW4
     * specialInputs[2] = Input_Speed     // IW6
     */

    //Variables for elevator levels - passed to HMI-Class
    private boolean[] stops = new boolean[5];                        //stops[1] = level 1, stops[2] = level 2, stops[3] = level 3, stops[4] = level 4
    public enum Req_Dir {Up, Down , DontCare};                      //Requested direction
    private Req_Dir[] Req_Dir_Array = new Req_Dir[5];
    public enum Mode {OnCall, IDLE}
    private Mode mode = Mode.IDLE;

    private boolean runningModbus = false;
    private boolean runningRest = false;


    private boolean[] levelInputs;       // IX0.1 bis IX3.3
    private boolean[] statusInputs;      // IX10.0 bis IX10.4
    private long[] specialInputs;        // [0] cycles, [1] aufzugID, [2] speed

    private boolean approachTimerUPRunning = false;
    private long approachTimerStartUP = 0;
    private boolean approachTimerDOWNRunning = false;
    private long approachTimerStartDOWN = 0;
    private boolean reachedTimerRunning = false;
    private long reachedTimerStart = 0;

    ModbusClass modbus;

    CallLogicClass callLogic;
    OPCUAInputClass opcuaInput;
    ElevatorSAClass elevatorSA;

    public CentralLogicClass(ModbusClass modbus)
    {
        this.modbus = modbus;

        callLogic = new CallLogicClass(stops,Req_Dir_Array,this);
        opcuaInput = new OPCUAInputClass(this);
        elevatorSA = new ElevatorSAClass(this,opcuaInput,modbus,callLogic);

    }
    //start Modbus
    public void startPollingModbus()
    {
        runningModbus = true;
        Thread pollingModbusThread = new Thread(() -> {
            while (runningModbus) {
                try {
                    modbus.readAllInputs();
                    levelInputs = modbus.getLevelInputs();
                    statusInputs = modbus.getStatusInputs();
                    specialInputs = modbus.getSpecialInputs();
                    modbus.updateLastLowerApproachSensorFromLevelInputs();
                    modbus.updateLastUpperApproachSensorFromLevelInputs();
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.err.println("Modbus reading error: " + e.getMessage());
                }
            }
        });
        //pollingModbusThread.setDaemon(true); // Thread stops if main stops
        pollingModbusThread.start();
    }

    public void startPollingRest()
    {
        runningRest = true;
        Thread pollingRestThread = new Thread(() -> {
            while (runningRest) {
                try {
                    opcuaInput.handleInputs();
                    callLogic.UpdateNextLevel();
                    calcfunctions();
                    elevatorSA.handleStateTransitions();
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.err.println("Rest reading error: " + e.getMessage());
                }
            }
        });
        //pollingModbusThread.setDaemon(true); // Thread stops if main stops
        pollingRestThread.start();
    }


    //special Information
    public void calcfunctions()
    {
        if(!hasAnyStop())
        {
            mode = Mode.IDLE;
        }

        //wird erst im 2 durchlauf akuellisiert- wenn wirklich schon los ist. damit kann man sehen ob die request im gleiche floor ist und dann wird die tür geöffnet
        if(elevatorSA.getCurrentState() != ElevatorSAClass.State.STOPPED_CLOSED_DOOR ||  elevatorSA.getCurrentState() != ElevatorSAClass.State.STOPPED_OPEN_DOOR)
        {
            mode = Mode.OnCall;
        }

        checkApproachTimerUP();
        checkApproachTimerDOWN();
        checkReachedTimer();

    }

    public boolean hasAnyStop() {
        for (boolean stop : stops) {
            if (stop) {
                return true;
            }
        }
        return false;
    }

    //working with timestamps - no timer
    public void checkApproachTimerUP() {
        if (levelInputs == null) {
            return;
        }

        // nextLevel 2: Lower Approach L2 = index 7, Upper Approach L2 = index 11
        if (callLogic.getNextLevel() == 2) {
            if (approachTimerUPRunning && levelInputs[11]) {
                approachTimerUPRunning = false;
                return;
            }

            if (!approachTimerUPRunning && levelInputs[7]) {
                approachTimerUPRunning = true;
                approachTimerStartUP = System.currentTimeMillis();
            }
        }

        // nextLevel 3: Lower Approach L3 = index 15, Upper Approach L3 = index 19
        else if (callLogic.getNextLevel() == 3) {
            if (approachTimerUPRunning && levelInputs[19]) {
                approachTimerUPRunning = false;
                return;
            }

            if (!approachTimerUPRunning && levelInputs[15]) {
                approachTimerUPRunning = true;
                approachTimerStartUP = System.currentTimeMillis();
            }
        }

        // nextLevel 4: Lower Approach L4 = index 23
        // Für Level 4 hast du keinen Upper Approach Sensor definiert
        else if (callLogic.getNextLevel() == 4) {
            if (!approachTimerUPRunning && levelInputs[23]) {
                approachTimerUPRunning = true;
                approachTimerStartUP = System.currentTimeMillis();
            }
        }
    }

    public void checkApproachTimerDOWN() {
        if (levelInputs == null) {
            return;
        }

        // nextLevel 1: Upper Approach L1 = index 3
        // Für Level 1 hast du keinen Lower Approach Sensor definiert
        if (callLogic.getNextLevel() == 1) {
            if (!approachTimerDOWNRunning && levelInputs[3]) {
                approachTimerDOWNRunning = true;
                approachTimerStartDOWN = System.currentTimeMillis();
            }
        }

        // nextLevel 2: Upper Approach L2 = index 11, Lower Approach L2 = index 7
        else if (callLogic.getNextLevel() == 2) {
            if (approachTimerDOWNRunning && levelInputs[7]) {
                approachTimerDOWNRunning = false;
                return;
            }

            if (!approachTimerDOWNRunning && levelInputs[11]) {
                approachTimerDOWNRunning = true;
                approachTimerStartDOWN = System.currentTimeMillis();
            }
        }

        // nextLevel 3: Upper Approach L3 = index 19, Lower Approach L3 = index 15
        else if (callLogic.getNextLevel() == 3) {
            if (approachTimerDOWNRunning && levelInputs[15]) {
                approachTimerDOWNRunning = false;
                return;
            }

            if (!approachTimerDOWNRunning && levelInputs[19]) {
                approachTimerDOWNRunning = true;
                approachTimerStartDOWN = System.currentTimeMillis();
            }
        }
    }

    public boolean checkReachedTimer() {
        if (levelInputs == null) {
            return false;
        }

        boolean reachedSensorActive = false;

        // nextLevel 1: Reached Sensor L1 = index 1
        if (callLogic.getNextLevel() == 1) {
            reachedSensorActive = levelInputs[1];
        }

        // nextLevel 2: Reached Sensor L2 = index 9
        else if (callLogic.getNextLevel() == 2) {
            reachedSensorActive = levelInputs[9];
        }

        // nextLevel 3: Reached Sensor L3 = index 17
        else if (callLogic.getNextLevel() == 3) {
            reachedSensorActive = levelInputs[17];
        }

        // nextLevel 4: Reached Sensor L4 = index 25
        else if (callLogic.getNextLevel() == 4) {
            reachedSensorActive = levelInputs[25];
        }

        // Timer starten, wenn Reached Sensor aktiv ist
        if (!reachedTimerRunning && reachedSensorActive) {
            reachedTimerRunning = true;
            reachedTimerStart = System.currentTimeMillis();
        }

        // Wenn Timer nicht läuft, false zurückgeben
        if (!reachedTimerRunning) {
            return false;
        }

        long elapsedMillis = System.currentTimeMillis() - reachedTimerStart;

        // Nach 2 Sekunden Timer stoppen
        if (elapsedMillis >= 2000) {
            reachedTimerRunning = false;
            return false;
        }

        // Nach 1 Sekunde true zurückgeben
        return elapsedMillis >= 1000;
    }



    //Getter und Setter

    public long getApproachTimerUPMillisSeconds() {
        if (!approachTimerUPRunning) {
            return 0;
        }

        return (System.currentTimeMillis() - approachTimerStartUP)/1000;
    }

    public void setApproachTimerUp(boolean set){
        approachTimerUPRunning = set;
    }

    public long getApproachTimerDOWNMillisSeconds() {
        if (!approachTimerDOWNRunning) {
            return 0;
        }

        return (System.currentTimeMillis() - approachTimerStartDOWN) / 1000;
    }

    public void setApproachTimerDOWN(boolean set) {
        approachTimerDOWNRunning = set;
    }


    public boolean getAnySaftyStop() {
        if (levelInputs == null) {
            return false;
        }

        return levelInputs[0]   // L1 SL
                || levelInputs[2]   // L1 SU
                || levelInputs[8]   // L2 SL
                || levelInputs[10]  // L2 SU
                || levelInputs[16]  // L3 SL
                || levelInputs[18]  // L3 SU
                || levelInputs[24]  // L4 SL
                || levelInputs[26]; // L4 SU
    }




    public boolean[] getStops() {
        return stops;
    }

    public void setStops(int index,boolean state) {
        this.stops[index] = state;
    }

    public Req_Dir[] getReq_Dir_Array() {
        return Req_Dir_Array;
    }

    public void setReq_Dir_Array(int index, Req_Dir reqDir) {
        this.Req_Dir_Array[index] = reqDir;
    }

    public boolean[] getLevelInputs() {
        return levelInputs;
    }

    public boolean[] getStatusInputs() {
        return statusInputs;
    }

    public long[] getSpecialInputs() {
        return specialInputs;
    }

    public Mode getMode() {
        return mode;
    }
}
