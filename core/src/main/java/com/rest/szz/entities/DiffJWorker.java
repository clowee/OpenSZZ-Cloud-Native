package com.rest.szz.entities;

import com.rest.szz.git.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.incava.analysis.FileDiff;
import org.incava.analysis.FileDiffs;
import org.incava.analysis.Report;
import org.incava.diffj.app.DiffJ;
import org.incava.diffj.app.Options;
import org.incava.ijdk.text.LocationRange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiffJWorker {

    public static List<LocationRange> getChanges(Git git, String commitId, String fileName) throws IOException {
        RevCommit commit = git.getCommit(commitId);
        RevCommit parent = git.getCommit(commit.getParent(0).getName());
        Report diffjReport = DiffJWorker.getReport(git, parent, commit, fileName);
        FileDiffs diffs = diffjReport.getDifferences();
        return diffs.stream()
            .filter(diff -> !diff.getType().equals(FileDiff.Type.ADDED))
            .map(change -> change.getFirstLocation())
            .collect(Collectors.toList());
    }

    public static Report getReport(Git git, RevCommit commitFrom, RevCommit commitTo, String fileName) throws IOException {
        File fileTo = new File("diffj_to_" + commitTo.getName() + ".java");
        byte[] fileContentTo = git.getFileContent(commitTo, fileName);
        saveFileContent(fileContentTo, fileTo);

        File fileFrom = new File("diffj_from_" + commitFrom.getName() + ".java");
        byte[] fileContentFrom = git.getFileContent(commitFrom, fileName);
        saveFileContent(fileContentFrom, fileFrom);

        Options opts = new Options();
        String[] args = {fileFrom.getPath(), fileTo.getPath()};
        List<String> names = opts.process(Arrays.asList(args));
        DiffJ diffj = new DiffJ(opts.showBriefOutput(), opts.showContextOutput(), opts.highlightOutput(),
            opts.recurse(),
            opts.getFirstFileName(), opts.getFromSource(),
            opts.getSecondFileName(), opts.getToSource());

        diffj.processNames(names);
        try {
            Files.delete(fileFrom.toPath());
            Files.delete(fileTo.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diffj.getReport();
    }

    private static void saveFileContent(byte[] content, File outputFile) throws IOException {
        if (content == null) {
            outputFile.createNewFile();
            return;
        }
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

