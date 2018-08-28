public class DivMod {
    static int[] nums = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static int loc = 0;
    static int max = 0;

    public static void main(String[] args) {

        nums[0] = 32;
        nums[2] = 10;

        //divmod algorithm
        while (nums[loc] != 0) {
           dec();
           right();
           inc();
           right();
           dec();
           while (nums[loc] != 0) {
               right();
               inc();
               right();
               right();
           }
           right();
           while (nums[loc] != 0) {
               inc();
               while (nums[loc] != 0) {
                   dec();
                   left();
                   inc();
                   right();
               }
               right();
               inc();
               right();
               right();
           }
           left();
           left();
           left();
           left();
           left();
           left();
        }

        System.out.println(nums[0]);
        System.out.println(nums[1]);
        System.out.println(nums[2]);
        System.out.println(nums[3]);
        System.out.println(nums[4]);
        System.out.println(nums[5]);
        System.out.println(max);
    }

    static void dec() {
        nums[loc]--;
    }

    static void inc() {
        nums[loc]++;
    }

    static void right() {
        loc++;
        if (loc > max)
            max = loc;
    }

    static void left() {
        loc--;
    }
}
