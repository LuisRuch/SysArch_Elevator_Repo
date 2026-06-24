public class OPCUAInputClass {


    private boolean insideLevel1 = false;
    private boolean insideLevel2 = false;
    private boolean insideLevel3 = false;
    private boolean insideLevel4 = true;

    private boolean outsideLevel1Up = false;
    private boolean outsideLevel2Up = false;
    private boolean outsideLevel2Down = true;
    private boolean outsideLevel3Up = false;
    private boolean outsideLevel3Down = true;
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
    private boolean supervisor = false;


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
            centralLogic.setReq_Dir_Array(0, CentralLogicClass.Req_Dir.DontCare);
            insideLevel1 = false;
        }

        if (insideLevel2) {
            centralLogic.setStops(2, true);
            centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.DontCare);
            centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.DontCare);
            insideLevel2 = false;
        }

        if (insideLevel3) {
            centralLogic.setStops(3, true);
            centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.DontCare);
            centralLogic.setReq_Dir_Array(4, CentralLogicClass.Req_Dir.DontCare);
            insideLevel3 = false;
        }

        if (insideLevel4) {
            centralLogic.setStops(4, true);
            centralLogic.setReq_Dir_Array(5, CentralLogicClass.Req_Dir.DontCare);
            insideLevel4 = false;
        }

        if (outsideLevel1Up) {
            if (centralLogic.getReq_Dir_Array()[0] != null) {
                centralLogic.setStops(1, true);
                centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.DontCare);
            }

            outsideLevel1Up = false;
        }

        if (outsideLevel2Up) {
            if (centralLogic.getReq_Dir_Array()[2] != null) {
                centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.DontCare);
                centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.DontCare);
            }
            else{
                centralLogic.setStops(2, true);
                centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.Up);
            }

            outsideLevel2Up = false;
        }

        if (outsideLevel2Down) {
            if (centralLogic.getReq_Dir_Array()[1] != null) {
                centralLogic.setReq_Dir_Array(1, CentralLogicClass.Req_Dir.DontCare);
                centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.DontCare);
            }
            else{
                centralLogic.setStops(2, true);
                centralLogic.setReq_Dir_Array(2, CentralLogicClass.Req_Dir.Down);
            }

            outsideLevel2Down = false;
        }

        if (outsideLevel3Up) {
            if (centralLogic.getReq_Dir_Array()[4] != null) {
                centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.DontCare);
                centralLogic.setReq_Dir_Array(4, CentralLogicClass.Req_Dir.DontCare);
            }
            else{
                centralLogic.setStops(3, true);
                centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.Up);
            }

            outsideLevel3Up = false;
        }

        if (outsideLevel3Down) {
            if (centralLogic.getReq_Dir_Array()[3] != null) {
                centralLogic.setReq_Dir_Array(3, CentralLogicClass.Req_Dir.DontCare);
                centralLogic.setReq_Dir_Array(4, CentralLogicClass.Req_Dir.DontCare);
            }
            else{
                centralLogic.setStops(3, true);
                centralLogic.setReq_Dir_Array(4, CentralLogicClass.Req_Dir.Down);
            }

            outsideLevel3Down = false;
        }

        if (outsideLevel4Down) {
            if (centralLogic.getReq_Dir_Array()[5] != null) {
                centralLogic.setStops(4, true);
                centralLogic.setReq_Dir_Array(5, CentralLogicClass.Req_Dir.DontCare);
            }

            outsideLevel4Down = false;
        }

        if (emergencyStop) {
            modbus.emergencyStop();
        }

        //supervisor functions
        if (speedV1Up) {
            modbus.startMotorUpV1();
        }

        if (speedV1Down) {
            modbus.startMotorDownV1();
        }

        if (speedV2Up) {
            modbus.startMotorUpV2();
        }

        if (speedV2Down) {
            modbus.startMotorDownV2();
        }

        if (specialCrawl != 0) {
            modbus.startCrawl(specialCrawl);
        }

    }


    //Getter und Setter
    public boolean getOpenDoor() {
        return OpenDoor;
    }

    public boolean getCloseDoor() {
        return CloseDoor;
    }

    public boolean getEmergencyStop() {
        return emergencyStop;
    }

    public boolean getReset(){
        return reset;
    }

    public boolean getSupervisor() {
        return supervisor;
    }


}