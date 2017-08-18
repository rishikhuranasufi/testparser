class MainClass {

    public static void main(def args) {
        println("Printing arguments #{bamboo.build.working.directory}");
        for(String arguments : args) {
            println (arguments);
        }
		Matcher.run("C:\\ATlogs.txt","C:\\test.html", Utils.STARTING_PATTERN,Utils.FINISHING_PATTERN,
        Utils.DATE_FORMATTER,Utils.DATE_PATTERN);
    }

} 