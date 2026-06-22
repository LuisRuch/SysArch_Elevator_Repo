//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//
//public class ElevatorSAClass {
//
//    private State currentState = State.STOPPED;
//    private State lastState = State.STOPPED;
//    private boolean wasReached = false;
//
//    private ScheduledExecutorService schedulerDoor;
//    private volatile int timerDoorLevel = 0;
//
//    CentralLogicClass centralLogic;
//    OPCUAInputClass opcuaInput;
//    ModbusClass modbus;
//    CallLogicClass callLogic;
//
//    public ElevatorSAClass(CentralLogicClass centralLogic, OPCUAInputClass opcuaInput, ModbusClass modbus, CallLogicClass callLogic)
//    {
//        this.callLogic = callLogic;
//        this.centralLogic = centralLogic;
//        this.opcuaInput = opcuaInput;
//        this.modbus = modbus;
//    }
//
//    public enum State {
//        RESETTING,
//        STOPPED,
//        OPENING_DOOR,
//        CLOSING_DOOR,
//        V1_UP,
//        V2_UP,
//        V1_DOWN,
//        V2_DOWN,
//        CRAWL
//    }
//
//    public void handleStateTransitions()throws Exception
//    {
//
//        if (opcuaInput.getReset() && currentState != State.RESETTING) {
//            changeState(State.RESETTING);
//            return;
//        }
//
//        switch (currentState) {
//
//            case RESETTING -> {
//                if (!opcuaInput.getReset()) {
//                    changeState(State.STOPPED);
//                }
//            }
//
//            case STOPPED -> {
//
//                //has to be before erasing stops / has to be if
//                if (do1() && centralLogic.getStatusInputs()[1]) {
//                    changeState(State.OPENING_DOOR);
//                }
//
//                //has to be if
//                //if in level and reach sensor then rest stop[] at that level
//                if(centralLogic.getStops()[callLogic.getCurrentLevel()] && (centralLogic.getLevelInputs()[1] || centralLogic.getLevelInputs()[9] || centralLogic.getLevelInputs()[17]  || centralLogic.getLevelInputs()[25])) {
//                    centralLogic.setStops(callLogic.getCurrentLevel(), false);
//                    centralLogic.setReq_Dir_Array(callLogic.getCurrentLevel(), null);
//                }
//
//                //here if else can start
//                if(dc1() && centralLogic.getStatusInputs()[0]){
//                    changeState(State.CLOSING_DOOR);
//                }
//                else if (AESU()) {
//                    changeState(State.V1_UP);
//                }
//                else if (U1()&& centralLogic.getStatusInputs()[1]) { //closed door to start moving
//                    changeState(State.V2_UP);
//                }
//                else if (AESD()) {
//                    changeState(State.V1_DOWN);
//                }
//                else if (D1()&& centralLogic.getStatusInputs()[1]) { //closed door to start moving
//                    changeState(State.V2_DOWN);
//                }
//                else if (AESC()) {
//                    changeState(State.CRAWL);
//                }
//
//
//            }
//
//            case OPENING_DOOR -> {
//                if (do2()) {
//                    changeState(State.STOPPED);
//                }
//            }
//
//            case CLOSING_DOOR -> {
//                if (dc2()) {
//                    changeState(State.STOPPED);
//                }
//            }
//
//            case V2_UP -> {
//                if (ES()) {
//                    changeState(State.STOPPED);
//                }
//                else if (U2()) {
//                    changeState(State.V1_UP);
//                }
//            }
//
//            //same transitions
//            case V1_UP, V1_DOWN -> {
//                if (ES()) {
//                    changeState(State.STOPPED);
//                }
//                else if (U3()) {
//                    changeState(State.CRAWL);
//                }
//            }
//
//            case V2_DOWN -> {
//                if (ES()) {
//                    changeState(State.STOPPED);
//                }
//                else if (D2()) {
//                    changeState(State.V1_DOWN);
//                }
//            }
//
//            case CRAWL -> {
//
//                if(!centralLogic.getReachedSensorActive())
//                {
//                    if(!wasReached)
//                    {
//                        if (callLogic.getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
//                            modbus.startCrawl(2);
//                        else
//                            modbus.startCrawl(-2);
//                    }
//                    else
//                    {
//                        if (callLogic.getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
//                            modbus.startCrawl(-1);
//                        else
//                            modbus.startCrawl(1);
//                    }
//                }
//
//                if(centralLogic.getReachedSensorActive())
//                    wasReached = true;
//
//                //Transitions
//                if (ES()) {
//                    changeState(State.STOPPED);
//                }
//                else if (finish()){
//                    wasReached = false;
//                    callLogic.setCurrentLevel(callLogic.getNextLevel());
//                    changeState(State.STOPPED);
//                }
//            }
//        }
//    }
//
//
//
//    private void changeState(State newState) throws Exception
//    {
//
//        if (currentState == newState) {
//            return;
//        }
//
//        lastState = currentState;
//        currentState = newState;
//
//        //on entry of state the follwoing actions will be caried out
//        switch (newState) {
//            case RESETTING:
//                performReset();
//                break;
//
//            case STOPPED:
//                modbus.stopMotor();
//                modbus.stopDoor();
//                break;
//
//            case OPENING_DOOR:
//                modbus.startOpenDoor();
//
//                break;
//
//            case CLOSING_DOOR:
//                modbus.startCloseDoor();
//                break;
//
//            case V1_UP:
//                modbus.startMotorUpV1();
//                break;
//
//            case V2_UP:
//                modbus.startMotorUpV2();
//                break;
//
//            case V1_DOWN:
//                modbus.startMotorDownV1();
//                break;
//
//            case V2_DOWN:
//                modbus.startMotorDownV2();
//                break;
//
//            //no use
//            case CRAWL:
//
//                break;
//        }
//
//
//    }
//
//
//
//    //no use
//    private void crawlapproach() {
//        //hier einmal thread aufrufen. der sich annährt
//    }
//
//
//
//
//    //Conditions from STOPPED_CLOSED_DOOR
//    private boolean do1() {
//        //Automatic open door, if elevator arrived at destination - no Emergency Stop  && last State was crawl
//        //or
//
//        //if had no calls(IDLE) and (new)request in same level as current elevator
//
//        if((!opcuaInput.getEmergencyStop() && lastState == State.CRAWL)  || (centralLogic.getMode() == CentralLogicClass.Mode.IDLE && centralLogic.getStops()[callLogic.getCurrentLevel()] && !opcuaInput.getEmergencyStop()))
//            return true;
//
//        else
//            return false;
//    }
//
//    private boolean AESU() {
//        //after emergncy stop if he was already in final approach - last stop had to be open door is there was no emergency
//        //last state not open door and no emergency stop
//
//        if(lastState == State.V1_UP && !opcuaInput.getEmergencyStop())
//            return true;
//
//        else
//            return false;
//    }
//
//    private boolean U1() {
//        //last state not open door and no emergency stop
//        //or
//        //differnce pos and no ES
//
//
//        if((lastState == State.V2_UP && !opcuaInput.getEmergencyStop()) || (callLogic.getdiffernce() > 0 && !opcuaInput.getEmergencyStop()))
//            return true;
//
//        else
//            return false;
//
//    }
//
//    private boolean AESD() {
//
//        if(lastState == State.V1_DOWN && !opcuaInput.getEmergencyStop())
//            return true;
//
//        else
//            return false;
//    }
//
//    private boolean D1() {
//
//        if((lastState == State.V2_DOWN && !opcuaInput.getEmergencyStop()) || (callLogic.getdiffernce() < 0 && !opcuaInput.getEmergencyStop()))
//            return true;
//
//        else
//            return false;
//
//    }
//
//    private boolean AESC() {
//
//        if(lastState == State.CRAWL && !opcuaInput.getEmergencyStop())
//            return true;
//
//        else
//            return false;
//    }
//
//    //Rest
//    private void performReset() throws Exception {
//
//
//        modbus.stopMotor();
//        modbus.stopDoor();
//        modbus.resetSimulation();
//
//
//        stopDoorTimer();
//        centralLogic.setApproachTimerUp(false);
//        centralLogic.setApproachTimerDOWN(false);
//        timerDoorLevel = 0;
//
//
//        centralLogic.setStops(1,false);
//        centralLogic.setStops(2,false);
//        centralLogic.setStops(3,false);
//        centralLogic.setStops(4,false);
//        centralLogic.setReq_Dir_Array(1, null);
//        centralLogic.setReq_Dir_Array(2, null);
//        centralLogic.setReq_Dir_Array(3, null);
//        centralLogic.setReq_Dir_Array(4, null);
//        centralLogic.setApproachTimerUp(false);
//        centralLogic.setApproachTimerStartUP(0);
//        centralLogic.setApproachTimerDOWN(false);
//        centralLogic.setApproachTimerStartDOWN(0);
//        centralLogic.setReachedTimerRunning(false);
//        centralLogic.setReachedTimerStart(0);
//        centralLogic.setMode(CentralLogicClass.Mode.IDLE);
//        modbus.setLastLowerApproachSensorLevel(0);
//        modbus.setLastUpperApproachSensorLevel(0);
//        callLogic.setCurrentLevel(1);
//        callLogic.setDifference(0);
//        callLogic.setNextLevel(1);
//        callLogic.setDirOfTrv(CentralLogicClass.Req_Dir.DontCare);
//    }
//    //Conditions from STOPPED_OPEN_DOOR
//    private boolean dc1() {
//        // Close door when:
//        // - call exists and 6 seconds waited and no ES
//        // OR
//        //no calls after 6 sec and doorclose
//        //OR
//        // - no call exists and 12 seconds waited and no ES
//
//        if ((getDoorTimerLevel() >= 6 && centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 6 && opcuaInput.getCloseDoor() && !opcuaInput.getEmergencyStop()) || (getDoorTimerLevel() >= 12 && !centralLogic.hasAnyStop() && !opcuaInput.getEmergencyStop()))
//        {
//            stopDoorTimer();
//            return true;
//        }
//        else
//            return false;
//    }
//
//    //v2 up state transitions
//    private boolean U2()
//    {
//        //one sec after approach sensor triggort (0,5m) left
//        //and level approach sensor == level form destination (because differnt destinatioin could be set in that time)
//        if (centralLogic.getApproachTimerUPMillisSeconds() >= 1 && modbus.getLastLowerApproachSensorLevel() == callLogic.getNextLevel())
//        {
//            centralLogic.setApproachTimerUp(false);
//            return true;
//        }
//        else
//            return false;
//    }
//
//    private boolean U3()
//    {
//        //look a dirofTrav
//        //and level approach sensor == level form destination
//        //and
//        //saftey sensor - no matter which one -> physical space of elevator important
//        if(callLogic.getDirOfTrv() == CentralLogicClass.Req_Dir.Up){
//            if((modbus.getLastLowerApproachSensorLevel() == callLogic.getNextLevel()) && centralLogic.getAnySafetyStop())
//                return true;
//        }
//        if(callLogic.getDirOfTrv() == CentralLogicClass.Req_Dir.Down){
//            if((modbus.getLastUpperApproachSensorLevel() == callLogic.getNextLevel()) && centralLogic.getAnySafetyStop())
//                return true;
//        }
//        return false;
//    }
//
//    //v2 down state transitions
//    private boolean D2()
//    {
//        //one sec after approach sensor triggort (0,5m) left
//        //and level approach sensor == level form destination (because differnt destinatioin could be set in that time)
//        if (centralLogic.getApproachTimerDOWNMillisSeconds() >= 1 && modbus.getLastUpperApproachSensorLevel() == callLogic.getNextLevel())
//        {
//            centralLogic.setApproachTimerDOWN(false);
//            return true;
//        }
//        else
//            return false;
//    }
//
//    //crawl
//    private boolean finish()
//    {
//        if(centralLogic.checkReachedTimer() && (centralLogic.getLevelInputs()[1] || centralLogic.getLevelInputs()[9] || centralLogic.getLevelInputs()[17]  || centralLogic.getLevelInputs()[25]))
//            return true;
//        else
//            return false;
//    }
//
//    //ES
//    private boolean ES()
//    {
//        if(opcuaInput.getEmergencyStop())
//            return true;
//        else
//            return false;
//    }
//
//    //doors
//    private boolean do2()
//    {
//        if(centralLogic.getStatusInputs()[0])
//        {
//            startDoorTimer();
//            return true;
//        }
//        else
//            return false;
//    }
//
//    private boolean dc2()
//    {
//        if(centralLogic.getStatusInputs()[1])
//            return true;
//        else
//            return false;
//    }
//
//    //des noch in central machen
//    //spezial funktions
//    //herer only used thread for timer
//    public void startDoorTimer() {
//        stopDoorTimer();
//        timerDoorLevel = 0;
//        schedulerDoor = Executors.newSingleThreadScheduledExecutor();
//
//        schedulerDoor.scheduleAtFixedRate(() -> {
//            timerDoorLevel++;
//        }, 0, 1, TimeUnit.SECONDS);
//    }
//
//    public int getDoorTimerLevel() {
//        return timerDoorLevel;
//    }
//
//    public void stopDoorTimer() {
//        if (schedulerDoor != null) {
//            schedulerDoor.shutdown();
//        }
//    }
//
//    //getter
//    public State getCurrentState() {
//        return currentState;
//    }
//
//}

