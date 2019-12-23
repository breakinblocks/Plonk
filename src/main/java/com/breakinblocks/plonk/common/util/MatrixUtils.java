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
            pitch = Math.toDegrees(Math.atan2(rot.m20, rot.m21));
            yaw = Math.toDegrees(Math.acos(rot.m22));
            roll = Math.toDegrees(-Math.atan2(rot.m02, rot.m12));
            // The above is actually not the pitch yaw and roll, it's rotation about z axis, then x axis, than z axis again (extrinsic rotation)
            // Suppose I gotta just do it myself lol
            // Minecraft also uses y as the vertical axis instead of z to add to the confusion
            // TODO: Work this out?
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("tx=").append(tx)
                    .append(",ty=").append(ty)
                    .append(",tz=").append(tz)
                    .append(",sx=").append(sx)
                    .append(",sy=").append(sy)
                    .append(",sz=").append(sz)
                    .append(",pitch=").append(pitch)
                    .append(",yaw=").append(yaw)
                    .append(",roll=").append(roll)
                    .append(",rotation=\n").append(rot)
                    .toString();
        }
    }
}
