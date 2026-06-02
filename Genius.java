package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@TeleOp(name = "Genius", group = "Linear Opmode")
public class Genius extends LinearOpMode {

    static final double COUNTS_PER_INCH = 909.0;

    @Override
    public void runOpMode() {
        // --- 1. Hardware Mapping ---
        // Drive motors
        DcMotor frontLeft  = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor backLeft   = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        DcMotor backRight  = hardwareMap.get(DcMotor.class, "backRight");

        // Mechanism motors
        // DcMotor intake     = hardwareMap.get(DcMotor.class, "intake");
        // DcMotor boot1      = hardwareMap.get(DcMotor.class, "boot1");
        // DcMotor boot2      = hardwareMap.get(DcMotor.class, "boot2");
        // DcMotor rackTester = hardwareMap.get(DcMotor.class, "rackTester");
        // DcMotor fly1 = hardwareMap.get(DcMotor.class, "flywheel");

        // Odometry encoders (wired through motor ports)
        DcMotor encoderX = hardwareMap.get(DcMotor.class, "encoderX");
        DcMotor encoderY = hardwareMap.get(DcMotor.class, "encoderY");

        // IMU
        IMU imu = hardwareMap.get(IMU.class, "imu");

        // --- 2. Motor / Sensor Configuration ---
        // Reverse the left side so "forward" spins all wheels the same way
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        // Reset and free-run the odometry encoders
        encoderX.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderY.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderX.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        encoderY.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Initialize the IMU
        IMU.Parameters parameters = new IMU.Parameters(
            new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
            )
        );
        imu.initialize(parameters);
        imu.resetYaw();

        telemetry.addData("Status", "Initialized - Press Start");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        // --- Toggle state tracking for the rack tester ---
        // boolean rackTesterOn = false;     // Whether the motor should be running
        boolean lastBButtonState = false; // Button state from the previous loop

        while (opModeIsActive()) {
            // --- 3. Read raw joystick inputs ---
            double rawY  = -gamepad1.left_stick_y;  // Forward/Backward
            double rawX  = gamepad1.left_stick_x;   // Strafing Left/Right
            double rawRx = gamepad1.right_stick_x;  // Turning
            boolean iny  = gamepad1.a;

            // --- 4. Rack tester toggle (Gamepad 1 'B' button) ---
            boolean currentBButtonState = gamepad1.b;
            // if (currentBButtonState && !lastBButtonState) {
            //     rackTesterOn = !rackTesterOn; // Flip the state on a fresh press
            // }
            // lastBButtonState = currentBButtonState;

            // rackTester.setPower(rackTesterOn ? -1 : 0);

            // --- 5. Cube the inputs for finer control near center ---
            double y  = Math.pow(rawY, 3);
            double x  = Math.pow(rawX, 3);
            double rx = Math.pow(rawRx, 3);

            // --- 6. Mecanum drive math ---
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1.0);
            double frontLeftPower  = (y + x + rx) / denominator;
            double backLeftPower   = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower  = (y + x - rx) / denominator;

            // --- 7. Send power to the drive motors ---
            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            frontRight.setPower(frontRightPower);
            backRight.setPower(backRightPower);

            // --- 8. Run the mechanism motors ---
            // boot1.setPower(-1);
            // boot2.setPower(1);
            // intake.setPower(1);

            // --- 9. Read odometry + IMU for telemetry ---
            int xPos = encoderX.getCurrentPosition();
            int yPos = encoderY.getCurrentPosition();
            double xInches = xPos / COUNTS_PER_INCH;
            double yInches = yPos / COUNTS_PER_INCH;

            YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
            double heading = angles.getYaw(AngleUnit.DEGREES);

            // --- 10. Telemetry ---
            // telemetry.addData("X Counts", yPos); //swapped x and y here these
            // telemetry.addData("Y Counts", xPos);
            telemetry.addData("X Inches", "%.2f", -yInches);
            telemetry.addData("Y Inches", "%.2f", xInches);
            telemetry.addData("Heading (deg)", "%.1f", heading);
            telemetry.addData("Button A", iny);
            // telemetry.addData("Rack Tester", rackTesterOn ? "ON" : "OFF");
            telemetry.update();
        }
    }
}
