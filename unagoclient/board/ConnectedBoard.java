package unagoclient.board;

import unagoclient.sound.UnaGoSound;

/**
 * This board overrides some methods from the Board class for connected boards.
 * It knows a ConnectedGoFrame to ask for permission of operations and to report
 * moves to it.
 */

public class ConnectedBoard extends Board {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ConnectedGoFrame CGF;

    public ConnectedBoard(int size, ConnectedGoFrame gf) {
        super(size, gf);
        this.CGF = gf;
    }

    /**
     * In a ConnectedBoard you cannot delete stones.
     */
    @Override
    public synchronized void deletemousec(int i, int j) {
    }

    /**
     * This is used to fix the game tree (after confirmation). Will not be
     * possible, if the GoFrame wants moves.
     */
    @Override
    public synchronized void insertnode() {
        if (this.positionNode.isLastMain() && this.CGF.wantsmove()) {
            return;
        }
        super.insertnode();
    }

    @Override
    public synchronized void movemouse(int i, int j) {
        if (this.positionNode.haschildren()) {
            return;
        }
        if (this.position.getColor(i, j) != 0) {
            return;
        }
        if (this.captured == 1 && this.capturei == i && this.capturej == j
                && this.gameFrame.getParameter("preventko", true)) {
            return;
        }
        if (this.positionNode.isMain() && this.CGF.wantsmove()) {
            if (this.CGF.moveset(i, j)) {
                this.sendX = i;
                this.sendY = j;
                this.update(i, j);
                this.copy();
                this.myColor = this.position.getNextTurnColor();
            }
            UnaGoSound.play("click", "", false);
        } else {
            this.set(i, j); // try to set a new move
        }
    }

    /**
     * Pass (report to the GoFrame if necessary.
     */
    @Override
    public synchronized void pass() {
        if (this.positionNode.haschildren()) {
            return;
        }
        if (this.gameFrame.blocked() && this.positionNode.isMain()) {
            return;
        }
        if (this.positionNode.isMain() && this.CGF.wantsmove()) {
            this.CGF.movepass();
            return;
        }
        super.pass();
    }

    /**
     * Completely remove a group (at end of game, before count), and note all
     * removals. This is only allowed in end nodes and if the GoFrame wants the
     * removal, it gets it.
     */
    @Override
    public synchronized void removegroup(int i0, int j0) {
        if (this.positionNode.haschildren()) {
            return;
        }
        if (this.position.getColor(i0, j0) == 0) {
            return;
        }
        if (this.CGF.wantsmove() && ((Node) this.positionNode.content()).main()) {
            this.CGF.moveset(i0, j0);
        }
        super.removegroup(i0, j0);
    }

    @Override
    public synchronized void setmouse(int i, int j, int c) {
        if (this.positionNode.isMain() && this.CGF.wantsmove()) {
            return;
        }
        super.setmouse(i, j, c);
    }

    /**
     * In a ConnectedBoard you cannot fix the game tree this way.
     */
    @Override
    public synchronized void setmousec(int i, int j, int c) {
    }

    /**
     * Take back the last move.
     */
    @Override
    public synchronized void undo() {
        if (this.positionNode.isMain() && this.CGF.wantsmove()) {
            if (!this.positionNode.haschildren()) {
                if (this.state != 1 && this.state != 2) {
                    this.clearremovals();
                }
                this.CGF.undo();
            }
            return;
        }
        super.undo();
    }
}
