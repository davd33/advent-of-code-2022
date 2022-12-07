package davd33.aoc.domain;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.lang.Integer.parseInt;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElfDeviceTerminal {

    private ElfDeviceTerminal root;

    private ElfDeviceTerminal parent;
    private Vector<ElfDeviceTerminal> children;
    private String name;
    private Type type;
    private int size;

    public ElfDeviceTerminal addChild(ElfDeviceTerminal e) {
        this.children = this.children.append(e);
        return this;
    }

    public Vector<Tuple3<String, Integer, Type>> collectSizes() {
        return this.getChildren()
                .foldLeft(Vector.empty(), (acc, terminal) -> terminal.collectSizes().appendAll(
                        acc.append(Tuple.of(terminal.getName(), terminal.totalSize(), terminal.getType()))));
    }

    public Integer totalSize() {
        if (Type.file.equals(type)) {
            return size;
        } else {
            return children
                    .foldLeft(0, (acc, terminal) -> acc + terminal.totalSize());
        }
    }

    public ElfDeviceTerminal parseLine(String line) throws ElfTerminalException {
        if (line.startsWith("$")) {
            // command
            Vector<String> command = Vector.of(line.split(" ")).filter(s -> !s.isBlank() && !s.equals("$"));
            if (command.head().equals("cd")) {
                String dest = command.tail().head();
                return dest.equals("..") ? this.parent : this.children.find(t -> t.name.equals(dest))
                        .getOrElseThrow(() -> new ElfTerminalException("CD - could not find dir - '" +
                                command.intersperse(" ").toCharSeq() + "'"));
            } else {
                return this;
            }
        } else {
            // listing
            Vector<String> lsEntry = Vector.of(line.split(" ")).filter(s -> !s.isBlank());
            return Try.of(() -> parseInt(lsEntry.head()))
                    .map(size -> this.addChild(this.newFile(lsEntry.tail().head(), size)))
                    .recover(NumberFormatException.class, __ -> this.addChild(
                            this.newDir(lsEntry.tail().head())))
                    .get();
        }
    }

    public static ElfDeviceTerminal newRoot() {
        var root = new ElfDeviceTerminal();
        root.root = root;
        root.parent = null;
        root.children = Vector.empty();
        root.name = "/";
        root.type = Type.directory;
        root.size = 0;
        return root;
    }

    protected ElfDeviceTerminal newDir(String name) {
        return new ElfDeviceTerminal(this.root, this, Vector.empty(), name, Type.directory, 0);
    }

    protected ElfDeviceTerminal newFile(String name, int size) {
        return new ElfDeviceTerminal(this.root, this, Vector.empty(), name, Type.file, size);
    }

    public enum Type {
        directory, file
    }

    public static class ElfTerminalException extends Exception {

        public ElfTerminalException(String message) {
            super(message);
        }
    }
}
