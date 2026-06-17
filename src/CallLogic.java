public class CallLogic {

    // This class will set the nextLevel - therefore the elevator will know where to go
    // this information will be given to the class Motor_Logic

    private CentralLogic.Req_Dir DirOfTrv = CentralLogic.Req_Dir.DontCare;
    private int currentLevel = 0;
    private int nextLevel;
    private int maxLevel = 4;
    private int minLevel = 1;

    private boolean[] stops;
    private CentralLogic.Req_Dir[] Req_Dir_Array;

    public CallLogic(boolean[] stops, CentralLogic.Req_Dir[] Req_Dir_Array, int currentLevel, CentralLogic logic) {
        this.stops = stops;
        this.Req_Dir_Array = Req_Dir_Array;
        this.currentLevel = currentLevel;
    }

    public int ChangeNextLevel() {

        if (DirOfTrv == CentralLogic.Req_Dir.Up) {
            for (int i = currentLevel; i <= maxLevel; i++) {

                if (stops[i]) {

                    if ((i != maxLevel && i != minLevel) &&
                            (DirOfTrv == Req_Dir_Array[i])) {

                        nextLevel = i;
                        return i;
                    } else {
                        nextLevel = i;
                        return i;
                    }
                }
            }
        }

        else if (DirOfTrv == CentralLogic.Req_Dir.Down) {
            for (int i = currentLevel; i >= minLevel; i--) {

                if (stops[i]) {

                    if ((i != maxLevel && i != minLevel) &&
                            (DirOfTrv == Req_Dir_Array[i])) {

                        nextLevel = i;
                        return i;
                    } else {
                        nextLevel = i;
                        return i;
                    }
                }
            }
        }

        else {
            for (int i = minLevel; i <= maxLevel; i++) {

                if (stops[i]) {
                    nextLevel = i;

                    if (i > currentLevel) {
                        DirOfTrv = CentralLogic.Req_Dir.Up;
                    } else if (i < currentLevel) {
                        DirOfTrv = CentralLogic.Req_Dir.Down;
                    } else {
                        DirOfTrv = CentralLogic.Req_Dir.DontCare;
                    }

                    return i;
                }
            }
        }

        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
}