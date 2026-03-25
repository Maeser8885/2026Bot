package frc.robot.subsystems;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ShooterConstants;

/**
 * ShooterSubsystem controls the rear-mounted dual-roller shooter, which is based on the 2026
 * kitbot launcher mechanism. It has two CIM motors on Spark Max controllers (in brushed mode):
 * <ul>
 *   <li><b>Launcher motor</b> — the outer roller that spins up to launch speed</li>
 *   <li><b>Feeder motor</b> — the inner roller that pushes fuel into the spinning launcher</li>
 * </ul>
 *
 * <p>The shooting sequence is:
 * <ol>
 *   <li>Operator holds the right trigger</li>
 *   <li>The launcher motor starts spinning immediately</li>
 *   <li>After ~1 second of spin-up time, the feeder motor automatically engages</li>
 *   <li>Fuel is pushed into the launcher and shot out</li>
 *   <li>Operator releases the trigger, both motors stop</li>
 * </ol>
 *
 * <p>This subsystem uses <b>open-loop control</b> (no encoder feedback) because CIM motors
 * don't have built-in encoders. Motor speed is controlled by setting a voltage level. The
 * rev-up delay is handled with a simple timer, not a speed measurement.
 *
 * <p>Current limits are set to 60A per motor to prevent brownouts — CIM motors draw very
 * high current at stall, which can cause the robot's battery voltage to drop and reset the
 * roboRIO.
 */
public class ShooterSubsystem extends SubsystemBase {

  private final SparkMax launcherMotor;
  private final SparkMax feederMotor;
  private final Timer launcherTimer = new Timer();
  private boolean launcherRunning = false;

  public ShooterSubsystem() {
    launcherMotor = new SparkMax(ShooterConstants.kLauncherMotorId, MotorType.kBrushed);
    feederMotor = new SparkMax(ShooterConstants.kFeederMotorId, MotorType.kBrushed);

    SparkMaxConfig launcherConfig = new SparkMaxConfig();
    launcherConfig
        .smartCurrentLimit(ShooterConstants.kCurrentLimit)
        .idleMode(IdleMode.kCoast)
        .inverted(false); // TODO: Test on robot — invert if launcher spins wrong way

    SparkMaxConfig feederConfig = new SparkMaxConfig();
    feederConfig
        .smartCurrentLimit(ShooterConstants.kCurrentLimit)
        .idleMode(IdleMode.kCoast)
        .inverted(false); // TODO: Test on robot — invert if feeder pushes wrong way

    launcherMotor.configure(launcherConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);
    feederMotor.configure(feederConfig,
        com.revrobotics.ResetMode.kResetSafeParameters,
        com.revrobotics.PersistMode.kPersistParameters);
  }

  /** Start the launcher motor at launch voltage. */
  public void runLauncher() {
    launcherMotor.setVoltage(ShooterConstants.kLauncherVoltage);
    if (!launcherRunning) {
      launcherTimer.reset();
      launcherTimer.start();
      launcherRunning = true;
    }
  }

  /** Start the feeder motor at feed voltage. */
  public void runFeeder() {
    feederMotor.setVoltage(ShooterConstants.kFeederVoltage);
  }

  /** Stop both motors. */
  public void stop() {
    launcherMotor.setVoltage(0);
    feederMotor.setVoltage(0);
    launcherTimer.stop();
    launcherTimer.reset();
    launcherRunning = false;
  }

  /** Returns true if the launcher has been spinning for long enough. */
  public boolean isLauncherReady() {
    return launcherRunning && launcherTimer.hasElapsed(ShooterConstants.kRevUpTimeSeconds);
  }

  /**
   * Returns a command that runs the full rev-up + feed sequence.
   * Hold to shoot, release to stop.
   * - Starts the launcher motor immediately
   * - Once rev-up time has elapsed, starts the feeder
   * - On end (button release or timeout), stops both motors
   */
  public Command revAndShoot() {
    return new Command() {
      {
        addRequirements(ShooterSubsystem.this);
      }

      @Override
      public void initialize() {
        runLauncher();
      }

      @Override
      public void execute() {
        if (isLauncherReady()) {
          runFeeder();
        }
      }

      @Override
      public void end(boolean interrupted) {
        stop();
      }

      @Override
      public boolean isFinished() {
        return false; // runs until interrupted (trigger release or timeout)
      }
    };
  }

  /** Returns a command that immediately stops both shooter motors. */
  public Command stopCommand() {
    return runOnce(this::stop);
  }

  // ===== Test mode helpers =====
  // These bypass the normal rev-up sequence and set raw percent output directly.
  // Only use these in test mode — in normal operation, use revAndShoot() instead.

  /** Set the launcher motor to a raw percent output (-1.0 to 1.0). For test mode only. */
  public void setLauncherPercent(double percent) {
    launcherMotor.set(percent);
  }

  /** Set the feeder motor to a raw percent output (-1.0 to 1.0). For test mode only. */
  public void setFeederPercent(double percent) {
    feederMotor.set(percent);
  }

  @Override
  public void periodic() {
    String state;
    if (!launcherRunning) {
      state = "Idle";
    } else if (!isLauncherReady()) {
      state = "Revving";
    } else {
      state = "Shooting";
    }
    SmartDashboard.putString("Shooter/State", state);
    SmartDashboard.putNumber("Shooter/Launcher Output",
        launcherMotor.getAppliedOutput());
    SmartDashboard.putNumber("Shooter/Feeder Output",
        feederMotor.getAppliedOutput());
  }
}
