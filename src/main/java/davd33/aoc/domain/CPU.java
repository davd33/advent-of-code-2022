package davd33.aoc.domain;

import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CPU {

    private int cycle;
    private int register;
    private Vector<SignalStrength> signalStrengthHistory;
    private Stream<Integer> monitoredCycles;

    public CPU() {
        this.cycle = 1;
        this.register = 1;
        this.signalStrengthHistory = Vector.empty();
        this.monitoredCycles = Stream.iterate(20, i -> i + 20);
    }

    public static Tuple2<String, Integer> parseInstruction(String instruction) {
        String[] parts = instruction.split(" ");
        return new Tuple2<>(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
    }

    public CPU noop() {
        nextCycle();
        logSignalStrength();
        return this;
    }

    public CPU addx(int x) {
        nextCycle();
        logSignalStrength();
        register += x;
        nextCycle();
        logSignalStrength();
        return this;
    }

    private void logSignalStrength() {
        if (isMonitoredCycle()) {
            signalStrengthHistory = signalStrengthHistory.append(new SignalStrength(cycle, register));
        }
    }

    public boolean isMonitoredCycle() {
        return monitoredCycles.takeUntil(c -> c > cycle).contains(cycle);
    }

    public void nextCycle() {
        System.out.print(
                (Vector.of(register, register -1, register +1).contains(Math.floorMod(cycle, 40)) ? "#" : ".") +
                        (cycle % 40 == 0 ? "\n" : ""));
        cycle++;
    }

    @Data
    public static class SignalStrength {
        private int cycle;
        private int signalStrength;
        private int register;
        public SignalStrength(int cycle, int register) {
            this.cycle = cycle;
            this.register = register;
            this.signalStrength = cycle * register;
        }
    }
}
