public class ElevatorSAClass {

    private State currentState = State.STOPPED_CLOSED_DOOR;
    private State lastState = State.STOPPED_CLOSED_DOOR;


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
                if (shouldOpenDoor()) {
                    changeState(State.STOPPED_OPEN_DOOR);
                }
                else if (canStartV1Up()) {
                    changeState(State.V1_UP);
                }
                else if (canStartV1Down()) {
                    changeState(State.V1_DOWN);
                }
                else if (canReturnToCrawlAfterEmergency()) {
                    changeState(State.CRAWL);
                }
            }

            case STOPPED_OPEN_DOOR -> {
                if (shouldCloseDoor()) {
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



    // ------------------------------------------------------------
    // Conditions from STOPPED_CLOSED_DOOR
    // ------------------------------------------------------------

    private boolean shouldOpenDoor() {
        // Open door when:
        // - no emergency stop and last state was CRAWL
        // OR
        // - no calls and door open signal is active
        return false;
    }

    private boolean canStartV1Up() {
        // Start moving up with V1 when:
        // - position difference exists
        // - no emergency stop is active
        return false;
    }

    private boolean canStartV1Down() {
        // Start moving down with V1 when:
        // - position difference exists
        // - no emergency stop is active
        return false;
    }

    private boolean canReturnToCrawlAfterEmergency() {
        // Enter crawl when:
        // - there was an emergency
        // - previous state was CRAWL
        return false;
    }

    // ------------------------------------------------------------
    // Conditions from STOPPED_OPEN_DOOR
    // ------------------------------------------------------------

    private boolean shouldCloseDoor() {
        // Close door when:
        // - call exists and 6 seconds waited
        // OR
        // - no call exists and 12 seconds waited
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

    // ------------------------------------------------------------
    // General conditions
    // ------------------------------------------------------------

    private boolean isEmergencyStopActive() {
        return false;
    }

    private boolean hasReachedSensor() {
        return false;
    }



    public State getCurrentState() {
        return currentState;
    }

    public State getLastState() {
        return lastState;
    }
}