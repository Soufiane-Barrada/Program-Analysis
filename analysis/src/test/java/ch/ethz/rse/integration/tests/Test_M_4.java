package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE SAFE

public class Test_M_4 {

    public static void m1(int a, int b, int c) {
        Store s = new Store(5, 20);
        if (c < 2){
            for (int i = 0; i < c; i++) {
                if (a >= 0 && a <= 5 && b >= 0 && b <= 5) {
                    s.get_delivery(a);
                    s.get_delivery(b);
                  }
            }
        }else{
            c = 2;
            while (c > 0) {
                if (a >= 0 && a <= 5 && b >= 0 && b <= 5) {
                    s.get_delivery(a);
                    s.get_delivery(b);
                }
                c--;
            }
        }
    }
}
