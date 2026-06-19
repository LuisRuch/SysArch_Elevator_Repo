public class PollingClass {

    CentralLogicClass centralLogic;
    CallLogicClass callLogic;
    OPCUAInputClass opcuaInput;
    ElevatorSAClass elevatorSA;
    ModbusClass modbus;

    private boolean runningModbus = false;
    private boolean runningRest = false;

    public PollingClass(CentralLogicClass centralLogic, CallLogicClass callLogic, OPCUAInputClass opcuaInput,ElevatorSAClass elevatorSA, ModbusClass modbus)
    {
        this.centralLogic = centralLogic;
        this.callLogic = callLogic;
        this.opcuaInput = opcuaInput;
        this.elevatorSA = elevatorSA;
        this.modbus = modbus;
    }


    //start Modbus
    public void startPollingModbus()
    {
        runningModbus = true;
        Thread pollingModbusThread = new Thread(() -> {
            while (runningModbus) {
                try {
                    modbus.readAllInputs();
                    centralLogic.setLevelInputs(modbus.getLevelInputs());
                    centralLogic.setStatusInputs(modbus.getStatusInputs());
                    centralLogic.setSpecialInputs(modbus.getSpecialInputs());
                    modbus.updateLastLowerApproachSensorFromLevelInputs();
                    modbus.updateLastUpperApproachSensorFromLevelInputs();
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.err.println("Modbus reading error: " + e.getMessage());
                }
            }
        });
        //pollingModbusThread.setDaemon(true); // Thread stops if main stops
        pollingModbusThread.start();
    }

    public void startPollingRest()
    {
        runningRest = true;
        Thread pollingRestThread = new Thread(() -> {
            while (runningRest) {
                try {
                    opcuaInput.handleInputs();
                    callLogic.UpdateNextLevel();
                    centralLogic.calcfunctions();
                    elevatorSA.handleStateTransitions();
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.err.println("Rest reading error: " + e.getMessage());
                }
            }
        });
        //pollingModbusThread.setDaemon(true); // Thread stops if main stops
        pollingRestThread.start();
    }
}
