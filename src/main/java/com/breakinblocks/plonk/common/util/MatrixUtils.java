package com.breakinblocks.plonk.common.util;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

public class MatrixUtils {
    /**
     * Calculate the difference between two transforms
     * Assumes both input matrices have an inverse
     *
     * @param a Start transformation
     * @param b End transformation
     * @return transform that when applied to a results in b
     */
    public static Matrix4d difference(Matrix4d a, Matrix4d b) {
        // a * D = b
        // D = a^-1 * b
        Matrix4d aInv = new Matrix4d();
        aInv.invert(a);
        Matrix4d D = new Matrix4d();
        D.mul(aInv, b);
        return D;
    }

    public static class TransformData {
        public final double tx, ty, tz;
        public final double sx, sy, sz;
        public final Matrix3d rot;
        public final double pitch, yaw, roll;

        public TransformData(Matrix4f mat) {
            this(new Matrix4d(mat));
        }

        public TransformData(Matrix4d mat) {
            // https://math.stackexchange.com/a/1463487
            mat = new Matrix4d(mat);
            tx = mat.m30;
            ty = mat.m31;
            tz = mat.m32;
            sx = new Vector3d(mat.m00, mat.m10, mat.m20).length();
            sy = new Vector3d(mat.m01, mat.m11, mat.m21).length();
            sz = new Vector3d(mat.m02, mat.m12, mat.m22).length();
            rot = new Matrix3d(
                    mat.m00 / sx, mat.m01 / sy, mat.m02 / sz,
                    mat.m10 / sx, mat.m11 / sy, mat.m12 / sz,
                    mat.m20 / sx, mat.m21 / sy, mat.m22 / sz
            );
            // https://en.wikipedia.org/wiki/Rotation_formalisms_in_three_dimensions#Conversion_formulae_between_formalisms
            //pitch = Math.toDegrees(Math.atan2(rot.m20, rot.m21));
            //yaw = Math.toDegrees(Math.acos(rot.m22));
            //roll = Math.toDegrees(-Math.atan2(rot.m02, rot.m12));
            // The above is actually not the pitch yaw and roll, it's rotation about z axis, then x axis, than z axis again (extrinsic rotation)
            // Suppose I gotta just do it myself lol
            // Rotation follows the order
            // Yaw (y axis rotation) -> Pitch (x axis rotation) -> Roll (z axis rotation)
            // Ry = [cos(yaw)   0           sin(yaw)    ]
            //      [0          1           0           ]
            //      [-sin(yaw)  1           cos(yaw)    ]
            //
            // Rx = [1          0           0           ]
            //      [0          cos(pitch)  -sin(pitch) ]
            //      [0          sin(pitch)  cos(pitch)  ]
            //
            // Rz = [cos(roll)  -sin(roll)  0           ]
            //      [sin(roll)  cos(roll)   0           ]
            //      [0          0           1           ]
            // R = Ry Rx Rz =
            // [cos(roll) cos(yaw) + sin(pitch) sin(roll) sin(yaw)  , -cos(yaw) sin(roll) + cos(roll) sin(pitch) sin(yaw)  , cos(pitch) sin(yaw)]
            // [cos(pitch) sin(roll)                                , cos(pitch) cos(roll)                                 , -sin(pitch)        ]
            // [cos(yaw) sin(pitch) sin(roll) - cos(roll) sin(yaw)  , cos(roll) cos(yaw) sin(pitch) + sin(roll) sin(yaw)   , cos(pitch) cos(yaw)]
            // m02/m22 = cos(pitch) sin(yaw) / (cos(pitch) cos(yaw)) = tan(yaw)
            yaw = Math.toDegrees(Math.atan2(rot.m02, rot.m22));
            // m12 = -sin(pitch)
            // m10^2 + m11^2    = (cos(pitch) sin(roll))^2 + (cos(pitch) cos(roll))^2)
            //                  = cos(pitch)^2 * (sin(roll)^2 + cos(roll)^2)
            //                  = cos(pitch)^2
            // m12 / sqrt(m10^2 + m11^2) = -tan(pitch)
            pitch = Math.toDegrees(-Math.atan2(rot.m12, Math.sqrt(Math.pow(rot.m10, 2) + Math.pow(rot.m11, 2))));
            // m10/m11 = cos(pitch) sin(roll) / (cos(pitch) cos(roll)) = tan(roll)
            roll = Math.toDegrees(Math.atan2(rot.m10, rot.m11));
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append(String.format("tx=%.3f", tx))
                    .append(String.format(" ty=%.3f", ty))
                    .append(String.format(" tz=%.3f", tz))
                    .append(String.format(" sx=%.3f", sx))
                    .append(String.format(" sy=%.3f", sy))
                    .append(String.format(" sz=%.3f", sz))
                    .append(String.format(" pitch=%.3f", pitch))
                    .append(String.format(" yaw=%.3f", yaw))
                    .append(String.format(" roll=%.3f", roll))
                    //.append(",rotation=\n").append(rot)
                    .toString();
        }
    }
}
