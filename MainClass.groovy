class MainClass {

    public static void main(def args) {
		def workingDir = System.getenv('bamboo.build.working.directory');
        println("Printing arguments "+ workingDir);
        for(String arguments : args) {
            println (arguments);
        }
		Matcher.run("C:\\ATlogs.txt",args[0]+"\"+args[1]+".html", Utils.STARTING_PATTERN,Utils.FINISHING_PATTERN,
        Utils.DATE_FORMATTER,Utils.DATE_PATTERN);
    }

} 