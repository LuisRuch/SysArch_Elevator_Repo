import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ElevatorSAClass {

    private State currentState = State.STOPPED_CLOSED_DOOR;
    private State lastState = State.STOPPED_CLOSED_DOOR;

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
        STOPPED_CLOSED_DOOR,
        STOPPED_OPEN_DOOR,
        V1_UP,
        V2_UP,
        V1_DOWN,
        V2_DOWN,
        CRAWL
    }

    public void handleStateTransitions()throws Exception
    {

        switch (currentState) {

            case STOPPED_CLOSED_DOOR -> {
                if (door1()) {
                    changeState(State.STOPPED_OPEN_DOOR);
                }
                else if (AESU()) {
                    changeState(State.V1_UP);
                }
                else if (U1()) {
                    changeState(State.V2_UP);
                }
                else if (AESD()) {
                    changeState(State.V1_DOWN);
                }
                else if (D1()) {
                    changeState(State.V2_DOWN);
                }
                else if (AESC()) {
                    changeState(State.CRAWL);
                }
            }

            case STOPPED_OPEN_DOOR -> {
                if (door2()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
            }

            case V2_UP -> {
                if (ES()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (U2()) {
                    changeState(State.V1_UP);
                }
            }

            //same transitions
            case V1_UP, V1_DOWN -> {
                if (ES()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (U3()) {
                    changeState(State.CRAWL);
                }
            }

            case V2_DOWN -> {
                if (ES()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (D2()) {
                    changeState(State.V1_DOWN);
                }
            }

            case CRAWL -> {

                if (ES() || finish()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
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
            case STOPPED_CLOSED_DOOR:
                modbus.stopMotor();
                modbus.stopDoor();
                break;

            case STOPPED_OPEN_DOOR:
                modbus.stopDoor();
                startDoorTimer();
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
    private boolean door1() {
        //Automatic open door, if elevator arrived at destination - no Emergency Stop  && last State was crawl
        //or
        //open door if no call and button clicked to open door
        //or
        //if had no calls(IDLE) and (new)request in same level as current elevator

        if((!opcuaInput.getEmergencyStop() && lastState == State.CRAWL) || (!centralLogic.getStops()[0] && opcuaInput.getOpenDoor()) || centralLogic.getMode() == CentralLogicClass.Mode.IDLE)
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


    //Conditions from STOPPED_OPEN_DOOR
    private boolean door2() {
        // Close door when:
        // - call exists and 6 seconds waited and no ES
        // OR
        //no calls after 6 sec and doorclose
        //OR
        // - no call exists and 12 seconds waited and no ES

        if ((getDoorTimerLevel() >= 6000 && centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 6000 && opcuaInput.getCloseDoor() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 12000 && centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()))
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
        if (centralLogic.getApproachTimerUPMillisSeconds() >= 1000 && modbus.getLastLowerApproachSensorLevel() == callLogic.getNextLevel())
        {
            centralLogic.setApproachTimerUp(false);
            return true;
        }
        else
            return false;
    }

    private boolean U3()
    {
        //and level approach sensor == level form destination
        //and
        //saftey sensor - no matter which one -> physical space of elevator important
        if((modbus.getLastLowerApproachSensorLevel() == callLogic.getNextLevel()) && centralLogic.getAnySaftyStop())
            return true;
        else
            return false;
    }

    //v2 down state transitions
    private boolean D2()
    {
        //one sec after approach sensor triggort (0,5m) left
        //and level approach sensor == level form destination (because differnt destinatioin could be set in that time)
        if (centralLogic.getApproachTimerDOWNMillisSeconds() >= 1000 && modbus.getLastUpperApproachSensorLevel() == callLogic.getNextLevel())
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
        if(centralLogic.checkReachedTimer() && (centralLogic.getLevelInputs()[1] || centralLogic.getLevelInputs()[9] || centralLogic.getLevelInputs()[17]  || centralLogic.getLevelInputs()[24]))
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


    //spezial funktions
    //herer only used thread for timer
    public void startDoorTimer() {
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