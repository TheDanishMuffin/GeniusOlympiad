package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "Genius", group = "Linear Opmode")
public class Genius extends LinearOpMode {

    static final double COUNTS_PER_INCH = 909.0;

    // --- Intake speed presets ---
    static final double BALL_SPEED = 0.45;
    static final double CUBE_SPEED = 0.21;

    // --- Catapult firing parameters ---
    static final double FIRE_KICK_POWER = 1.0;    // Motor power during fire kick (negative if wrong direction)
    static final long   FIRE_KICK_TIME_MS = 200;  // Duration of motor kick during fire (milliseconds)
    static final double WIND_POWER = -1;          // Power used to cock the arm back

    @Override
    public void runOpMode() {
        // --- 1. Hardware Mapping ---
        DcMotor frontLeft  = hardwareMap.get(DcMotor.class, "frontLeft");
        DcMotor backLeft   = hardwareMap.get(DcMotor.class, "backLeft");
        DcMotor frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        DcMotor backRight  = hardwareMap.get(DcMotor.class, "backRight");

        DcMotor intake     = hardwareMap.get(DcMotor.class, "intake");
        Servo kicker1      = hardwareMap.get(Servo.class, "kicker1");
        Servo kicker2      = hardwareMap.get(Servo.class, "kicker2");
        DcMotor roller1    = hardwareMap.get(DcMotor.class, "roller1");
        DcMotor roller2    = hardwareMap.get(DcMotor.class, "roller2");
        DcMotor catapult   = hardwareMap.get(DcMotor.class, "catapult");

        IMU imu = hardwareMap.get(IMU.class, "imu");

        // --- 2. Configuration ---
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        kicker1.setPosition(0.5);
        kicker2.setPosition(0.5);

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

        // --- State tracking ---
        boolean kicker1Active = false;
        boolean kicker2Active = false;
        boolean lastAButtonState = false;
        boolean lastBButtonState = false;
        boolean lastLeftBumperState = false;

        // Catapult fire state
        boolean firing = false;
        long fireStartTime = 0;

        double intakeSpeed = BALL_SPEED;

        while (opModeIsActive()) {
            // --- 3. Read inputs ---
            double rawY  = -gamepad1.left_stick_y;
            double rawX  = gamepad1.left_stick_x;
            double rawRx = gamepad1.right_stick_x;

            double y  = Math.pow(rawY, 3);
            double x  = Math.pow(rawX, 3);
            double rx = Math.pow(rawRx, 3);

            // --- 4. Mecanum drive ---
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1.0);
            double frontLeftPower  = (y + x + rx) / denominator;
            double backLeftPower   = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower  = (y + x - rx) / denominator;

            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            frontRight.setPower(frontRightPower);
            backRight.setPower(backRightPower);

            // --- 5. Roller motors (always on) ---
            roller1.setPower(.8);
            roller2.setPower(.8);

            // --- 6. Intake speed selection ---
            if (gamepad1.y) {
                intakeSpeed = BALL_SPEED;
            }
            if (gamepad1.x) {
                intakeSpeed = CUBE_SPEED;
            }
            intake.setPower(intakeSpeed);

            // --- 7. Kicker 1 toggle (A button) ---
            boolean currentAButtonState = gamepad1.a;
            if (currentAButtonState && !lastAButtonState) {
                kicker1Active = !kicker1Active;
            }
            lastAButtonState = currentAButtonState;

            kicker1.setPosition(kicker1Active ? 0.0 : 0.5);

            // --- 8. Kicker 2 toggle (B button) ---
            boolean currentBButtonState = gamepad1.b;
            if (currentBButtonState && !lastBButtonState) {
                kicker2Active = !kicker2Active;
            }
            lastBButtonState = currentBButtonState;

            kicker2.setPosition(kicker2Active ? 0.0 : 0.5);

            // --- 9. Catapult control ---
            // RIGHT BUMPER (hold): wind catapult back
            // LEFT BUMPER (press): fire — motor kick + float
            
            boolean currentLeftBumperState = gamepad1.left_bumper;
            boolean fireTriggered = currentLeftBumperState && !lastLeftBumperState;
            lastLeftBumperState = currentLeftBumperState;

            if (fireTriggered && !firing) {
                // Start firing sequence
                firing = true;
                fireStartTime = System.currentTimeMillis();
                catapult.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            }

            if (firing) {
                // We're in the middle of a fire sequence
                long elapsed = System.currentTimeMillis() - fireStartTime;
                if (elapsed < FIRE_KICK_TIME_MS) {
                    // Motor kick phase: drive forward to add force
                    catapult.setPower(FIRE_KICK_POWER);
                } else {
                    // Kick phase done: float for rest of swing
                    catapult.setPower(0);
                    firing = false;
                }
            } else if (gamepad1.right_bumper) {
                // Winding: cock the arm back
                catapult.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                catapult.setPower(-WIND_POWER);  // negative = wind direction (swap sign if wrong)
            } else {
                // Idle: brake holds position
                catapult.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                catapult.setPower(0);
            }

            // --- 10. Telemetry ---
            YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
            double heading = angles.getYaw(AngleUnit.DEGREES);

            telemetry.addData("Heading (deg)", "%.1f", heading);
            telemetry.addData("Intake Speed", "%.2f", intakeSpeed);
            telemetry.addData("Intake Mode", intakeSpeed == BALL_SPEED ? "BALLS" : "CUBES");
            telemetry.addData("Kicker1 (A)", kicker1Active ? "OUT" : "REST");
            telemetry.addData("Kicker2 (B)", kicker2Active ? "OUT" : "REST");
            
            String catState;
            if (firing) catState = "FIRING (kick)";
            else if (gamepad1.right_bumper) catState = "WINDING";
            else catState = "IDLE";
            telemetry.addData("Catapult", catState);
            telemetry.update();
        }
    }
}
