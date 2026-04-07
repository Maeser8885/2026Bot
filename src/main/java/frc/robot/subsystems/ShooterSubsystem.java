// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.Constants.ShooterConstants;

public class ShooterSubsystem extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */
  SparkMax shooterMotor;
  SparkMax feederMotor;


  public ShooterSubsystem() {
    shooterMotor = new SparkMax(ShooterConstants.shooterCANId, MotorType.kBrushless);
    feederMotor = new SparkMax(ShooterConstants.feederCANId, MotorType.kBrushless);
  }

  /**
   * 
   * @return a command that does an action
   */
  public Command exampleMethodCommand() {
    return runOnce(() -> {});
  }

  public Command shootAndFeed(){
    return runOnce(() -> {
      //Start both motors
      shooterMotor.set(ShooterConstants.shooterMaxSpeed);
      feederMotor.set(-ShooterConstants.feederMaxSpeed);
    });
  }

  public Command unfeed() {
    return runOnce(() -> {
      //Start both motors
      shooterMotor.set(ShooterConstants.shooterMaxSpeed);
      feederMotor.set(ShooterConstants.feederMaxSpeed);
    });
  }

  public Command spinUpAndShoot(){
    return new SequentialCommandGroup(
      //Start Shooter motor
      runOnce(() -> shooterMotor.set(ShooterConstants.shooterMaxSpeed)),
      //Wait for Delay time defined in constants
      new WaitCommand(ShooterConstants.shootToFeedDelay),
      //Start Feeder motor
      runOnce(() -> feederMotor.set(-ShooterConstants.feederMaxSpeed))

    );
  }

  public Command stop(){
    return run(() -> {
      getCurrentCommand().cancel();
      shooterMotor.set(0);
      feederMotor.set(0);
    });
  }
  
  @Override
  public void periodic() {
    
  }

  @Override
  public void simulationPeriodic() {
  }
}
