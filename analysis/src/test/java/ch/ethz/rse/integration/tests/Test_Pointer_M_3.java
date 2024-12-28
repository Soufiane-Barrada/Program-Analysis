package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE SAFE

public class Test_Pointer_M_3 {
    public static void m1(int j) {
        Store s;
        
        if(j > 0){
            s = new Store(5, 5);
        }else{
            s = new Store(10, 10);
        }

        if (j > 0){
            s.get_delivery(1);
        }else if (j < -10){
            s.get_delivery(2);
        }else{
            s.get_delivery(3);
        }

        if (j > 0){
            s.get_delivery(1);
        }else{
            s.get_delivery(2);
        }
    }   
}
