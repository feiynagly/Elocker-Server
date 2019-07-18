package test;

import java.util.ArrayList;
import java.util.List;

public class Test1 {
    public static void main(String args[]) {
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }

        OpList opList = new OpList(numbers);
        opList.add();
        for (int i : numbers) {
            System.out.println("number : " + i);
        }

    }

    private static class OpList {
        private List<Integer> numbers;

        public OpList(List<Integer> numbers) {
            this.numbers = numbers;
        }

        public void add() {
            for (int i = 100; i < 110; i++) {
                numbers.add(i);
            }
        }
    }
}
