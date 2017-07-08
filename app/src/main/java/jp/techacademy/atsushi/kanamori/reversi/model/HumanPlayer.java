package jp.techacademy.atsushi.kanamori.reversi.model;

public class HumanPlayer extends Player {

    public HumanPlayer(Cell.E_STATUS turn, String name, Board board) {
        super(turn, name, board);
    }

    @Override
    public boolean isHuman() {
        return true;
    }

    @Override
    public void startThinking(IPlayerCallback callback) {
        callback.onEndThinking(null);
    }

    @Override
    public void stopThinking() {
        // Do nothing.
    }
}
