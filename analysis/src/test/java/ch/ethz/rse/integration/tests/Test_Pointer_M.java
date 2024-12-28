package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY UNSAFE
// FITS_IN_RESERVE UNSAFE

public class Test_Pointer_M {

    public static void m1(int j) {
        //It looks safe but the master solution said UNSAFE for fits_in_trolley and fits_in_reserve
        //So just checking all the possibility for each get_delivery might be an ok strategy.
        Store s;
        Store s1;
        
        if(j > 0){
            s = new Store(5, 5);
            s1 = new Store(10,10);
        }else{
            s = new Store(10, 10);
            s1 = new Store(5,5);
        }

        s1.get_delivery(1);
        if (j > 0){
            s.get_delivery(4);
        }else{
            s.get_delivery(7);
        }
    }   
}
