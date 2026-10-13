package es.um.dis.formatOquo.main;

import java.io.File;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.model.IRI;

import es.um.dis.formatOquo.utils.ScaleCreationConfiguration;
import es.um.dis.formatOquo.utils.ScalesUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "OQUOScales", version = "OQUOScales 0.0.1", mixinStandardHelpOptions = true)
public class AddScalesMain implements Callable<Integer> {

	@Option(names = { "-t", "--tsv-file" }, required = true, description = "TSV file with scales specification. This should contain the following columns in this order:\n"
			+ "metric: IRI for the metric.\n"
			+ "input_scale: IRI for the input scale.\n"
			+ "output_scale: IRI for the output scale. It must be https://purl.org/oquo#OquareFiveCategoriesScale, defined in oquo-core.\n"
			+ "1: Range to assign with the OQuaRE category 1 (not acceptable). E.g. (8, 11], [0, 0.2), [0.8, Inf), (-Inf, 5], etc.\n"
			+ "2: Range to assign with the OQuaRE category 2 (insufficient). E.g. (8, 11], [0, 0.2), [0.8, Inf), (-Inf, 5], etc.\n"
			+ "3: Range to assign with the OQuaRE category 3 (minimally acceptable). E.g. (8, 11], [0, 0.2), [0.8, Inf), (-Inf, 5], etc.\n"
			+ "4: Range to assign with the OQuaRE category 4 (acceptable). E.g. (8, 11], [0, 0.2), [0.8, Inf), (-Inf, 5], etc.\n"
			+ "5: Range to assign with the OQuaRE category 5 (exceeds the requirements). E.g. (8, 11], [0, 0.2), [0.8, Inf), (-Inf, 5], etc.\n")
	private File tsvFile;

	@Option(names = { "-i", "--ontology" }, required = true, description = "Ontology to include the scales in IRI format (e.g. file:///documents/ontology.owl or https://purl.org/oquo-huron)")
	private String ontologyInputIRI;

	@Option(names = { "-o", "--output" }, required = true, description = "Output file with the modified ontology.")
	private File ontologyOutputFile;

	@Option(names = { "--overwrite-scales" }, required = false, description = "Overwrite existing scales.")
	private boolean overwriteScale;

	@Option(names = { "--create-metric" }, required = false, description = "Create metric if not exists.")
	private boolean createMetricIfNotExist;

	@Option(names = { "--create-input-scale" }, required = false, description = "Create input scale if not exists.")
	private boolean createInputRawScaleIfNotExist;

	public static void main(String[] args) {
		int exitCode = new CommandLine(new AddScalesMain()).execute(args);
		System.exit(exitCode);
	}


	@Override
	public Integer call() throws Exception {
		ScaleCreationConfiguration config = new ScaleCreationConfiguration(overwriteScale, createMetricIfNotExist, createInputRawScaleIfNotExist);
		ScalesUtils.generateScales(IRI.create(ontologyInputIRI), ontologyOutputFile, tsvFile, config);
		return 0;
	}

}
