package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Basic_Test_Mixed_1 {
    public static void m1() {
        int volume = 10; 
        int i = 5;
        Store s1 = new Store(2, 4); 
        if (i < 2) {
            volume = -volume;
        } 
       
        s1.get_delivery(volume);

      }
}
