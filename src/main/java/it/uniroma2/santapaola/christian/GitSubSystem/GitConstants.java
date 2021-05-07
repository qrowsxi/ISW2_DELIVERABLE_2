package it.uniroma2.santapaola.christian.GitSubSystem;

public class GitConstants {
    public static final String EMPTY_TREE_ID = "4b825dc642cb6eb9a060e54bf8d69288fbee4904";
    public static final String HEAD = "HEAD";

    public static Tag getEmptyTreeTag() {
        return new Tag("EmptyTree", EMPTY_TREE_ID);
    }

    public static Tag getHeadTag() {
        return new Tag("HEAD", HEAD);
    }
}
