import java.io.IOException;
import java.net.UnknownHostException;

import de.re.easymodbus.modbusclient.ModbusClient;

public class ModbusClass {

    private ModbusClient client;

    private static final String HOST = "ea-pc111.ei.htwg-konstanz.de";
    private static final int PORT = 508;

    // Output/Coil addresses: ("we write")
    private static final int Output_Simulation_Reset = 0;     // QX0.0
    private static final int Output_motor_down_V2 = 8;        // QX1.0
    private static final int Output_motor_down_V1 = 9;        // QX1.1
    private static final int Output_motor_up_V1 = 10;         // QX1.2
    private static final int Output_motor_up_V2 = 11;         // QX1.3
    private static final int Output_Close_Door = 12;          // QX1.4
    private static final int Output_Open_Door = 13;           // QX1.5
    private static final int Output_Crawl = 1;                // QW1

    // Input addresses: Abstandssensoren
    private static final int Input_l1sl = 1;                  // IX0.1
    private static final int Input_l1r = 2;                   // IX0.2
    private static final int Input_l1su = 3;                  // IX0.3
    private static final int Input_l1au = 4;                  // IX0.4

    private static final int Input_l2al = 8;                  // IX1.0
    private static final int Input_l2sl = 9;                  // IX1.1
    private static final int Input_l2r = 10;                  // IX1.2
    private static final int Input_l2su = 11;                 // IX1.3
    private static final int Input_l2au = 12;                 // IX1.4

    private static final int Input_l3al = 16;                 // IX2.0
    private static final int Input_l3sl = 17;                 // IX2.1
    private static final int Input_l3r = 18;                  // IX2.2
    private static final int Input_l3su = 19;                 // IX2.3
    private static final int Input_l3au = 20;                 // IX2.4

    private static final int Input_l4al = 24;                 // IX3.0
    private static final int Input_l4sl = 25;                 // IX3.1
    private static final int Input_l4r = 26;                  // IX3.2
    private static final int Input_l4su = 27;                 // IX3.3

    // Input addresses: Status-Bools
    private static final int Input_Door_Opened = 80;          // IX10.0
    private static final int Input_Door_Closed = 81;          // IX10.1
    private static final int Input_Motor_Ready = 82;          // IX10.2
    private static final int Input_Motor_On = 83;             // IX10.3
    private static final int Input_Motor_Error = 84;          // IX10.4

    // Input/Register addresses
    private static final int Input_Cycles = 1;                // ID1
    private static final int Input_AufzugID = 4;              // IW4
    private static final int Input_Speed = 6;                 // IW6

    // gespeicherte Inputs
    private boolean[] levelInputs;       // IX0.1 bis IX3.3
    private boolean[] statusInputs;      // IX10.0 bis IX10.4
    private long[] specialInputs;        // [0] cycles, [1] aufzugID, [2] speed

    private int lastLowerApproachSensorLevel = 0;
    private int lastUpperApproachSensorLevel = 0;

    // Modbus config
    public ModbusClass() throws UnknownHostException, IOException {
        client = new ModbusClient(HOST, PORT);
    }

    public void connect() throws Exception {
        client.Connect();
        System.out.println("Modbus verbunden mit " + HOST + ":" + PORT);
    }

//    public void disconnect() throws Exception {
//        client.Disconnect();
//    }


    // Input methods
    public void readLevelInputs() throws Exception {
        // IX0.1 bis IX3.3: Adresse 1 bis 27 => 27 Bits
        this.levelInputs = client.ReadDiscreteInputs(Input_l1sl, 27);
    }

    public void readStatusInputs() throws Exception {
        // IX10.0 bis IX10.4: Adresse 80 bis 84 => 5 Bits
        this.statusInputs = client.ReadDiscreteInputs(Input_Door_Opened, 5);
    }

    public void readSpecialInputs() throws Exception {
        this.specialInputs = new long[3];

        int[] cyclesRegister = client.ReadInputRegisters(Input_Cycles, 2);
        int[] aufzugIDRegister = client.ReadInputRegisters(Input_AufzugID, 1);
        int[] speedRegister = client.ReadInputRegisters(Input_Speed, 1);

        long cycles = ((long) cyclesRegister[0] << 16) | (cyclesRegister[1] & 0xFFFF);
        int aufzugID = aufzugIDRegister[0];
        int speed = (short) speedRegister[0];

        this.specialInputs[0] = cycles;
        this.specialInputs[1] = aufzugID;
        this.specialInputs[2] = speed;
    }

    public void readAllInputs() throws Exception {
        readLevelInputs();
        readStatusInputs();
        readSpecialInputs();
    }


    // Getter methods
    public boolean[] getLevelInputs() {
        return levelInputs;
    }

