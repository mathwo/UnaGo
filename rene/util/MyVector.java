package rene.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This is a more effective replacement of the Vector class. It is based on a
 * growing array. If an object is removed, it is replaced by null. The class
 * knows about the first null object (the gap). Searching for elements or other
 * operations automatically compress the array by copying the elements upwards,
 * but only as far as they need to go anyway.
 * <p>
 * Accessing an element is very effective, at least the second time. If you want
 * to make sure, it is always effective, compress first. The most effective way
 * is to get the object array itself.
 * <p>
 * The objects can be enumerated. The object returned by nextElement() is found
 * very rapidly. E.g. it can be deleted at once.
 * <p>
 * Enumeration is not reentrant. Do it only once each time.
 * <p>
 * Nothing in this class is synchronized!
 */

public class MyVector<klasse> implements Enumeration<klasse>, Iterator<klasse>,
Iterable<klasse> {
    public static void main(String args[]) {
        final MyVector V = new MyVector();
        for (int i = 1; i <= 10; i++) {
            V.addElement("Element " + i);
        }
        for (int i = 4; i <= 9; i++) {
            V.removeElement("Element " + i);
        }
        System.out.println("--> " + V.elementAt(3));
        System.out.println(V.ON + " elements, " + V.OLast + " used, " + V.Gap
                + " gap.");
        System.out.println("--> " + V.elementAt(3));
        System.out.println(V.ON + " elements, " + V.OLast + " used, " + V.Gap
                + " gap.");
        for (int i = 11; i <= 20; i++) {
            V.addElement("Element " + i);
        }
        System.out.println(V.ON + " elements, " + V.OLast + " used ," + V.Gap
                + " gap.");
        final Enumeration E = V.elements();
        while (E.hasMoreElements()) {
            System.out.println((String) E.nextElement());
        }
        System.out.println(V.ON + " elements, " + V.OLast + " used, " + V.Gap
                + " gap.");
    }

    klasse O[];
    int OSize, ON, OLast, Gap;

    int EN = 0;

    public MyVector() {
        this(8);
    }

    public MyVector(int initsize) {
        this.O = (klasse[]) (new Object[initsize]);
        this.OSize = initsize;
        this.OLast = this.ON = 0;
        this.Gap = -1;
    }

    /**
     * Add an element. Extend the array, if necessary.
     */
    public void addElement(klasse o) {
        if (this.OLast >= this.OSize) {
            this.extend();
        }
        this.O[this.OLast++] = o;
        this.ON++;
    }

    /**
     * Compress the array.
     */
    public void compress() {
        if (this.Gap < 0) {
            return;
        }
        int k = this.Gap;
        for (int i = this.Gap; i < this.OLast; i++) {
            if (this.O[i] == null) {
                continue;
            }
            this.O[k++] = this.O[i];
        }
        this.ON = k;
        for (int i = k; i < this.OLast; i++) {
            this.O[i] = null;
        }
        this.Gap = -1;
        this.OLast = this.ON;
    }

    /**
     * Copy the array into an object array of at least the same size.
     */
    public void copyInto(Object o[]) {
        this.compress();
        System.arraycopy(this.O, 0, o, 0, this.ON);
    }

    /**
     * Get the element at a given position. Second access will always be
     * effective. First access compresses. Throws an exception, if the index is
     * invalid.
     */
    public klasse elementAt(int n) {
        if (n < 0 || n >= this.ON) {
            throw new ArrayIndexOutOfBoundsException(n);
        }
        if (this.Gap < 0 || n < this.Gap) {
            return this.O[n];
        }
        int k = this.Gap;
        for (int i = this.Gap; i < this.OLast; i++) {
            if (this.O[i] == null) {
                continue;
            }
            this.O[k] = this.O[i];
            this.O[i] = null;
            if (k == n) {
                final klasse ret = this.O[k];
                k++;
                this.Gap = k;
                if (this.Gap >= this.ON) {
                    for (int j = this.Gap; j < this.OLast; j++) {
                        this.O[j] = null;
                    }
                    this.OLast = this.ON;
                    this.Gap = -1;
                }
                return ret;
            }
            k++;
        }
        // never happens
        throw new ArrayIndexOutOfBoundsException(n);
    }

    /**
     * Get an enumeration of this array.
     */
    public Enumeration<klasse> elements() {
        this.compress();
        this.EN = 0;
        return this;
    }

    /**
     * Test for equality with another vector, using equals.
     */
    public boolean equals(MyVector V) {
        if (V.ON != this.ON) {
            return false;
        }
        V.compress();
        this.compress();
        for (int i = 0; i < this.ON; i++) {
            if (!V.O[i].equals(this.O[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test for equality with another vector, using object equality.
     */
    public boolean equalsIdentical(MyVector V) {
        if (V.ON != this.ON) {
            return false;
        }
        V.compress();
        this.compress();
        for (int i = 0; i < this.ON; i++) {
            if (V.O[i] != this.O[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extend the array, or get space by compressing it.
     */
    public void extend() {
        if (this.ON < this.OLast / 2) {
            this.compress();
            return;
        }
        final klasse o[] = (klasse[]) (new Object[2 * this.OSize]);
        System.arraycopy(this.O, 0, o, 0, this.OLast);
        this.OSize *= 2;
        this.O = o;
    }

    /**
     * Get the array itself (compressed). Make sure, you also use size() to
     * determine the true length of the array. Do not change objects beyond the
     * size! Do not set objects to null!
     */
    public Object[] getArray() {
        this.compress();
        return this.O;
    }

    /**
     * Method for Enumeration.
     */
    @Override
    public boolean hasMoreElements() {
        while (this.EN < this.OLast && this.O[this.EN] == null) {
            this.EN++;
        }
        return this.EN < this.OLast;
    }

    /**
     * Method for Iterator
     */
    @Override
    public boolean hasNext() {
        while (this.EN < this.OLast && this.O[this.EN] == null) {
            this.EN++;
        }
        return this.EN < this.OLast;
    }

    /**
     * Find an element. Compress on the way. Check for the last element,
     * returned by nextElement() first. Equality is checked with the equal()
     * function.
     *
     * @return -1, if not found.
     */
    public int indexOf(klasse o) {
        if (this.EN > 0 && this.EN <= this.OLast
                && this.O[this.EN - 1].equals(o)) {
            return this.EN - 1;
        }
        if (this.Gap < 0) {
            for (int i = 0; i < this.OLast; i++) {
                if (this.O[i].equals(o)) {
                    return i;
                }
            }
            return -1;
        }
        for (int i = 0; i < this.Gap; i++) {
            if (this.O[i].equals(o)) {
                return i;
            }
        }
        int k = this.Gap;
        for (int i = this.Gap; i < this.OLast; i++) {
            if (this.O[i] == null) {
                continue;
            }
            if (this.O[i].equals(o)) {
                this.Gap = k;
                return i;
            }
            this.O[k++] = this.O[i];
            this.O[i] = null;
        }
        this.ON = k;
        for (int i = k; i < this.OLast; i++) {
            this.O[i] = null;
        }
        this.Gap = -1;
        this.OLast = this.ON;
        return -1;
    }

    /**
     * Method for iteration
     */
    @Override
    public Iterator<klasse> iterator() {
        return this;
    }

    /**
     * Method for Iterator
     */
    @Override
    public klasse next() {
        if (!this.hasMoreElements()) {
            throw new ArrayIndexOutOfBoundsException(this.OLast);
        }
        return this.O[this.EN++];
    }

    /**
     * Method for Enumeration.
     */
    @Override
    public klasse nextElement() {
        if (!this.hasMoreElements()) {
            throw new ArrayIndexOutOfBoundsException(this.OLast);
        }
        return this.O[this.EN++];
    }

    /**
     * Method for iterator
     */
    @Override
    public void remove() {
        final int i = this.EN - 1;
        if (this.EN < 0) {
            return;
        }
        this.O[i] = null;
        this.ON--;
        if (this.Gap < 0 || this.Gap > i) {
            this.Gap = i;
        }
        if (i == this.OLast - 1) {
            this.OLast--;
        }
        while (this.OLast > 0 && this.O[this.OLast - 1] == null) {
            this.OLast--;
        }
        if (this.Gap >= this.OLast) {
            this.Gap = -1;
        }
    }

    /**
     * Clear this array, but keep its memory!
     */
    public void removeAllElements() {
        for (int i = 0; i < this.OLast; i++) {
            this.O[i] = null;
        }
        this.ON = this.OLast = 0;
        this.Gap = -1;
    }

    /**
     * Remove a single element. This will also compress the part below the
     * element, or all, if it is not found.
     */
    public void removeElement(klasse o) {
        final int i = this.indexOf(o);
        if (i < 0) {
            return;
        }
        this.O[i] = null;
        this.ON--;
        if (this.Gap < 0 || this.Gap > i) {
            this.Gap = i;
        }
        if (i == this.OLast - 1) {
            this.OLast--;
        }
        while (this.OLast > 0 && this.O[this.OLast - 1] == null) {
            this.OLast--;
        }
        if (this.Gap >= this.OLast) {
            this.Gap = -1;
        }
    }

    /**
     * @return the number of objects in the vector.
     */
    public int size() {
        return this.ON;
    }

    /**
     * Truncate the vector to n elements, if it has more.
     */
    public void truncate(int n) {
        if (n >= this.ON) {
            return;
        }
        this.compress();
        for (int i = n; i < this.OLast; i++) {
            this.O[i] = null;
        }
        this.OLast = this.ON = n;
    }
}
