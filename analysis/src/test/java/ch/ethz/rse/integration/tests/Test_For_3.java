package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE UNSAFE

public class Test_For_3 {

    public static void m1(int j) {
        Store s1 = new Store(5, 5);
        for(int i = 0; i < 5; i++){
            if(j + i == 5){
                s1.get_delivery(j + i);
            }
        }
    }
}
