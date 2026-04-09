// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.Optional;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.LimelightHelpers;
import frc.robot.Constants.VisionConstants;

public class VisionSubsystem extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */


  public VisionSubsystem() {
    LimelightHelpers.setCameraPose_RobotSpace(VisionConstants.limelightName, 
    Units.inchesToMeters(VisionConstants.forwardOffset), //Y 13 in
    Units.inchesToMeters(VisionConstants.rightOffset), //X 0 in
    Units.inchesToMeters(VisionConstants.upwardOffset), //Z 6.75 in
    VisionConstants.roll, //Roll
    VisionConstants.pitch, //Pitch
    VisionConstants.yaw  //Yaw
    );

    // LimelightHelpers.SetFiducialIDFiltersOverride(VisionConstants.limelightName, VisionConstants.idFiltersOverride);
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
  }

  public Command visionUpkeep(DriveSubsystem driveSubsystem){
    return run(() -> {driveSubsystem.visionUpdate(getPoseEstimate());});
  }

  public LimelightHelpers.PoseEstimate getPoseEstimate() {
        return LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(VisionConstants.limelightName);
  }

  @Override
  public void simulationPeriodic() {
  }
}
