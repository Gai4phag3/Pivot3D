# 🤖 Pivot3D

**Pivot3D** is a **simple 3D component management API** designed for articulated mechanisms like robotic arms and joints. It provides a clean way to manage the **absolute rotation** of robot parts around defined **pivot points**, incorporates angle offsets for **calibration**, and ensures your simulation accurately aligns with the behavior of your real-world mechanism.

---

## ✨ Features

* **Absolute Rotation:** Rotate components precisely around their defined pivot points.
* **Angle Offsets:** Apply **per-component starting angle offsets** to align the simulation's zero position with your physical mechanism's zero.
* **Directional Control:** Fine-tune rotation by setting **per-axis positive/negative rotation control** (yaw, pitch, roll).
* **WPILib Compatibility:** Seamlessly integrates with WPILib's `Pose3d`, `Rotation3d`, and `Translation3d` objects.
* **Logging:** Tracks both **initial and final positions** for easy logging and simulation analysis.

---

## 🚀 Getting Started

Here's how to integrate and use **Pivot3D** in your robot code.

### 1. Initialize the System

Create a `Pivot3D` instance, specifying the total number of components (joints/parts).

```java
// For 2 components (e.g., a 2-joint arm)
Pivot3D components = new Pivot3D(2);
```
2. Set Initial Positions and Pivots
Define the component's initial world position and the pivot point it rotates around.

```java
// Component 0: Starting at (0.5, 0.0, 0.2) rotating around (0.0, 0.0, 0.0)
components.setInitialPosition(0, 0.5, 0.0, 0.2, new Translation3d(0.0, 0.0, 0.0));

// Component 1: Starting at (1.0, 0.0, 0.2) rotating around (0.5, 0.0, 0.0)
components.setInitialPosition(1, 1.0, 0.0, 0.2, new Translation3d(0.5, 0.0, 0.0));
```
3. Set Starting Angle Offsets (Calibration Step)
This step aligns the simulation's zero angles with the physical mechanism's actual zero position.

```java
// Component 0: Yaw=0°, Roll=0°, Pitch=15° offset
components.setStartingAngleOffset(0, 0, 0, 15);

// Component 1: Yaw=-10°, Roll=0°, Pitch=0° offset
components.setStartingAngleOffset(1, -10, 0, 0);
```
4. Set Per-Axis Rotation Directions
Use this to flip an axis's direction if your physical sensor or motor rotates oppositely to what is desired in the simulation.

```java
// Component 0
components.setYawDirection(0, true);  // Positive yaw is normal
components.setPitchDirection(0, false); // Invert pitch
components.setRollDirection(0, true);

// Component 1
components.setYawDirection(1, false); // Invert yaw
components.setPitchDirection(1, true);
components.setRollDirection(1, true);
```
5. Move to Absolute Angles
Command the component to an absolute orientation in degrees.

```java

double measuredYaw = 45.0; 
double measuredRoll = 0.0;
double measuredPitch = 30.0;

// Component 0 moves to the measured orientation
components.goToAngle(0, measuredYaw, measuredRoll, measuredPitch);

// Component 1 moves to 0° on all axes
components.goToAngle(1, 0.0, 0.0, 0.0);
```
6. Access Final Positions
Retrieve the resulting Pose3d for use in other simulations or visualizations.

```java

Pose3d pose0 = components.getFinalPose(0);
Pose3d pose1 = components.getFinalPose(1);

```

# Calibration & Logging

## 🔬 Calibration

To achieve a perfect match between your simulation and the real mechanism:

1. **Move to Zero**  
   Manually move the physical mechanism to its known “zero” position.

2. **Read Angles**  
   Read the current angles from your sensors for this position.

3. **Set Offsets**  
   Use these sensor readings as the starting angle offsets in `Pivot3D`.

4. **Check Direction**  
   Verify and adjust the per-axis directions (e.g., `setYawDirection`, `setPitchDirection`, `setRollDirection`) as needed.

5. **Save**  
   Save your determined offsets for consistent startup.

**✅ Result:** After calibration, commanding a certain angle in your simulation will match the real-world movement exactly.

---

## 📈 Logging

Call `periodic()` in your main robot loop to update the loggable data.

```java

@Override
public void robotPeriodic() {
    components.periodic();
}
```
# Logging & Notes

The system logs the following data (useful for NT/AdvantageScope visualization):

- **ZeroedComponentPoses**: The component's position after applying the initial offset (reference zero).  
- **FinalComponentPoses**: The component's current position after applying the rotation from `goToAngle()`.

---

## 📋 Notes

- Positions are in **meters**.  
- Angles are in **degrees**.
- The goToAngle() method always sets an absolute orientation; there is no cumulative angle accumulation.  
- Every component rotates around its specified Translation3d pivot point.
