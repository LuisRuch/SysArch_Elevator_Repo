public class ElevatorSAClass {

    private State currentState = State.STOPPED_CLOSED_DOOR;
    private State lastState = State.STOPPED_CLOSED_DOOR;

    public ElevatorSAClass() {}

    public enum State {
        STOPPED_CLOSED_DOOR,
        STOPPED_OPEN_DOOR,
        V1_UP,
        V2_UP,
        V1_DOWN,
        V2_DOWN,
        CRAWL
    }

    public void handleStateTransitions() {

        switch (currentState) {

            case STOPPED_CLOSED_DOOR -> {
                //Modbus befehl  hier einfügen
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
                //Modbus befehl  hier einfügen
                if (shouldCloseDoor()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
            }

            case V1_UP -> {
                //Modbus befehl  hier einfügen
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
                //Modbus befehl  hier einfügen
                if (isEmergencyStopActive()) {
                    changeState(State.STOPPED_CLOSED_DOOR);
                }
                else if (canMoveFromV2ToV1()) {
                    changeState(State.V1_UP);
                }
            }

            case V1_DOWN -> {
                //Modbus befehl  hier einfügen
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
                //Modbus befehl  hier einfügen
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



    private void changeState(State newState) {

        if (currentState == newState) {
            return;
        }

        lastState = currentState;
        currentState = newState;

        // kann mann auch fpr die anderen machen dann wird nur einemal gesetzt und nicht immer wieder neu -> weniger communication über Modbus
        if(newState == State.CRAWL)
        {
            gehtDoch();
        }
    }




    private void gehtDoch() {
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