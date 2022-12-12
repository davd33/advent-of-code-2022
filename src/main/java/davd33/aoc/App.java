package davd33.aoc;

import com.google.common.io.Resources;
import davd33.aoc.domain.*;
import davd33.aoc.domain.MonkeysBusiness.Monkey;
import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Tuple2;
import static java.lang.Integer.parseInt;

@Log4j2
public class App {

    public static void main(String[] args) {
        runDay12();
    }

    private static <T> T log(T value) {
        log.info(value);
        return value;
    }

    public static Vector<Tuple3<Tuple2<Integer, Integer>, Integer, Integer>> path(

            Function1<Tuple2<Integer, Integer>, Option<Character>> getChar,
            Tuple2<Tuple2<Integer, Integer>, Integer> from,
            TreeSet<Tuple2<Integer, Integer>> visited) {

        Function1<Tuple2<Tuple2<Integer, Integer>, Integer>,
                Vector<Tuple2<Tuple2<Tuple2<Integer, Integer>, Character>, Integer>>> streamFn = S -> Vector.of(
                        getChar.apply(Tuple.of(S._1._1 - 1, S._1._2)).map(c -> Tuple.of(Tuple.of(S._1._1 - 1, S._1._2), c)),
                        getChar.apply(Tuple.of(S._1._1 + 1, S._1._2)).map(c -> Tuple.of(Tuple.of(S._1._1 + 1, S._1._2), c)),
                        getChar.apply(Tuple.of(S._1._1, S._1._2 - 1)).map(c -> Tuple.of(Tuple.of(S._1._1, S._1._2 - 1), c)),
                        getChar.apply(Tuple.of(S._1._1, S._1._2 + 1)).map(c -> Tuple.of(Tuple.of(S._1._1, S._1._2 + 1), c)))
                .zipWithIndex()
                .filter(t -> t._1.isDefined())
                .map(t -> t.map1(Option::get))
                .foldLeft(Vector.<Tuple2<Tuple2<Tuple2<Integer, Integer>, Character>, Integer>>empty(),
                        (acc, t) -> t._1._2.equals('E') ? Vector.of(t) : (acc.exists(a -> a._1._2.equals('E')) ? acc : acc.append(t)))
                .filter(data -> getChar.apply(S._1).get().equals('S') || data._1._2.equals('E') || (
                        data._1._2.charValue() - getChar.apply(S._1).get().charValue() <= 1 &&
                                !visited.contains(data._1._1)));

        return streamFn
                .andThen(f -> Tuple.of(f, f.maxBy(t -> t._1._2)))
                .andThen(f -> f._1
                        .filter(t -> f._2.exists(t2 -> t2._1._2.equals(t._1._2)))
                        .map(posIndex -> posIndex.map1(Tuple2::_1))
                        .map(posIndex -> Tuple.of(posIndex._1, posIndex._2)))
                .andThen(f -> f
                        .map(t -> Tuple.of(t._1, t._2, visited.size() + 1))
                        .flatMap(pos -> path(getChar, Tuple.of(pos._1, pos._2), visited.add(pos._1)))).apply(from);
    }

