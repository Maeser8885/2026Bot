// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.*;

import java.io.File;
import java.io.IOException;
import java.util.function.DoubleSupplier;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.LimelightHelpers;
import swervelib.SwerveDrive;
import swervelib.SwerveInputStream;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

public class DriveSubsystem extends SubsystemBase {

  SwerveDrive swerveDrive;

  /* Constructor yippee!! */ 
  public DriveSubsystem() {

    RobotConfig config;
    try{
      config = RobotConfig.fromGUISettings();

      AutoBuilder.configure(
            () ->swerveDrive.getPose(), // Robot pose supplier
            (Pose2d pose) -> swerveDrive.resetOdometry(pose), // Method to reset odometry (will be called if your auto has a starting pose)
            () -> swerveDrive.getRobotVelocity(), // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
            (speeds, feedforwards) -> swerveDrive.drive(speeds), // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds. Also optionally outputs individual module feedforwards
            new PPHolonomicDriveController( // PPHolonomicController is the built in path following controller for holonomic drive trains
                    new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants
                    new PIDConstants(5.0, 0.0, 0.0) // Rotation PID constants
            ),
            config, // The robot configuration
            () -> {
              // Boolean supplier that controls when the path will be mirrored for the red alliance
              // This will flip the path being followed to the red side of the field.
              // THE ORIGIN WILL REMAIN ON THE BLUE SIDE

              var alliance = DriverStation.getAlliance();
              if (alliance.isPresent()) {
                return alliance.get() == DriverStation.Alliance.Red;
              }
              return false;
            },
            this // Reference to this subsystem to set requirements
    );
    } catch (Exception e) {
      // Handle exception as needed
      e.printStackTrace();
    }

    





    
    double maxSpeed = Constants.DriveConstants.maxSpeed.in(MetersPerSecond);
    File swerveJsonDirectory = new File(Filesystem.getDeployDirectory(), "swerve");
    try {
      swerveDrive = new SwerveParser(swerveJsonDirectory).createSwerveDrive(maxSpeed);
      
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Issue with parsing swerve Json directory.");
    }

    SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH;
  }

  /**
   * 
   * @return a command that does an action
   */
  public Command exampleMethodCommand() {
    return runOnce(() -> {});
  }

  public void visionUpdate(LimelightHelpers.PoseEstimate poseEstimate) {
    if(poseEstimate.tagCount > 0) {
      swerveDrive.addVisionMeasurement(poseEstimate.pose, poseEstimate.timestampSeconds);
    }
  }
  
  @Override
  public void periodic() {
    swerveDrive.updateOdometry();

    swerveDrive.swerveDrivePoseEstimator.addVisionMeasurement(null, 0);
  }

  public Command zeroYaw(){
    return run(() -> swerveDrive.zeroGyro());
  }

  @Override
  public void simulationPeriodic() {
  }

  public Command setAngleOffset(double degrees) {
    return run( () -> {swerveDrive.setGyroOffset(new Rotation3d(0, 0, degrees));});
  }

  public Command driveAngularVelocity(DoubleSupplier xInput, DoubleSupplier yInput, DoubleSupplier thetaInput) {


    return run( () -> {
      swerveDrive.drive(new Translation2d(yInput.getAsDouble() * swerveDrive.getMaximumChassisVelocity(),
                                          -xInput.getAsDouble() * swerveDrive.getMaximumChassisVelocity()),
                        thetaInput.getAsDouble() * swerveDrive.getMaximumChassisAngularVelocity(), 
                        false,
                        false);
    });

  }

  public Command driveAngularVelocityFO(DoubleSupplier xInput, DoubleSupplier yInput, DoubleSupplier thetaInput){
      return run(() -> {
        boolean isRed = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red;

        double driveX = isRed? -xInput.getAsDouble() : xInput.getAsDouble();
        double driveY = isRed? -yInput.getAsDouble() : yInput.getAsDouble();

      // method that makes the robot drive, inputting x y and angle inputs multiplied by the maximum velocities
      swerveDrive.drive(new Translation2d(driveY * swerveDrive.getMaximumChassisVelocity(),
                                          -driveX * swerveDrive.getMaximumChassisVelocity()),
                        thetaInput.getAsDouble() * swerveDrive.getMaximumChassisAngularVelocity(), 
                        true,
                        false);
    });
  }

  public Command driveDirectAngleFO(DoubleSupplier xMovement, DoubleSupplier yMovement, DoubleSupplier xFacingDir, DoubleSupplier yFacingDir){

    boolean isRed = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red;

        DoubleSupplier driveX = isRed? () -> -xMovement.getAsDouble() : xMovement;
        DoubleSupplier driveY = isRed? () -> -yMovement.getAsDouble() : yMovement;
        DoubleSupplier headingX = isRed? () -> -xFacingDir.getAsDouble() : xFacingDir;
        DoubleSupplier headingY = isRed? () -> -yFacingDir.getAsDouble() : yFacingDir;
        
    //Creating new SweveInputStream
    SwerveInputStream inputStream =  SwerveInputStream.of(swerveDrive, driveX, driveY).
    withControllerHeadingAxis(headingX, headingY)
    .deadband(.1).withControllerRotationAxis(() -> 0).headingWhile(true)
    
    ;
    return run ( () -> {
      // NOTE: CHECK IF CENTER OF ROTATION IS OFF AND PASS IT AS SECOND ARGUMENT IF SO
      swerveDrive.driveFieldOriented(inputStream.get());
    });
  }
}
