public class CallLogicClass {


    // This class will set the nextLevel - therefore the elevator will know where to go
    // this information will be given to the class Motor_Logic

    private CentralLogicClass.Req_Dir DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
    private int currentLevel = 0;
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

    //hier noch restrection, dass wenn in zustand v1 dass dannn nicht mehr ge#ndert werden kann
    public void UpdateNextLevel() {

        if (DirOfTrv == CentralLogicClass.Req_Dir.Up) {
            for (int i = currentLevel; i <= maxLevel; i++) {

                if (stops[i]) {

                    if ((i != maxLevel && i != minLevel) &&
                            (DirOfTrv == Req_Dir_Array[i])) {

                        nextLevel = i;
                        difference = Math.abs(nextLevel - currentLevel);
                        return;
                    } else {
                        nextLevel = i;
                        difference = Math.abs(nextLevel - currentLevel);
                        return;
                    }
                }
            }
        }

        else if (DirOfTrv == CentralLogicClass.Req_Dir.Down) {
            for (int i = currentLevel; i >= minLevel; i--) {

                if (stops[i]) {

                    if ((i != maxLevel && i != minLevel) &&
                            (DirOfTrv == Req_Dir_Array[i])) {

                        nextLevel = i;
                        difference = Math.abs(nextLevel - currentLevel);
                        return;
                    } else {
                        nextLevel = i;
                        difference = Math.abs(nextLevel - currentLevel);
                        return;
                    }
                }
            }
        }

        else {
            for (int i = minLevel; i <= maxLevel; i++) {

                if (stops[i]) {
                    nextLevel = i;

                    if (i > currentLevel) {
                        DirOfTrv = CentralLogicClass.Req_Dir.Up;
                    } else if (i < currentLevel) {
                        DirOfTrv = CentralLogicClass.Req_Dir.Down;
                    } else {
                        DirOfTrv = CentralLogicClass.Req_Dir.DontCare;
                    }

                    difference = Math.abs(nextLevel - currentLevel);
                    return;
                }
            }
        }
        difference = Math.abs(nextLevel - currentLevel);
    }

    public int getdiffernce()
    {
        return difference;
    }

    public int getCurrentLevel()
    {
        return currentLevel;
    }

    public int getNextLevel()
    {
        return nextLevel;
    }
}