public class CallLogicClass
{
    // This class sets the nextLevel - therefore the elevator knows where to go.
    // The information is used by the elevator state machine / motor logic.

    private CentralLogicClass.Req_Dir DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
    private int currentLevel = 1;
    private int nextLevel = 1;
    private final int maxLevel = 4;
    private final int minLevel = 1;
    private int difference = 0;

    private final boolean[] stops;
    private final CentralLogicClass.Req_Dir[] Req_Dir_Array;

    public CallLogicClass(boolean[] stops,
                          CentralLogicClass.Req_Dir[] Req_Dir_Array,
                          CentralLogicClass logic)
    {
        this.stops = stops;
        this.Req_Dir_Array = Req_Dir_Array;
    }

    /*
     * searchUp:
     * true  -> search from the current level to the top
     * false -> search from the current level to the bottom
     *
     * sameDirection:
     * true  -> cabin calls / DontCare / hall calls in travel direction
     * false -> hall calls against the current travel direction
     */
    private boolean findNextLevel(boolean searchUp, boolean sameDirection)
    {
        int step = searchUp ? 1 : -1;
        int startLevel = currentLevel;

        /*
         * If a destination is already active, the elevator is normally moving
         * or is committed to a trip. In that case, do not select the old
         * currentLevel again. Otherwise a new call behind the elevator could
         * replace the destination while travelling.
         */
        if (difference != 0)
            startLevel += step;

        for (int i = startLevel; searchUp ? i <= maxLevel : i >= minLevel; i += step)
        {
            if (!stops[i])
                continue;

            CentralLogicClass.Req_Dir requestedDirection = Req_Dir_Array[i];

            // null and DontCare are cabin calls / calls without a direction.
            boolean requestMatchesTravelDirection =
                    requestedDirection == null
                            || requestedDirection == CentralLogicClass.Req_Dir.DontCare
                            || requestedDirection == DirOfTrv;

            if (sameDirection != requestMatchesTravelDirection)
                continue;

            setFoundLevel(i);
            return true;
        }

        return false;
    }

