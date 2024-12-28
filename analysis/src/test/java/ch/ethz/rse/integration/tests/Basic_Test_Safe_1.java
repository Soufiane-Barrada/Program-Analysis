package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE SAFE

public class Basic_Test_Safe_1 {

  public static void m1() {
    int j = 2;
    int k = j + 1;
    int l = k * 2;

    Store s = new Store(20, 20);
    if (0 <= j && j <= 3){
        s.get_delivery(j);
    }

    s.get_delivery(k);
    s.get_delivery(l);

    Store s2 = new Store(4, 4);
    s2.get_delivery(4);
  }
}