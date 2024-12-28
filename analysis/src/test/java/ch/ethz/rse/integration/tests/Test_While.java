package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Test_While {

    public static void m1(int j) {
        int k = j + 1;
        Store s1 = new Store(5, 5);
        if (k > 0){
            s1.get_delivery(k);
        }

        int l = 10;
        int m = l;
        s1.get_delivery(m);

        Store s = new Store(10, 10);
        while(j > 0){
            k = k + 1;
            s.get_delivery(j);
            j = j - 1;
        }        
    }
    
}
