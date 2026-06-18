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
    private boolean[] reachedSensors;

    ModbusClass modbus;

    CallLogicClass callLogic;
    OPCUAInputClass opcuaInput;
    ElevatorSAClass elevatorSA;

    public CentralLogicClass(ModbusClass modbus)
    {
        this.modbus = modbus;

        callLogic = new CallLogicClass(stops,Req_Dir_Array,this);
        opcuaInput = new OPCUAInputClass(stops,Req_Dir_Array);
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
                    reachedSensors = modbus.getReachsensors();
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

        //des kann noch in CallLogic gebaut werden
        difference = callLogic.getnextLevel() - callLogicurrentLevel;
    }

    public boolean hasAnyStop() {
        for (boolean stop : stops) {
            if (stop) {
                return true;
            }
        }
        return false;
    }



    //Getter und Setter

    public boolean[] getStops() {
        return stops;
    }

    public void setStops(boolean[] stops) {
        this.stops = stops;
    }

    public Req_Dir[] getReq_Dir_Array() {
        return Req_Dir_Array;
    }

    public void setReq_Dir_Array(Req_Dir[] req_Dir_Array) {
        this.Req_Dir_Array = req_Dir_Array;
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

    public boolean getReachedSensors(int level) {
        return reachedSensors[level];
    }

    public Mode getMode() {
        return mode;
    }
}
