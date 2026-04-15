package frc.robot.subsystems;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Intake implementation using the <b>Spark Max onboard PID controller</b>.
 *
 * <p>This is the simpler of the two intake implementations. The PID math runs on the Spark Max
 * motor controller itself — you call {@code setSetpoint(angle)} and the Spark Max continuously
 * adjusts motor power to hold that position. The roboRIO just tells it where to go.
 *
 * <p><b>Limitations:</b>
 * <ul>
 *   <li>No gravity compensation — the PID fights gravity with the same gains it uses for
 *       everything else, which can cause the arm to slam down when deploying.</li>
 *   <li>Symmetric output limits only — the Spark Max caps output the same in both directions.
 *       You can't set "firm downward, soft upward" for ball compliance.</li>
 * </ul>
 *
 * <p>If you need gravity feedforward or asymmetric compliance, use {@link IntakeRoboRioPid} instead.
 *
 * @see IntakeSubsystem
 * @see IntakeRoboRioPid
 */
public class IntakeSparkMaxPid extends SubsystemBase implements IntakeSubsystem {

  // Symmetric output cap — Spark Max onboard PID only supports one range for both directions.
  // If the arm slams or bounces, consider switching to IntakeRoboRioPid which can cap
  // each direction independently.
  private static final double kArmMaxOutput = 0.4;
  private static final double kArmMinOutput = -0.4;

  private final SparkMax armMotor;
  private final SparkMax rollerMotor;
  private final RelativeEncoder armEncoder;
  private final SparkClosedLoopController armController;

  public IntakeSparkMaxPid() {
    armMotor = new SparkMax(IntakeConstants.kArmMotorId, MotorType.kBrushless);
    rollerMotor = new SparkMax(IntakeConstants.kRollerMotorId, MotorType.kBrushless);

    // Configure arm motor
    SparkMaxConfig armConfig = new SparkMaxConfig();
    armConfig
        .smartCurrentLimit(IntakeConstants.kArmCurrentLimit)
        .idleMode(IdleMode.kBrake)
        .inverted(false); // TODO: Test on robot — invert if arm deploys wrong direction

    // Encoder conversion: NEO rotations -> arm degrees
    armConfig.encoder
        .positionConversionFactor(360.0 / IntakeConstants.kArmGearRatio);

    // PID for position control with output capped for compliance
    armConfig.closedLoop
        .p(IntakeConstants.kArmP)
        .i(IntakeConstants.kArmI)
        .d(IntakeConstants.kArmD)
        .outputRange(kArmMinOutput, kArmMaxOutput);

    // Software soft limits — hardware-level protection
    armConfig.softLimit
        .forwardSoftLimitEnabled(true)
        .forwardSoftLimit(IntakeConstants.kForwardSoftLimit)
        .reverseSoftLimitEnabled(true)
        .reverseSoftLimit(IntakeConstants.kReverseSoftLimit);

    armMotor.configure(armConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    // Configure roller motor
    SparkMaxConfig rollerConfig = new SparkMaxConfig();
    rollerConfig
        .smartCurrentLimit(IntakeConstants.kRollerCurrentLimit)
        .idleMode(IdleMode.kCoast)
        .inverted(false); // TODO: Test on robot — invert if rollers push fuel outward

    rollerMotor.configure(rollerConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);

    armEncoder = armMotor.getEncoder();
    armController = armMotor.getClosedLoopController();

    // Zero the encoder on startup — arm must be in stowed position at power-on
    armEncoder.setPosition(IntakeConstants.kStowedAngleDegrees);
  }

  @Override
  public void deploy() {
    armController.setSetpoint(IntakeConstants.kDeployedAngleDegrees, ControlType.kPosition);
  }

  @Override
  public void stow() {
    armController.setSetpoint(IntakeConstants.kStowedAngleDegrees, ControlType.kPosition);
  }

  @Override
  public void runRollers() {
    rollerMotor.set(IntakeConstants.kRollerSpeed);
  }

  @Override
  public void stopRollers() {
    rollerMotor.set(0);
  }

  @Override
  public void stop() {
    armMotor.set(0);
    rollerMotor.set(0);
  }

  @Override
  public double getArmAngle() {
    return armEncoder.getPosition();
  }

  @Override
  public boolean isAtPosition(double targetDegrees) {
    return Math.abs(getArmAngle() - targetDegrees) < IntakeConstants.kPositionToleranceDegrees;
  }

  @Override
  public void setRollerPercent(double percent) {
    rollerMotor.set(percent);
  }

  @Override
  public void nudgeArmTarget(double deltaDegrees) {
    double target = getArmAngle() + deltaDegrees;
    target = MathUtil.clamp(target,
        IntakeConstants.kReverseSoftLimit, IntakeConstants.kForwardSoftLimit);
    armController.setSetpoint(target, ControlType.kPosition);
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Intake/Arm Angle", getArmAngle());
    SmartDashboard.putNumber("Intake/Roller Output", rollerMotor.getAppliedOutput());
    SmartDashboard.putBoolean("Intake/Arm Deployed",
        isAtPosition(IntakeConstants.kDeployedAngleDegrees));
    SmartDashboard.putBoolean("Intake/Arm Stowed",
        isAtPosition(IntakeConstants.kStowedAngleDegrees));
    SmartDashboard.putString("Intake/PID Mode", "Spark Max (onboard)");
  }
}
