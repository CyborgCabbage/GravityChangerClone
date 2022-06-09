package me.andrew.gravitychanger.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class QuaternionDouble {
    public static final QuaternionDouble IDENTITY = new QuaternionDouble(0.0, 0.0, 0.0, 1.0);
    private double x;
    private double y;
    private double z;
    private double w;

    public QuaternionDouble(double _x, double _y, double _z, double _w) {
        x = _x;
        y = _y;
        z = _z;
        w = _w;
    }

    public QuaternionDouble(Vec3d axis, double rotationAngle, boolean degrees) {
        if (degrees) {
            rotationAngle *= Math.PI / 180;
        }
        double f = Math.sin(rotationAngle / 2.0);
        x = axis.getX() * f;
        y = axis.getY() * f;
        z = axis.getZ() * f;
        w = Math.cos(rotationAngle / 2.0);
    }

    public QuaternionDouble(double _x, double _y, double _z, boolean degrees) {
        if (degrees) {
            _x *= Math.PI / 180;
            _y *= Math.PI / 180;
            _z *= Math.PI / 180;
        }
        double f = Math.sin(0.5f * _x);
        double g = Math.cos(0.5f * _x);
        double h = Math.sin(0.5f * _y);
        double i = Math.cos(0.5f * _y);
        double j = Math.sin(0.5f * _z);
        double k = Math.cos(0.5f * _z);
        x = f * i * k + g * h * j;
        y = g * h * k - f * i * j;
        z = f * h * k + g * i * j;
        w = g * i * k - f * h * j;
    }

    public QuaternionDouble(QuaternionDouble other) {
        x = other.x;
        y = other.y;
        z = other.z;
        w = other.w;
    }

    public static QuaternionDouble fromEulerYxz(double x, double y, double z) {
        QuaternionDouble q = IDENTITY.copy();
        q.hamiltonProduct(new QuaternionDouble(0.0, Math.sin(x / 2.0), 0.0, Math.cos(x / 2.0)));
        q.hamiltonProduct(new QuaternionDouble(Math.sin(y / 2.0), 0.0, 0.0, Math.cos(y / 2.0)));
        q.hamiltonProduct(new QuaternionDouble(0.0, 0.0, Math.sin(z / 2.0), Math.cos(z / 2.0)));
        return q;
    }

    public static QuaternionDouble fromEulerXyzDegrees(Vec3d vector) {
        return fromEulerXyz(Math.toRadians(vector.getX()), Math.toRadians(vector.getY()), Math.toRadians(vector.getZ()));
    }

    public static QuaternionDouble fromEulerXyz(Vec3d vector) {
        return fromEulerXyz(vector.getX(), vector.getY(), vector.getZ());
    }

    public static QuaternionDouble fromEulerXyz(double x, double y, double z) {
        QuaternionDouble q = IDENTITY.copy();
        q.hamiltonProduct(new QuaternionDouble(Math.sin(x / 2.0), 0.0, 0.0, Math.cos(x / 2.0)));
        q.hamiltonProduct(new QuaternionDouble(0.0, Math.sin(y / 2.0), 0.0, Math.cos(y / 2.0)));
        q.hamiltonProduct(new QuaternionDouble(0.0, 0.0, Math.sin(z / 2.0), Math.cos(z / 2.0)));
        return q;
    }

    public Vec3d toEulerYxz() {
        double f = getW() * getW();
        double g = getX() * getX();
        double h = getY() * getY();
        double i = getZ() * getZ();
        double j = f + g + h + i;
        double k = 2.0 * getW() * getX() - 2.0 * getY() * getZ();
        double l = Math.asin(k / j);
        if (Math.abs(k) > 0.999f * j) {
            return new Vec3d(2.0 * Math.atan2(getX(), getW()), l, 0.0);
        }
        return new Vec3d(Math.atan2(2.0 * getY() * getZ() + 2.0 * getX() * getW(), f - g - h + i), l, Math.atan2(2.0 * getX() * getY() + 2.0 * getW() * getZ(), f + g - h - i));
    }

    public Vec3d toEulerYxzDegrees() {
        Vec3d vec = toEulerYxz();
        return new Vec3d(Math.toDegrees(vec.getX()), Math.toDegrees(vec.getY()), Math.toDegrees(vec.getZ()));
    }

    public Vec3d toEulerXyz() {
        double f = getW() * getW();
        double g = getX() * getX();
        double h = getY() * getY();
        double i = getZ() * getZ();
        double j = f + g + h + i;
        double k = 2.0 * getW() * getX() - 2.0 * getY() * getZ();
        double l = Math.asin(k / j);
        if (Math.abs(k) > 0.999f * j) {
            return new Vec3d(l, 2.0 * Math.atan2(getY(), getW()), 0.0);
        }
        return new Vec3d(l, Math.atan2(2.0 * getX() * getZ() + 2.0 * getY() * getW(), f - g - h + i), Math.atan2(2.0 * getX() * getY() + 2.0 * getW() * getZ(), f - g + h - i));
    }

    public Vec3d toEulerXyzDegrees() {
        Vec3d vec = toEulerXyz();
        return new Vec3d(Math.toDegrees(vec.getX()), Math.toDegrees(vec.getY()), Math.toDegrees(vec.getZ()));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuaternionDouble q = (QuaternionDouble)o;
        if (Double.compare(q.x, x) != 0) {
            return false;
        }
        if (Double.compare(q.y, y) != 0) {
            return false;
        }
        if (Double.compare(q.z, z) != 0) {
            return false;
        }
        return Double.compare(q.w, w) == 0;
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(x);
        int i = (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(y);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(z);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(w);
        i = 31 * i + (int)(l ^ l >>> 32);
        return i;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("QuaternionDouble[").append(getW()).append(" + ");
        s.append(getX()).append("i + ");
        s.append(getY()).append("j + ");
        s.append(getZ()).append("k]");
        return s.toString();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getW() {
        return w;
    }

    public void hamiltonProduct(QuaternionDouble other) {
        double f = getX();
        double g = getY();
        double h = getZ();
        double i = getW();
        double j = other.getX();
        double k = other.getY();
        double l = other.getZ();
        double m = other.getW();
        x = i * j + f * m + g * l - h * k;
        y = i * k - f * l + g * m + h * j;
        z = i * l + f * k - g * j + h * m;
        w = i * m - f * j - g * k - h * l;
    }

    public void scale(double scale) {
        x *= scale;
        y *= scale;
        z *= scale;
        w *= scale;
    }

    public void conjugate() {
        x = -x;
        y = -y;
        z = -z;
    }

    public void set(double _x, double _y, double _z, double _w) {
        x = _x;
        y = _y;
        z = _z;
        w = _w;
    }
    
    public void normalize() {
        double f = getX() * getX() + getY() * getY() + getZ() * getZ() + getW() * getW();
        if (f > 1.0E-6f) {
            double g = MathHelper.fastInverseSqrt(f);
            x *= g;
            y *= g;
            z *= g;
            w *= g;
        } else {
            x = 0.0;
            y = 0.0;
            z = 0.0;
            w = 0.0;
        }
    }

    public static double magnitude(QuaternionDouble q) {
        return Math.sqrt(q.getW() * q.getW() + q.getX() * q.getX() + q.getY() * q.getY() + q.getZ() * q.getZ());
    }

    public static double magnitudeSq(QuaternionDouble q) {
        return q.getW() * q.getW() + q.getX() * q.getX() + q.getY() * q.getY() + q.getZ() * q.getZ();
    }

    public static void inverse(QuaternionDouble q) {
        q.conjugate();
        q.scale(1.0F / magnitudeSq(q));
    }

    public Vec3d rotate(Vec3d vec) {
        QuaternionDouble q = new QuaternionDouble(this);
        q.hamiltonProduct(new QuaternionDouble(vec.getX(), vec.getY(), vec.getZ(), 0.0f));
        QuaternionDouble q2 = new QuaternionDouble(this);
        q2.conjugate();
        q.hamiltonProduct(q2);
        return new Vec3d(q.getX(), q.getY(), q.getZ());
    }

    public Quaternion toFloat(){
        return new Quaternion((float)x,(float)y,(float)z,(float)w);
    }

    public QuaternionDouble copy() {
        return new QuaternionDouble(this);
    }
}


