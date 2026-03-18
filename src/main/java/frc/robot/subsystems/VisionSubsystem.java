// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.LimelightHelpers;
import frc.robot.Constants.VisionConstants;

public class VisionSubsystem extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */
  public VisionSubsystem() {
    LimelightHelpers.setCameraPose_RobotSpace(VisionConstants.limelightName, 0, 0, 0, 0, 0, 0);
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

  @Override
  public void simulationPeriodic() {
  }
}
