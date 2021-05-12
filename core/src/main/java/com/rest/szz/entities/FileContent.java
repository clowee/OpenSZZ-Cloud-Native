package com.rest.szz.entities;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileContent {
    private String fileExtension;
    private List<String> lines;

    private static List<String> filesWithCStyleComments = Arrays.asList("c","java","kt","cs","cpp","h","js","ts","swift","m","mm","r", "go","scala","sc","dart", "dm", "groovy", "gvy", "gy", "gsh");
    private static List<String> filesWithShellStyleComments = Arrays.asList("sh","bash","zsh");
    private static List<String> markupFiles = Arrays.asList("html","htm","xml");
    private static List<String> cssFiles = Arrays.asList("css","sass","scss","less");
    private static List<String> filesWithPythonStyleComments = Arrays.asList("py","ex","exs");

    private static List<String> supportedFileExtensions = Stream.of(
        filesWithCStyleComments,
        filesWithShellStyleComments,
        markupFiles,cssFiles,
        Arrays.asList("sql","php","rb"))
        .flatMap(Collection::stream).collect(Collectors.toList());

    public FileContent(byte[] fileContentBytes, String fileExtension) {
        this.fileExtension = fileExtension;
        this.lines =  Arrays.asList(new String(fileContentBytes).split(System.lineSeparator()));
    }

    public List<Integer> getCommentLines() {
        if (!supportedFileExtensions.contains(this.fileExtension)) return new LinkedList<>();
        List<Integer> commentLines = new LinkedList<>();
        boolean blockCommentStarted = false;
        for (int i = 0; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            if (blockCommentStarted) {
                if (isBlockCommentFinished(line,fileExtension)) {
                    blockCommentStarted = false;
                }
                commentLines.add(i+1);
                continue;
            }
            if (isBlockCommentStarted(line,fileExtension)) {
                blockCommentStarted = true;
                commentLines.add(i+1);
                continue;
            }
            if (isInlineComment(line, fileExtension) || isSingleLineBlockComment(line, fileExtension)) {
                commentLines.add(i+1);
                continue;
            }
        }
        return commentLines;
    }

    public static boolean isBlockCommentFinished(String line, String fileExtension) {
        String regex = filesWithCStyleComments.contains(fileExtension)
            || Arrays.asList("sql","php").contains(fileExtension)
            || cssFiles.contains(fileExtension)
            ? ".*\\*/\\s*$"
            : markupFiles.contains(fileExtension)
            ? ".*-->\\s*$"
            : filesWithPythonStyleComments.contains(fileExtension)
            ? ".*(\"\"\"|\'\'\')\\s*$"
            : fileExtension.equals("rb")
            ? ".*=end\\s*$"
            : fileExtension.equals("pl")
            ? "^=cut"
            : fileExtension.equals("coffee")
            ? ".*###\\s*$"
            : fileExtension.equals("lua")
            ? ".*--]]\\s*$"
            : filesWithShellStyleComments.contains(fileExtension)
            ? "^COMMENT"
            : null;
        if (regex == null) return false;
        return matchString(line, regex);
    }

    public static boolean isBlockCommentStarted(String line, String fileExtension) {
        String regex = filesWithCStyleComments.contains(fileExtension)
            || Arrays.asList("sql","php").contains(fileExtension)
            || cssFiles.contains(fileExtension)
            ? "^\\s*/\\*(?!.*?\\*/)"
            : markupFiles.contains(fileExtension)
            ? "^\\s*<!--(?!.*?-->)"
            : filesWithPythonStyleComments.contains(fileExtension)
            ? "^\\s*(\"\"\"(?!.*?\"\"\")|\'\'\'(?!.*?\'\'\'))"
            : fileExtension.equals("rb")
            ? "^\\s*=begin(?!.*?=end)"
            : fileExtension.equals("pl")
            ? "^="
            : fileExtension.equals("coffee")
            ? "^\\s*###(?!.*?###)"
            : fileExtension.equals("lua")
            ? "^\\s*--[[(?!.*?--]])"
            : filesWithShellStyleComments.contains(fileExtension)
            ? "^<<COMMENT"
            : null;
        if (regex == null) return false;
        return matchString(line, regex);
    }

    public static boolean isSingleLineBlockComment(String line, String fileExtension) {
        String regex = filesWithCStyleComments.contains(fileExtension)
            || Arrays.asList("sql","php").contains(fileExtension)
            || cssFiles.contains(fileExtension)
            ? "^\\s*(/\\*.*?\\*/\\s*$)"
            : markupFiles.contains(fileExtension)
            ? "^\\s*<!--.*?-->\\s*$"
            : filesWithPythonStyleComments.contains(fileExtension)
            ? "^\\s*(\"\"\".*?\"\"\"\\s*$|\'\'\'.*?\'\'\'\\s*$)"
            : fileExtension.equals("rb")
            ? "^\\s*(=begin.*?=end\\s*$)"
            : fileExtension.equals("rb")
            ? "^\\s*(--[[.*?=--]]\\s*$)"
            : null;
        if (regex == null) return false;
        return matchString(line, regex);
    }

    public static boolean isInlineComment(String line, String fileExtension) {
        String regex = filesWithCStyleComments.contains(fileExtension)
            || fileExtension.equals("rs")
            ? "^\\s*//"
            : filesWithShellStyleComments.contains(fileExtension)
            || filesWithPythonStyleComments.contains(fileExtension)
            || Arrays.asList("rb","pl","coffee").contains(fileExtension)
            ? "^\\s*#"
            : fileExtension.equals("php")
            ? "^\\s*(#|//)"
            : Arrays.asList("lua","sql").contains(fileExtension)
            ? "^\\s*--"
            : null;
        if (regex == null) return false;
        return matchString(line, regex);
    }

    public static boolean matchString(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }
}
