import java.util.HashMap;
import java.util.LinkedHashMap;

class MainClass {

    public static void main(def args) {

        println("Printing arguments ");
        for(String arguments : args) {
            println (arguments);
        }
		String workingDir = args[0];
		String buildNumber = args[1];
		String jobNames = args[2];
        String bambooUrl = args[3];
		String outPutURL = workingDir+"/output/"+buildNumber+".html";
		
		String [] jobNamesInArray= jobNames.split(",");
                HashMap<String, String> downStreamConsoleUrls = new LinkedHashMap();

        for(String jobName : jobNamesInArray){
            downStreamConsoleUrls.put(jobName,bambooUrl+jobName+"/build_logs/"+jobName+"-"+buildNumber+".log");
        }
		
		Matcher.run(downStreamConsoleUrls,outPutURL, Utils.STARTING_PATTERN,Utils.FINISHING_PATTERN,
        Utils.DATE_FORMATTER,Utils.DATE_PATTERN);
    }

} 