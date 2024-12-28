package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE


public class Test_For_5 {
    public static void m1(int j, int k) {
        Store s = new Store(10, 10);
        for(int i = 0; i < j; i++){
            for(int l = 0; l < k; l++){
                s.get_delivery(i);
            }
        }
    }
}
