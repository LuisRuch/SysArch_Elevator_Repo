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


    CentralLogicClass centralLogic;

    //Constructor
    public OPCUAInputClass(CentralLogicClass centralLogic) {
        this.centralLogic = centralLogic;
    }



    public void handleInputs() throws Exception {
        if (insideLevel1) {
            centralLogic.setStops(1, true);
            insideLevel1 = false;
        }

        if (insideLevel2) {
            centralLogic.setStops(2, true);
            insideLevel2 = false;
        }

        if (insideLevel3) {
            centralLogic.setStops(3, true);
            insideLevel3 = false;
        }

        if (insideLevel4) {
            centralLogic.setStops(4, true);
            insideLevel4 = false;
        }

        if (outsideLevel1Up) {
            centralLogic.setStops(1, true);
            centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.Up);
            outsideLevel1Up = false;
        }

        if (outsideLevel2Up) {
            centralLogic.setStops(2, true);
            centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.Up);
            outsideLevel2Up = false;
        }

        if (outsideLevel2Down) {
            centralLogic.setStops(2, true);
            centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.Down);
            outsideLevel2Down = false;
        }

        if (outsideLevel3Up) {
            centralLogic.setStops(3, true);
            centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.Up);
            outsideLevel3Up = false;
        }

        if (outsideLevel3Down) {
            centralLogic.setStops(3, true);
            centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.Down);
            outsideLevel3Down = false;
        }

        if (outsideLevel4Down) {
            centralLogic.setStops(4, true);
            centralLogic.setReq_Dir_Array(4, CentralLogicClass.Req_Dir.Down);
            outsideLevel4Down = false;
        }
    }


    //Getter und Setter
    public boolean getInsideLevel1() {
        return insideLevel1;
    }

    public void setInsideLevel1(boolean insideLevel1) {
        this.insideLevel1 = insideLevel1;
    }

    public boolean getInsideLevel2() {
        return insideLevel2;
    }

    public void setInsideLevel2(boolean insideLevel2) {
        this.insideLevel2 = insideLevel2;
    }

    public boolean getInsideLevel3() {
        return insideLevel3;
    }

    public void setInsideLevel3(boolean insideLevel3) {
        this.insideLevel3 = insideLevel3;
    }

    public boolean getInsideLevel4() {
        return insideLevel4;
    }

    public void setInsideLevel4(boolean insideLevel4) {
        this.insideLevel4 = insideLevel4;
    }

    public boolean getOutsideLevel1Up() {
        return outsideLevel1Up;
    }

    public void setOutsideLevel1Up(boolean outsideLevel1Up) {
        this.outsideLevel1Up = outsideLevel1Up;
    }

    public boolean getOutsideLevel2Up() {
        return outsideLevel2Up;
    }

    public void setOutsideLevel2Up(boolean outsideLevel2Up) {
        this.outsideLevel2Up = outsideLevel2Up;
    }

    public boolean getOutsideLevel2Down() {
        return outsideLevel2Down;
    }

    public void setOutsideLevel2Down(boolean outsideLevel2Down) {
        this.outsideLevel2Down = outsideLevel2Down;
    }

    public boolean getOutsideLevel3Up() {
        return outsideLevel3Up;
    }

    public void setOutsideLevel3Up(boolean outsideLevel3Up) {
        this.outsideLevel3Up = outsideLevel3Up;
    }

    public boolean getOutsideLevel3Down() {
        return outsideLevel3Down;
    }

    public void setOutsideLevel3Down(boolean outsideLevel3Down) {
        this.outsideLevel3Down = outsideLevel3Down;
    }

    public boolean getOutsideLevel4Down() {
        return outsideLevel4Down;
    }

    public void setOutsideLevel4Down(boolean outsideLevel4Down) {
        this.outsideLevel4Down = outsideLevel4Down;
    }

    public boolean getOpenDoor() {
        return OpenDoor;
    }

    public void setOpenDoor(boolean openDoor) {
        OpenDoor = openDoor;
    }

    public boolean getCloseDoor() {
        return CloseDoor;
    }

    public void setCloseDoor(boolean closeDoor) {
        CloseDoor = closeDoor;
    }

    public boolean getEmergencyStop() {
        return emergencyStop;
    }

    public void setEmergencyStop(boolean emergencyStop) {
        this.emergencyStop = emergencyStop;
    }

}