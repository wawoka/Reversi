package jp.techacademy.atsushi.kanamori.reversi;

import java.util.List;

import jp.techacademy.atsushi.kanamori.reversi.model.Board;
import jp.techacademy.atsushi.kanamori.reversi.model.Cell;
import jp.techacademy.atsushi.kanamori.reversi.model.IPlayerCallback;
import jp.techacademy.atsushi.kanamori.reversi.model.Player;
import jp.techacademy.atsushi.kanamori.reversi.model.Cell.E_STATUS;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class ReversiView extends View implements IPlayerCallback {

    private static final int VIEW_ID = 1000;

    private Board mBoard;

    private Paint mPaintScreenBg = new Paint();
    private Paint mPaintBoardBg = new Paint();
    private Paint mPaintBoardBorder = new Paint();
    private Paint mPaintCellFgB = new Paint();
    private Paint mPaintCellFgW = new Paint();
    private Paint mPaintCellAvB = new Paint();
    private Paint mPaintCellAvW = new Paint();
    private Paint mPaintTextFg = new Paint();
    private Paint mPaintTurnRect = new Paint();
    private Paint mPaintWinnerRect = new Paint();
    private Paint mPaintCellCur = new Paint();

    private int mWidth;
    private int mHeight;
    private static final float CELL_SIZE_FACTOR = 0.42f;
    private boolean mPaused;

    public ReversiView(Context context) {
        super(context);

        setId(VIEW_ID);
        setFocusable(true);

        mPaintScreenBg.setColor(getResources().getColor(R.color.screen_bg));
        mPaintBoardBg.setColor(getResources().getColor(R.color.board_bg));
        mPaintBoardBorder.setColor(getResources().getColor(R.color.board_border));
        mPaintCellFgB.setColor(getResources().getColor(R.color.cell_fg_black));
        mPaintCellFgW.setColor(getResources().getColor(R.color.cell_fg_white));
        mPaintCellAvB.setColor(getResources().getColor(R.color.cell_fg_black));
        mPaintCellAvW.setColor(getResources().getColor(R.color.cell_fg_white));
        mPaintCellCur.setColor(getResources().getColor(R.color.cell_fg_current));
        mPaintTextFg.setColor(getResources().getColor(R.color.text_fg));
        mPaintTurnRect.setColor(getResources().getColor(R.color.turn_rect));
        mPaintWinnerRect.setColor(getResources().getColor(R.color.winner_rect));

        //アンチエイリアスを指定。これをしないと縁がギザギザになる。
        mPaintCellFgB.setAntiAlias(true);
        mPaintCellFgW.setAntiAlias(true);
        mPaintCellAvB.setAntiAlias(true);
        mPaintCellAvW.setAntiAlias(true);
        mPaintCellCur.setAntiAlias(true);

        mPaintCellAvB.setAlpha(32);
        mPaintCellAvW.setAlpha(64);
        mPaintCellCur.setAlpha(128);

        mPaintTextFg.setAntiAlias(true);
        mPaintTextFg.setStyle(Style.FILL);

        //参考URL:
        // http://y-anz-m.blogspot.com/2010/02/android-multi-screen.html
        // http://y-anz-m.blogspot.com/2010/05/androiddimension.html
        Resources res = getResources();
        int fontSize = res.getDimensionPixelSize(R.dimen.font_size_status);
        mPaintTextFg.setTextSize(fontSize);

        mPaintTurnRect.setAntiAlias(true);
        mPaintTurnRect.setAlpha(128);
        mPaintTurnRect.setStyle(Style.STROKE);
        mPaintTurnRect.setStrokeWidth(5f);

        mPaintWinnerRect.setAntiAlias(true);
        mPaintWinnerRect.setAlpha(192);
        mPaintWinnerRect.setStyle(Style.STROKE);
        mPaintWinnerRect.setStrokeWidth(5f);

        init(false);
    }

    public void init(boolean auto_start){
        mBoard = new Board();
        mPaused = false;

        mBoard.setPlayer1(Player.getPlayer1(getContext(), mBoard, E_STATUS.Black));
        mBoard.setPlayer2(Player.getPlayer2(getContext(), mBoard, E_STATUS.White));

        invalidate();

        Utils.d("init");
        if (auto_start){
            callPlayer();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.mWidth = getWidth();
        this.mHeight = getHeight();
        mBoard.setSize(this.mWidth, this.mHeight);

        drawBoard(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int r = (int)(y / mBoard.getCellHeidht());
                int c = (int)(x / mBoard.getCellWidth());
                if (r < Board.ROWS && c < Board.COLS && r >=0 && c >= 0){
                    Player p = mBoard.getCurrentPlayer();
                    if (p != null && p.isHuman()){
                        move(new Point(c, r));
                    }
                }
                break;
            default:
        }

        return true;
    }

    private void move(Point point){
        List<Cell> changedCells = null;

        if (mBoard.getCell(point).getReversibleCells().size() == 0){
            String s = String.format("Invalid move. (r,c=%d,%d)", point.y, point.x);
            //Toast.makeText(this.getContext(), s, Toast.LENGTH_SHORT).show();
            Utils.d(s);
            return;
        }

        changedCells = mBoard.changeCell(point, mBoard.getTurn());

        int nextAvailableCellCount = mBoard.changeTurn(changedCells);
        if (nextAvailableCellCount == 0){
            if (mBoard.countBlankCells() == 0){				//全部のセルが埋まった場合は終了
                finish();
            } else {
                showSkippMessage();					//スキップ
                nextAvailableCellCount = mBoard.changeTurn(changedCells);
                if (nextAvailableCellCount == 0){	//どちらも打つ場所が無くなった場合は終了
                    finish();
                }
            }
        }

        callPlayer();

        if (changedCells != null){
            for (Cell cell : changedCells) {
                invalidate(cell.getRect());			//変更された領域のみを再描画
            }
        }
    }

    @Override
    public void onEndThinking(Point pos) {
        if (pos == null) return;
        if (pos.y < 0 || pos.x < 0) return;
        if (mPaused) return;

        move(pos);
    }

    @Override
    public void onProgress() {
        invalidate(0, (int)mBoard.getRectF().bottom, mWidth, mHeight);
    }

    @Override
    public void onPointStarted(Point pos) {
        Cell cell = mBoard.getCell(pos);
        invalidate(cell.getRect());			//変更された領域のみを再描画
    }

    @Override
    public void onPointEnded(Point pos) {
        Cell cell = mBoard.getCell(pos);
        invalidate(cell.getRect());			//変更された領域のみを再描画
    }

    private void finish(){
        mBoard.setFinished();
//		showCountsToast();
    }

    public void showCountsToast(){
        Cell.E_STATUS winner = mBoard.getWinnerStatus();
        String msg = "Black: " + mBoard.countCells(Cell.E_STATUS.Black) + "\n"
                + "White: " + mBoard.countCells(Cell.E_STATUS.White) + "\n\n";

        if (mBoard.isFinished()){
            if (winner != Cell.E_STATUS.None){
                msg += "Winner is: " + Cell.statusToDisplay(winner) + "!!";
            } else {
                msg += "Draw game!";
            }
        } else {
            if (winner != Cell.E_STATUS.None){
                msg += Cell.statusToDisplay(winner) + " is winning...\n\n";
            }
            msg += mBoard.getTurnDisplay() + "'s turn.";
        }
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_LONG).show();
        Utils.d(msg);
    }

    private void showSkippMessage(){
        String msg = Cell.statusToDisplay(mBoard.getTurn()) + " has been skipped.";
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
        Utils.d(msg);
    }

    private void drawBoard(Canvas canvas){

        if (mBoard.getRectF().width() <= 0f ) return;

        float bw = mBoard.getRectF().width();
        float bh = mBoard.getRectF().height();
        float cw = mBoard.getCellWidth();
        float ch = mBoard.getCellHeidht();
        float w = cw * CELL_SIZE_FACTOR;
        boolean show_hints = Pref.getShowHints(getContext());

        //画面全体の背景
        canvas.drawRect(0 ,0, mWidth, mHeight, mPaintScreenBg);

        //ボードの背景
        canvas.drawRect(mBoard.getRectF(), mPaintBoardBg);

        //縦線
        for (int i = 0; i < Board.COLS; i++) {
            canvas.drawLine(cw * (i+1), 0, cw * (i+1), bh, mPaintBoardBorder);
        }
        //横線
        for (int i = 0; i < Board.ROWS; i++) {
            canvas.drawLine(0, ch * (i+1), bw, ch * (i+1), mPaintBoardBorder);
        }

        //全てのCellについてコマが配置されていれば描く
        Cell[][] cells = mBoard.getCells();
        for (int i = 0; i < Board.ROWS; i++) {
            for (int j = 0; j < Board.COLS; j++) {
                Cell cell =cells[i][j];
                Cell.E_STATUS st = cell.getStatus();

                if (st == E_STATUS.Black){
                    canvas.drawCircle(cell.getCx(), cell.getCy(), w, mPaintCellFgB);
                } else if(st == E_STATUS.White){
                    canvas.drawCircle(cell.getCx(), cell.getCy(), w, mPaintCellFgW);
                } else {
                    drawHints(cell, canvas, cw, show_hints);
                }
            }
        }

        //手番の表示、現在の黒と白の数の表示
        drawStatus(canvas);
    }

    private void drawHints(Cell cell, Canvas canvas, float cw, boolean show_hints){
        if (!show_hints){
            return;
        }

        float aw = cw * 0.1f;

        //次に配置可能なセルであれば小さな丸を表示する
        if (cell.getReversibleCells().size() > 0){
            if (mBoard.getTurn() == Cell.E_STATUS.Black){
                canvas.drawCircle(cell.getCx(), cell.getCy(), aw, mPaintCellAvB);
            } else {
                canvas.drawCircle(cell.getCx(), cell.getCy(), aw, mPaintCellAvW);
            }
        } else {
            canvas.drawCircle(cell.getCx(), cell.getCy(), aw, mPaintBoardBg);
        }
    }

    private void drawStatus(Canvas canvas){
        Resources res = getResources();
        float turn_rect_inset = res.getDimension(R.dimen.turn_rect_inset);
        float turn_rect_round = res.getDimension(R.dimen.turn_rect_round);
        float turn_circle_x = res.getDimension(R.dimen.turn_circle_x);
        float turn_circle_y = res.getDimension(R.dimen.turn_circle_y);
        float turn_text_x = res.getDimension(R.dimen.turn_text_x);
        float turn_text_y = res.getDimension(R.dimen.turn_text_y);
        float top = mBoard.getRectF().bottom;
        float center = mBoard.getRectF().width() / 2f;

        if (!mBoard.isFinished()){
            RectF rect;
            if (mBoard.getTurn() == E_STATUS.Black){
                rect = new RectF(turn_rect_inset, top + turn_rect_inset, center - turn_rect_inset, mHeight - turn_rect_inset);
            } else {
                rect = new RectF(center + turn_rect_inset, top + turn_rect_inset, mWidth - turn_rect_inset, mHeight - turn_rect_inset);
            }
            mPaintTurnRect.setStyle(Style.FILL);
            mPaintTurnRect.setAlpha(128);
            canvas.drawRoundRect(rect, turn_rect_round, turn_rect_round, mPaintTurnRect);	//背景
            mPaintTurnRect.setStyle(Style.STROKE);
            mPaintTurnRect.setAlpha(255);
            canvas.drawRoundRect(rect, turn_rect_round, turn_rect_round, mPaintTurnRect);	//枠
        }

        canvas.drawCircle(turn_circle_x, top + turn_circle_y, mBoard.getCellWidth() * CELL_SIZE_FACTOR, mPaintCellFgB);
        String s = String.valueOf(mBoard.countCells(E_STATUS.Black));
        canvas.drawText(s, turn_text_x, top + turn_text_y, mPaintTextFg);					//黒のコマ数
        canvas.drawText(mBoard.getPlayer1().getName(), turn_circle_x, top + turn_text_y*1.8f, mPaintTextFg);			//黒の名前

        canvas.drawCircle(center + turn_circle_x, top + turn_circle_y, mBoard.getCellWidth() * CELL_SIZE_FACTOR, mPaintCellFgB);
        canvas.drawCircle(center + turn_circle_x, top + turn_circle_y, mBoard.getCellWidth() * CELL_SIZE_FACTOR * 0.94f, mPaintCellFgW);
        s = String.valueOf(mBoard.countCells(E_STATUS.White));
        canvas.drawText(s, center + turn_text_x, top + turn_text_y, mPaintTextFg);		//白のコマ数
        canvas.drawText(mBoard.getPlayer2().getName(), center + turn_circle_x, top + turn_text_y*1.8f, mPaintTextFg);  //白の名前

        //コンピュータの思考中の場合は進捗状況を表示。
        Player p = mBoard.getCurrentPlayer();
        if (p != null && !p.isHuman()){
//			s = String.valueOf(mBoard.getCurrentPlayer().getProgress()) + "%";
//			canvas.drawText(s, rect.left + turn_circle_x, top + turn_text_y*2.5f, mPaintTextFg);

            Cell cell = p.getCurrentCell();
            if (cell != null){
                canvas.drawCircle(cell.getCx(), cell.getCy(),  mBoard.getCellWidth() * CELL_SIZE_FACTOR, mPaintCellCur);
                invalidate(cell.getRect());
            }
        }

        if (mBoard.isFinished()){
            drawWinner(canvas);
        }

        invalidate(0, (int)mBoard.getRectF().bottom, mWidth, mHeight);
    }

    private void drawWinner(Canvas canvas){
        Resources res = getResources();
        float center_x = mBoard.getRectF().width() / 2f;
        float center_y = mBoard.getRectF().height() / 2f;
        String s;

        if (mBoard.getWinnerStatus() == E_STATUS.Black){
            s = mBoard.getPlayer1().getName() + " wins!";
        } else if (mBoard.getWinnerStatus() == E_STATUS.White) {
            s = mBoard.getPlayer2().getName() + " wins!";
        } else {
            s = "Draw game!";
        }

        Paint paintBg = new Paint();
        paintBg.setColor(Color.BLACK);
        paintBg.setAlpha(128);
        canvas.drawRect(mBoard.getRectF(), paintBg);

        Paint paint = new Paint(mPaintTextFg);
        paint.setColor(Color.YELLOW);
        paint.setAlpha(255);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(mPaintTextFg.getTextSize() * 1.8f);
        paint.setTextSkewX(-0.3f);
        paint.setShadowLayer(2, 2, 2, Color.argb(200, 0, 0, 0));

        canvas.save();
        canvas.rotate(-30, center_x, center_y);
        canvas.drawText(s, center_x, center_y, paint);
        canvas.restore();

        invalidate();			//タイミングによっては文字の一部が消える場合があるので必要。
    }

    public String getState(){
        String s = mBoard.getStateString();
        Utils.d("getState: state=" + s);
        return s;
    }

    public void pause(){
        mPaused = true;

        Player p = mBoard.getCurrentPlayer();
        if (p != null && !p.isHuman()){
            //別スレッドで思考ルーチンが動いていれば中断する。
            p.stopThinking();
        }
    }

    public void resume(String state){
        Utils.d("onResume: state=" + state);

        mPaused = false;
        if (!TextUtils.isEmpty(state)){
            mBoard.loadFromStateString(state);

            mBoard.setPlayer1(Player.getPlayer1(getContext(), mBoard, E_STATUS.Black));
            mBoard.setPlayer2(Player.getPlayer2(getContext(), mBoard, E_STATUS.Black));
        }

        callPlayer();
    }

    private void callPlayer(){
        if (mPaused) return;

        Player p = mBoard.getCurrentPlayer();
        if (p != null && !p.isHuman()){
            p.startThinking(this);
        }
    }
}