    private void setFoundLevel(int level)
    {
        nextLevel = level;
        difference = nextLevel - currentLevel;

        // DirOfTrv always describes the physical travel direction.
        if (difference > 0)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;
        else if (difference < 0)
            DirOfTrv = CentralLogicClass.Req_Dir.Down;
        else
            DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
    }

    private void clearDestination()
    {
        nextLevel = currentLevel;
        difference = 0;
        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
    }

//    private boolean hasStopAbove()
//    {
//        for (int i = currentLevel + 1; i <= maxLevel; i++)
//        {
//            if (stops[i])
//                return true;
//        }
//        return false;
//    }
//
//    private boolean hasStopBelow()
//    {
//        for (int i = currentLevel - 1; i >= minLevel; i--)
//        {
//            if (stops[i])
//                return true;
//        }
//        return false;
//    }

//    private int distanceToNextStopAbove()
//    {
//        for (int i = currentLevel + 1; i <= maxLevel; i++)
//        {
//            if (stops[i])
//                return i - currentLevel;
//        }
//        return Integer.MAX_VALUE;
//    }
//
//    private int distanceToNextStopBelow()
//    {
//        for (int i = currentLevel - 1; i >= minLevel; i--)
//        {
//            if (stops[i])
//                return currentLevel - i;
//        }
//        return Integer.MAX_VALUE;
//    }
//
//    private void chooseInitialDirection()
//    {
//        boolean stopAbove = hasStopAbove();
//        boolean stopBelow = hasStopBelow();
//
//        if (stopAbove && !stopBelow)
//        {
//            DirOfTrv = CentralLogicClass.Req_Dir.Up;
//        }
//        else if (!stopAbove && stopBelow)
//        {
//            DirOfTrv = CentralLogicClass.Req_Dir.Down;
//        }
//        else if (stopAbove && stopBelow)
//        {
//            // If calls exist on both sides, take the closer side first.
//            // In a tie, keep the old behaviour and prefer Up.
//            if (distanceToNextStopAbove() <= distanceToNextStopBelow())
//                DirOfTrv = CentralLogicClass.Req_Dir.Up;
//            else
//                DirOfTrv = CentralLogicClass.Req_Dir.Down;
//        }
//    }

