package es.um.dis.formatOquo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import es.um.dis.formatOquo.pojo.CardinalToOrdinalScaleConversion;
import es.um.dis.formatOquo.pojo.OquareCategory;
import es.um.dis.formatOquo.pojo.OquareScaleValueByOrdinalItem;
import es.um.dis.formatOquo.pojo.OquareScaleValueByRange;
import es.um.dis.formatOquo.pojo.OrdinalToOrdinalScaleConversion;

public class ScalesUtils {
	private static final int CSV_MIN_FIELDS = 8;
	private static final IRI RATIO_SCALE = IRI.create("http://www.wurvoc.org/vocabularies/om-1.8/Ratio_scale");
	private static final IRI ORDINAL_SCALE = IRI.create("http://www.wurvoc.org/vocabularies/om-1.8/Ordinal_scale");


	public static void generateScales(IRI inputOntology, File outputOntology, File scalesFile, ScaleCreationConfiguration config) throws IOException, CsvException, OWLOntologyCreationException, OWLOntologyStorageException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(inputOntology);
		OWLReasoner reasoner = new StructuralReasonerFactory().createReasoner(ontology);
		List<String[]> oquoScales = readTSV(scalesFile);
		oquoScales = oquoScales.subList(1, oquoScales.size()); // skip header
		for (String [] line : oquoScales) {
			// Each line is fully validated before modifying the ontology, so that an invalid line neither creates
			// entities nor removes existing conversions.
			if (line.length < CSV_MIN_FIELDS) {
				System.out.println(String.format("Line %s has less than %d columns; skipping", Arrays.toString(line), CSV_MIN_FIELDS));
				continue;
			}
			if(!checkConfig(ontology, line, config)) {
				continue;
			}
			IRI inputScaleIRI = IRI.create(line [1]);
			// Missing input scales are created as ratio scales.
			Set<IRI> inputScaleTypes = ontology.containsIndividualInSignature(inputScaleIRI, Imports.INCLUDED)
					? getParents(inputScaleIRI, reasoner)
					: Set.of(RATIO_SCALE);
			if(inputScaleTypes.contains(RATIO_SCALE)) {
				List<OquareScaleValueByRange> scaleValues = getOquareScaleValuesByRange(line);
				if (scaleValues == null) {
					continue;
				}
				prepareOntology(ontology, reasoner, line, config);
				addScaleRatioConversion(ontology, new CardinalToOrdinalScaleConversion(line[1], line[2], scaleValues));
			} else if(inputScaleTypes.contains(ORDINAL_SCALE)) {
				List<OquareScaleValueByOrdinalItem> scaleValues = getOquareScaleValuesByOrdinalItem(line[3], line[4], line[5], line[6], line[7]);
				prepareOntology(ontology, reasoner, line, config);
				addScaleOrdinalConversion(ontology, new OrdinalToOrdinalScaleConversion(line[1], line[2], scaleValues));
			} else {
				System.out.println(String.format("Input scale <%s> is neither a ratio nor an ordinal scale; skipping", inputScaleIRI));
			}
		}

