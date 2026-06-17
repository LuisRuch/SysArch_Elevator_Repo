public class Main {
    public static void main(String[] args)
    {
        //creates ModbusClass + start Modbus
        ModbusClass modbus = new ModbusClass();
            modbus.connect();
            Thread.sleep(200);
            modbus.readAllInputs();


        //creates CentralLogic Class + passes references
        LogicClass central = new LogicClass(modbus);
            central.startPollingModbus();
            central.startPollingRest();
    }
}