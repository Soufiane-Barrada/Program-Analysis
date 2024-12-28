package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE UNSAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Basic_Test_Unsafe_1 {

  public static void m1() {
    int j = 2;
    int k = j + 1;
    int l = k * 2;

    Store s = new Store(2, 4);
    if (0 <= j && j <= 3){
        s.get_delivery(j);
    }

    s.get_delivery(k);
    s.get_delivery(l);

    j = j - k;
    s.get_delivery(j);

    Store s2 = new Store(4, 4);
    s2.get_delivery(4);
  }
}