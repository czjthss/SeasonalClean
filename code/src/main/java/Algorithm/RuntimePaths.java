package Algorithm;

import java.nio.file.Files;
import java.nio.file.Path;

final class RuntimePaths {
    private RuntimePaths() {}

    static Path algorithmDirectory(String child) {
        Path current = Path.of("").normalize();
        Path fromCode = current.resolve("src/main/java/Algorithm").resolve(child);
        if (Files.isDirectory(fromCode)) {
            return fromCode;
        }

        Path fromRoot = current.resolve("code/src/main/java/Algorithm").resolve(child);
        if (Files.isDirectory(fromRoot)) {
            return fromRoot;
        }

        throw new IllegalStateException(
                "Run SeasonalClean from the repository root or the code directory.");
    }

    static String pythonExecutable() {
        return System.getenv().getOrDefault("PYTHON", "python3");
    }
}
