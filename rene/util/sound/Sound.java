package rene.util.sound;

import java.io.InputStream;

public interface Sound {
    public String getName();

    public void load(InputStream in);

    public void load(String file);

    public void setName(String Name);

    public void start();
}