    public static void runDay12() {
        URL inputUrl = Resources.getResource("input/d12-test");
        String input = Try.of(() -> Resources.toString(inputUrl, StandardCharsets.UTF_8)).get();
        Vector<Vector<Character>> map = Vector.of(input.split("\n")).map(s -> Vector.ofAll(s.toCharArray()));
        Vector<Tuple2<Integer, Integer>> S_E = map.zipWithIndex().flatMap(y -> y._1.zipWithIndex()
                        .map(x -> Vector.of('S', 'E').contains(x._1) ? Tuple.of(x._2, y._2) : null)
                        .filter(Objects::nonNull));

        Function1<Tuple2<Integer, Integer>, Option<Character>> getChar = (pos) ->
                pos._1 >= 0 && pos._1 < map.head().size() && pos._2 >= 0 && pos._2 < map.size() ?
                        Option.of(map.get(pos._2).get(pos._1)) : Option.none();

        Vector<Tuple3<Tuple2<Integer, Integer>, Integer, Integer>> path = path(getChar, Tuple.of(S_E.head(), -1), TreeSet.empty());

        map.zipWithIndex().forEach(y -> {
            y._1.zipWithIndex().forEach(x -> {
                var match = path.zipWithIndex().find(t -> t._1._1.equals(Tuple.of(x._2, y._2)));

                Character step = match.flatMap(m -> m._2 +1 < path.size() ? Some(path.get(m._2 +1)) : Option.none())
                        .map(t -> t.map2(direction -> Vector.of('<', '>', '^', 'v').get(direction)))
                        .map(t -> t._2)
                        .getOrElse(match.flatMap(m -> m._2 +1 >= path.size() ? Some('E') : Option.none())
                                .getOrElse('.'));

                System.out.print(step);
            });
            System.out.println();
        });

        for (Vector<Character> y : map) {
            System.out.println(y.toCharSeq());
        }

        log.info(path.size());
    }

    public static void runDay11() {
        URL inputUrl = Resources.getResource("input/d11");
        String input = Try.of(() -> Resources.toString(inputUrl, StandardCharsets.UTF_8)).get();

        final String MONKEY_PATTERN = "Monkey (\\d*):";
        final String START_ITEMS = "Starting items: ([0-9, ]+)?";
        final String OPERATION = "Operation: new = old ([-*/+]) (\\d+|old)";
        final String TEST = "Test: divisible by (\\d*)";
        final String IF = "If [a-z]+: throw to monkey (\\d*)";

        Vector<String> patterns = Vector.of(MONKEY_PATTERN, START_ITEMS, OPERATION, TEST, IF);

        Vector<Monkey> monkeys = Vector.ofAll(input.lines())
                .foldLeft(Vector.<Monkey>empty(), (acc, line) -> patterns.map(p -> Pattern.compile(p).matcher(line))
                        .filter(Matcher::find)
                        .headOption()
                        .map(m -> Match(m.pattern().pattern()).of(
                                Case($(MONKEY_PATTERN),
                                        () -> acc.append(Monkey.of(parseInt(m.group(1))))),
                                Case($(START_ITEMS), () -> acc.update(acc.size() - 1, acc.last().addWorries(
                                        Vector.of(m.group(1).split(", ")).map(Integer::parseInt).map(BigInteger::valueOf)))),
                                Case($(OPERATION), () -> acc.update(acc.size() - 1, acc.last().setNewWorryFn(Match(m.group(1)).of(
                                        Case($("*"), __ -> old ->
                                                old.multiply(m.group(2).equals("old") ?
                                                        old : BigInteger.valueOf(parseInt(m.group(2))))),
                                        Case($("+"), __ -> old ->
                                                old.add(BigInteger.valueOf(parseInt(m.group(2)))))
                                )))),
                                Case($(TEST), () -> acc.update(acc.size() - 1, acc.last()
                                        .setDivisibleBy(parseInt(m.group(1)))
                                        .setThrowToFn(worry ->
                                                worry.mod(BigInteger.valueOf(parseInt(m.group(1)))).equals(BigInteger.ZERO) ?
                                                        0 : 1))),
                                Case($(IF), () -> acc.update(acc.size() - 1, acc.last().addThrowToMonkey(parseInt(m.group(1))))),
                                Case($(), () -> acc)))
                        .getOrElse(acc));

        final MonkeysBusiness mb2 = new MonkeysBusiness(1, 1000, true, Vector.of(1, 19, 20));
        monkeys.forEach(mb2::addMonkey);
        mb2.rounds(10000);
        log.info("Result = {}", mb2.computeMonkeyBusiness());
    }

