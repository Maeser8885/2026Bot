// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.Autos;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.ExampleSubsystem;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.studica.frc.AHRS;
import com.studica.frc.AHRS.NavXComType;

import edu.wpi.first.wpilibj.AnalogEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...

  //TODO UNCOMMENT THIS
  //DriveSubsystem m_driveSubsystem = new DriveSubsystem();

  AnalogEncoder frEncoder = new AnalogEncoder(2);
  AnalogEncoder flEncoder = new AnalogEncoder(1);
  AnalogEncoder brEncoder = new AnalogEncoder(3);
  AnalogEncoder blEncoder = new AnalogEncoder(0);

  SparkMax frdMotor = new SparkMax(55, MotorType.kBrushless);
  SparkMax fldMotor = new SparkMax(57, MotorType.kBrushless);
  SparkMax brdMotor = new SparkMax(53, MotorType.kBrushless);
  SparkMax bldMotor = new SparkMax(62, MotorType.kBrushless);

  SparkMax frtMotor = new SparkMax(56, MotorType.kBrushless);
  SparkMax fltMotor = new SparkMax(49, MotorType.kBrushless);
  SparkMax brtMotor = new SparkMax(60, MotorType.kBrushless);
  SparkMax bltMotor = new SparkMax(59, MotorType.kBrushless);

  AHRS navX = new AHRS(NavXComType.kMXP_SPI);

  private final ExampleSubsystem m_exampleSubsystem = new ExampleSubsystem();
  private final DriveSubsystem m_driveSubsystem = new DriveSubsystem();

  // Replace with CommandPS4Controller or CommandJoystick if needed
  private final CommandXboxController m_driverController = new CommandXboxController(OperatorConstants.kDriverControllerPort);

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the trigger bindings

    configureBindings();
    m_driveSubsystem.setDefaultCommand(m_driveSubsystem.driveDirectAngleFO(
      () -> m_driverController.getRawAxis(0),//LEFT X
      () -> -m_driverController.getRawAxis(1),//LEFT Y
      () -> m_driverController.getRawAxis(2),//RIGHT X
      () -> -m_driverController.getRawAxis(3)//RIGHT Y
      ));
    
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    // Schedule `ExampleCommand` when `exampleCondition` changes to `true`
    new Trigger(m_exampleSubsystem::exampleCondition)
        .onTrue(new ExampleCommand(m_exampleSubsystem));

    // Schedule `exampleMethodCommand` when the Xbox controller's B button is pressed,
    // cancelling on release.
    //m_driverController.b().whileTrue(m_exampleSubsystem.exampleMethodCommand());
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return Autos.exampleAuto(m_exampleSubsystem);
  }

  public void debugPeriodic(){
    SmartDashboard.putNumber("FREncoder ABSOLUTE", frEncoder.get());
    SmartDashboard.putNumber("FLEncoder ABSOLUTE", flEncoder.get());
    SmartDashboard.putNumber("BREncoder ABSOLUTE", brEncoder.get());
    SmartDashboard.putNumber("BLEncoder ABSOLUTE", blEncoder.get());

    SmartDashboard.putNumber("FRD REL ENCODER", frdMotor.getEncoder().getPosition());
    SmartDashboard.putNumber("FLD REL ENCODER", fldMotor.getEncoder().getPosition());
    SmartDashboard.putNumber("BRD REL ENCODER", brdMotor.getEncoder().getPosition());
    SmartDashboard.putNumber("BLD REL ENCODER", bldMotor.getEncoder().getPosition());

    SmartDashboard.putNumber("FRT REL ENCODER", frtMotor.getEncoder().getPosition());
    SmartDashboard.putNumber("FLT REL ENCODER", fltMotor.getEncoder().getPosition());
    SmartDashboard.putNumber("BRT REL ENCODER", brtMotor.getEncoder().getPosition());
    SmartDashboard.putNumber("BLT REL ENCODER", bltMotor.getEncoder().getPosition());

    SmartDashboard.putNumber("NAVX YAW", navX.getYaw());

  }
}
