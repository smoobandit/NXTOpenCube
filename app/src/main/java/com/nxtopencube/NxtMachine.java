package com.nxtopencube;

public class NxtMachine {
    public NxtAxis[] axes = new NxtAxis[2];
    private char upside = 'U';
    private char downside = 'D';

    public NxtMachine() {
        this.axes[0] = new NxtAxis(this, "NS");
        this.axes[0].setSide(0, 'F');
        this.axes[0].setSide(1, 'B');
        this.axes[1] = new NxtAxis(this, "EW");
        this.axes[1].setSide(0, 'L');
        this.axes[1].setSide(1, 'R');
    }

    public NxtAxis getAxis(int index) {
        return this.axes[index];
    }

    private char getSideFromMove(String move) {
        return move.charAt(0);
    }

    private int getTurnFromMove(String move) {
        if (move.length() == 1) {
            return 1;
        }
        return move.charAt(1) == '2' ? 2 : -1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void moveCube(String movestring) {
        int fa;
        int fq;
        int ma;
        int mq;
        String[] moves = movestring.split("\\s+");
        int im = 0;
        while (im < moves.length) {
            char side = getSideFromMove(moves[im]);
            if (this.upside == side || this.downside == side) {
                int ta = Math.random() > 0.5d ? 0 : 1;
                int tq = ta == 1 ? 0 : 1;
                if (this.axes[ta].isFixed()) {
                    fa = ta;
                    fq = tq;
                } else {
                    fa = tq;
                    fq = ta;
                }
                this.axes[fq].makeOpen();
                this.axes[fq].makeVertical();
                boolean cw = Math.random() > 0.5d;
                this.axes[fa].makeTurn(cw ? 1 : -1, cw ? -1 : 1);
                this.axes[fq].makeClose();
                char us = this.upside;
                char ds = this.downside;
                this.upside = this.axes[fq].getSide((!(cw && fa == 0) && (cw || fa != 1)) ? 1 : 0);
                this.downside = this.axes[fq].getSide(((cw || fa != 0) && !(cw && fa == 1)) ? 1 : 0);
                this.axes[fq].setSide((!(cw && fa == 0) && (cw || fa != 1)) ? 1 : 0, ds);
                this.axes[fq].setSide(((cw || fa != 0) && !(cw && fa == 1)) ? 1 : 0, us);
            }
            int a = getTurnFromMove(moves[im]);
            int b = 0;
            im++;
            if (im < moves.length) {
                char s = getSideFromMove(moves[im]);
                if ((side == 'R' && s == 'L') || ((side == 'L' && s == 'R') || ((side == 'U' && s == 'D') || ((side == 'D' && s == 'U') || ((side == 'F' && s == 'B') || (side == 'B' && s == 'F')))))) {
                    b = getTurnFromMove(moves[im]);
                    im++;
                }
            }
            if (this.axes[0].getSide(0) == side || this.axes[0].getSide(1) == side) {
                ma = 0;
                mq = 1;
            } else {
                ma = 1;
                mq = 0;
            }
            this.axes[mq].makeFixe();
            if (this.axes[ma].getSide(0) == side) {
                this.axes[ma].makeTurn(a, b);
            } else {
                this.axes[ma].makeTurn(b, a);
            }
        }
        this.axes[0].makeFixe();
        this.axes[1].makeFixe();
    }
}
