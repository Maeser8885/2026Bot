// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.*;

import java.io.File;
import java.io.IOException;
import java.util.function.DoubleSupplier;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import swervelib.SwerveDrive;
import swervelib.SwerveInputStream;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

public class DriveSubsystem extends SubsystemBase {

  SwerveDrive swerveDrive;

  /* Constructor yippee!! */ 
  public DriveSubsystem() {
    
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
  
  @Override
  public void periodic() {
    swerveDrive.updateOdometry();

    swerveDrive.swerveDrivePoseEstimator.addVisionMeasurement(null, 0);
  }

  @Override
  public void simulationPeriodic() {
  }

  public Command driveAngularVelocityFO(DoubleSupplier xInput, DoubleSupplier yInput, DoubleSupplier thetaInput){
      return run(() -> {
      // method that makes the robot drive, inputting x y and angle inputs multiplied by the maximum velocities
      swerveDrive.drive(new Translation2d(xInput.getAsDouble() * swerveDrive.getMaximumChassisVelocity(),
                                          yInput.getAsDouble() * swerveDrive.getMaximumChassisVelocity()),
                        thetaInput.getAsDouble() * swerveDrive.getMaximumChassisAngularVelocity(), 
                        true,
                        false);
    });
  }

  public Command driveDirectAngleFO(DoubleSupplier xMovement, DoubleSupplier yMovement, DoubleSupplier xFacingDir, DoubleSupplier yFacingDir){
    //Creating new SweveInputStream
    SwerveInputStream inputStream =  SwerveInputStream.of(swerveDrive, xMovement, yMovement).
    withControllerHeadingAxis(xFacingDir, yFacingDir)
    .deadband(.1)
    
    ;
    return run ( () -> {
      // NOTE: CHECK IF CENTER OF ROTATION IS OFF AND PASS IT AS SECOND ARGUMENT IF SO
      swerveDrive.driveFieldOriented(inputStream.get());
    });
  }
}
