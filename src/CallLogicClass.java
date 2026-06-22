public class CallLogicClass
{


    // This class will set the nextLevel - therefore the elevator will know where to go
    // this information will be given to the class Motor_Logic

    private CentralLogicClass.Req_Dir DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
    private int currentLevel = 1;
    private int nextLevel = 1;
    private int maxLevel = 4;
    private int minLevel = 1;
    private int difference = 0;

    private boolean[] stops;
    private CentralLogicClass.Req_Dir[] Req_Dir_Array;

    public CallLogicClass(boolean[] stops, CentralLogicClass.Req_Dir[] Req_Dir_Array, CentralLogicClass logic) {
        this.stops = stops;
        this.Req_Dir_Array = Req_Dir_Array;
    }

    //countDir - true - count to top
    //searchDir - true - lool to top
    //samediff - true - same
    private boolean findNextLevel(int currentLevel, int minLevel, int maxLevel, boolean countDir, boolean searchDir, boolean samediff)
    {
        for (int i = currentLevel; searchDir ? i <= maxLevel : i >= minLevel; i += countDir ? 1 : -1)
        {
            if (stops[i])
            {
                if ((i != maxLevel && i != minLevel)
                        && (stops[i] || (samediff ?  DirOfTrv == Req_Dir_Array[i] : DirOfTrv != Req_Dir_Array[i]))) {
                    nextLevel = i;
                    difference = nextLevel - currentLevel;
                    return true;
                }
            }
        }

        if(searchDir)
            if (stops[maxLevel] == true) {
                nextLevel = maxLevel;
                difference = nextLevel - currentLevel;
                return true;
            }
        else
            if (stops[minLevel] == true) {
                nextLevel = minLevel;
                difference = nextLevel - currentLevel;
                return true;
            }
        return false;
    }




    //hier noch restrection, dass wenn in zustand v1 dass dannn nicht mehr ge#ndert werden kann
    public void UpdateNextLevel()
    {
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.DontCare)
            setDirOfTrv(CentralLogicClass.Req_Dir.Up);

        //looking for staff in same direction wanting to go same
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,true))
                return;
        }
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,true))
                return;
        }

        //looking for staff in same direction wanting to go differnt
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,false))
                return;
        }
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,false))
                return;
        }

        //switching DirOfTrv
        if(DirOfTrv == CentralLogicClass.Req_Dir.Down)
            DirOfTrv = CentralLogicClass.Req_Dir.Up;

        else if(DirOfTrv == CentralLogicClass.Req_Dir.Up)
            DirOfTrv = CentralLogicClass.Req_Dir.Down;


        //looking for staff in different direction wanting to go same(because changed)
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,true))
                return;
        }
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,true))
                return;
        }

        //looking for staff in different direction wanting to go different
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Up)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, false,false,false))
                return;
        }
        if(getDirOfTrv() == CentralLogicClass.Req_Dir.Down)
        {
            if(findNextLevel(currentLevel, minLevel, maxLevel, true,true,false))
                return;
        }

        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
        difference = 0;


//
//        while(a)
//        if(search(dir of trave up, such nach oben))
//            break;
//        if(a)
//        search(dir of trave up, such nach unten)
//
//
//
//
//        if (DirOfTrv == CentralLogicClass.Req_Dir.Up) {
//            for (int i = currentLevel; i <= maxLevel; i++) {
//
//                if (stops[i]) {
//
//                    if ((i != maxLevel && i != minLevel) && (stops[i]||
//                            (DirOfTrv == Req_Dir_Array[i]))) {
//                        nextLevel = i;
//                        difference = nextLevel - currentLevel;
//                        return;
//                    }
//                }
//            }
//
//            if (stops[maxLevel] == true) {
//                nextLevel = maxLevel;
//                difference = nextLevel - currentLevel;
//                return;
//            }
//        }
//
//        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
//        {
//            for (int i = currentLevel; i >= minLevel; i--) {
//
//                if (stops[i]) {
//
//                    if ((i != maxLevel && i != minLevel) && (stops[i]||
//                            (DirOfTrv == Req_Dir_Array[i]))) {
//
//                        nextLevel = i;
//                        difference = nextLevel - currentLevel;
//                        return;
//                    }
//                }
//            }
//
//            if (stops[minLevel] == true) {
//                nextLevel = minLevel;
//                difference = nextLevel - currentLevel;
//                return;
//            }
//        }
//
//
//        if(DirOfTrv == CentralLogicClass.Req_Dir.Down){
//            DirOfTrv = CentralLogicClass.Req_Dir.Up;
//        }
//        else
//            DirOfTrv = CentralLogicClass.Req_Dir.Down;
//
//        if (DirOfTrv == CentralLogicClass.Req_Dir.Up) {
//            for (int i = currentLevel; i <= maxLevel; i++) {
//
//                if (stops[i]) {
//
//                    if ((i != maxLevel && i != minLevel) && (stops[i]||
//                            (DirOfTrv == Req_Dir_Array[i]))) {
//                        nextLevel = i;
//                        difference = nextLevel - currentLevel;
//                        return;
//                    }
//                }
//            }
//
//            if (stops[maxLevel] == true) {
//                nextLevel = maxLevel;
//                difference = nextLevel - currentLevel;
//                return;
//            }
//        }
//
//        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down)
//        {
//            for (int i = currentLevel; i >= minLevel; i--) {
//
//                if (stops[i]) {
//
//                    if ((i != maxLevel && i != minLevel) && (stops[i]||
//                            (DirOfTrv == Req_Dir_Array[i]))) {
//
//                        nextLevel = i;
//                        difference = nextLevel - currentLevel;
//                        return;
//                    }
//                }
//            }
//
//            if (stops[minLevel] == true) {
//                nextLevel = minLevel;
//                difference = nextLevel - currentLevel;
//                return;
//            }
//        }
//
//        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
//        difference = 0;
//        return;

//        else
//        {
//            for (int i = minLevel; i <= maxLevel; i++) {
//
//                if (stops[i]) {
//                    nextLevel = i;
//
//                    if (i > currentLevel) {
//                        DirOfTrv = CentralLogicClass.Req_Dir.Up;
//                    } else if (i < currentLevel) {
//                        DirOfTrv = CentralLogicClass.Req_Dir.Down;
//                    } else {
//                        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
//                    }
//
//                    difference = nextLevel - currentLevel;
//                    return;
//                }
//            }
//        }
//
//        difference = nextLevel - currentLevel;

    }

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
