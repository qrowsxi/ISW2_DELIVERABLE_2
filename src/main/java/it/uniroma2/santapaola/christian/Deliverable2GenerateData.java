package it.uniroma2.santapaola.christian;

import it.uniroma2.santapaola.christian.git.exception.GitHandlerException;
import it.uniroma2.santapaola.christian.mining.ClassState;
import it.uniroma2.santapaola.christian.mining.ProjectState;
import it.uniroma2.santapaola.christian.mining.RepositoryMiner;
import it.uniroma2.santapaola.christian.utility.CSVWriter;
import it.uniroma2.santapaola.christian.utility.OutputDirectory;
import it.uniroma2.santapaola.christian.utility.ProjectData;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Deliverable2GenerateData {

    private static final Logger LOGGER = Logger.getLogger(Deliverable2GenerateData.class.getName());

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.log(Level.SEVERE, "USAGE: command <output-folder> <repository-folder>");
            return;
        }
        String output = args[0];
        String repository = args[1];
        OutputDirectory outputDirectory = new OutputDirectory(output, repository);
        ProjectData bookkeeper = new ProjectData("bookkeeper", "BOOKKEEPER", "https://issues.apache.org", "https://github.com/apache/bookkeeper", "^(refs\\/tags\\/)(.*)(?<name>\\d+.\\d+.\\d+)$");
        ProjectData openjpa = new ProjectData("openjpa", "OPENJPA", "https://issues.apache.org", "https://github.com/apache/openjpa", "^(refs\\/tags\\/)(?<name>\\d+.\\d+.\\d+)$");

        try {
            doProjectAnalysis(bookkeeper, outputDirectory);
            doProjectAnalysis(openjpa, outputDirectory);
        } catch (Exception e) {
            Logger.getLogger("isw2").log(Level.SEVERE, e.getMessage());
        }
    }

    /** buildRepositoryMiner: si occupa di instanziare un istanza di RepositoryMiner
     * @param ProjectData: Contiene le informazioni riguardanti un progetto, quali il suoi link git e jira.
     * @param OutputDirectory: Contiene le informazioni riguradanti dove salvare il risultato dell'estrazione.
     * */
    public static RepositoryMiner buildRepositoryMiner(ProjectData projectData, OutputDirectory outputDirectory) throws IOException, GitHandlerException {
        return new RepositoryMiner(
                outputDirectory.getRepository() + projectData.getProjectName(),
                projectData.getGitUrl(),
                projectData.getJiraProjectName(),
                projectData.getJiraUrl(),
                projectData.getTagPattern());
    }

    private static String getBuggy(boolean buggy) {
        if (buggy) {
            return "YES";
        } else {
            return "NO";
        }
    }

    /**
    * doProjectAnalysis: Dato un progetto software esegue l'estrazione dei dati richiesti.
     * @param ProjectData: Contiene le informazioni riguardanti un progetto, quali il suoi link git e jira.
     * @param OutputDirectory: Contiene le informazioni riguradanti dove salvare il risultato dell'estrazione.
     * L'analisi del progetto consiste nelle seguenti operazioni:
     * 1. recuperare il repository tramite il comando git clone, se il repository è gia presente il clone verra saltato.
     * 2. Instanziare la classe CSVWriter, la quale si occupa di scrivere file in formato csv.
     * 3. Per ogni versione del progetto, eseguire l'analisi e scriverla sul file csv.
    */
    public static void doProjectAnalysis(ProjectData projectData, OutputDirectory outputDirectory) throws IOException, GitHandlerException {
        RepositoryMiner miner = buildRepositoryMiner(projectData, outputDirectory);
        Logger.getLogger("isw2").log(Level.INFO, "repository successfully cloned");
        String[] fields = new String[]{"Version", "File Name", "LOC", "LOC_touched",
                "NR", "NFix", "NAuth",
                "LOC_added", "MAX_LOC_added", "AVG_LOC_ADDED",
                "Churn", "MAX_churn", "AVG_Churn",
                "ChgSetSize", "MAX_ChgSetSize", "AVG_ChgSetSize",
                "AGE", "WeightedAge", "Buggy"};
        Logger.getLogger("isw2").log(Level.INFO, "init CSVWriter()");
        CSVWriter csvWriter = new CSVWriter(new File(outputDirectory.getOutput() + projectData.getCSVOutput()), fields);
        Logger.getLogger("isw2").log(Level.INFO, "CSVWriter() Successfully initialized");
        csvWriter.writeFieldName();
        Logger.getLogger("isw2").log(Level.INFO, "Start writing data");
        ProjectState projectState = miner.newProjectState();
        while (projectState.next()) {
            for (String file : projectState.keySet()) {
                ClassState classState = projectState.getState(file);
                if (projectState.getVersion() <= projectState.getNumReleaseToProcess() || classState.isBuggy()) {
                    String[] row = new String[]{
                            Integer.toString(projectState.getVersion()),
                            classState.getClassName(),
                            Long.toString(classState.getLoc()),
                            Long.toString(classState.getTouchedLoc()),
                            Long.toString(classState.getNoRevision()),
                            Long.toString(classState.getNoFix()),
                            Long.toString(classState.getNoAuth()),
                            Long.toString(classState.getAddedLoc()),
                            Long.toString(classState.getMaxAddedLoc()),
                            Double.toString(classState.getAvgAddedLoc()),
                            Long.toString(classState.getChurn()),
                            Long.toString(classState.getMaxChurn()),
                            Double.toString(classState.getAvgChurn()),
                            Long.toString(classState.getChangedFileSet()),
                            Long.toString(classState.getMaxChangedFileSet()),
                            Double.toString(classState .getAvgChangedFileSet()),
                            Long.toString(classState.getAge()),
                            Double.toString(classState.getWeightedAge()),
                            getBuggy(classState.isBuggy())
                    };
                    csvWriter.writeLine(row);
                }
            }
            csvWriter.flush();
        }
    }
}
