package jp.techacademy.atsushi.kanamori.reversi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import android.graphics.Point;
import jp.techacademy.atsushi.kanamori.reversi.model.Cell.E_STATUS;

public class ComputerPlayerLevel1 extends ComputerPlayer {

    private static int WAIT_MSEC = 10;
    private Random mRnd;

    //場所毎の重み付け
    protected static final int[][] weight_table
            = { { 40,-12,  0, -1, -1,  0,-12, 40 },
            {-12,-15, -3, -3, -3, -3,-15,-12 },
            {  0, -3,  0, -1, -1,  0, -3,  0 },
            { -1, -3, -1, -1, -1, -1, -3, -1 },
            { -1, -3, -1, -1, -1, -1, -3, -1 },
            {  0, -3,  0, -1, -1,  0, -3,  0 },
            {-12,-15, -3, -3, -3, -3,-15,-12 },
            { 40,-12,  0, -1, -1,  0,-12, 40 }
    };

    public ComputerPlayerLevel1(E_STATUS turn, String name, Board board){
        super(turn, name, board);
        mRnd = new Random();
    }

    @Override
    protected Point think() {
        Point pos = null;

        try {
            Thread.sleep(WAIT_MSEC);
        } catch (InterruptedException e) {
            setStopped(true);
        }
        if (isStopped()) return pos;					//中断フラグが立っていたら抜ける。

        //コマを置く事が出来るセルのリストを得る。
        ArrayList<Cell> available_cells = mBoard.getAvailableCells();
        if (available_cells.size() == 0){
            return pos;
        }

        //評価値の高いものから降順にソート。
        Collections.sort(available_cells, new WeightComparator(weight_table));

        if (isStopped()) return pos;					//中断フラグが立っていたら抜ける。


        ArrayList<Cell> max_cells = new ArrayList<Cell>();
        max_cells.add(available_cells.get(0));						//ソート後先頭に来たものを最終候補リストに追加。
        int max_weight = getWeight(available_cells.get(0), weight_table);

        //２番目以降で先頭と同じ評価値を持つものを最終候補リストに追加。
        for (int i = 1; i < available_cells.size(); i++) {
            Cell current = available_cells.get(i);
            if (max_weight == getWeight(current, weight_table)){
                max_cells.add(current);
            } else {
                break;
            }
        }

        //最終候補が複数ある場合はそのなかからランダムに選ぶ。
        int n = mRnd.nextInt(max_cells.size());
        Cell chosenCell = max_cells.get(n);
        pos = chosenCell.getPoint();

        return pos;
    }

}