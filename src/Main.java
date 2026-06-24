public class Main {

    public static void main(String[] args) throws Exception
    {
        //creates ModbusClass + start Modbus
        ModbusClass modbus = new ModbusClass();
            modbus.connect();
        Thread.sleep(200);

        //pre-Program Modbus-Reset
        modbus.resetSimulation();
        Thread.sleep(200);
        modbus.resetSimulation();
        Thread.sleep(200);
        modbus.stopMotor();
        Thread.sleep(200);
        modbus.stopMotor();
        Thread.sleep(200);
        modbus.stopDoor();
        Thread.sleep(200);
        modbus.stopDoor();
        Thread.sleep(200);



        //creates CentralLogic Class + passes references
        CentralLogicClass central = new CentralLogicClass(modbus);
            central.startPolling();
    }
}