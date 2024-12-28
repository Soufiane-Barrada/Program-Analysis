package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE UNSAFE

public class Test_While_2 {

    public static void m1() {
        int counter = 0;
        Store s = new Store(100, 100);
        while(counter < 100){
            counter = counter + 1;
            s.get_delivery(counter);
        }        
    }
}
