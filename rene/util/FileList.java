package rene.util;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

class FileFilter {
    char F[][];

    public FileFilter(String s) {
        final StringTokenizer t = new StringTokenizer(s);
        final int n = t.countTokens();
        this.F = new char[n][];
        for (int i = 0; i < n; i++) {
            this.F[i] = t.nextToken().toCharArray();
        }
    }

    public char[] filter(int i) {
        return this.F[i];
    }

    public int filterCount() {
        return this.F.length;
    }
}

/**
 * This class parses a subtree for files that match a pattern. The pattern may
 * contain one or more * and ? as usual. The class delivers an enumerator for
 * the files, or may be subclassed to handle the files directly. The routines
 * directory and file can be used to return, if more scanning is necessary.
 */

public class FileList {
    Vector V = new Vector(), Vdir = new Vector();
    boolean Stop;
    boolean Recurse;
    String Dir, Filter;
    boolean UseCase = false;

    public FileList(String dir) {
        this(dir, "*", true);
    }

    public FileList(String dir, String filter) {
        this(dir, filter, true);
    }

    public FileList(String dir, String filter, boolean recurse) {
        this.Stop = false;
        this.Recurse = recurse;
        this.Dir = dir;
        this.Filter = filter;
        if (this.Dir.equals("-")) {
            this.Dir = ".";
            this.Recurse = false;
        } else if (this.Dir.startsWith("-")) {
            this.Dir = this.Dir.substring(1);
            this.Recurse = false;
        }
    }

    /**
     * @param dir
     *            The directory that has been found.
     * @return false if recursion should stop here. (i.e. that directory needs
     *         not be parsed).
     */
    protected boolean directory(File dir) {
        return true;
    }

    /**
     * Return an Enumeration with the directories.
     */
    public Enumeration dirs() {
        return this.Vdir.elements();
    }

    /**
     * @param file
     *            The file that has been found.
     * @return false if you need no more file at all.
     */
    protected boolean file(File file) {
        return true;
    }

    /**
     * Return an Enumeration with the files.
     */
    public Enumeration files() {
        return this.V.elements();
    }

    void find(File dir, FileFilter filter) {
        if (!this.directory(dir)) {
            return;
        }
        final String list[] = dir.list();
        loop: for (int i = 0; i < list.length; i++) {
            final SortFile file = new SortFile(dir, list[i]);
            if (file.isDirectory()) {
                this.Vdir.addElement(file);
                if (this.Recurse) {
                    this.find(file, filter);
                }
            } else {
                String filename = file.getName();
                if (!this.UseCase) {
                    filename = filename.toLowerCase();
                }
                final char fn[] = filename.toCharArray();
                for (int j = 0; j < filter.filterCount(); j++) {
                    if (this.match(fn, 0, filter.filter(j), 0)) {
                        this.Stop = !this.file(file);
                        if (this.Stop) {
                            break loop;
                        }
                        this.V.addElement(file);
                    }
                }
            }
            if (this.Stop) {
                break;
            }
        }
        this.parsed(dir);
    }

    /**
     * Returns a canonical version of the directory
     */
     public String getDir() {
         final File dir = new File(this.Dir);
         try {
             return (dir.getCanonicalPath());
         } catch (final Exception e) {
             return "Dir does not exist!";
         }
     }

     boolean match(char filename[], int n, char filter[], int m) {
         if (filter == null) {
             return true;
         }
         if (m >= filter.length) {
             return n >= filename.length;
         }
         if (n >= filename.length) {
             return m == filter.length - 1 && filter[m] == '*';
         }
         if (filter[m] == '?') {
             return this.match(filename, n + 1, filter, m + 1);
         }
         if (filter[m] == '*') {
             if (m == filter.length - 1) {
                 return true;
             }
             for (int i = n; i < filename.length; i++) {
                 if (this.match(filename, i, filter, m + 1)) {
                     return true;
                 }
             }
             return false;
         }
         if (filter[m] == filename[n]) {
             return this.match(filename, n + 1, filter, m + 1);
         }
         return false;
     }

     /**
      * @param dir
      *            The directory that has been parsed.
      */
     protected void parsed(File dir) {
     }

     public void search() {
         this.Stop = false;
         final File file = new File(this.Dir);
         if (!this.UseCase) {
             this.Filter = this.Filter.toLowerCase();
         }
         if (file.isDirectory()) {
             this.find(file, new FileFilter(this.Filter));
         }
     }

     public void setCase(boolean usecase) {
         this.UseCase = usecase;
     }

     /**
      * @return The number of files found.
      */
     public int size() {
         return this.V.size();
     }

     /**
      * Sort the result.
      */
     public void sort() {
         int i, n = this.V.size();
         Object v[] = new Object[n];
         for (i = 0; i < n; i++) {
             v[i] = this.V.elementAt(i);
         }
         Arrays.sort(v);
         for (i = 0; i < n; i++) {
             this.V.setElementAt(v[i], i);
         }
         n = this.Vdir.size();
         v = new Object[n];
         for (i = 0; i < n; i++) {
             v[i] = this.Vdir.elementAt(i);
         }
         Arrays.sort(v);
         for (i = 0; i < n; i++) {
             this.Vdir.setElementAt(v[i], i);
         }
     }

     public void sort(int type) {
         SortFile.SortBy = type;
         this.sort();
         SortFile.SortBy = SortFile.NAME;
     }

     /**
      * This stops the search from other threads.
      */
     public void stopIt() {
         this.Stop = true;
     }
}

class SortFile extends File implements Comparable<File> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String S;
    static int SortBy = 0;
    final public static int NAME = 0, DATE = 1;

    public SortFile(File dir, String name) {
        super(dir, name);
        try {
            this.S = this.getCanonicalPath().toUpperCase();
        } catch (final Exception e) {
            this.S = "";
        }
    }

    @Override
    public int compareTo(File o) {
        final SortFile f = (SortFile)o;
        if (SortFile.SortBy == SortFile.DATE) {
            final long n = f.lastModified();
            final long m = this.lastModified();
            if (n < m) {
                return -1;
            }
            if (n > m) {
                return 1;
            }
            return 0;
        }
        return -f.S.compareTo(this.S);
    }
}
