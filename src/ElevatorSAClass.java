import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ElevatorSAClass {

    private State currentState = State.STOPPED;
    private State lastState = State.STOPPED;

    private ScheduledExecutorService schedulerDoor;
    private volatile int timerDoorLevel = 0;

    CentralLogicClass centralLogic;
    OPCUAInputClass opcuaInput;
    ModbusClass modbus;
    CallLogicClass callLogic;

    public ElevatorSAClass(CentralLogicClass centralLogic, OPCUAInputClass opcuaInput, ModbusClass modbus, CallLogicClass callLogic)
    {
        this.callLogic = callLogic;
        this.centralLogic = centralLogic;
        this.opcuaInput = opcuaInput;
        this.modbus = modbus;
    }

    public enum State {
        RESETTING,
        STOPPED,
        OPENING_DOOR,
        CLOSING_DOOR,
        V1_UP,
        V2_UP,
        V1_DOWN,
        V2_DOWN,
        CRAWL
    }

    public void handleStateTransitions()throws Exception
    {

        if (opcuaInput.getReset() && currentState != State.RESETTING) {
            changeState(State.RESETTING);
            return;
        }

        switch (currentState) {

            case RESETTING -> {
                if (!opcuaInput.getReset()) {
                    changeState(State.STOPPED);
                }
            }

            case STOPPED -> {

                //has to be before erasing stops / has to be if
                if (do1() && centralLogic.getStatusInputs()[1]) {
                    changeState(State.OPENING_DOOR);
                }

                //has to be if
                //if in level and reach sensor then rest stop[] at that level
                if(centralLogic.getStops()[callLogic.getCurrentLevel()] && (centralLogic.getLevelInputs()[1] || centralLogic.getLevelInputs()[9] || centralLogic.getLevelInputs()[17]  || centralLogic.getLevelInputs()[25])) {
                    centralLogic.setStops(callLogic.getCurrentLevel(), false);
                    centralLogic.setReq_Dir_Array(callLogic.getCurrentLevel(), null);
                }

                //here if else can start
                if(dc1() && centralLogic.getStatusInputs()[0]){
                    changeState(State.CLOSING_DOOR);
                }
                else if (AESU()) {
                    changeState(State.V1_UP);
                }
                else if (U1()&& centralLogic.getStatusInputs()[1]) { //closed door to start moving
                    changeState(State.V2_UP);
                }
                else if (AESD()) {
                    changeState(State.V1_DOWN);
                }
                else if (D1()&& centralLogic.getStatusInputs()[1]) { //closed door to start moving
                    changeState(State.V2_DOWN);
                }
                else if (AESC()) {
                    changeState(State.CRAWL);
                }


            }

            case OPENING_DOOR -> {
                if (do2()) {
                    changeState(State.STOPPED);
                }
            }

            case CLOSING_DOOR -> {
                if (dc2()) {
                    changeState(State.STOPPED);
                }
            }

            case V2_UP -> {
                if (ES()) {
                    changeState(State.STOPPED);
                }
                else if (U2()) {
                    changeState(State.V1_UP);
                }
            }

            //same transitions
            case V1_UP, V1_DOWN -> {
                if (ES()) {
                    changeState(State.STOPPED);
                }
                else if (U3()) {
                    changeState(State.CRAWL);
                }
            }

            case V2_DOWN -> {
                if (ES()) {
                    changeState(State.STOPPED);
                }
                else if (D2()) {
                    changeState(State.V1_DOWN);
                }
            }

            case CRAWL -> {

                if (ES()) {
                    changeState(State.STOPPED);
                }
                else if (finish()){
                    callLogic.setCurrentLevel(callLogic.getNextLevel());
                    changeState(State.STOPPED);
                }
            }
        }
    }



    private void changeState(State newState) throws Exception
    {

        if (currentState == newState) {
            return;
        }

        lastState = currentState;
        currentState = newState;

        //on entry of state the follwoing actions will be caried out
        switch (newState) {
            case RESETTING:
                performReset();
                break;

            case STOPPED:
                modbus.stopMotor();
                modbus.stopDoor();
                break;

            case OPENING_DOOR:
                modbus.startOpenDoor();

                break;

            case CLOSING_DOOR:
                modbus.startCloseDoor();
                break;

            case V1_UP:
                modbus.startMotorUpV1();
                break;

            case V2_UP:
                modbus.startMotorUpV2();
                break;

            case V1_DOWN:
                modbus.startMotorDownV1();
                break;

            case V2_DOWN:
                modbus.startMotorDownV2();
                break;

            case CRAWL:

                crawlapproach();
                break;
        }


    }




    private void crawlapproach() {
        //hier einmal thread aufrufen. der sich annährt
    }




    //Conditions from STOPPED_CLOSED_DOOR
    private boolean do1() {
        //Automatic open door, if elevator arrived at destination - no Emergency Stop  && last State was crawl
        //or

        //if had no calls(IDLE) and (new)request in same level as current elevator

        if((!opcuaInput.getEmergencyStop() && lastState == State.CRAWL)  || (centralLogic.getMode() == CentralLogicClass.Mode.IDLE && centralLogic.getStops()[callLogic.getCurrentLevel()] && !opcuaInput.getEmergencyStop()))
            return true;

        else
            return false;
    }

    private boolean AESU() {
        //after emergncy stop if he was already in final approach - last stop had to be open door is there was no emergency
        //last state not open door and no emergency stop

        if(lastState == State.V1_UP && !opcuaInput.getEmergencyStop())
            return true;

        else
            return false;
    }

    private boolean U1() {
        //last state not open door and no emergency stop
        //or
        //differnce pos and no ES


        if((lastState == State.V2_UP && !opcuaInput.getEmergencyStop()) || (callLogic.getdiffernce() > 0 && !opcuaInput.getEmergencyStop()))
            return true;

        else
            return false;

    }

    private boolean AESD() {

        if(lastState == State.V1_DOWN && !opcuaInput.getEmergencyStop())
            return true;

        else
            return false;
    }

    private boolean D1() {

        if((lastState == State.V2_DOWN && !opcuaInput.getEmergencyStop()) || (callLogic.getdiffernce() < 0 && !opcuaInput.getEmergencyStop()))
            return true;

        else
            return false;

    }

    private boolean AESC() {

        if(lastState == State.CRAWL && !opcuaInput.getEmergencyStop())
            return true;

        else
            return false;
    }

    //Rest
    private void performReset() throws Exception {


        modbus.stopMotor();
        modbus.stopDoor();
        modbus.resetSimulation();


        stopDoorTimer();
        centralLogic.setApproachTimerUp(false);
        centralLogic.setApproachTimerDOWN(false);
        timerDoorLevel = 0;


        centralLogic.setStops(1,false);
        centralLogic.setStops(2,false);
        centralLogic.setStops(3,false);
        centralLogic.setStops(4,false);
        centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.DontCare);
        centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.DontCare);
        centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.DontCare);
        centralLogic.setReq_Dir_Array(4, CentralLogicClass.Req_Dir.DontCare);
        centralLogic.setApproachTimerUp(false);
        centralLogic.setApproachTimerStartUP(0);
        centralLogic.setApproachTimerDOWN(false);
        centralLogic.setApproachTimerStartDOWN(0);
        centralLogic.setReachedTimerRunning(false);
        centralLogic.setReachedTimerStart(0);
        centralLogic.setMode(CentralLogicClass.Mode.IDLE);
        modbus.setLastLowerApproachSensorLevel(0);
        modbus.setLastUpperApproachSensorLevel(0);
        callLogic.setCurrentLevel(1);
        callLogic.setDifference(0);
        callLogic.setNextLevel(1);
        callLogic.setDirOfTrv(CentralLogicClass.Req_Dir.DontCare);
    }
    //Conditions from STOPPED_OPEN_DOOR
    private boolean dc1() {
        // Close door when:
        // - call exists and 6 seconds waited and no ES
        // OR
        //no calls after 6 sec and doorclose
        //OR
        // - no call exists and 12 seconds waited and no ES

        if ((getDoorTimerLevel() >= 6 && centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 6 && opcuaInput.getCloseDoor() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 12 && !centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()))
        {
            stopDoorTimer();
            return true;
        }
        else
            return false;
    }

    //v2 up state transitions
    private boolean U2()
    {
        //one sec after approach sensor triggort (0,5m) left
        //and level approach sensor == level form destination (because differnt destinatioin could be set in that time)
        if (centralLogic.getApproachTimerUPMillisSeconds() >= 1 && modbus.getLastLowerApproachSensorLevel() == callLogic.getNextLevel())
        {
            centralLogic.setApproachTimerUp(false);
            return true;
        }
        else
            return false;
    }

    private boolean U3()
    {
        //look a dirofTrav
        //and level approach sensor == level form destination
        //and
        //saftey sensor - no matter which one -> physical space of elevator important
        if(callLogic.getDirOfTrv() == CentralLogicClass.Req_Dir.Up){
            if((modbus.getLastLowerApproachSensorLevel() == callLogic.getNextLevel()) && centralLogic.getAnySafetyStop())
                return true;
        }
        if(callLogic.getDirOfTrv() == CentralLogicClass.Req_Dir.Down){
            if((modbus.getLastUpperApproachSensorLevel() == callLogic.getNextLevel()) && centralLogic.getAnySafetyStop())
                return true;
        }
        return false;
    }

    //v2 down state transitions
    private boolean D2()
    {
        //one sec after approach sensor triggort (0,5m) left
        //and level approach sensor == level form destination (because differnt destinatioin could be set in that time)
        if (centralLogic.getApproachTimerDOWNMillisSeconds() >= 1 && modbus.getLastUpperApproachSensorLevel() == callLogic.getNextLevel())
        {
            centralLogic.setApproachTimerDOWN(false);
            return true;
        }
        else
            return false;
    }

    //crawl
    private boolean finish()
    {
        if(centralLogic.checkReachedTimer() && (centralLogic.getLevelInputs()[1] || centralLogic.getLevelInputs()[9] || centralLogic.getLevelInputs()[17]  || centralLogic.getLevelInputs()[25]))
            return true;
        else
            return false;
    }

    //ES
    private boolean ES()
    {
        if(opcuaInput.getEmergencyStop())
            return true;
        else
            return false;
    }

    //doors
    private boolean do2()
    {
        if(centralLogic.getStatusInputs()[0])
        {
            startDoorTimer();
            return true;
        }
        else
            return false;
    }

    private boolean dc2()
    {
        if(centralLogic.getStatusInputs()[1])
            return true;
        else
            return false;
    }

    //des noch in central machen
    //spezial funktions
    //herer only used thread for timer
    public void startDoorTimer() {
        stopDoorTimer();
        timerDoorLevel = 0;
        schedulerDoor = Executors.newSingleThreadScheduledExecutor();

        schedulerDoor.scheduleAtFixedRate(() -> {
            timerDoorLevel++;
        }, 0, 1, TimeUnit.SECONDS);
    }

    public int getDoorTimerLevel() {
        return timerDoorLevel;
    }

    public void stopDoorTimer() {
        if (schedulerDoor != null) {
            schedulerDoor.shutdown();
        }
    }

    //getter
    public State getCurrentState() {
        return currentState;
    }

}