    public static void runDay10() {
        URL inputUrl = Resources.getResource("input/d10");
        String input = Try.of(() -> Resources.toString(inputUrl, StandardCharsets.UTF_8)).get();
        Integer part1res = Vector.ofAll(input.lines())
                .foldLeft(new CPU(), (cpu, line) -> Match(CPU.parseInstruction(line)).of(
                        Case($Tuple2($("noop"), $()), cpu::noop),
                        Case($Tuple2($("addx"), $(instanceOf(Integer.class))), (__, x) -> cpu.addx(x))))
                .getSignalStrengthHistory()
                .filter(s -> Vector.of(20, 60, 100, 140, 180, 220).contains(s.getCycle()))
                .foldLeft(0, (acc, s) -> acc + s.getSignalStrength());
        log.info("Part 1: {}", part1res);
    }

    public static void runDay9() {
        URL inputUrl = Resources.getResource("input/d9");
        String input = Try.of(() -> Resources.toString(inputUrl, StandardCharsets.UTF_8)).get();

        Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> computeHAntTPos = (newHPos, oldTPos) ->
                Match(oldTPos).of(
                        // on same horizontal line - left
                        Case($Tuple2($((Integer x) -> x < (newHPos._1) - 1), $(newHPos._2)),
                                (x, y) -> {
                                    log.debug("horizontal left \n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(x + 1, y);
                                }),
                        // on same horizontal line - right
                        Case($Tuple2($((Integer x) -> x > (newHPos._1) + 1), $(newHPos._2)),
                                (x, y) -> {
                                    log.debug("horizontal right \n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(x - 1, y);
                                }),

                        // on same vertical line - up
                        Case($Tuple2($(newHPos._1), $((Integer y) -> y > (newHPos._2) + 1)),
                                (x, y) -> {
                                    log.debug("vertical up \n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(x, y - 1);
                                }),
                        // on same vertical line - down
                        Case($Tuple2($(newHPos._1), $((Integer y) -> y < (newHPos._2) - 1)),
                                (x, y) -> {
                                    log.debug("vertical down \n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(x, y + 1);
                                }),

                        // on diagonal up left
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 < (newHPos._1 - 1) && xy._2 < (newHPos._2 - 1)),
                                (xy) -> {
                                    log.debug("diag up left\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 + 1, xy._2 + 1);
                                }),

                        // on diagonal down left
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 < (newHPos._1 - 1) && xy._2 > (newHPos._2 + 1)),
                                (xy) -> {
                                    log.debug("diag down left\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 + 1, xy._2 - 1);
                                }),

                        // on diagonal up right
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 > (newHPos._1 + 1) && xy._2 < (newHPos._2 - 1)),
                                (xy) -> {
                                    log.debug("diag up right\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 - 1, xy._2 + 1);
                                }),

                        // on diagonal down right
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 > (newHPos._1 + 1) && xy._2 > (newHPos._2 + 1)),
                                (xy) -> {
                                    log.debug("diag down right\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 - 1, xy._2 - 1);
                                }),


                        // on diagonal up left
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 < (newHPos._1 - 1) && xy._2 < (newHPos._2)),
                                (xy) -> {
                                    log.debug("diag up left\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 + 1, xy._2 + 1);
                                }),

                        // on diagonal down left
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 < (newHPos._1 - 1) && xy._2 > (newHPos._2)),
                                (xy) -> {
                                    log.debug("diag down left\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 + 1, xy._2 - 1);
                                }),

