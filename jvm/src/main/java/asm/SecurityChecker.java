package asm;

public class SecurityChecker {
    public static boolean main(String[] args) {
        System.out.println("SecurityChecker.checkSecurity ...");
        if ((System.currentTimeMillis() & 0x1) == 0){
            return false;
        }else{
            return true;
        }
    }

}