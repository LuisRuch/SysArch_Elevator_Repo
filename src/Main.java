import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception
    {
        //creates ModbusClass + start Modbus
        ModbusClass modbus = new ModbusClass();
            modbus.connect();
            Thread.sleep(200);
            modbus.readAllInputs();


        //creates CentralLogic Class + passes references
        CentralLogicClass central = new CentralLogicClass(modbus);
            central.startPollingModbus();
            central.startPollingRest();
    }
}