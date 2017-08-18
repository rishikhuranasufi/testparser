
import hudson.model.AbstractBuild
import hudson.model.BuildListener

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class Utils {
    public static final Pattern STARTING_PATTERN = ~/STARTING TEST: (.*)\.(.+)/
    public static final Pattern FINISHING_PATTERN = ~/FINISHING TEST: (.*)\.(.+)/
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss,SSS')
    public static final Pattern DATE_PATTERN = ~/([0-9]{4}\-[0-9]{2}\-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3})/
    public static final String JENKINS__HOME = "JENKINS_HOME";
    public static final String BASE_LOCATION = "/userContent/log-parser/";
    public static final String JOB__NAME = "JOB_NAME";
    public static final String HTML = ".html";
    public static final String SEPARATOR = "/";



    public static synchronized String getOutputFile(AbstractBuild build, BuildListener listener, boolean isDownStreamLocation) {
        String jenkinsHome = build.getEnvironment(listener).get(JENKINS__HOME, "");
        String jobName = build.getEnvironment(listener).get(JOB__NAME, "");
        if (isDownStreamLocation)
            jobName = jobName + "/downStream";
        Path targetDir = Paths.get(jenkinsHome, BASE_LOCATION, File.separator ,jobName);
        return String.format("%s%s%d%s", createOutputDirectory(targetDir, listener), File.separator, build.getNumber(), HTML);
    }

    private static String createOutputDirectory(Path targetDir, BuildListener listener) {
        if (!Files.exists(targetDir)) {
            listener.getLogger().println("Directory " + targetDir + " doesn't exist, creating it");
            Files.createDirectories(targetDir)
        }
        if (!Files.exists(targetDir)) {
            String errmsg = String.format("Path: %s does not exist even if existence was checked and it should have been created in case missing.\n", targetDir);
            listener.getLogger().println(errmsg);
            throw new RuntimeException(errmsg);
        }
        return targetDir.toString()
    }

    public static boolean isPatternValid(String value) {
        try {
            Pattern.compile(value);
        } catch (PatternSyntaxException exception) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public static boolean isValidDateFormatPattern(String value) {
        try {
            DateTimeFormatter.ofPattern(value);
        } catch (IllegalArgumentException exception) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}