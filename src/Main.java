import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception
    {
        //creates ModbusClass + start Modbus
        ModbusClass modbus = new ModbusClass();
            modbus.connect();
        Thread.sleep(200);
        modbus.resetSimulation();
        Thread.sleep(200);
        modbus.resetSimulation();
        System.out.println("nach motor");
        Thread.sleep(200);

        //2900 one
        //6400 two
        //diff 3500
        //9900 three
        modbus.startMotorUpV2();
        Thread.sleep(2900);

        modbus.startMotorUpV1();
        Thread.sleep(1500);

        modbus.startCrawl(1);
        Thread.sleep(540);

        modbus.stopMotor();
        Thread.sleep(1000);


        Thread.sleep(4000);

        modbus.startMotorUpV2();
        Thread.sleep(6400);

        modbus.startMotorUpV1();
        Thread.sleep(1500);

        modbus.startCrawl(1);
        Thread.sleep(540);

        modbus.stopMotor();
        Thread.sleep(1000);

        Thread.sleep(4000);



//
//        modbus.startMotorUpV2();
//        Thread.sleep(2900);
//
//        modbus.startMotorUpV1();
//        Thread.sleep(1500);
//
//        modbus.startCrawl(1);
//        Thread.sleep(540);
//
//        modbus.stopMotor();
//        Thread.sleep(1000);
//
//        Thread.sleep(4000);

        //downnnnnnnnnnnnnnnnnnnn
        modbus.startMotorDownV2();
        Thread.sleep(3250);

        modbus.startMotorDownV1();
        Thread.sleep(1600);

        modbus.startCrawl(-1);
        Thread.sleep(540);

        modbus.stopMotor();
        Thread.sleep(4000);



        modbus.startMotorDownV2();
        Thread.sleep(3250);

        modbus.startMotorDownV1();
        Thread.sleep(1600);

        modbus.startCrawl(-1);
        Thread.sleep(540);

        modbus.stopMotor();


        Thread.sleep(4000);

        modbus.startMotorDownV2();
        Thread.sleep(3250);

        modbus.startMotorDownV1();
        Thread.sleep(1600);

        modbus.startCrawl(-1);
        Thread.sleep(540);

        modbus.stopMotor();
        Thread.sleep(1000);


        modbus.readAllInputs();


        //creates CentralLogic Class + passes references
        CentralLogicClass central = new CentralLogicClass(modbus);
            central.start();
    }
}