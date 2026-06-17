public class OPCUAInputClass {


    private boolean insideLevel1 = false;
    private boolean insideLevel2 = false;
    private boolean insideLevel3 = false;
    private boolean insideLevel4 = false;

    private boolean outsideLevel1Up = false;
    private boolean outsideLevel2Up = false;
    private boolean outsideLevel2Down = false;
    private boolean outsideLevel3Up = false;
    private boolean outsideLevel3Down = false;
    private boolean outsideLevel4Down = false;

    private boolean OpenDoor = false;
    private boolean CloseDoor = false;
    private boolean emergencyStop = false;


    private boolean speedV1Up = false;
    private boolean speedV1Down = false;
    private boolean speedV2Up = false;
    private boolean speedV2Down = false;
    private int specialCrawl = 0;
    private boolean reset = false;




    private boolean[] stops = new boolean[5];                                            //stops[1] = level 1, stops[2] = level 2, stops[3] = level 3, stops[4] = level 4
    private CentralLogic.Req_Dir[] Req_Dir_Array = new CentralLogic.Req_Dir[3];                //[2] = level 2, [3] = level 3
    private CentralLogic central;



    //Constructor
    public OPCUAInputClass(boolean[] stops, CentralLogic.Req_Dir[] Req_Dir_Array, CentralLogic logic ) {

        this.stops = stops;
        this.Req_Dir_Array = Req_Dir_Array;
        this.central = logic;
    }



    public void handleInputs() throws Exception {
        if (insideLevel1) {
            stops[1] = true;
            insideLevel1 = false;
        }

        if (insideLevel2) {
            stops[2] = true;
            insideLevel2 = false;
        }

        if (insideLevel3) {
            stops[3] = true;
            insideLevel3 = false;
        }

        if (insideLevel4) {
            stops[4] = true;
            insideLevel4 = false;
        }

        if (outsideLevel1Up) {
            stops[1] = true;
            Req_Dir_Array[1] = CentralLogic.Req_Dir.Up;
            outsideLevel1Up = false;
        }

        if (outsideLevel2Up) {
            stops[2] = true;
            Req_Dir_Array[2] = CentralLogic.Req_Dir.Up;
            outsideLevel2Up = false;
        }

        if (outsideLevel2Down) {
            stops[2] = true;
            Req_Dir_Array[2] = CentralLogic.Req_Dir.Down;
            outsideLevel2Down = false;
        }

        if (outsideLevel3Up) {
            stops[3] = true;
            Req_Dir_Array[3] = CentralLogic.Req_Dir.Up;
            outsideLevel3Up = false;
        }

        if (outsideLevel3Down) {
            stops[3] = true;
            Req_Dir_Array[3] = CentralLogic.Req_Dir.Down;
            outsideLevel3Down = false;
        }

        if (outsideLevel4Down) {
            stops[4] = true;
            Req_Dir_Array[4] = CentralLogic.Req_Dir.Down;
            outsideLevel4Down = false;
        }
    }


    public void setDoorOpen(boolean value) {
        OpenDoor = value;
    }

    public boolean getDoorOpen() {
        return OpenDoor;
    }

    public void setDoorClose(boolean value) {
        CloseDoor = value;
    }

    public boolean getDoorClose() {
        return CloseDoor;
    }

    public void setEmergencyStop(boolean value) {
        emergencyStop = value;
    }

    public boolean getEmergencyStop() {
        return emergencyStop;
    }

    public void setReset(boolean value) {
        reset = value;
    }

    public boolean getReset() {
        return reset;
    }

    public void setSpeedV1Up(boolean value) {
        speedV1Up = value;
    }

    public boolean getSpeedV1Up() {
        return speedV1Up;
    }

    public void setSpeedV1Down(boolean value) {
        speedV1Down = value;
    }

    public boolean getSpeedV1Down() {
        return speedV1Down;
    }

    public void setSpeedV2Up(boolean value) {
        speedV2Up = value;
    }

    public boolean getSpeedV2Up() {
        return speedV2Up;
    }

    public void setSpeedV2Down(boolean value) {
        speedV2Down = value;
    }

    public boolean getSpeedV2Down() {
        return speedV2Down;
    }

    public void setSpecialCrawl(int value) {
        specialCrawl = value;
    }

    public int getSpecialCrawl() {
        return specialCrawl;
    }

}