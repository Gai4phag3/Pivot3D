package frc.robot.util;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

/**
 * CustomAssets manages multiple components in 3D space, allowing for
 * absolute rotation around a pivot, per-component angle offsets, 
 * and per-axis rotation direction adjustments.
 */
public class CustomAssets {

    private Pose3d[] initialPoses;        // Initial world positions
    private Pose3d[] finalPoses;          // Updated positions after rotation

    private Translation3d[] pivots;       // Rotation center for each component
    private Translation3d[] initialOffsets; // Component offset from pivot at zero position

    private Rotation3d[] angleOffsets;    // Starting angle offsets per component

    private double[] yawMultiplier;       // Per-axis positive direction
    private double[] pitchMultiplier;
    private double[] rollMultiplier;

    // ------------------------------
    // Constructor
    // ------------------------------

    /**
     * Constructs a CustomAssets instance with a specified number of components.
     * Initializes all positions, pivots, offsets, angle offsets, and multipliers.
     *
     * @param numComponents The number of components to manage.
     */
    public CustomAssets(int numComponents) {

        initialPoses    = new Pose3d[numComponents];
        finalPoses      = new Pose3d[numComponents];
        pivots          = new Translation3d[numComponents];
        initialOffsets  = new Translation3d[numComponents];
        angleOffsets    = new Rotation3d[numComponents];

        yawMultiplier   = new double[numComponents];
        pitchMultiplier = new double[numComponents];
        rollMultiplier  = new double[numComponents];

        for (int i = 0; i < numComponents; i++) {
            initialPoses[i]    = new Pose3d();
            finalPoses[i]      = new Pose3d();
            pivots[i]          = new Translation3d();
            initialOffsets[i]  = new Translation3d();
            angleOffsets[i]    = new Rotation3d();

            yawMultiplier[i]   = 1.0;
            pitchMultiplier[i] = 1.0;
            rollMultiplier[i]  = 1.0;
        }
    }

    // ------------------------------
    // Set initial position and pivot
    // ------------------------------

    /**
     * Sets the initial world position of a component and its pivot point.
     * The pivot is the center of rotation for this component.
     *
     * @param index The component index.
     * @param x The X-coordinate in meters.
     * @param y The Y-coordinate in meters.
     * @param z The Z-coordinate in meters.
     * @param pivot The pivot (rotation center) for this component.
     */
    public void setInitialPosition(int index, double x, double y, double z, Translation3d pivot) {
        Pose3d pose = new Pose3d(x, y, z, new Rotation3d());

        initialPoses[index] = pose;
        finalPoses[index]   = pose;

        pivots[index] = pivot;

        // Offset of component relative to pivot
        initialOffsets[index] = pose.getTranslation().minus(pivot);
    }

    // ------------------------------
    // Set starting angle offset (degrees)
    // ------------------------------

    /**
     * Sets a starting rotation offset for a component. This is useful
     * for aligning the CAD zero with the actual mechanism zero.
     *
     * @param index The component index.
     * @param yawDeg Yaw offset in degrees.
     * @param rollDeg Roll offset in degrees.
     * @param pitchDeg Pitch offset in degrees.
     */
    public void setStartingAngleOffset(int index, double yawDeg, double rollDeg, double pitchDeg) {
        angleOffsets[index] = new Rotation3d(
            Units.degreesToRadians(rollDeg),
            Units.degreesToRadians(pitchDeg),
            Units.degreesToRadians(yawDeg)
        );
    }

    // ------------------------------
    // Set per-axis positive direction
    // ------------------------------

    /**
     * Sets the positive rotation direction for yaw for a component.
     *
     * @param index The component index.
     * @param positive True if positive angles correspond to the mechanism's positive rotation, false to invert.
     */
    public void setYawDirection(int index, boolean positive) {
        yawMultiplier[index] = positive ? 1.0 : -1.0;
    }

    /**
     * Sets the positive rotation direction for pitch for a component.
     *
     * @param index The component index.
     * @param positive True if positive angles correspond to the mechanism's positive rotation, false to invert.
     */
    public void setPitchDirection(int index, boolean positive) {
        pitchMultiplier[index] = positive ? 1.0 : -1.0;
    }

    /**
     * Sets the positive rotation direction for roll for a component.
     *
     * @param index The component index.
     * @param positive True if positive angles correspond to the mechanism's positive rotation, false to invert.
     */
    public void setRollDirection(int index, boolean positive) {
        rollMultiplier[index] = positive ? 1.0 : -1.0;
    }

    // ------------------------------
    // Move to absolute angle (degrees)
    // ------------------------------

    /**
     * Rotates a component to an absolute orientation around its pivot.
     * The rotation is applied using degrees and considers per-axis multipliers
     * and starting angle offsets.
     *
     * @param index The component index.
     * @param yaw Desired yaw angle in degrees.
     * @param roll Desired roll angle in degrees.
     * @param pitch Desired pitch angle in degrees.
     */
    public void goToAngle(int index, double yaw, double roll, double pitch) {

        // Apply per-axis direction
        yaw   *= yawMultiplier[index];
        roll  *= rollMultiplier[index];
        pitch *= pitchMultiplier[index];

        // Convert degrees to radians
        Rotation3d absoluteRot = new Rotation3d(
            Units.degreesToRadians(roll),
            Units.degreesToRadians(pitch),
            Units.degreesToRadians(yaw)
        );

        // Add starting angle offset
        Rotation3d finalRot = angleOffsets[index].plus(absoluteRot);

        // Rotate initial offset around pivot
        Translation3d rotatedOffset = initialOffsets[index].rotateBy(finalRot);

        // Compute new world position
        Translation3d newPos = pivots[index].plus(rotatedOffset);

        finalPoses[index] = new Pose3d(newPos, finalRot);
    }

    // ------------------------------
    // Logging
    // ------------------------------

    /**
     * Periodically logs the zeroed and final component poses
     * to the Logger for visualization or debugging.
     */
    public void periodic() {
        Logger.recordOutput("ZeroedComponentPoses", initialPoses);
        Logger.recordOutput("FinalComponentPoses", finalPoses);
    }

    // ------------------------------
    // Optional getters
    // ------------------------------

    /**
     * Gets the final pose (position + orientation) of a component.
     *
     * @param index The component index.
     * @return The final Pose3d of the component.
     */
    public Pose3d getFinalPose(int index) {
        return finalPoses[index];
    }

    /**
     * Gets the initial pose (position + orientation) of a component.
     *
     * @param index The component index.
     * @return The initial Pose3d of the component.
     */
    public Pose3d getInitialPose(int index) {
        return initialPoses[index];
    }
}
