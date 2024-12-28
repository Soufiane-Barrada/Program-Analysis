package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Test_For_2 {

    public static void m1(int j) {
        int k = j + 1;
        Store s = new Store(10, 10);
        for(int i = 0; i < 20; i++){
            k = k + 1;
            s.get_delivery(i);
        }
        
    }
}
