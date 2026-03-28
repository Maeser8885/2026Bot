// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import static edu.wpi.first.units.Units.*;

import org.ejml.equation.IntegerSequence.Range;

import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Time;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static class OperatorConstants {
    public static final int kDriverControllerPort = 0;
    public static final int kOperatorControllerPort = 1;
  }

  public static class DriveConstants {
    public static LinearVelocity maxSpeed = FeetPerSecond.of(8);
  }

  public static class ShooterConstants {
    //CAN IDs
    public static final int shooterCANId = 58; //TODO: Change these ports
    public static final int feederCANId = 51;

    //Speeds
    public static final double feederMaxSpeed = 1;
    public static final double shooterMaxSpeed = 1;
    public static final Time shootToFeedDelay = Seconds.of(0.5);
  }

  public static class VisionConstants {
    public static String limelightName = "";

    public static double forwardOffset = 13;
    public static double upwardOffset = 6.75;
    public static double rightOffset = 0;

    public static int[] idFiltersOverride = {1, 2, 3, 4,5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};//TODO ABSOLUTELY FIX THIS!!!!!!!
  }

  public static class IntakeConstants {
    // CAN IDs
    public static final int kArmMotorId = 50;
    public static final int kRollerMotorId = 48;

    // Arm gear ratio (20:1 reduction between NEO and arm pivot)
    public static final double kArmGearRatio = 20.0;

    // Arm positions in degrees (0 = stowed/vertical, positive = deployed)
    public static final double kStowedSetpoint = 0.0;
    public static final double kDeployedSetpoint = -5.4;

    // Soft limits — prevent arm from going past physical range
    public static final float kForwardSoftLimit = 2.0f;
    public static final float kReverseSoftLimit = -7.0f;

    // Arm PID gains — start with P only, add D if it oscillates
    public static final double kArmP = 0.05;
    public static final double kArmI = 0.0;
    public static final double kArmD = 0.0;

    // Arm PID output cap — limits max force for compliance
    public static final double kArmMaxOutput = 0.4;
    public static final double kArmMinOutput = -0.4;

    // Roller speed (percent output, 0.0 to 1.0)
    public static final double kRollerSpeed = 0.4;

    // Current limits
    public static final int kArmCurrentLimit = 40;
    public static final int kRollerCurrentLimit = 35;

    // Position tolerance for isAtPosition check
    public static final double kPositionToleranceDegrees = 3.0;
  }
}
