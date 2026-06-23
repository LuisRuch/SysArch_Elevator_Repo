//public class CallLogicClass
//{
//
//
//    // This class will set the nextLevel - therefore the elevator will know where to go
//    // this information will be given to the class Motor_Logic
//
//    private CentralLogicClass.Req_Dir DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
//    private int currentLevel = 1;
//    private int nextLevel = 1;
//    private int maxLevel = 4;
//    private int minLevel = 1;
//    private int difference = 0;
//
//    private boolean[] stops;
//    private CentralLogicClass.Req_Dir[] Req_Dir_Array;
//
//    public CallLogicClass(boolean[] stops, CentralLogicClass.Req_Dir[] Req_Dir_Array, CentralLogicClass logic) {
//        this.stops = stops;
//        this.Req_Dir_Array = Req_Dir_Array;
//    }
//
//    //countDir - true - count to top
//    //searchDir - true - lool to top
//    //samediff - true - same
//    private boolean findNextLevel(int currentLevel, int minLevel, int maxLevel, boolean countDir, boolean searchDir, boolean samediff)
//    {
//        for (int i = currentLevel; searchDir ? i <= maxLevel : i >= minLevel; i += countDir ? 1 : -1)
//        {
//            if (stops[i])
//            {
//                if ((i != maxLevel && i != minLevel)
//                        && (stops[i] || (samediff ?  DirOfTrv == Req_Dir_Array[i] : DirOfTrv != Req_Dir_Array[i]))) {
//                    nextLevel = i;
//                    difference = nextLevel - currentLevel;
//                    return true;
//                }
//            }
//        }
//
//        if(searchDir)
//            if (stops[maxLevel] == true) {
//                nextLevel = maxLevel;
//                difference = nextLevel - currentLevel;
//                return true;
//            }
//        else
//            if (stops[minLevel] == true) {
//                nextLevel = minLevel;
//                difference = nextLevel - currentLevel;
//                return true;
//            }
//        return false;
//    }
//
//
//
//
//    //hier noch restrection, dass wenn in zustand v1 dass dannn nicht mehr ge#ndert werden kann
//    public void UpdateNextLevel()
//    {
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.DontCare)
//            setDirOfTrv(CentralLogicClass.Req_Dir.Up);
//
//        //looking for staff in same direction wanting to go same
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,true))
//                return;
//        }
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,true))
//                return;
//        }
//
//        //looking for staff in same direction wanting to go differnt
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,false))
//                return;
//        }
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,false))
//                return;
//        }
//
//        //switching DirOfTrv
//        if(DirOfTrv == CentralLogicClass.Req_Dir.Down)
//            DirOfTrv = CentralLogicClass.Req_Dir.Up;
//
//        else if(DirOfTrv == CentralLogicClass.Req_Dir.Up)
//            DirOfTrv = CentralLogicClass.Req_Dir.Down;
//
//
//        //looking for staff in different direction wanting to go same(because changed)
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,true))
//                return;
//        }
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,true))
//                return;
//        }
//
//        //looking for staff in different direction wanting to go different
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,false))
//                return;
//        }
//        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
//        {
//            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,false))
//                return;
//        }
//
//        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
//        difference = 0;
//
//
////
////        while(a)
////        if(search(dir of trave up, such nach oben))
////            break;
////        if(a)
////        search(dir of trave up, such nach unten)
////
////
////
////
////        if (DirOfTrv == CentralLogicClass.Req_Dir.Up) {
////            for (int i = currentLevel; i <= maxLevel; i++) {
////
////                if (stops[i]) {
////
////                    if ((i != maxLevel && i != minLevel) && (stops[i]||
////                            (DirOfTrv == Req_Dir_Array[i]))) {
////                        nextLevel = i;
////                        difference = nextLevel - currentLevel;
////                        return;
////                    }
////                }
////            }
////
////            if (stops[maxLevel] == true) {
////                nextLevel = maxLevel;
////                difference = nextLevel - currentLevel;
////                return;
////            }
////        }
////
////        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
////        {
////            for (int i = currentLevel; i >= minLevel; i--) {
////
////                if (stops[i]) {
////
////                    if ((i != maxLevel && i != minLevel) && (stops[i]||
////                            (DirOfTrv == Req_Dir_Array[i]))) {
////
////                        nextLevel = i;
////                        difference = nextLevel - currentLevel;
////                        return;
////                    }
////                }
////            }
////
////            if (stops[minLevel] == true) {
////                nextLevel = minLevel;
////                difference = nextLevel - currentLevel;
////                return;
////            }
////        }
////
////
////        if(DirOfTrv == CentralLogicClass.Req_Dir.Down){
////            DirOfTrv = CentralLogicClass.Req_Dir.Up;
////        }
////        else
////            DirOfTrv = CentralLogicClass.Req_Dir.Down;
////
////        if (DirOfTrv == CentralLogicClass.Req_Dir.Up) {
////            for (int i = currentLevel; i <= maxLevel; i++) {
////
////                if (stops[i]) {
////
////                    if ((i != maxLevel && i != minLevel) && (stops[i]||
////                            (DirOfTrv == Req_Dir_Array[i]))) {
////                        nextLevel = i;
////                        difference = nextLevel - currentLevel;
////                        return;
////                    }
////                }
////            }
////
////            if (stops[maxLevel] == true) {
////                nextLevel = maxLevel;
////                difference = nextLevel - currentLevel;
////                return;
////            }
////        }
////
////        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
////        {
////            for (int i = currentLevel; i >= minLevel; i--) {
////
////                if (stops[i]) {
////
////                    if ((i != maxLevel && i != minLevel) && (stops[i]||
////                            (DirOfTrv == Req_Dir_Array[i]))) {
////
////                        nextLevel = i;
////                        difference = nextLevel - currentLevel;
////                        return;
////                    }
////                }
////            }
////
////            if (stops[minLevel] == true) {
////                nextLevel = minLevel;
////                difference = nextLevel - currentLevel;
////                return;
////            }
////        }
////
////        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
////        difference = 0;
////        return;
//
////        else
////        {
////            for (int i = minLevel; i <= maxLevel; i++) {
////
////                if (stops[i]) {
////                    nextLevel = i;
////
////                    if (i > currentLevel) {
////                        DirOfTrv = CentralLogicClass.Req_Dir.Up;
////                    } else if (i < currentLevel) {
////                        DirOfTrv = CentralLogicClass.Req_Dir.Down;
////                    } else {
////                        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
////                    }
////
////                    difference = nextLevel - currentLevel;
////                    return;
////                }
////            }
////        }
////
////        difference = nextLevel - currentLevel;
//
//    }
//
//    public int getdiffernce()
//    {
//        return difference;
//    }
//
//    public void setDifference(int diff)
//    {
//        difference = diff;
//    }
//
//    public int getCurrentLevel()
//    {
//        return currentLevel;
//    }
//
//    public void setCurrentLevel(int curr)
//    {
//        currentLevel = curr;
//    }
//
//    public int getNextLevel()
//    {
//        return nextLevel;
//    }
//
//    public void setNextLevel(int next)
//    {
//        nextLevel = next;
//    }
//
//    public void setDirOfTrv(CentralLogicClass.Req_Dir DirOfTrv)
//    {
//        this.DirOfTrv = DirOfTrv;
//    }
//
//    public CentralLogicClass.Req_Dir getDirOfTrv()
//    {
//        return DirOfTrv;
//    }
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
    CentralLogicClass logic;

    public CallLogicClass(boolean[] stops,
                          CentralLogicClass.Req_Dir[] Req_Dir_Array,
                          CentralLogicClass logic)
    {
        this.stops = stops;
        this.Req_Dir_Array = Req_Dir_Array;
        this.logic = logic;
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
    private CentralLogicClass.Req_Dir getDownRequest(int level)
    {
        return switch (level)
        {
            case 2 -> Req_Dir_Array[1];
            case 3 -> Req_Dir_Array[3];
            case 4 -> Req_Dir_Array[5];
            default -> null; // Stockwerk 1 hat keinen Down-Taster
        };
    }

    private CentralLogicClass.Req_Dir getUpRequest(int level)
    {
        return switch (level)
        {
            case 1 -> Req_Dir_Array[0];
            case 2 -> Req_Dir_Array[2];
            case 3 -> Req_Dir_Array[4];
            default -> null; // Stockwerk 4 hat keinen Up-Taster
        };
    }



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
            CentralLogicClass.Req_Dir upRequest = getUpRequest(i);
            CentralLogicClass.Req_Dir downRequest = getDownRequest(i);

            boolean hasDontCareRequest =
                    upRequest == CentralLogicClass.Req_Dir.DontCare
                            || downRequest == CentralLogicClass.Req_Dir.DontCare;

            boolean requestMatchesTravelDirection =
                    hasDontCareRequest
                            || (DirOfTrv == CentralLogicClass.Req_Dir.Up
                            && upRequest == CentralLogicClass.Req_Dir.Up)
                            || (DirOfTrv == CentralLogicClass.Req_Dir.Down
                            && downRequest == CentralLogicClass.Req_Dir.Down);

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

    private boolean hasStopAbove()
    {
        for (int i = currentLevel + 1; i <= maxLevel; i++)
        {
            if (stops[i])
                return true;
        }
        return false;
    }

    private boolean hasStopBelow()
    {
        for (int i = currentLevel - 1; i >= minLevel; i--)
        {
            if (stops[i])
                return true;
        }
        return false;
    }

    private int distanceToNextStopAbove()
    {
        for (int i = currentLevel + 1; i <= maxLevel; i++)
        {
            if (stops[i])
                return i - currentLevel;
        }
        return Integer.MAX_VALUE;
    }

    private int distanceToNextStopBelow()
    {
        for (int i = currentLevel - 1; i >= minLevel; i--)
        {
            if (stops[i])
                return currentLevel - i;
        }
        return Integer.MAX_VALUE;
    }

    private void chooseInitialDirection()
    {
        boolean stopAbove = hasStopAbove();
        boolean stopBelow = hasStopBelow();

        if (stopAbove && !stopBelow)
        {
            DirOfTrv = CentralLogicClass.Req_Dir.Up;
        }
        else if (!stopAbove && stopBelow)
        {
            DirOfTrv = CentralLogicClass.Req_Dir.Down;
        }
        else if (stopAbove && stopBelow)
        {
            // If calls exist on both sides, take the closer side first.
            // In a tie, keep the old behaviour and prefer Up.
            if (distanceToNextStopAbove() <= distanceToNextStopBelow())
                DirOfTrv = CentralLogicClass.Req_Dir.Up;
            else
                DirOfTrv = CentralLogicClass.Req_Dir.Down;
        }
    }

    private void switchDirection()
    {
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
            DirOfTrv = CentralLogicClass.Req_Dir.Down;
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;
    }

    public void UpdateNextLevel()
    {
        //new next level
        if(logic.getReachedSensorActive())
        {
            if(logic.getStatusInputs()[1])
                nextLevel=1;
            if(logic.getStatusInputs()[9])
                nextLevel= 2;
            if(logic.getStatusInputs()[17])
                nextLevel= 3;
            if(logic.getStatusInputs()[25])
                nextLevel= 4;
        }
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
//        if (DirOfTrv == CentralLogicClass.Req_Dir.DontCare)
//        {
//            clearDestination();
//            return;
//        }

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

