package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import java.util.Map;
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
        DcMotor fly1 = hardwareMap.get(DcMotor.class, "flywheel");

        // Reverse the left side motors so driving "forward" makes all wheels spin the same way
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            // 2. Get the raw joystick inputs
            // Note: pushing the stick forward gives a negative Y value, so we reverse it with a minus sign
            double rawY = -gamepad1.left_stick_y; // Forward/Backward
            double rawX = gamepad1.left_stick_x;  // Strafing Left/Right
            double rawRx = gamepad1.right_stick_x; // Turning
            boolean iny = gamepad1.a; 
            


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

            // 5. Send the power to the Yellow Jacket motors
            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            frontRight.setPower(frontRightPower);
            backRight.setPower(backRightPower);

            //adjust speeds here
            intake.setPower(1);
            fly1.setPower(-0.2);
        }
    }
}