                        // on diagonal up right
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 > (newHPos._1 + 1) && xy._2 < (newHPos._2)),
                                (xy) -> {
                                    log.debug("diag up right\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 - 1, xy._2 + 1);
                                }),

                        // on diagonal down right
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 > (newHPos._1 + 1) && xy._2 > (newHPos._2)),
                                (xy) -> {
                                    log.debug("diag down right\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 - 1, xy._2 - 1);
                                }),


                        // on diagonal up left
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 < (newHPos._1) && xy._2 < (newHPos._2 - 1)),
                                (xy) -> {
                                    log.debug("diag up left\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 + 1, xy._2 + 1);
                                }),

                        // on diagonal down left
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 < (newHPos._1) && xy._2 > (newHPos._2 + 1)),
                                (xy) -> {
                                    log.debug("diag down left\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 + 1, xy._2 - 1);
                                }),

                        // on diagonal up right
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 > (newHPos._1) && xy._2 < (newHPos._2 - 1)),
                                (xy) -> {
                                    log.debug("diag up right\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 - 1, xy._2 + 1);
                                }),

                        // on diagonal down right
                        Case($((Tuple2<Integer, Integer> xy) -> xy._1 > (newHPos._1) && xy._2 > (newHPos._2 + 1)),
                                (xy) -> {
                                    log.debug("diag down right\n{} - {}", oldTPos, newHPos);
                                    return Tuple.of(xy._1 - 1, xy._2 - 1);
                                }),

                        // default
                        Case($(), () -> {
                            log.debug("T unchanged\n{} - {}", oldTPos, newHPos);
                            return oldTPos;
                        }));

        var res = Vector.ofAll(input.lines())
                .map(l -> l.split(" "))
                .map(a -> Tuple.of(a[0], parseInt(a[1])))
                .foldLeft(Tuple.of(HashSet.empty(), Vector.fill(10, Tuple.of(0, 0))), (tailPosAndRopePositions, action) ->
                        Vector.fill(action._2, 0).foldLeft(tailPosAndRopePositions, (acc, __) -> {
                            Vector<Tuple2<Integer, Integer>> newPositions = acc._2.foldLeft(Vector.empty(), (positions, knot) -> {

                                if (positions.isEmpty()) {
                                    var hKnot = positions.isEmpty() ? knot : positions.last();
                                    Tuple2<Integer, Integer> newHPos = Match(action._1).of(
                                            Case($("R"), Tuple.of(hKnot._1 + 1, hKnot._2)),
                                            Case($("D"), Tuple.of(hKnot._1, hKnot._2 - 1)),
                                            Case($("L"), Tuple.of(hKnot._1 - 1, hKnot._2)),
                                            Case($("U"), Tuple.of(hKnot._1, hKnot._2 + 1)));
                                    return positions.append(newHPos);
                                }

                                return positions.append(computeHAntTPos.apply(positions.last(), knot));
                            });
                            return Tuple.of(acc._1.add(newPositions.last()), newPositions);
                        }))
                ._1
                .size();

        log.info("Part 1: {}", res);
    }

    public static void runDay8() {
        URL inputUrl = Resources.getResource("input/d8");
        String input = Try.of(() -> Resources.toString(inputUrl, StandardCharsets.UTF_8)).get();

        String[] lines = input.lines().toArray(String[]::new);
        ForestScan forestScan = new ForestScan(lines);
        log.info("Day 8 - Part 1: {}", forestScan.countTrees());
        log.info("Day 8 - Part 2: {}", forestScan.highestScenicScore());
    }

    public static void runDay7() {
        final URL url = Resources.getResource("input/d7");
        final String input = Try.of(() -> Resources.toString(url, StandardCharsets.UTF_8))
                .onFailure(log::error)
                .get();
        ElfDeviceTerminal elfDeviceTerminal = Vector.ofAll(input.lines())
                .tail()
                .foldLeft(ElfDeviceTerminal.newRoot(), (acc, line) ->
                        Try.of(() -> acc.parseLine(line)).get());

        log.info("Part 1 - \n{}", elfDeviceTerminal.getRoot()
                .collectSizes()
                .filter(t -> ElfDeviceTerminal.Type.directory.equals(t._3) && t._2 <= 100000)
                .foldLeft(0, (acc, t) -> acc + t._2));

        int SPACE_LEFT = 70000000 - elfDeviceTerminal.getRoot().totalSize();
        int INCREASE_NEEDED = 30000000 - SPACE_LEFT;

        log.info("Part 2 - \n{}", elfDeviceTerminal.getRoot()
                .collectSizes()
                .sortBy(t -> t._2)
                .find(t -> ElfDeviceTerminal.Type.directory.equals(t._3) && t._2 >= INCREASE_NEEDED));
    }

    private static void runDay6() {
        final URL url = Resources.getResource("input/d6");
        final String input = Try.of(() -> Resources.toString(url, StandardCharsets.UTF_8))
                .onFailure(log::error)
                .get();
        final int START_OF_PACKET_MARKER = 4;
        final int START_OF_MSG_MARKER = 14;
        Vector.ofAll(Vector.ofAll(input.lines()).head().toCharArray())
                .sliding(START_OF_PACKET_MARKER)
                .zipWithIndex()
                .find(s -> s._1.size() == s._1.toSet().size())
                .map(s -> s._2 + START_OF_PACKET_MARKER)
                .forEach(i -> log.info("Part 1: {}", i));
        Vector.ofAll(Vector.ofAll(input.lines()).head().toCharArray())
                .sliding(START_OF_MSG_MARKER)
                .zipWithIndex()
                .find(s -> s._1.size() == s._1.toSet().size())
                .map(s -> s._2 + START_OF_MSG_MARKER)
                .forEach(i -> log.info("Part 2: {}", i));
    }

    private static void runDay5() {
        final URL url = Resources.getResource("input/d5");
        final String input = Try.of(() -> Resources.toString(url, StandardCharsets.UTF_8))
                .onFailure(log::error)
                .get();
        final String[] stackActionsInput = input.split(" 1   2   3");
        Vector<String> stackInput = stackActionsInput[0].lines().collect(Vector.collector());
        HashMap<Integer, List<Character>> stacks = stackInput.foldRight(HashMap.empty(), (line, acc) ->
                Vector.ofAll(line.toCharArray()).grouped(4)
                        .zipWithIndex()
                        .map(charsIndex -> Tuple.of(charsIndex._2, List.of(charsIndex._1.get(1))))
                        .filter(t -> t._2.head() != ' ')
                        .foldRight(acc, (crate, acc2) -> acc2.put(crate._1,
                                acc2.getOrElse(crate._1, List.empty()).push(crate._2.head()))));
        Vector<String> actionInput = stackActionsInput[1].lines().collect(Vector.collector());
        String part1Result = actionInput.filter(l -> !l.isBlank()).foldLeft(stacks, (newStacks, line) -> {
                    log.debug("{}", line);
                    // move {how many} from {stack} to {stack}
                    Pattern p = Pattern.compile("move ([0-9]+) from ([0-9]+) to ([0-9]+)");
                    Matcher m = p.matcher(line);

                    if (m.find())
                        return Vector.fill(parseInt(m.group(1)), 0).foldLeft(newStacks, (acc, __) -> {
                            int from = parseInt(m.group(2)) - 1;
                            int to = parseInt(m.group(3)) - 1;
                            return acc
                                    .put(from, acc.get(from).get().tail())
                                    .put(to, acc.get(to).get().push(acc.get(from).get().head()));
                        });
                    else
                        return newStacks;
                })
                .foldLeft("", (acc, e) -> acc + e._2.head());
        log.info("Part 1: {}", part1Result);

        String part2Result = actionInput.filter(l -> !l.isBlank()).foldLeft(stacks, (newStacks, line) -> {
                    log.debug("{}", line);
                    // move {how many} from {stack} to {stack}
                    Pattern p = Pattern.compile("move ([0-9]+) from ([0-9]+) to ([0-9]+)");
                    Matcher m = p.matcher(line);

                    if (m.find()) {
                        int howMany = parseInt(m.group(1));
                        int from = parseInt(m.group(2)) - 1;
                        int to = parseInt(m.group(3)) - 1;
                        var res = newStacks
                                .put(from, newStacks.get(from).get()
                                        .drop(howMany))
                                .put(to, newStacks.get(to).get()
                                        .prependAll(newStacks.get(from).get().take(howMany)));
                        res.forEach((k, v) -> log.debug("{}: {}", k, v));
                        return res;
                    } else {
                        newStacks.forEach((k, v) -> log.debug("{}: {}", k, v));
                        return newStacks;
                    }
                })
                .foldLeft("", (acc, e) -> acc + e._2.head());
        log.info("Part 2: {}", part2Result);
    }

    private static void runDay4() {
        URL day2URL = Resources.getResource("input/d4");
        Try<String> inputTry = Try.of(() -> Resources.toString(day2URL, StandardCharsets.UTF_8));

        Try<Vector<Tuple2<Set<Integer>, Set<Integer>>>> pairOfElvesRanges = inputTry.map(s -> Vector.of(s.split("\n")))
                .map(lines -> lines.map(line -> line.split(","))
                        .map(elfRanges -> Tuple.of(
                                Tuple.of(
                                        parseInt(elfRanges[0].split("-")[0]),
                                        parseInt(elfRanges[0].split("-")[1])),
                                Tuple.of(
                                        parseInt(elfRanges[1].split("-")[0]),
                                        parseInt(elfRanges[1].split("-")[1]))))
                        .map(elfPairRanges -> elfPairRanges
                                .map1(range1 -> Stream.from(range1._1).takeUntil(i -> i > range1._2).toSet())
                                .map2(range2 -> Stream.from(range2._1).takeUntil(i -> i > range2._2).toSet())));

        pairOfElvesRanges.map(p -> p
                        .filter(elfPair -> elfPair._1.containsAll(elfPair._2) || elfPair._2.containsAll(elfPair._1))
                        .size())
                .forEach(log::info);

        pairOfElvesRanges.map(p -> p
                        .filter(elfPair -> !elfPair._1.intersect(elfPair._2).isEmpty())
                        .size())
                .forEach(log::info);
    }

    private static void runDay3() {
        final Map<Character, Integer> priorities = Vector.ofAll("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        .toCharArray())
                .zipWithIndex().toMap(t -> Tuple.of(t._1, t._2 + 1));

        URL day2URL = Resources.getResource("input/d3");
        Try<String> inputTry = Try.of(() -> Resources.toString(day2URL, StandardCharsets.UTF_8));

        inputTry.map(s -> Vector.of(s.split("\n"))).map(lines ->
                        lines.map(line ->
                                        Vector.ofAll(line.toCharArray()).zipWithIndex().sortBy(t -> t._2)
                                                .takeUntil(charIndexTuple -> charIndexTuple._2 == (line.length() / 2))
                                                .map(t -> t._1))
                                .zipWith(lines.map(line ->
                                                Vector.ofAll(line.toCharArray()).zipWithIndex().sortBy(t -> t._2)
                                                        .dropUntil(charIndexTuple -> charIndexTuple._2 == (line.length() / 2))
                                                        .map(t -> t._1)),
                                        Tuple2::new)
                                .flatMap(firstSecondRucksack -> firstSecondRucksack._1
                                        .filter(firstSecondRucksack._2::contains)
                                        .distinct()
                                        .map(priorities::get)
                                        .filter(Option::isDefined)
                                        .map(Option::get))
                                .sum())
                .forEach(log::info);

        inputTry.map(input -> input.split("\n"))
                .map(lines -> Vector.ofAll(Vector.of(lines).grouped(3))
                        .map(elfGroup -> elfGroup.map(elfSack -> Vector.ofAll(elfSack.toCharArray()).toSet())
                                .fold(Set(), (acc, set) -> acc.isEmpty() ? set : set.intersect(acc))
                                .map(priorities::get)
                                .filter(Option::isDefined)
                                .map(Option::get)
                                .sum())
                        .sum())
                .forEach(log::info);
    }

    private static void runDay2() {
        final String OPPONENT_ROCK = "A";
        final String OPPONENT_PAPER = "B";
        final String OPPONENT_SCISSOR = "C";
        final String ROCK = "X";
        final String PAPER = "Y";
        final String SCISSOR = "Z";
        final int LOOSE = 0;
        final int DRAW = 3;
        final int WIN = 6;

        Function2<String, String, Integer> roundScore = (opponentShape, selectedShape) ->
                Match(Tuple.of(opponentShape, selectedShape)).of(
                        Case($Tuple2($(OPPONENT_ROCK), $(ROCK)), DRAW),
                        Case($Tuple2($(OPPONENT_ROCK), $(PAPER)), WIN),
                        Case($Tuple2($(OPPONENT_ROCK), $(SCISSOR)), LOOSE),

                        Case($Tuple2($(OPPONENT_PAPER), $(ROCK)), LOOSE),
                        Case($Tuple2($(OPPONENT_PAPER), $(PAPER)), DRAW),
                        Case($Tuple2($(OPPONENT_PAPER), $(SCISSOR)), WIN),

                        Case($Tuple2($(OPPONENT_SCISSOR), $(ROCK)), WIN),
                        Case($Tuple2($(OPPONENT_SCISSOR), $(PAPER)), LOOSE),
                        Case($Tuple2($(OPPONENT_SCISSOR), $(SCISSOR)), DRAW));

        URL day2URL = Resources.getResource("input/d2");
        Try<String> inputTry = Try.of(() -> Resources.toString(day2URL, StandardCharsets.UTF_8));

        // part 1
        inputTry
                .map(input -> Vector.of(input.split("\n"))
                        .map(l -> l.split(" ")).map(shapes ->
                                Match(shapes[1]).of(
                                        Case($(ROCK), 1),
                                        Case($(PAPER), 2),
                                        Case($(SCISSOR), 3))
                                        +
                                        roundScore.apply(shapes[0], shapes[1]))
                        .sum().intValue())
                .forEach(score -> log.info("---RESULT = {}---", score));

        // part 2
        inputTry
                .map(input -> Vector.of(input.split("\n"))
                        .map(l -> l.split(" "))
                        .map(shapes -> Option.of(Match(shapes[1]).of(
                                        Case($(ROCK /*need loose*/), Match(shapes[0]).of(
                                                Case($(OPPONENT_ROCK), SCISSOR),
                                                Case($(OPPONENT_PAPER), ROCK),
                                                Case($(OPPONENT_SCISSOR), PAPER))),
                                        Case($(PAPER /*need draw*/), Match(shapes[0]).of(
                                                Case($(OPPONENT_ROCK), ROCK),
                                                Case($(OPPONENT_PAPER), PAPER),
                                                Case($(OPPONENT_SCISSOR), SCISSOR))),
                                        Case($(SCISSOR /*need win*/), Match(shapes[0]).of(
                                                Case($(OPPONENT_ROCK), PAPER),
                                                Case($(OPPONENT_PAPER), SCISSOR),
                                                Case($(OPPONENT_SCISSOR), ROCK)))))
                                .map(myNewShape -> Match(myNewShape).of(
                                        Case($(ROCK), 1),
                                        Case($(PAPER), 2),
                                        Case($(SCISSOR), 3))
                                        +
                                        roundScore.apply(shapes[0], myNewShape))
                                .get())
                        .sum().intValue())
                .forEach(score -> log.info("---RESULT PART 2 = {}---", score));

    }

    private static void runDay1() {
        List<ElfStuff> elfStuffs = top3ElvesCalories();
        log.info(elfStuffs.head());
        log.info(elfStuffs.map(ElfStuff::getCalories).sum());
    }

    private static List<ElfStuff> top3ElvesCalories() {
        URL day1URL = Resources.getResource("input/d1");
        return Try.of(() -> Resources.toString(day1URL, StandardCharsets.UTF_8))
                .map(s -> Vector.of(s.split("\n"))
                        .foldLeft(List.<ElfStuff>empty(), (acc, line) -> line.isBlank() ?
                                acc.push(ElfStuff.of()) :
                                acc.tail().push(acc.head().addItem(parseInt(line))))
                        .sortBy(ElfStuff::getCalories)
                        .takeRight(3)
                        .reverse())
                .onFailure(log::error)
                .get();
    }
}
