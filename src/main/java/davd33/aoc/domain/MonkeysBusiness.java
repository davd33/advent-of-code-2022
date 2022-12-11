package davd33.aoc.domain;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import lombok.Data;

import java.math.BigInteger;

public class MonkeysBusiness {

    private static final Boolean DEBUG = true;

    private final Integer RELIEF_FACTOR;
    private Map<Integer, Monkey> monkeys;
    private final int loggingModulo;
    private final boolean logOnlyInspections;
    private final Vector<Integer> logTheseRounds;

    public MonkeysBusiness(int reliefFactor, int loggingModulo, boolean logOnlyInspections, Vector<Integer> logTheseRounds) {
        this.monkeys = HashMap.empty();
        this.RELIEF_FACTOR = reliefFactor;
        this.loggingModulo = loggingModulo;
        this.logOnlyInspections = logOnlyInspections;
        this.logTheseRounds = logTheseRounds;
    }

    public void addMonkey(Monkey monkey) {
        monkeys = monkeys.put(monkey.id, monkey);
    }

    public BigInteger computeMonkeyBusiness() {
        return this.getMonkeysNbOfInspectedItems()
                .map(Tuple2::_2)
                .map(BigInteger::valueOf)
                .sorted()
                .reverse()
                .take(2)
                .fold(BigInteger.ONE, BigInteger::multiply);
    }

    public void rounds(int n) {
        logMonkeys(0);
        Vector.range(1, n+1).forEach(this::round);
    }

    public Vector<Tuple2<Integer, Integer>> getMonkeysNbOfInspectedItems() {
        return monkeys.mapValues(m -> Tuple.of(m.id, m.countInspectedItems)).values().toVector();
    }

    private void round(int i) {
        Vector<BigInteger> divisible = monkeys.mapValues(Monkey::getDivisibleBy).values().toVector();
        monkeys.forEach(m -> m._2.getWorries().forEach(item -> {
            m._2.inspectNextItem();
            var worryLevel = m._2.getNewWorry(item, divisible).divide(BigInteger.valueOf(RELIEF_FACTOR));
            var receiverMonkey = monkeys.get(m._2.whichMonkeyToThrowTo(worryLevel)).get();
            monkeys = monkeys
                    .put(receiverMonkey.id,
                            receiverMonkey.addWorry(worryLevel))
                    .put(m._2.id, m._2.clearWorries());
        }));
        logMonkeys(i);
    }

    private void logMonkeys(int round) {
        if (!DEBUG) return;
        if (!logTheseRounds.contains(round) && round % loggingModulo != 0) return;

        System.out.println("Round " + round);
        monkeys.forEach(m -> System.out.printf("Monkey %d: %s%n", m._2.id,
                logOnlyInspections ? m._2.countInspectedItems : m._2.worries.map(Object::toString).intersperse(", ").mkString()));
        System.out.println();
    }
    
    @Data
    public static class Monkey {
        private int id;
        private Vector<BigInteger> worries;
        private Function1<BigInteger, BigInteger> newWorryFn;
        private Function1<BigInteger, Integer> throwToFn;
        private Vector<Integer> throwToMonkeys;
        private int countInspectedItems;
        private BigInteger divisibleBy;

        public Monkey(int id) {
            this.id = id;
            this.worries = Vector.empty();
            this.newWorryFn = i -> i;
            this.throwToFn = BigInteger::intValue;
            this.throwToMonkeys = Vector.empty();
        }

        public BigInteger getNewWorry(BigInteger worry, Vector<BigInteger> divisible) {
            var res = newWorryFn.apply(worry);
            BigInteger distinct = divisible.tail()
                    .foldLeft(divisible.head(), (acc, i) -> (i.multiply(acc)).divide(i.gcd(acc)));
            return res.mod(distinct);
        }

        public Monkey setDivisibleBy(int divisibleBy) {
            this.divisibleBy = BigInteger.valueOf(divisibleBy);
            return this;
        }

        public void inspectNextItem() {
            countInspectedItems++;
        }

        public Integer whichMonkeyToThrowTo(BigInteger worryLevel) {
            return throwToMonkeys.get(throwToFn.apply(worryLevel));
        }

        public Monkey setNewWorryFn(Function1<BigInteger, BigInteger> newWorryFn) {
            this.newWorryFn = newWorryFn;
            return this;
        }

        public Monkey setThrowToFn(Function1<BigInteger, Integer> throwToFn) {
            this.throwToFn = throwToFn;
            return this;
        }

        public static Monkey of(int id) {
            return new Monkey(id);
        }

        public Monkey clearWorries() {
            worries = Vector.empty();
            return this;
        }

        public Monkey addThrowToMonkey(int monkeyId) {
            throwToMonkeys = throwToMonkeys.append(monkeyId);
            return this;
        }

        public Monkey addWorry(BigInteger worry) {
            this.worries = this.worries.append(worry);
            return this;
        }

        public Monkey addWorries(Vector<BigInteger> worries) {
            this.worries = this.worries.appendAll(worries);
            return this;
        }
    }
}
