package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE SAFE

public class Test_Pointer_M_2 {

    public static void m1(int j) {
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
            s.get_delivery(3);
        }else{
            s.get_delivery(4);
        }
    }   
}
