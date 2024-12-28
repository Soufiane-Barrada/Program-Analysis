package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Test_For {

    public static void m1(int j) {

        Store s = new Store(10, 10);
        for(int i = 0; i < j; i++){
            s.get_delivery(i);
        }

        
    }
}
