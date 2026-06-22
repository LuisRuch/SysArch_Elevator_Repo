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

    //"memory"
    private boolean speedV1UpHandled = false;
    private boolean speedV1DownHandled = false;
    private boolean speedV2UpHandled = false;
    private boolean speedV2DownHandled = false;
    private boolean crawlHandled = false;
    private boolean resetHandled = false;

    CentralLogicClass centralLogic;
    ModbusClass modbus;

    //Constructor
    public OPCUAInputClass(CentralLogicClass centralLogic, ModbusClass modbus) {
        this.centralLogic = centralLogic;
        this.modbus = modbus;
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

        //supervisor functions
        if (reset) {
            modbus.resetSimulation();
            reset = false;
        }


        if (speedV1Up && !speedV1UpHandled) {
            modbus.startMotorUpV1();
            speedV1UpHandled = true;
        }

        if (!speedV1Up) {
            speedV1UpHandled = false;
        }


        if (speedV1Down && !speedV1DownHandled) {
            modbus.startMotorDownV1();
            speedV1DownHandled = true;
        }

        if (!speedV1Down) {
            speedV1DownHandled = false;
        }


        if (speedV2Up && !speedV2UpHandled) {
            modbus.startMotorUpV2();
            speedV2UpHandled = true;
        }

        if (!speedV2Up) {
            speedV2UpHandled = false;
        }


        if (speedV2Down && !speedV2DownHandled) {
            modbus.startMotorDownV2();
            speedV2DownHandled = true;
        }

        if (!speedV2Down) {
            speedV2DownHandled = false;
        }


        if (specialCrawl != 0 && !crawlHandled) {
            modbus.startCrawl(specialCrawl);
            crawlHandled = true;
        }

        if (specialCrawl == 0) {
            crawlHandled = false;
        }

    }


    //Getter und Setter
    public void setOutsideLevel4Down(boolean outsideLevel4Down) {
        this.outsideLevel4Down = outsideLevel4Down;
    }

    public boolean getOpenDoor() {
        return OpenDoor;
    }

    public boolean getCloseDoor() {
        return CloseDoor;
    }

    public boolean getEmergencyStop() {
        return emergencyStop;
    }


}