package davd33.aoc;

import com.google.common.io.Resources;
import davd33.aoc.domain.ElfDeviceTerminal;
import davd33.aoc.domain.ElfStuff;
import davd33.aoc.domain.ForestScan;
import io.vavr.Function0;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Tuple2;
import static java.lang.Integer.parseInt;

@Log4j2
public class App {

    public static void main(String[] args) {
        runDay9();
    }

    public static void runDay9() {
        URL inputUrl = Resources.getResource("input/d9-test");
        String input = Try.of(() -> Resources.toString(inputUrl, StandardCharsets.UTF_8)).get();

        Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>,
                Tuple2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>> computeHAntTPos = (newHPos, oldTPos) ->
                Tuple.of(Match(oldTPos).of(
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
                                })),
                        newHPos);

        Function2<
                Vector<Vector<Tuple2<Integer, Integer>>>,
                Tuple2<String, Integer>,
                Vector<Tuple2<Integer, Integer>>> fn =

                (Vector<Vector<Tuple2<Integer, Integer>>> s, Tuple2<String, Integer> action) ->

                        s.last().sliding(2, 2).map(knotPair ->
                                computeHAntTPos.apply(Match(action._1).of(
                                        Case($("R"), Tuple.of(knotPair.head()._1 + 1, knotPair.head()._2)),
                                        Case($("D"), Tuple.of(knotPair.head()._1, knotPair.head()._2 - 1)),
                                        Case($("L"), Tuple.of(knotPair.head()._1 - 1, knotPair.head()._2)),
                                        Case($("U"), Tuple.of(knotPair.head()._1, knotPair.head()._2 + 1))),
                                knotPair.last())._2).toVector();

        var part1Res = Vector.ofAll(input.lines())
                .map(l -> l.split(" "))
                .map(a -> Tuple.of(a[0], parseInt(a[1])))
                .foldLeft(Vector.of(Vector.fill(2, Tuple.of(0, 0))), (ropePositions, action) ->
                        Vector.fill(action._2, 0).foldLeft(ropePositions, (acc, __) ->
                                acc.append(fn.apply(acc, action))))
                .map(IndexedSeq::last)
                .toSet()
                .size();
        log.info("Part 1: {}", part1Res);
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
