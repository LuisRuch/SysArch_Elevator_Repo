public class CallLogicClass
{
    private CentralLogicClass.Req_Dir DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
    private int currentLevel = 1;
    private int nextLevel = 1;
    private final int maxLevel = 4;
    private final int minLevel = 1;
    private int difference = 0;

    private final boolean[] stops;
    private final CentralLogicClass.Req_Dir[] Req_Dir_Array;
    CentralLogicClass logic;

    public CallLogicClass(boolean[] stops, CentralLogicClass.Req_Dir[] Req_Dir_Array, CentralLogicClass logic)
    {
        this.stops = stops;
        this.Req_Dir_Array = Req_Dir_Array;
        this.logic = logic;
    }


    private CentralLogicClass.Req_Dir getDownRequest(int level)
    {
        return switch (level)
        {
            case 2 -> Req_Dir_Array[2];
            case 3 -> Req_Dir_Array[4];
            case 4 -> Req_Dir_Array[5];
            default -> null; // Stockwerk 1 hat keinen Down-Taster
        };
    }

    private CentralLogicClass.Req_Dir getUpRequest(int level)
    {
        return switch (level)
        {
            case 1 -> Req_Dir_Array[0];
            case 2 -> Req_Dir_Array[1];
            case 3 -> Req_Dir_Array[3];
            default -> null; // Stockwerk 4 hat keinen Up-Taster
        };
    }

    //"body" to find the next level
    private boolean findNextLevel(boolean searchUp, boolean sameDirection)
    {
        int step = searchUp ? 1 : -1;
        int startLevel = currentLevel;

        if (difference != 0)
        {
            startLevel += step;

            ElevatorSAClass.State state = logic.elevatorSA.getCurrentState();

            boolean searchingInCurrentV2Direction =
                    (state == ElevatorSAClass.State.V2_UP && searchUp)
                            || (state == ElevatorSAClass.State.V2_DOWN && !searchUp);

            if (searchingInCurrentV2Direction)
            {
                int nrOfLvlTrv = logic.elevatorSA.getNrOfLvlTrv();
                double gesamtstrecke = logic.elevatorSA.getGesamtstrecke();

                if (nrOfLvlTrv > 1 && gesamtstrecke >= 200.0)
                {
                    int nichtMehrErreichbareLevel =
                            1 + (int) ((gesamtstrecke - 200.0) / 350.0);

                    nichtMehrErreichbareLevel =
                            Math.min(nichtMehrErreichbareLevel, nrOfLvlTrv - 1);

                    startLevel += nichtMehrErreichbareLevel * step;
                }
            }
        }

        for (int i = startLevel;
             searchUp ? i <= maxLevel : i >= minLevel;
             i += step)
        {
            if (!stops[i])
                continue;

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


    private void switchDirection()
    {
        if (DirOfTrv == CentralLogicClass.Req_Dir.Up)
            DirOfTrv = CentralLogicClass.Req_Dir.Down;
        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;
    }


    //"heart" - process to find fitting nextLevel
    public void UpdateNextLevel()
    {


        if(logic.getReachedSensorActive())
        {
            if(logic.getLevelInputs()[1])
                nextLevel=1;
            else if(logic.getLevelInputs()[9])
                nextLevel= 2;
            else if(logic.getLevelInputs()[17])
                nextLevel= 3;
            else if(logic.getLevelInputs()[25])
                nextLevel= 4;
        }


        if (difference == 0 && stops[currentLevel])
        {
            setFoundLevel(currentLevel);
            return;
        }

        if (DirOfTrv == CentralLogicClass.Req_Dir.DontCare)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;



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


    //Setter and Getter
    public int getdiffernce()
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

