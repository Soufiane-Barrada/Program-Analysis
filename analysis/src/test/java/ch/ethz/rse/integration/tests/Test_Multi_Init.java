package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Test_Multi_Init {

    public static void m1(){
        Store s = new Store(10, 10);
        s.get_delivery(5);

        s = new Store(5, 5);
        s.get_delivery(6); 
    }
       
}
