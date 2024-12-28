// DISABLED
package ch.ethz.rse.integration.tests;
import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE UNSAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE UNSAFE

public class Test_M_5 {

    public static void m1(int c) {
        Store s = new Store(10, 20);
    
        for (int i = 1; i <= c; i++) {
            int a = 0, b = 1, sum = 0;
    
            for (int j = 1; j <= i; j++) {
                sum += a;
                int next = a + b;
                a = b;
                b = next;
            }
          if (sum <= 10) {
            s.get_delivery(sum);
          }
        }
    }
}
