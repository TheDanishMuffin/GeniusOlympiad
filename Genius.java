package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name="Genius", group="Linear Opmode")
public class Genius extends LinearOpMode {

    @Override
    public void runOpMode() {
        // 1. Hardware Mapping: Tells the code what your motors are named on the Hubs
        DcMotor frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        DcMotor backRight = hardwareMap.get(DcMotor.class, "backRight");
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        
        DcMotor boot1 = hardwareMap.get(DcMotor.class, "boot1");
        DcMotor boot2 = hardwareMap.get(DcMotor.class, "boot2");
        
        // DcMotor fly1 = hardwareMap.get(DcMotor.class, "flywheel");
        
        DcMotor rackTester = hardwareMap.get(DcMotor.class, "rackTester");

        // Reverse the left side motors so driving "forward" makes all wheels spin the same way
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        
        waitForStart();

        if (isStopRequested()) return;

        // --- NEW VARIABLES FOR TOGGLE LOGIC ---
        boolean rackTesterOn = false; // Tracks if the motor should be running
        boolean intakeOn = false;
        boolean lastBButtonState = false; // Tracks the button's state in the previous loop
        // --------------------------------------

        while (opModeIsActive()) {
            // 2. Get the raw joystick inputs
            double rawY = -gamepad1.left_stick_y; // Forward/Backward
            double rawX = gamepad1.left_stick_x;  // Strafing Left/Right
            double rawRx = gamepad1.right_stick_x; // Turning
            boolean iny = gamepad1.a; 
            
            // --- RACK TESTER TOGGLE LOGIC (Using Gamepad 1 'B' Button) ---
            boolean currentBButtonState = gamepad1.b; // You can change 'b' to 'a', 'x', or 'y' if you prefer
            
            // If the button is pressed down NOW, but wasn't pressed in the last microsecond:
            if (currentBButtonState && !lastBButtonState) {
                rackTesterOn = !rackTesterOn; // Flip the state! (Off becomes On, On becomes Off)
            }
            lastBButtonState = currentBButtonState; // Save the current state for the next loop

            // Apply the power based on our toggle state
            if (rackTesterOn) {
                rackTester.setPower(-1); // Runs when toggled ON
            } else {
                rackTester.setPower(0);  // Stops when toggled OFF
            }
            // -------------------------------------------------------------
            boolean currentBButtonState = gamepad1.x; // You can change 'b' to 'a', 'x', or 'y' if you prefer
            
            // If the button is pressed down NOW, but wasn't pressed in the last microsecond:
            if (currentBButtonState && !lastBButtonState) {
                intakeOn = !intakeOn; // Flip the state! (Off becomes On, On becomes Off)
            }
            lastBButtonState = currentBButtonState; // Save the current state for the next loop

            // Apply the power based on our toggle state
            if (intakeOn) {
                rackTester.setPower(1); // Runs when toggled ON
            } else {
                rackTester.setPower(0);  // Stops when toggled OFF
            }
            // -------------------------------------------------------------

            // 3. Cube the inputs for minute movement control
            double y = Math.pow(rawY, 3);
            double x = Math.pow(rawX, 3);
            double rx = Math.pow(rawRx, 3);

            // 4. The Mecanum Math: Calculate power for each specific wheel
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1.0);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            // 5. Send the power to the motors
            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            frontRight.setPower(frontRightPower);
            backRight.setPower(backRightPower);
            boot1.setPower(-1);
            boot2.setPower(1);
            // intake.setPower(1);
        }
    }
}