    public boolean[] getStatusInputs() {
        return statusInputs;
    }

    public long[] getSpecialInputs() {
        return specialInputs;
    }

    //vielleich in central
    //des gleich noch für down
    public void updateLastLowerApproachSensorFromLevelInputs() {
        if (levelInputs == null) {
            return;
        }

        if (levelInputs[7]) {
            lastLowerApproachSensorLevel = 2;
        } else if (levelInputs[15]) {
            lastLowerApproachSensorLevel = 3;
        } else if (levelInputs[23]) {
            lastLowerApproachSensorLevel = 4;
        }
    }

    public int getLastLowerApproachSensorLevel() {
        return lastLowerApproachSensorLevel;
    }

    public void updateLastUpperApproachSensorFromLevelInputs() {
        if (levelInputs == null) {
            return;
        }

        if (levelInputs[3]) {
            lastUpperApproachSensorLevel = 1;
        } else if (levelInputs[11]) {
            lastUpperApproachSensorLevel = 2;
        } else if (levelInputs[19]) {
            lastUpperApproachSensorLevel = 3;
        }
    }

    public int getLastUpperApproachSensorLevel() {
        return lastUpperApproachSensorLevel;
    }

    public void setLastLowerApproachSensorLevel(int level) {
        this.lastLowerApproachSensorLevel = level;
    }

    public void setLastUpperApproachSensorLevel(int level) {
        this.lastUpperApproachSensorLevel = level;
    }



    // Output methods
    public void resetSimulation() throws Exception {
        client.WriteSingleCoil(Output_Simulation_Reset, true);
        Thread.sleep(100);
        client.WriteSingleCoil(Output_Simulation_Reset, false);
        System.out.println("Reset ausgeführt");
    }

    public void startOpenDoor() throws Exception {
        client.WriteSingleCoil(Output_Close_Door, false);
        client.WriteSingleCoil(Output_Open_Door, true);
        System.out.println("Tür öffnen: Motor EIN");
    }

    public void startCloseDoor() throws Exception {
        client.WriteSingleCoil(Output_Open_Door, false);
        client.WriteSingleCoil(Output_Close_Door, true);
        System.out.println("Tür schließen: Motor EIN");
    }

    public void stopDoor() throws Exception {
        client.WriteSingleCoil(Output_Open_Door, false);
        client.WriteSingleCoil(Output_Close_Door, false);
        System.out.println("Türmotor AUS");
    }


    // Motor methods
    public void stopMotor() throws Exception {
        client.WriteSingleCoil(Output_motor_down_V2, false);
        client.WriteSingleCoil(Output_motor_down_V1, false);
        client.WriteSingleCoil(Output_motor_up_V1, false);
        client.WriteSingleCoil(Output_motor_up_V2, false);

        // QW1 ist ein Register, kein Coil
        client.WriteSingleRegister(Output_Crawl, 0);

        System.out.println("Aufzugmotor AUS");
    }

    public void startMotorDownV2() throws Exception {
        stopMotor();
        client.WriteSingleCoil(Output_motor_down_V2, true);
        System.out.println("Motor runter V2 EIN");
    }

    public void startMotorDownV1() throws Exception {
       stopMotor();
        client.WriteSingleCoil(Output_motor_down_V1, true);
        System.out.println("Motor runter V1 EIN");
    }

    public void startMotorUpV1() throws Exception {
        stopMotor();
        client.WriteSingleCoil(Output_motor_up_V1, true);
        System.out.println("Motor hoch V1 EIN");
    }

    public void startMotorUpV2() throws Exception {
        stopMotor();
        client.WriteSingleCoil(Output_motor_up_V2, true);
        System.out.println("Motor hoch V2 EIN");
    }

    public void startCrawl(int speed) throws Exception {
        if (speed < -5 || speed > 5) {
            throw new IllegalArgumentException("Crawl speed muss zwischen -5 und 5 liegen");
        }

        stopMotor();

        // QW1 ist ein Register, kein Coil
        client.WriteSingleRegister(Output_Crawl, speed);

        System.out.println("Crawl-Speed EIN: " + speed);
    }

    public void emergencyStop() throws Exception {
        System.out.println("in emergencystop - modbusclass");
        client.WriteSingleCoil(Output_motor_down_V2, false);
        client.WriteSingleCoil(Output_motor_down_V1, false);
        client.WriteSingleCoil(Output_motor_up_V1, false);
        client.WriteSingleCoil(Output_motor_up_V2, false);
        client.WriteSingleRegister(Output_Crawl, 0);
        client.WriteSingleCoil(Output_Open_Door, false);
        client.WriteSingleCoil(Output_Close_Door, false);
    }
}