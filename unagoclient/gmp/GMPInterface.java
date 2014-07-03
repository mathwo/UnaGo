package unagoclient.gmp;

public interface GMPInterface {
    public int getBoardSize();

    public int getColor();

    public int getHandicap();

    public int getRules();

    public void gotAnswer(int a);

    public void gotMove(int color, int pos);

    public void gotOk();
}