    private void switchDirection()
    {
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
            DirOfTrv = CentralLogicClass.Req_Dir.Down;
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;
    }

    public void UpdateNextLevel()
    {
        /*
         * A request on the current floor may only be selected when no trip is
         * currently active. During a trip, currentLevel is still the last
         * reached floor and is therefore behind the elevator.
         */
        if (difference == 0 && stops[currentLevel])
        {
            setFoundLevel(currentLevel);
            return;
        }

        if (DirOfTrv == CentralLogicClass.Req_Dir.DontCare)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;
            //chooseInitialDirection();

        // No calls above or below and no usable call on the current floor.
        if (DirOfTrv == CentralLogicClass.Req_Dir.DontCare)
        {
            clearDestination();
            return;
        }

        // 1. Search in the current travel direction for matching calls.
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
        {
            if (findNextLevel(true, true))
                return;
        }
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
        {
            if (findNextLevel(false, true))
                return;
        }

        // 2. Search in the current travel direction for opposite hall calls.
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
        {
            if (findNextLevel(true, false))
                return;
        }
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
        {
            if (findNextLevel(false, false))
                return;
        }

        // 3. Nothing was found ahead. Change the travel direction.
        switchDirection();

        // 4. Search in the new direction for matching calls.
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
        {
            if (findNextLevel(true, true))
                return;
        }
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
        {
            if (findNextLevel(false, true))
                return;
        }

        // 5. Search in the new direction for opposite hall calls.
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
        {
            if (findNextLevel(true, false))
                return;
        }
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
        {
            if (findNextLevel(false, false))
                return;
        }

        clearDestination();
    }

    // Kept for compatibility with your ElevatorSAClass.
    public int getdiffernce()
    {
        return difference;
    }

    // Correctly spelled additional getter.
    public int getDifference()
    {
        return difference;
    }

    public void setDifference(int diff)
    {
        difference = diff;
    }

    public int getCurrentLevel()
    {
        return currentLevel;
    }

    public void setCurrentLevel(int curr)
    {
        currentLevel = curr;
    }

    public int getNextLevel()
    {
        return nextLevel;
    }

    public void setNextLevel(int next)
    {
        nextLevel = next;
    }

    public void setDirOfTrv(CentralLogicClass.Req_Dir DirOfTrv)
    {
        this.DirOfTrv = DirOfTrv;
    }

    public CentralLogicClass.Req_Dir getDirOfTrv()
    {
        return DirOfTrv;
    }
}


