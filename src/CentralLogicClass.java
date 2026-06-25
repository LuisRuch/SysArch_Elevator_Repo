public class CentralLogicClass {

    /*
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
     * statusInputs[0] = IX10.0 = Input_Door_Opened
     * statusInputs[1] = IX10.1 = Input_Door_Closed
     * statusInputs[2] = IX10.2 = Input_Motor_Ready
     * statusInputs[3] = IX10.3 = Input_Motor_On
     * statusInputs[4] = IX10.4 = Input_Motor_Error
     *
     *
     * specialInputs[0] = Input_Cycles    // ID1
     * specialInputs[1] = Input_AufzugID  // IW4
     * specialInputs[2] = Input_Speed     // IW6
     */

    //Datatypes
    public enum Req_Dir {Up, Down , DontCare};
    public enum Mode {OnCall, IDLE}

    //Modbus-Inputs
    private boolean[] levelInputs;
    private boolean[] statusInputs;
    private long[] specialInputs;

    //Variables for Calls
    private boolean[] stops = new boolean[5];
    private Req_Dir[] Req_Dir_Array = new Req_Dir[6];

    //Other Variables
    private boolean runningPolling = false;
    private Mode mode = Mode.IDLE;
    public boolean wasInSupervisor = false;
    private boolean[] reachedHelper = new boolean[5];
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
        opcuaInput = new OPCUAInputClass(this,modbus);
        elevatorSA = new ElevatorSAClass(this,opcuaInput,modbus,callLogic);
    }

    public void startPolling() throws Exception
    {
        //Read all Inputs before starting the Polling
        modbus.readAllInputs();
        setLevelInputs(modbus.getLevelInputs());
        setStatusInputs(modbus.getStatusInputs());
        setSpecialInputs(modbus.getSpecialInputs());
        opcuaInput.handleInputs();

        runningPolling = true;
        Thread pollingThread = new Thread(() -> {
            while (runningPolling) {
                try {
                    modbus.readAllInputs();
                    setLevelInputs(modbus.getLevelInputs());
                    setStatusInputs(modbus.getStatusInputs());
                    setSpecialInputs(modbus.getSpecialInputs());
                    reachsensorFunc();
                    opcuaInput.handleInputs();
                    if(!opcuaInput.getSupervisor())
                    {
                        if (elevatorSA.getCurrentState() != ElevatorSAClass.State.V1_UP
                                && elevatorSA.getCurrentState() != ElevatorSAClass.State.V1_DOWN
                                && elevatorSA.getCurrentState() != ElevatorSAClass.State.CRAWL
                                && (elevatorSA.getCurrentState() != ElevatorSAClass.State.STOPPED
                                || getReachedSensorActive()))
                            callLogic.UpdateNextLevel();

                        calcfunctions();
                        elevatorSA.handleStateTransitions();
                    }
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.err.println("Error while Polling: " + e.getMessage());
                }
            }
        });
        pollingThread.start();
    }



    //Calling helping functions
    public void calcfunctions()
    {
        switchBackFromSupervisor();
        if(!hasAnyStop())
        {
            mode = Mode.IDLE;
        }

        if(elevatorSA.getCurrentState() != ElevatorSAClass.State.STOPPED &&  elevatorSA.getCurrentState() != ElevatorSAClass.State.OPENING_DOOR &&  elevatorSA.getCurrentState() != ElevatorSAClass.State.CLOSING_DOOR)
        {
            mode = Mode.OnCall;
        }

        checkReachedTimer();
    }

    //polled right after Modbus-Input -> so nothing gehts lost
    public void reachsensorFunc()
    {
        if(elevatorSA.getCurrentState() == ElevatorSAClass.State.CRAWL)
        {
            if(getLevelInputs()[1])
                reachedHelper[1] = true;

            if(getLevelInputs()[9])
                reachedHelper[2] = true;

            if(getLevelInputs()[17])
                reachedHelper[3] = true;

            if(getLevelInputs()[25])
                reachedHelper[4] = true;

        }

    }

    //Other helper functions
    public boolean hasAnyStop() {
        for (boolean stop : stops) {
            if (stop) {
                return true;
            }
        }
        return false;
    }

    public void switchBackFromSupervisor()
    {

        if(opcuaInput.getSupervisor())
            wasInSupervisor = true;

        if(wasInSupervisor && opcuaInput.getSupervisor() == false)
        {

            if(levelInputs[1])
                callLogic.setCurrentLevel(1);
            if(levelInputs[9])
                callLogic.setCurrentLevel(2);
            if(levelInputs[17])
                callLogic.setCurrentLevel(3);
            if(levelInputs[25])
                callLogic.setCurrentLevel(25);

            wasInSupervisor = false;
        }

    }

    public boolean checkReachedTimer() {

        if (!reachedTimerRunning && getReachedSensorActive()) {
            reachedTimerRunning = true;
            reachedTimerStart = System.currentTimeMillis();
        }

        if (!reachedTimerRunning) {
            return false;
        }

        long elapsedMillis = System.currentTimeMillis() - reachedTimerStart;

        if (elapsedMillis >= 2000) {
            reachedTimerRunning = false;
            return false;
        }

        return elapsedMillis >= 1000;
    }



    //Getter and Setter
    public void setReachedTimerRunning(boolean running)
    {
        reachedTimerRunning = running;
    }

    public void setReachedTimerStart(long value)
    {
        reachedTimerStart = value;
    }


    public boolean getReachedSensorActive()
    {
        return  levelInputs[1]  // Reached Sensor L1
                || levelInputs[9]   // Reached Sensor L2
                || levelInputs[17]  // Reached Sensor L3
                ||  levelInputs[25]; // Reached Sensor L4
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

    public void setMode(Mode modeInsert) {
        mode = modeInsert;
    }

    public void setLevelInputs(boolean[] levelInputs) {
        this.levelInputs = levelInputs;
    }

    public void setStatusInputs(boolean[] statusInputs) {
        this.statusInputs = statusInputs;
    }

    public void setSpecialInputs(long[] specialInputs) {
        this.specialInputs = specialInputs;
    }

    public void setReachedHelper(boolean reachedHelper, int level) {
        this.reachedHelper[level] = reachedHelper;
    }

    public boolean[] getReachedHelper() {
        return reachedHelper;
    }
}