		try (FileOutputStream outputStream = new FileOutputStream(outputOntology)) {
			ontology.saveOntology(outputStream);
		}
	}



	private static boolean checkConfig(OWLOntology ontology, String[] line, ScaleCreationConfiguration config) {
		IRI metricIRI = IRI.create(line[0]);
		IRI inputScale = IRI.create(line[1]);
		IRI outputScale = IRI.create(line[2]);
		boolean metricExists = ontology.containsClassInSignature(metricIRI, Imports.INCLUDED);
		boolean inputScaleExists = ontology.containsIndividualInSignature(inputScale, Imports.INCLUDED);
		boolean outputScaleExists = ontology.containsIndividualInSignature(outputScale, Imports.INCLUDED);

		if(!metricExists && !config.isCreateMetricIfNotExist()) {
			System.out.println(String.format("Metric <%s> not found; skipping", metricIRI));
			return false;
		}

		if(!inputScaleExists && !config.isCreateInputRawScaleIfNotExist()) {
			System.out.println(String.format("Input scale <%s> not found; skipping", inputScale));
			return false;
		}

		// The TSV columns 1-5 map to the OQuaRE categories, so the output scale is always the OQuaRE five categories scale.
		if(!IRI.create(OWLUtils.OQUARE_FIVE_CATEGORIES_SCALE_IRI).equals(outputScale)) {
			System.out.println(String.format("Output scale <%s> is not <%s>; skipping", outputScale, OWLUtils.OQUARE_FIVE_CATEGORIES_SCALE_IRI));
			return false;
		}

		if(!outputScaleExists) {
			System.out.println(String.format("Output scale <%s> not found (does the ontology import oquo-core?); skipping", outputScale));
			return false;
		}

		return true;
	}

	/**
	 * Creates the missing metric and input scale and, if requested, removes the existing conversions between the
	 * input and output scales. It must only be called once the line has been validated.
	 */
	private static void prepareOntology(OWLOntology ontology, OWLReasoner reasoner, String[] line, ScaleCreationConfiguration config) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		IRI metricIRI = IRI.create(line[0]);
		IRI inputScale = IRI.create(line[1]);
		IRI outputScale = IRI.create(line[2]);

		if(!ontology.containsClassInSignature(metricIRI, Imports.INCLUDED)) {
			OWLUtils.createClass(ontology, metricIRI);
			OWLUtils.addSubclassOf(ontology, df.getOWLClass(metricIRI), df.getOWLClass(OWLUtils.QM_NS + "DerivedMeasure"));
		}

		if(!ontology.containsIndividualInSignature(inputScale, Imports.INCLUDED)) {
			OWLUtils.createIndividual(ontology, inputScale, df.getOWLClass(OWLUtils.RAW_SCALE_IRI));
			OWLUtils.addClassAssertion(ontology, inputScale, RATIO_SCALE);
		}

		if(config.isOverwriteScaleConversion()) {
			removeScaleConversion(ontology, reasoner, inputScale, outputScale);
		}
	}



	private static void removeScaleConversion(OWLOntology ontology, OWLReasoner reasoner, IRI inputScaleIRI, IRI outputScaleIRI) {
		List<OWLNamedIndividual> scaleConversionsToRemove = getScaleConversionIndividualsToRemove(ontology, reasoner, inputScaleIRI, outputScaleIRI);
		List<OWLNamedIndividual> scalePointMappingsToremove = getScalePointMappingsToRemove(ontology, reasoner, scaleConversionsToRemove);
		List<OWLNamedIndividual> inputIntervalsToRemove = getInputIntervalsToRemove(ontology, reasoner, scalePointMappingsToremove);

		OWLEntityRemover remover = new OWLEntityRemover(ontology);
		if(inputIntervalsToRemove != null) {
			for(OWLNamedIndividual individual : inputIntervalsToRemove) {
				individual.accept(remover);
			}
		}

		if(scalePointMappingsToremove != null) {
			for(OWLNamedIndividual individual : scalePointMappingsToremove) {
				individual.accept(remover);
			}
		}

		if(scaleConversionsToRemove != null) {
			for(OWLNamedIndividual individual : scaleConversionsToRemove) {
				individual.accept(remover);
			}
		}
		ontology.getOWLOntologyManager().applyChanges(remover.getChanges());
	}









	private static List<OWLNamedIndividual> getScaleConversionIndividualsToRemove(OWLOntology ontology, OWLReasoner reasoner, IRI inputScaleIRI, IRI outputScaleIRI) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLClass scaleConversion = df.getOWLClass(OWLUtils.OQUO_NS + "ScaleConversion");
		Set<OWLClass> scaleConversionClasses = reasoner.getSubClasses(scaleConversion, false).entities().collect(Collectors.toSet());
		scaleConversionClasses.add(scaleConversion);
		OWLObjectProperty hasInputScale = df.getOWLObjectProperty(OWLUtils.OQUO_NS + "hasInputScale");
        OWLObjectProperty hasOutputScale = df.getOWLObjectProperty(OWLUtils.OQUO_NS + "hasOutputScale");
        OWLNamedIndividual inputScale = df.getOWLNamedIndividual(inputScaleIRI);
        OWLNamedIndividual outputScale = df.getOWLNamedIndividual(outputScaleIRI);

        return scaleConversionClasses.stream()
	        .flatMap(ontology::classAssertionAxioms)
	        .map(OWLClassAssertionAxiom::getIndividual)
	        .filter(OWLIndividual::isNamed)
	        .map(OWLIndividual::asOWLNamedIndividual)
	        .distinct()
	        .filter(ind -> OWLUtils.hasValue(ontology, ind, hasInputScale, inputScale))
	        .filter(ind -> OWLUtils.hasValue(ontology, ind, hasOutputScale, outputScale))
	        .collect(Collectors.toList());

	}

	private static List<OWLNamedIndividual> getScalePointMappingsToRemove(OWLOntology ontology, OWLReasoner reasoner,
			List<OWLNamedIndividual> scaleConversionsToRemove) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectProperty hasScalePointMapping = df.getOWLObjectProperty(OWLUtils.OQUO_NS + "hasScalePointMapping");
		return scaleConversionsToRemove.stream()
	            .flatMap(ontology::objectPropertyAssertionAxioms)
	            .filter(ax -> ax.getProperty().equals(hasScalePointMapping))
	            .map(OWLObjectPropertyAssertionAxiom::getObject)
				.filter(OWLIndividual::isNamed)
		        .map(OWLIndividual::asOWLNamedIndividual)
	            .distinct()
	            .collect(Collectors.toList());
	}

	private static List<OWLNamedIndividual> getInputIntervalsToRemove(OWLOntology ontology, OWLReasoner reasoner,
			List<OWLNamedIndividual> scalePointMappingsToremove) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLObjectProperty hasInputCardinalInterval = df.getOWLObjectProperty(OWLUtils.OQUO_NS + "hasInputCardinalInterval");
		return scalePointMappingsToremove.stream()
			.flatMap(ontology::objectPropertyAssertionAxioms)
			.filter(ax -> ax.getProperty().equals(hasInputCardinalInterval))
			.map(OWLObjectPropertyAssertionAxiom::getObject)
			.filter(OWLIndividual::isNamed)
	        .map(OWLIndividual::asOWLNamedIndividual)
	        .distinct()
	        .collect(Collectors.toList());
	}

	private static Set<IRI> getParents(IRI inputScaleIRI, OWLReasoner reasoner) {
		OWLNamedIndividual inputScaleIndividual = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(inputScaleIRI);
		return reasoner.getTypes(inputScaleIndividual).entities().map(x -> x.getIRI()).collect(Collectors.toSet());
	}

	private static List<OquareScaleValueByOrdinalItem> getOquareScaleValuesByOrdinalItem(String string1, String string2,
			String string3, String string4, String string5) {
		List<OquareScaleValueByOrdinalItem> oquareScaleValuesByOrdinalItem = new ArrayList<>();

		if(string1 != null && !string1.strip().isEmpty()) {
			oquareScaleValuesByOrdinalItem.add(new OquareScaleValueByOrdinalItem(OquareCategory.CATEGORY1, string1));
		}
		if(string2 != null && !string2.strip().isEmpty()) {
			oquareScaleValuesByOrdinalItem.add(new OquareScaleValueByOrdinalItem(OquareCategory.CATEGORY2, string2));
		}
		if(string3 != null && !string3.strip().isEmpty()) {
			oquareScaleValuesByOrdinalItem.add(new OquareScaleValueByOrdinalItem(OquareCategory.CATEGORY3, string3));
		}
		if(string4 != null && !string4.strip().isEmpty()) {
			oquareScaleValuesByOrdinalItem.add(new OquareScaleValueByOrdinalItem(OquareCategory.CATEGORY4, string4));
		}
		if(string5 != null && !string5.strip().isEmpty()) {
			oquareScaleValuesByOrdinalItem.add(new OquareScaleValueByOrdinalItem(OquareCategory.CATEGORY5, string5));
		}

		return oquareScaleValuesByOrdinalItem;
	}

	/**
	 * Parses the ranges of the five OQuaRE categories (columns 3 to 7). "-" means that the category has no range.
	 *
	 * @return the scale values, or null if any range is empty or invalid
	 */
	private static List<OquareScaleValueByRange> getOquareScaleValuesByRange(String[] line) {
		List<OquareScaleValueByRange> scaleValues = new ArrayList<>();
		OquareCategory[] categories = OquareCategory.values();
		for (int i = 0; i < categories.length; i++) {
			String rangeString = line[3 + i];
			Range<Double> range;
			try {
				range = IntervalUtils.parseInterval(rangeString);
			} catch (RuntimeException e) {
				range = null;
			}
			if (range == null && !"-".equals(rangeString.strip())) {
				System.out.println(String.format("Invalid range \"%s\" for category %d of <%s>; skipping", rangeString, categories[i].getValue(), line[0]));
				return null;
			}
			scaleValues.add(new OquareScaleValueByRange(categories[i], range));
		}
		return scaleValues;
	}

	private static List<String[]> readTSV(File filePath) throws IOException, CsvException {
		CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath)).withCSVParser(parser).build()) {
			return reader.readAll();
		}
	}

	private static void addScaleOrdinalConversion(OWLOntology ontology, OrdinalToOrdinalScaleConversion conversionInfo) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();

		OWLAxiom axiom;
		OWLNamedIndividual inputScale = df.getOWLNamedIndividual(conversionInfo.getInputScaleIRI());
		OWLNamedIndividual outputScale = df.getOWLNamedIndividual(conversionInfo.getOutputScaleIRI());

		IRI scaleConversionIRI = IRI.create(OWLUtils.OQUO_NS + "scaleConversion-" + UUID.randomUUID().toString());
		OWLNamedIndividual scaleConversion = df.getOWLNamedIndividual(scaleConversionIRI);
		axiom = df.getOWLDeclarationAxiom(scaleConversion);
		ontology.add(axiom);

		OWLClass conversionType = df.getOWLClass(OWLUtils.OQUO_NS + "OrdinalToOrdinalScaleConversion");
		axiom = df.getOWLClassAssertionAxiom(conversionType, scaleConversion);
		ontology.add(axiom);

		String scaleDefinition = String.format("The definition of the conversion between the scales <%s> and <%s>.", conversionInfo.getInputScaleIRI(), conversionInfo.getOutputScaleIRI());
		OWLUtils.addAnnotation(ontology, scaleConversion, df.getRDFSComment().getIRI(), scaleDefinition);

		String scaleConversionLabel = IRI.create(conversionInfo.getInputScaleIRI()).getShortForm() + "To" + IRI.create(conversionInfo.getOutputScaleIRI()).getShortForm() + "Conversion";
		scaleConversionLabel = String.join(" ", StringUtils.splitByCharacterTypeCamelCase(scaleConversionLabel)).toLowerCase();
		OWLUtils.addAnnotation(ontology, scaleConversion, df.getRDFSLabel().getIRI(), scaleConversionLabel);

		OWLUtils.addIndividualRelation(ontology, scaleConversion, IRI.create(OWLUtils.HAS_INPUT_SCALE_IRI), inputScale);
		OWLUtils.addIndividualRelation(ontology, scaleConversion, IRI.create(OWLUtils.HAS_OUTPUT_SCALE_IRI), outputScale);

		List<OWLNamedIndividual> scalePointMappings = getScalePointMappings(ontology, conversionInfo);
		for (OWLNamedIndividual scalePointMapping : scalePointMappings) {
			OWLUtils.addIndividualRelation(ontology, scaleConversion, IRI.create(OWLUtils.HAS_SCALE_POINT_MAPPING_IRI), scalePointMapping);
		}
	}

	private static List<OWLNamedIndividual> getScalePointMappings(OWLOntology ontology,
			OrdinalToOrdinalScaleConversion conversionInfo) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		List<OWLNamedIndividual> scalePointMappings = new ArrayList<>();
		for(OquareScaleValueByOrdinalItem oquareScaleValue : conversionInfo.getScaleValues()) {
			OWLNamedIndividual scalePointMapping = df.getOWLNamedIndividual(OWLUtils.OQUO_NS + "scalePointMapping-" + UUID.randomUUID().toString());
			ontology.add(df.getOWLDeclarationAxiom(scalePointMapping));
			OWLAxiom axiom = df.getOWLClassAssertionAxiom(df.getOWLClass(OWLUtils.ORDINAL_TO_ORDINAL_SCALE_POINT_MAPPING_IRI), scalePointMapping);
			ontology.add(axiom);
			OWLNamedIndividual categoryIndividual = df.getOWLNamedIndividual(oquareScaleValue.getCategory().getIri());
			OWLUtils.addIndividualRelation(ontology, scalePointMapping, IRI.create(OWLUtils.HAS_OUTPUT_SCALE_CATEGORY_IRI), categoryIndividual);
			OWLNamedIndividual inputCategoryIndividual = df.getOWLNamedIndividual(oquareScaleValue.getOrdinalItem());
			OWLUtils.addIndividualRelation(ontology, scalePointMapping, IRI.create(OWLUtils.HAS_INPUT_SCALE_CATEGORY_IRI), inputCategoryIndividual);
			String scalePointMappingsLabel = String.format("mapping %s -> %d (%s)", oquareScaleValue.getOrdinalItem(), oquareScaleValue.getCategory().getValue(), oquareScaleValue.getCategory().getDescription());
			scalePointMappings.add(scalePointMapping);
			OWLUtils.addAnnotation(ontology, scalePointMapping, df.getRDFSLabel().getIRI(), scalePointMappingsLabel);

		}
		return scalePointMappings;
	}

	private static void addScaleRatioConversion(OWLOntology ontology, CardinalToOrdinalScaleConversion conversionInfo) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom;
		OWLNamedIndividual inputScale = df.getOWLNamedIndividual(conversionInfo.getInputScaleIRI());
		OWLNamedIndividual outputScale = df.getOWLNamedIndividual(conversionInfo.getOutputScaleIRI());

		IRI scaleConversionIRI = IRI.create(OWLUtils.OQUO_NS + "scaleConversion-" + UUID.randomUUID().toString());
		OWLNamedIndividual scaleConversion = df.getOWLNamedIndividual(scaleConversionIRI);
		axiom = df.getOWLDeclarationAxiom(scaleConversion);
		ontology.add(axiom);

		OWLClass conversionType = df.getOWLClass(OWLUtils.OQUO_NS + "CardinalToOrdinalScaleConversion");
		axiom = df.getOWLClassAssertionAxiom(conversionType, scaleConversion);
		ontology.add(axiom);

		String scaleDefinition = String.format("The definition of the conversion between the scales <%s> and <%s>.", conversionInfo.getInputScaleIRI(), conversionInfo.getOutputScaleIRI());
		OWLUtils.addAnnotation(ontology, scaleConversion, df.getRDFSComment().getIRI(), scaleDefinition);

		String scaleConversionLabel = IRI.create(conversionInfo.getInputScaleIRI()).getShortForm() + "To" + IRI.create(conversionInfo.getOutputScaleIRI()).getShortForm() + "Conversion";
		scaleConversionLabel = String.join(" ", StringUtils.splitByCharacterTypeCamelCase(scaleConversionLabel)).toLowerCase();
		OWLUtils.addAnnotation(ontology, scaleConversion, df.getRDFSLabel().getIRI(), scaleConversionLabel);

		OWLUtils.addIndividualRelation(ontology, scaleConversion, IRI.create(OWLUtils.HAS_INPUT_SCALE_IRI), inputScale);
		OWLUtils.addIndividualRelation(ontology, scaleConversion, IRI.create(OWLUtils.HAS_OUTPUT_SCALE_IRI), outputScale);

		List<OWLNamedIndividual> scalePointMappings = getScalePointMappings(ontology, conversionInfo);
		for (OWLNamedIndividual scalePointMapping : scalePointMappings) {
			OWLUtils.addIndividualRelation(ontology, scaleConversion, IRI.create(OWLUtils.HAS_SCALE_POINT_MAPPING_IRI), scalePointMapping);
		}
	}

	private static List<OWLNamedIndividual> getScalePointMappings(OWLOntology ontology, CardinalToOrdinalScaleConversion conversionInfo) {
		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		List<OWLNamedIndividual> scalePointMappings = new ArrayList<>();
		for (OquareScaleValueByRange oquareScaleValue : conversionInfo.getScaleValues()) {
			OWLNamedIndividual scalePointMapping = df.getOWLNamedIndividual(OWLUtils.OQUO_NS + "scalePointMapping-" + UUID.randomUUID().toString());
			ontology.add(df.getOWLDeclarationAxiom(scalePointMapping));
			OWLAxiom axiom = df.getOWLClassAssertionAxiom(df.getOWLClass(OWLUtils.CARDINAL_TO_ORDINAL_SCALE_POINT_MAPPING_IRI), scalePointMapping);
			ontology.add(axiom);
			OWLNamedIndividual categoryIndividual = df.getOWLNamedIndividual(oquareScaleValue.getCategory().getIri());
			OWLUtils.addIndividualRelation(ontology, scalePointMapping, IRI.create(OWLUtils.HAS_OUTPUT_SCALE_CATEGORY_IRI), categoryIndividual);
			OWLNamedIndividual interval = getInterval(ontology, oquareScaleValue.getRange());
			String scalePointMappingsLabel = String.format("mapping null -> %d (%s)", oquareScaleValue.getCategory().getValue(), oquareScaleValue.getCategory().getDescription());
			if (interval != null) {
				OWLUtils.addIndividualRelation(ontology, scalePointMapping, IRI.create(OWLUtils.HAS_INPUT_CARDINAL_INTERVAL_IRI), interval);
				scalePointMappingsLabel = String.format("mapping %s -> %d (%s)", oquareScaleValue.getRange().toString(), oquareScaleValue.getCategory().getValue(), oquareScaleValue.getCategory().getDescription());
			}
			scalePointMappings.add(scalePointMapping);
			OWLUtils.addAnnotation(ontology, scalePointMapping, df.getRDFSLabel().getIRI(), scalePointMappingsLabel);
		}
		return scalePointMappings;
	}

	private static OWLNamedIndividual getInterval(OWLOntology ontology, Range<Double> range) {
		if (range == null) {
			return null;
		}

		OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLAxiom axiom;

		String intervalIRI = OWLUtils.OQUO_NS + "interval-" + UUID.randomUUID().toString();
		OWLNamedIndividual interval = df.getOWLNamedIndividual(intervalIRI);
		axiom = df.getOWLDeclarationAxiom(interval);
		ontology.add(axiom);

		OWLClass intervalClass = df.getOWLClass(OWLUtils.INTERVAL_IRI);
		axiom = df.getOWLClassAssertionAxiom(intervalClass, interval);
		ontology.add(axiom);

		OWLUtils.addIndividualDataProperty(ontology, interval, IRI.create(OWLUtils.HAS_INTERVAL_LOWER_VALUE_IRI), range.lowerEndpoint());
		OWLNamedIndividual lowerBoundType = null;
		if (BoundType.CLOSED.equals(range.lowerBoundType())) {
			lowerBoundType = df.getOWLNamedIndividual(OWLUtils.CLOSED_BOUND_TYPE_IRI);

		} else if (BoundType.OPEN.equals(range.lowerBoundType())) {
			lowerBoundType = df.getOWLNamedIndividual(OWLUtils.OPEN_BOUND_TYPE_IRI);
		}
		OWLUtils.addIndividualRelation(ontology, interval, IRI.create(OWLUtils.HAS_INTERVAL_LOWER_BOUND_TYPE_IRI), lowerBoundType);


		OWLUtils.addIndividualDataProperty(ontology, interval, IRI.create(OWLUtils.HAS_INTERVAL_UPPER_VALUE_IRI), range.upperEndpoint());
		OWLNamedIndividual upperBoundType = null;
		if (BoundType.CLOSED.equals(range.upperBoundType())) {
			upperBoundType = df.getOWLNamedIndividual(OWLUtils.CLOSED_BOUND_TYPE_IRI);

		} else if (BoundType.OPEN.equals(range.upperBoundType())) {
			upperBoundType = df.getOWLNamedIndividual(OWLUtils.OPEN_BOUND_TYPE_IRI);
		}
		OWLUtils.addIndividualRelation(ontology, interval, IRI.create(OWLUtils.HAS_INTERVAL_UPPER_BOUND_TYPE_IRI), upperBoundType);

		OWLUtils.addAnnotation(ontology, interval, df.getRDFSLabel().getIRI(), String.format("interval %s", range.toString()));

		return interval;
	}

}
