package org.mltds.sargeras.utils;

/**
 * @author sunyi 2019/2/15.
 */
public class Pair<A, B> {
    private A a;
    private B b;

    public Pair(A var1, B var2) {
        this.a = var1;
        this.b = var2;
    }

    public A getA() {
        return this.a;
    }

    public B getB() {
        return this.b;
    }

    public String toString() {
        return this.a + "=" + this.b;
    }

    public int hashCode() {
        return this.a.hashCode() * 13 + (this.b == null ? 0 : this.b.hashCode());
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof Pair)) {
            return false;
        } else {
            Pair var2 = (Pair) var1;
            if (this.a != null) {
                if (!this.a.equals(var2.a)) {
                    return false;
                }
            } else if (var2.a != null) {
                return false;
            }

            if (this.b != null) {
                if (!this.b.equals(var2.b)) {
                    return false;
                }
            } else if (var2.b != null) {
                return false;
            }

            return true;
        }
    }
}
