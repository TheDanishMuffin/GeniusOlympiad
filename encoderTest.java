package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@TeleOp(name = "encoderTest", group = "Test")
public class encoderTest extends LinearOpMode {
    
    static final double COUNTS_PER_INCH = 909.0;
    
    @Override
    public void runOpMode() {
        DcMotor encoderX = hardwareMap.get(DcMotor.class, "encoderX");
        DcMotor encoderY = hardwareMap.get(DcMotor.class, "encoderY");
        IMU imu = hardwareMap.get(IMU.class, "imu");
        
        encoderX.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderY.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        encoderX.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        encoderY.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        
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
        
        while (opModeIsActive()) {
            int xPos = encoderX.getCurrentPosition();
            int yPos = encoderY.getCurrentPosition();
            double xInches = xPos / COUNTS_PER_INCH;
            double yInches = yPos / COUNTS_PER_INCH;
            
            YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
            double heading = angles.getYaw(AngleUnit.DEGREES);
            
            telemetry.addData("X Counts", xPos);
            telemetry.addData("Y Counts", yPos);
            telemetry.addData("X Inches", "%.2f", xInches);
            telemetry.addData("Y Inches", "%.2f", yInches);
            telemetry.addData("Heading (deg)", "%.1f", heading);
            telemetry.update();
        }
    }
}
