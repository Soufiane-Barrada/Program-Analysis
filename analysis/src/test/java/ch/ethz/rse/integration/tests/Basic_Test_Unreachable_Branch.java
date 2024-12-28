package ch.ethz.rse.integration.tests;

import ch.ethz.rse.Store;

// expected results:
// NON_NEGATIVE SAFE
// FITS_IN_TROLLEY SAFE
// FITS_IN_RESERVE SAFE

public class Basic_Test_Unreachable_Branch {

    public void m2(int value, int value2, int value3) {
        Store s = new Store(2, 10);
        if(value >= value2){
            s.get_delivery(2);
        } else if (value == value2) {
            s.get_delivery(value + value2 * value3); //safe because it does not reach here
        } else if (value > value2) {
            s.get_delivery(-2);//safe because it does not reach here
        } else if (value <= value2) {
            s.get_delivery(2);
        } else if (value < value2) {
            s.get_delivery((value + value2) * value3);//safe because it does not reach here
        } else if (value != value2) {
            s.get_delivery(100);//safe because it does not reach here
        }

        if(value >= value3){
            s.get_delivery(2);
        } else if (value == value3) {
            s.get_delivery(value2 * value3 - value); //safe because it does not reach here
        } else if (value > value3) {
            s.get_delivery(-2);//safe because it does not reach here
        } else if (value <= value3) {
            s.get_delivery(2);
        } else if (value < value3) {
            s.get_delivery(100);//safe because it does not reach here
        } else if (value != value3) {
            s.get_delivery(100);//safe because it does not reach here
        }
    }
}