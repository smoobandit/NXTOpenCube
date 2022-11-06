package com.nxtopencube;

public class NxtAxis {
    private String direction;
    NxtMachine machine;
    private char[] side = new char[2];
    private int[] align = new int[2];
    private int[] status = new int[2];

    public NxtAxis(NxtMachine machinevalue, String dirvalue) {
        this.machine = machinevalue;
        this.direction = dirvalue;
        this.align[0] = 0;
        this.align[1] = 0;
        this.status[0] = 0;
        this.align[1] = 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public char getSide(int index) {
        return this.side[index];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setSide(int index, char sideval) {
        this.side[index] = sideval;
    }

    String getDirection() {
        return this.direction;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isFixed() {
        return this.align[0] == 0 && this.align[1] == 0 && this.status[0] == 0 && this.status[1] == 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeFixe() {
        if (!isFixed()) {
            if (this.align[0] == 1 && this.align[1] == 1) {
                makeOpen();
                makeVertical();
            } else {
                if (this.align[0] == 1 && this.align[1] == 0) {
                    if (this.status[0] == 0) {
                        open(1, 0);
                        this.status[0] = 1;
                    }
                    turn(Math.random() > 0.5d ? 1 : -1, 0);
                    this.align[0] = 0;
                }
                if (this.align[0] == 0 && this.align[1] == 1) {
                    if (this.status[1] == 0) {
                        open(0, 1);
                        this.status[1] = 1;
                    }
                    turn(0, Math.random() > 0.5d ? 1 : -1);
                    this.align[1] = 0;
                }
            }
            makeClose();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeOpen() {
        open(this.status[0] == 0 ? 1 : 0, this.status[1] == 0 ? 1 : 0);
        this.status[0] = 1;
        this.status[1] = 1;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeClose() {
        close(this.status[0] == 1 ? 1 : 0, this.status[1] == 1 ? 1 : 0);
        this.status[0] = 0;
        this.status[1] = 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeVertical() {
        int a = this.align[0] == 1 ? Math.random() < 0.5d ? 1 : -1 : 0;
        int b = this.align[1] == 1 ? Math.random() < 0.5d ? 1 : -1 : 0;
        turn(a, b);
        this.align[0] = 0;
        this.align[1] = 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void makeTurn(int turna, int turnb) {
        turn(turna, turnb);
        if (turna == 1 || turna == -1) {
            this.align[0] = this.align[0] == 0 ? 1 : 0;
        }
        if (turnb == 1 || turnb == -1) {
            this.align[1] = this.align[1] == 0 ? 1 : 0;
        }
    }

    private void open(int a, int b) {
        NxtMain.connNxtSM1.sendCommand("OPEN(" + this.direction + "," + Integer.toString(a) + "," + Integer.toString(b) + ")");
    }

    private void close(int a, int b) {
        NxtMain.connNxtSM1.sendCommand("CLOSE(" + this.direction + "," + Integer.toString(a) + "," + Integer.toString(b) + ")");
    }

    private void turn(int a, int b) {
        String text = "TURN(" + this.direction + "," + Integer.toString(a) + "," + Integer.toString(b) + ")";
        if (this.direction.equals("NS")) {
            NxtMain.connNxtSM2.sendCommand(text);
        }
        if (this.direction.equals("EW")) {
            NxtMain.connNxtSM1.sendCommand(text);
        }
    }
}
