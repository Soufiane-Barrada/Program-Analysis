package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Test_M_1 {

    public static void m1(short j) {
        Store s = new Store(5, 5);
        if (j > 0){
            s.get_delivery(j);
        }
    }
    
}
