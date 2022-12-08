package davd33.aoc.domain;

import io.vavr.Function2;
import io.vavr.collection.Vector;

import static java.lang.Integer.parseInt;

public class ForestScan {
    private final Vector<String> map;
    private final int width;
    private final int height;

    public ForestScan(String[] map) {
        this.map = Vector.of(map);
        this.width = map[0].length();
        this.height = map.length;
    }

    private boolean isTreeVisible(int x, int y) {
        int tree = parseInt(map.get(y).charAt(x) + "");
        Vector<Integer> treesUp = Vector.range(0, y)
                .map(i -> parseInt(map.get(i).charAt(x) + ""));
        Vector<Integer> treesDown = Vector.range(y, height)
                .drop(1)
                .map(i -> parseInt(map.get(i).charAt(x) + ""));
        Vector<Integer> treesLeft = Vector.range(0, x)
                .map(i -> parseInt(map.get(y).charAt(i) + ""));
        Vector<Integer> treesRight = Vector.range(x, width)
                .drop(1)
                .map(i -> parseInt(map.get(y).charAt(i) + ""));
        boolean res = !treesUp.exists(t -> t >= tree) ||
                !treesDown.exists(t -> t >= tree) ||
                !treesLeft.exists(t -> t >= tree) ||
                !treesRight.exists(t -> t >= tree);
        return res;
    }

    public int countTrees() {
        // for every tree, check if it's visible from every side
        return map.zipWithIndex()
                .map(treesY -> Vector.ofAll(treesY._1.toCharArray())
                        .zipWithIndex()
                        .count(treesX -> isTreeVisible(treesX._2, treesY._2)))
                .sum().intValue();
    }

    private int computeScenicScore(int x, int y) {
        int tree = parseInt(map.get(y).charAt(x) + "");

        Vector<Integer> treesUp = Vector.range(0, y)
                .map(i -> parseInt(map.get(i).charAt(x) + ""))
                .reverse();
        Vector<Integer> treesUpTaken = treesUp.takeWhile(t -> t < tree);

        Vector<Integer> treesDown = Vector.range(y, height)
                .drop(1)
                .map(i -> parseInt(map.get(i).charAt(x) + ""));
        Vector<Integer> treesDownTaken = treesDown.takeWhile(t -> t < tree);

        Vector<Integer> treesLeft = Vector.range(0, x)
                .map(i -> parseInt(map.get(y).charAt(i) + ""))
                .reverse();
        Vector<Integer> treesLeftTaken = treesLeft.takeWhile(t -> t < tree);

        Vector<Integer> treesRight = Vector.range(x, width)
                .drop(1)
                .map(i -> parseInt(map.get(y).charAt(i) + ""));
        Vector<Integer> treesRightTaken = treesRight.takeWhile(t -> t < tree);

        return
                (treesUp.size() == treesUpTaken.size() ? treesUpTaken.size() : treesUpTaken.size() + 1) *
                (treesDown.size() == treesDownTaken.size() ? treesDownTaken.size() : treesDownTaken.size() + 1) *
                (treesLeft.size() == treesLeftTaken.size() ? treesLeftTaken.size() : treesLeftTaken.size() + 1) *
                (treesRight.size() == treesRightTaken.size() ? treesRightTaken.size() : treesRightTaken.size() + 1);
    }

    public int highestScenicScore() {
        return map.zipWithIndex()
                .flatMap(treesY -> Vector.ofAll(treesY._1.toCharArray())
                        .zipWithIndex()
                        .map(treesX -> computeScenicScore(treesX._2, treesY._2)))
                .max().get();
    }
}
