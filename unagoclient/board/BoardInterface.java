package unagoclient.board;

import java.awt.*;

/**
 * Encorporated the board into an environment (such as GoFrame).
 */

public interface BoardInterface {
    public void activate();

    // board displays already or not

    // append something to the comment area only
    public void addComment(String s);

    // board is painted and displayed for the first time

    public void advanceTextmark();

    // set the comment area to that string
    public void appendComment(String s);

    public boolean askInsert();

    public boolean askUndo();

    public Color backgroundColor();

    public Color blackColor();

    public boolean blackOnly();

    public Color blackSparkleColor();

    public boolean blocked();

    public Color boardColor();

    public Font boardFont();

    // return a font for the board

    // blocks changed at the end of main variation

    public boolean boardShowing();

    // Various Color settings:
    public boolean bwColor(); // black and white only?

    public Color getColor(String S, int red, int green, int blue);

    // request setting of the next textmark A,B,C,... or 1,2,3,...

    // Comment area:
    public String getComment();

    // enable/disable navigation buttons
    // 1=left, 2=right, 3=varup, 4=varmain, 5=down, 6=up, 7=main

    public boolean getParameter(String S, boolean f);

    // check the menu item for the current state
    // 1=black, 2=white, 3=setblack, 4=setwhite,
    // 5=mark, 6=letter, 7=hide, 10=textmark

    public Color labelColor(int color);

    // enable the correct marker item
    // marker is from FIELD.SQUARE etc.

    public boolean lastNumber(); // flag to show last number

    public Color markerColor(int color);

    public String resourceString(String S);

    public void result(int b, int w);

    // used to notify that board did "Pass" and "Undo"
    // usually should call Board.addcomment()

    // get the content of the comment area
    public void setComment(String s);

    public void setLabel(String s); // next move prompt

    // Board sets two labels, which may be used in a frame
    public void setLabelM(String s); // position of cursor

    // should open an "Delete Moves" modal dialog and return,
    // if undo was allowed

    public void setMarkState(int marker);

    // should open an "Change Game Tree" modal dialog and return,
    // if the node insertion was allowed

    public void setState(int n);

    // called if a move was received at end of main variation,
    // but current position is not visible

    public void setState(int n, boolean flag);

    // sends the result of a game back from the done function
    // when counted at the end of the main tree.

    // get flags:
    public boolean showTarget(); // flag for target rectangle

    // translate the Resource for me
    // check Board.java for necessary translations

    public String version();

    // get a named parameter with boolean value
    // check Board.java for necessary parameters
    // default is f

    public Color whiteColor();

    // get a named parameter with Color value
    // check Board.java for necessary parameters
    // default is the given color

    public Color whiteSparkleColor();

    // return the program version for SGF versioning

    public void yourMove(boolean f);

}
