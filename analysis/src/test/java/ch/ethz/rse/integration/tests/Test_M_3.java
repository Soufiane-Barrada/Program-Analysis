package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE UNSAFE

public class Test_M_3 {

    public static void m1(int a, int b, int c) {
        Store s = new Store(5, 20);
        for (int i = 0; i < c; i++) {
            if (a >= 0 && a <= 5 && b >= 0 && b <= 5) {
                s.get_delivery(a);
                s.get_delivery(b);
              }
        }

        a = b + 1;
        c = a + 1;
        if(a > b && b < c){
            s.get_delivery(1);
        } else {
            s.get_delivery(100);
        }

        if(a >= b && b <= c){
            s.get_delivery(1);
        } else {
            s.get_delivery(100);
        }
    }

    public static void m2(int a, int b, int c) {
        Store s = new Store(5, 20);
        for (int i = 0; i < c; i++) {
            if (a >= 0 && a <= 5 && b >= 0 && b <= 5) {
                s.get_delivery(a);
                s.get_delivery(b);
              }
        }

        a = b + 1;
        c = a + 1;
        if(a > b && b < c){
            s.get_delivery(1);
        } else {
            s.get_delivery(100);
        }

        if(a >= b && b <= c){
            s.get_delivery(1);
        } else {
            s.get_delivery(100);
        }
    }
}
