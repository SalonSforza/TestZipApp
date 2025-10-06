package com.nikitin.app;

import com.nikitin.app.data.db.filler.DataBaseFiller;

public class Main {
    public static void main(String[] args) {
        DataBaseFiller filler = new DataBaseFiller();
        filler.fillDataBase();
    }
}