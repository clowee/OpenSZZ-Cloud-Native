package com.rest.szz.entities;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.rest.szz.entities.Transaction.FileInfo;
import com.rest.szz.git.Git;
import org.eclipse.jgit.revwalk.RevCommit;

public class LinkUtils {
    public static String longestCommonSubstrings(String s, String t) {
        int[][] table = new int[s.length()][t.length()];
        int longest = 0;
        Set<String> result = new HashSet<>();

        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j < t.length(); j++) {
                if (s.charAt(i) != t.charAt(j)) {
                    continue;
                }

                table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
                if (table[i][j] > longest) {
                    longest = table[i][j];
                    result.clear();
                }
                if (table[i][j] == longest) {
                    result.add(s.substring(i - longest + 1, i + 1));
                }
            }
        }
        return result.toString();
    }

    public static Issue.Resolution getResolutionFromString(String str) {
        try{
            return Issue.Resolution.valueOf(str.toUpperCase().replace(" ", "").replace("'", ""));
        }
        catch(Exception e){
            return Issue.Resolution.NONE;
        }
    }

    public static Issue.Status getStatusFromString(String str) {
        try{
            return Issue.Status.valueOf(str.toUpperCase());
        }
        catch(Exception e){
            return Issue.Status.UNCONFIRMED;
        }
    }

    public static Set<String> stringToSet(String str) {
        String resultString = str.replace("[", "").replace("]", "");
        Set<String> result = new HashSet<>();
        if (resultString.length() > 0) {
            List<String> resultsList = Arrays.asList(resultString.split("\\s*,\\s*"));
            result = new HashSet<>(resultsList);
        }
        return result;
    }

    public static boolean containsKeywords(String str) {
        String comment = str.toLowerCase();
        Pattern patter1 = Pattern.compile("fix(e[ds])?|bugs?|defects?|patch", Pattern.CASE_INSENSITIVE);
        Pattern patter2 = Pattern.compile("^[0-9]*$", Pattern.CASE_INSENSITIVE);
        Matcher p1 = patter1.matcher(comment.toLowerCase());
        Matcher p2 = patter2.matcher(comment);
        boolean b1 = p1.find();
        boolean b2 = p2.find();
        if (b1 || b2)
            return true;
        return false;
    }

    public static boolean isCodeFile(FileInfo file) {
        if (!file.filename.contains(".")) return false;
        List<String> extensionsToIgnore = Arrays.asList("txt","md");
        return extensionsToIgnore.stream().noneMatch(extension -> file.filename.endsWith("." + extension));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static Set<String> getMatches(String source, String regex) {
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Set<String> result = new LinkedHashSet<>();
        Matcher resultMatcher = pattern.matcher(source);
        while (resultMatcher.find()) {
            result.add(resultMatcher.group());
        }
        return result;
    }

    public static List<Integer> getLinesMinus(Git git, String commitId, String fileName, boolean ignoreCommentChanges, PrintWriter l) {
        List<Integer> linesMinus = new LinkedList<>();
        String diff = git.getDiff(commitId, fileName, l);
        if (diff == null || diff.isEmpty()) return linesMinus;
        linesMinus = git.getLinesMinus(diff);
        if (ignoreCommentChanges) {
            String parent = git.getPreviousCommit(commitId, l);
            List<Integer> commentLines = git.getCommentLines(parent, fileName);
            linesMinus = linesMinus.stream().filter(line -> !commentLines.contains(line)).collect(Collectors.toList());
        }
        return linesMinus;
    }


    public static Set<Suspect> getSuspectsByAddressedIssues(Set<String> issueIds, String currentIssueKey, Git git,String source) {
        Set<Suspect> suspects = new LinkedHashSet<>();
        issueIds.stream()
            .filter(issueId -> !issueId.equals(currentIssueKey))
            .forEach(issueId -> {
                List<Transaction> transactions = git.getCommits(issueId);
                List<Transaction> filteredTransactions = transactions.stream().filter(t -> {
                    List<FileInfo> changedCodeFiles = t.getFiles().stream().filter(file -> LinkUtils.isCodeFile(file)).collect(Collectors.toList());
                    return changedCodeFiles.size() > 0;
                }).collect(Collectors.toList());
                if (filteredTransactions.size() > 0) {
                    List<Suspect> foundSuspects = transactions.stream()
                        .map(t -> new Suspect(t.getId(),t.getTimeStamp(),null,source))
                        .collect(Collectors.toList());
                    suspects.addAll(foundSuspects);
                }
            });
        return suspects.stream().filter(LinkUtils.distinctByKey(s -> s.getCommitId())).collect(Collectors.toSet());
    }

    public static Set<Suspect> getSuspectsByIssueDescriptionAndComments(Git git, String currentCommitId, String projectName, Issue issue) {
        String lookBehind = "(?<=(((introduc(ed|ing)|started|broken) ((this|the) (bug|issue|error) )?(in|by|with)|caused by|due to|after|before|because( of)?|since) ))";
        String lookAhead = "(?=( (introduced|caused)|[^.,:]* cause))";
        String issueIdPattern = projectName+"[ ]*-[ ]*[0-9]+";
        String currentIssueKey = projectName + "-" + issue.getId();
        String commitShaPattern = "(\\b|(?<=(\\br)))[0-9a-f]{5,41}\\b";
        String issuePattern = (lookBehind + issueIdPattern) + "|" + (issueIdPattern + lookAhead);
        String commitPattern = (lookBehind + commitShaPattern) + "|" + (commitShaPattern + lookAhead);
        String text = issue.getDescription()+issue.getComments();
        Set<String> issueMatches = LinkUtils.getMatches(text,issuePattern);
        Set<String> commitMatches = LinkUtils.getMatches(text,commitPattern);
        Set<Suspect> newSuspects = getSuspectsByAddressedIssues(issueMatches, currentIssueKey, git,"description/comments");
        commitMatches.forEach(sha -> {
            RevCommit commit = git.getCommit(sha);
            if (commit != null && !commit.getName().equals(currentCommitId)) {
                newSuspects.add(LinkUtils.generateSuspect(commit,"description/comments"));
            }
        });
        return newSuspects.stream().filter(LinkUtils.distinctByKey(s -> s.getCommitId())).collect(Collectors.toSet());
    }

    public static Boolean isJavaFile(FileInfo file) {
        return file.filename.endsWith(".java");
    }

    public static Suspect generateSuspect(RevCommit commit, String fileName) {
        Long temp = Long.parseLong(commit.getCommitTime()+"") * 1000;
        return new Suspect(commit.getName(), new Date(temp), fileName, null);
    }

}
