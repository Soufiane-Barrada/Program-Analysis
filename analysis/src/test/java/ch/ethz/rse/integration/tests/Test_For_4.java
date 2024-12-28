// DISABLED because somehow the test causes build failure on the pipeline sometimes
package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE UNSAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE UNSAFE

public class Test_For_4 {

    public static void m1(int j) {
        Store s1 = new Store(5, 5);
        for(int k = 0; k < 5; k++){
            for(int i = 0; i < 5; i++){
                if(j + i + k < 5){
                    s1.get_delivery(j + i + k);
                }
            }
        }
    }
}
