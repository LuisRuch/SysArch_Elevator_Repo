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
                if (d1()) {
                    changeState(State.STOPPED_OPEN_DOOR);
                }
                else if (u11()) {
                    changeState(State.V1_UP);
                }
                else if (u12()) {
                    changeState(State.V2_UP);
                }
                else if (d11()) {
                    changeState(State.V1_DOWN);
                }
                else if (d12()) {
                    changeState(State.V2_DOWN);
                }
                else if (c1()) {
                    changeState(State.CRAWL);
                }
            }

            case STOPPED_OPEN_DOOR -> {
                if (d2()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
            }

            case V1_UP -> {
                if (isEmergencyStopActive()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (canMoveFromV1ToV2()) {
                    changeState(State.V2_UP);
                }
                else if (canEnterCrawlFromV1()) {
                    changeState(State.CRAWL);
                }
            }

            case V2_UP -> {
                if (isEmergencyStopActive()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (canMoveFromV2ToV1()) {
                    changeState(State.V1_UP);
                }
            }

            case V1_DOWN -> {
                if (isEmergencyStopActive()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (canMoveFromV1ToV2()) {
                    changeState(State.V2_DOWN);
                }
                else if (canEnterCrawlFromV1()) {
                    changeState(State.CRAWL);
                }
            }

            case V2_DOWN -> {
                if (isEmergencyStopActive()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (canMoveFromV2ToV1()) {
                    changeState(State.V1_DOWN);
                }
            }

            case CRAWL -> {

                if (isEmergencyStopActive() || hasReachedSensor()) {
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
    private boolean d1() {
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

    private boolean u11() {
        //after emergncy stop if he was already in final approach - last stop had to be open door is there was no emergency
        //last state not open door and no emergency stop

        if(lastState == State.V1_UP && !opcuaInput.getEmergencyStop())
            return true;

        else
            return false;
    }

    private boolean u12() {
        //last state not open door and no emergency stop
        //or
        //differnce pos and no ES


        if((lastState == State.V2_UP && !opcuaInput.getEmergencyStop()) || (callLogic.getdiffernce() > 0 && !opcuaInput.getEmergencyStop()))
            return true;

        else
            return false;

    }

    private boolean d11() {

        if(lastState == State.V1_DOWN && !opcuaInput.getEmergencyStop())
            return true;

        else
            return false;
    }

    private boolean d12() {

        if((lastState == State.V2_DOWN && !opcuaInput.getEmergencyStop()) || (callLogic.getdiffernce() < 0 && !opcuaInput.getEmergencyStop()))
            return true;

        else
            return false;

    }

    private boolean c1() {

        if(lastState == State.CRAWL && !opcuaInput.getEmergencyStop())
            return true;

        else
            return false;
    }


    //Conditions from STOPPED_OPEN_DOOR
    private boolean d2() {
        // Close door when:
        // - call exists and 6 seconds waited and no ES
        // OR
        //no calls after 6 sec and doorclose
        //OR
        // - no call exists and 12 seconds waited an no ES

        if ((getDoorTimerLevel() >= 6000 && centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 600 && opcuaInput.getCloseDoor() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 1200 && centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()))
        {
            stopDoorTimer();
            return true;
        }
        else
            return false;
    }


    // ------------------------------------------------------------
    // Conditions from V1_UP / V1_DOWN
    // Same functions are used for both directions
    // ------------------------------------------------------------

    private boolean canMoveFromV1ToV2() {
        // For UP and DOWN:
        // - no emergency stop was active recently
        // - 0.5 seconds after V1 start
        return false;
    }

    private boolean canEnterCrawlFromV1() {
        // Enter crawl when:
        // - lower safety of target level is reached
        return false;
    }

    // ------------------------------------------------------------
    // Conditions from V2_UP / V2_DOWN
    // Same functions are used for both directions
    // ------------------------------------------------------------

    private boolean canMoveFromV2ToV1() {
        // For UP and DOWN:
        // - after approach sensor starts, wait 1 second
        // - then check if approach sensor level equals target level
        return false;
    }


    //spezial funktions
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