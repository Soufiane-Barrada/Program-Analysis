package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE SAFE

public class Basic_Test_Safe_3 {

  public static void m1() {
    int j = 2;
    int k = j;
    int l = k * 2;

    Store s1 = new Store(2, 4);
    Store s2 = s1;

    if (j == k){
        s2.get_delivery(j);
    } else {
        s1.get_delivery(3 * j);
        s2 = new Store(1, 1);
    }

    s2.get_delivery(2);
  }
}